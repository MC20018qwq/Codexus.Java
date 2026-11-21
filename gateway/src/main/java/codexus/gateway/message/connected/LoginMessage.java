package codexus.gateway.message.connected;

import codexus.cipher.protocol.*;
import codexus.cipher.utils.exception.*;
import codexus.csharpsup.DotNetJson;
import codexus.gateway.connection.*;
import codexus.gateway.entities.EntityResponse;
import codexus.gateway.entities.web.*;
import codexus.gateway.entities.web.gateway.*;
import codexus.gateway.enums.Platform;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import java.util.concurrent.*;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "login")
public final class LoginMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        EntityLoginRequest request = null;
        try {
            request = DotNetJson.deserialize(payload, EntityLoginRequest.class);
            if (request == null) throw new IllegalArgumentException("Invalid login request");
            Platform platform = requirePlatform(request.getPlatform());
            if ("send_code".equals(request.Channel)) {
                if (!handler.getX19().MPay.sendSmsCodeAsync(request.Details).join()) throw new IllegalStateException("Failed to send SMS code");
                send(connection, "login", EntityResponse.success(36, "")); return;
            }
            String id;
            if ("active".equals(request.Channel) || "active_with_captcha".equals(request.Channel)) {
                EntityActiveWithCaptcha captcha = "active_with_captcha".equals(request.Channel)
                        ? DotNetJson.deserialize(request.Details, EntityActiveWithCaptcha.class) : null;
                if ("active_with_captcha".equals(request.Channel) && captcha == null) throw new IllegalArgumentException("Invalid captcha");
                var stored = handler.getUsers().getUserByEntityId(captcha == null ? request.Details : captcha.UserId);
                if (stored == null) throw new IllegalArgumentException("User not found");
                // The saved platform selects the original 4399 PC or COM authorization route.
                platform = requirePlatform(stored.getPlatform());
                id = stored.UserId;
                if (!stored.Authorized || handler.getUsers().getAvailableUser(id) == null) {
                    String details = stored.Details;
                    if (captcha != null) {
                        var password = DotNetJson.deserialize(details, EntityPasswordRequest.class);
                        if (password == null) throw new IllegalArgumentException("Invalid user");
                        password.Captcha = captcha.Captcha; password.CaptchaIdentifier = captcha.Identifier;
                        details = DotNetJson.serializeRelaxed(password);
                    }
                    try { id = login(handler, stored.Channel, stored.Type, details, platform); }
                    catch (RuntimeException error) {
                        Throwable cause = unwrap(error);
                        if (captcha == null && cause instanceof CaptchaException e) {
                            send(connection, "login", EntityResponse.error(1001, CaptchaException.clone(e, stored.UserId))); return;
                        }
                        throw error;
                    }
                }
            } else id = login(handler, request.Channel, request.Type, request.Details, platform);
            send(connection, "login", EntityResponse.success(""));
            send(connection, "login/success", id);
            new GetAccountMessage().handleMessage(handler, connection);
        } catch (Exception error) {
            Throwable cause = unwrap(error);
            String message = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
            if (cause instanceof CaptchaException captcha)
                message = CaptchaException.clone(captcha, request == null ? "" : request.Details).getMessage();
            send(connection, "login", EntityResponse.error(cause instanceof VerifyException ? 1002 : 1001, message));
        }
    }
    private static Platform requirePlatform(Platform platform) {
        if (platform == null) throw new IllegalArgumentException("Unknown platform");
        if (platform == Platform.Mobile) throw new IllegalArgumentException("PE/Bedrock 不在迁移范围内");
        return platform;
    }
    private static String login(ClientHandler handler, String channel, String type, String details, Platform platform) {
        if (("4399pc".equals(channel) || "4399com".equals(channel)) && "password".equals(type)) {
            var password = password(details);
            String cookie;
            if (platform == Platform.Mixed || "4399com".equals(channel))
                cookie = handler.getCom4399().loginAndAuthorize(password.Account, password.Password).join();
            else cookie = handler.getPc4399().loginWithPasswordAsync(password.Account, password.Password, password.CaptchaIdentifier, password.Captcha).join();
            var result = handler.getX19().loginWithCookie(cookie);
            var saved = new EntityPasswordRequest(); saved.Account = password.Account; saved.Password = password.Password;
            return save(handler, result, channel, "password", DotNetJson.serializeRelaxed(saved), true, platform);
        }
        if (!"netease".equals(channel)) throw new IllegalArgumentException("Unknown login channel or type");
        return switch (type) {
            case "sms" -> {
                var code = DotNetJson.deserialize(details, EntityCodeRequest.class);
                if (code == null) throw new IllegalArgumentException("Invalid phone login details");
                var launcher = handler.getX19();
                var ticket = launcher.MPay.verifySmsCodeAsync(code.Phone, code.Code).join();
                if (ticket == null) throw new IllegalStateException("Failed to verify SMS code");
                var user = launcher.MPay.finishSmsCodeAsync(code.Phone, ticket.Ticket).join();
                if (user == null) throw new IllegalStateException("Failed to finish SMS code");
                yield login(handler, channel, "cookie", DotNetJson.serializeRelaxed(WPFLauncher.generateCookie(user, launcher.MPay.getDevice())), platform);
            }
            case "cookie" -> {
                var result = handler.getX19().loginWithCookie(details);
                yield save(handler, result, result.channel(), "cookie", details, "netease".equals(result.channel()), platform);
            }
            case "password" -> {
                var password = password(details); var launcher = handler.getX19();
                var user = launcher.loginWithEmailAsync(password.Account, password.Password).join();
                var cookie = WPFLauncher.generateCookie(user, launcher.MPay.getDevice());
                var result = launcher.loginWithCookie(cookie);
                yield save(handler, result, result.channel(), "cookie", DotNetJson.serializeRelaxed(cookie), true, platform);
            }
            default -> throw new IllegalArgumentException("Unknown login channel or type");
        };
    }
    private static EntityPasswordRequest password(String details) {
        var password = DotNetJson.deserialize(details, EntityPasswordRequest.class);
        if (password == null) throw new IllegalArgumentException("Invalid password login details");
        return password;
    }
    private static String save(ClientHandler handler, WPFLauncher.CookieLogin result, String channel, String type, String details, boolean persist, Platform platform) {
        var authentication = result.user(); handler.getUsers().addUserToMaintain(authentication);
        var user = new EntityUser(); user.UserId = authentication.EntityId; user.Authorized = true; user.AutoLogin = false;
        user.Channel = channel; user.Type = type; user.Details = details;
        handler.getUsers().addUser(user, persist, platform); return user.UserId;
    }
    private static Throwable unwrap(Throwable error) {
        while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null) error = error.getCause();
        return error;
    }
}
