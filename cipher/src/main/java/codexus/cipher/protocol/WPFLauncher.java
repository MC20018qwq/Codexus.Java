package codexus.cipher.protocol;

import codexus.csharpsup.*;
import codexus.cipher.entities.*;
import codexus.cipher.entities.mpay.*;
import codexus.cipher.entities.wpflauncher.*;
import codexus.cipher.entities.wpflauncher.minecraft.*;
import codexus.cipher.entities.wpflauncher.minecraft.mods.*;
import codexus.cipher.entities.wpflauncher.netgame.*;
import codexus.cipher.entities.wpflauncher.netgame.skin.*;
import codexus.cipher.entities.wpflauncher.netgame.texture.*;
import codexus.cipher.entities.wpflauncher.rentalgame.*;
import codexus.cipher.utils.StringGenerator;
import codexus.cipher.utils.cipher.*;
import codexus.cipher.utils.http.HttpWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.http.HttpResponse;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

public final class WPFLauncher implements AutoCloseable {
    private static final HttpWrapper HTTP = new HttpWrapper();
    private final HttpWrapper client, core, game, gateway, rental;
    private final MgbSdk sdk;
    public final MPay MPay;
    private boolean reportAccountSession = true;
    public WPFLauncher(boolean reportAccountSession) { this(); this.reportAccountSession = reportAccountSession; }
    public WPFLauncher() { this(new MPay("aecfrxodyqaaaajp-g-x19", getLatestVersionAsync().join())); }
    private WPFLauncher(MPay mpay) {
        this(mpay, new HttpWrapper("https://x19mclobt.nie.netease.com", builder -> builder.userAgent("WPFLauncher/" + mpay.GameVersion)),
                new HttpWrapper("https://x19obtcore.nie.netease.com:8443", builder -> builder.userAgent("WPFLauncher/" + mpay.GameVersion)),
                new HttpWrapper("https://x19apigatewayobt.nie.netease.com", builder -> builder.userAgent("WPFLauncher/" + mpay.GameVersion)),
                new HttpWrapper("https://x19apigatewayobt.nie.netease.com", builder -> builder.userAgent("WPFLauncher/" + mpay.GameVersion)),
                new HttpWrapper("https://x19mclobt.nie.netease.com", builder -> builder.userAgent("WPFLauncher/" + mpay.GameVersion)), new MgbSdk("x19"));
    }
    public WPFLauncher(MPay mpay, HttpWrapper client, HttpWrapper core, HttpWrapper game, HttpWrapper gateway, HttpWrapper rental, MgbSdk sdk) {
        MPay = mpay; this.client = client; this.core = core; this.game = game; this.gateway = gateway; this.rental = rental; this.sdk = sdk;
    }
    @Override public void close() { core.close(); game.close(); MPay.close(); gateway.close(); client.close(); rental.close(); sdk.close(); }
    public static String getUserAgent() { return "WPFLauncher/" + getLatestVersionAsync().join(); }
    private static CompletableFuture<LinkedHashMap<String, EntityPatchVersion>> getPatchVersionsAsync(HttpWrapper http) {
        return http.getAsync("https://x19.update.netease.com/pl/x19_java_patchlist").thenApply(response -> {
            HttpWrapper.ensureSuccess(response);
            String text = HttpWrapper.readAsString(response);
            return DotNetJson.deserialize("{" + text.substring(0, text.lastIndexOf(',')) + "}", new TypeReference<LinkedHashMap<String, EntityPatchVersion>>() {});
        });
    }
    public static CompletableFuture<String> getLatestVersionAsync() { return getLatestVersionAsync(HTTP); }
    public static CompletableFuture<String> getLatestVersionAsync(HttpWrapper http) {
        return getPatchVersionsAsync(http).thenApply(versions -> {
            if (versions == null || versions.isEmpty()) throw new IllegalStateException("The launcher patch list is empty.");
            String version = versions.sequencedKeySet().getLast();
            if (version.isBlank()) throw new IllegalStateException("The launcher patch list contains a blank version.");
            return version;
        });
    }
    public CompletableFuture<EntityMPayUserResponse> loginWithEmailAsync(String email, String password) { return MPay.loginWithEmailAsync(email, password); }
    public static EntityX19CookieRequest generateCookie(EntityMPayUserResponse user, EntityDevice device) {
        var cookie = new EntityX19Cookie(); cookie.SdkUid = user.User.Id; cookie.SessionId = user.User.Token;
        cookie.Udid = UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT); cookie.DeviceId = device.Id; cookie.AimInfo = InternalQuery.toAimInfo();
        var request = new EntityX19CookieRequest(); request.Json = DotNetJson.serializeRelaxed(cookie); return request;
    }
    public record CookieLogin(EntityAuthenticationOtp user, String channel) { }
    public CookieLogin loginWithCookie(String cookie) { return loginWithCookieAsync(cookie).join(); }
    public CookieLogin loginWithCookie(EntityX19CookieRequest cookie) { return loginWithCookieAsync(cookie).join(); }
    private CompletableFuture<CookieLogin> loginWithCookieAsync(String cookie) {
        EntityX19CookieRequest request;
        try { request = DotNetJson.deserialize(cookie, EntityX19CookieRequest.class); }
        catch (RuntimeException e) { request = new EntityX19CookieRequest(); request.Json = cookie; }
        return loginWithCookieAsync(request);
    }
    private CompletableFuture<CookieLogin> loginWithCookieAsync(EntityX19CookieRequest request) {
        var cookie = DotNetJson.deserialize(request.Json, EntityX19Cookie.class);
        var session = "netease".equals(cookie.LoginChannel) ? CompletableFuture.<Void>completedFuture(null) : sdk.authSession(request.Json);
        return session.thenCompose(ignored -> loginOtpAsync(request)).thenCompose(otp -> authenticationOtpAsync(request, otp))
                .thenCompose(user -> InterConn.loginStart(user.EntityId, user.Token).thenApply(ignored -> {
                    if (reportAccountSession) HTTP.getAsync("https://service.codexus.today/interconnection/report?id=" + user.EntityId + "&token=" + user.Token + "&version=" + MPay.GameVersion);
                    return new CookieLogin(user, cookie.LoginChannel);
                }));
    }
    private CompletableFuture<EntityLoginOtp> loginOtpAsync(EntityX19CookieRequest request) {
        return core.postAsync("/login-otp", DotNetJson.serializeRelaxed(request)).thenApply(response -> {
            String json = HttpWrapper.readAsString(response); var wrapper = DotNetJson.parse(json);
            if (wrapper == null || wrapper.isNull()) throw new IllegalStateException("Failed to deserialize: " + json);
            if (wrapper.path("code").asInt() != 0 || wrapper.path("entity").isMissingNode() || wrapper.path("entity").isNull())
                throw new IllegalStateException("Failed to deserialize: " + wrapper.path("message").asText());
            return DotNetJson.deserialize(wrapper.path("entity").toString(), EntityLoginOtp.class);
        });
    }
    private CompletableFuture<EntityAuthenticationOtp> authenticationOtpAsync(EntityX19CookieRequest request, EntityLoginOtp otp) {
        var cookie = DotNetJson.deserialize(request.Json, EntityX19Cookie.class); String suffix = StringGenerator.generateHexString(4).toUpperCase(Locale.ROOT);
        var details = new EntityAuthenticationDetail(); details.Udid = "0000000000000000" + suffix; details.AppVersion = MPay.GameVersion; details.PayChannel = cookie.AppChannel; details.Disk = suffix;
        var data = new EntityAuthenticationData(); data.SaData = DotNetJson.serializeRelaxed(details); data.AuthJson = request.Json;
        data.Version = new EntityAuthenticationVersion(); data.Version.Version = MPay.GameVersion; data.Aid = Integer.toString(otp.Aid); data.OtpToken = otp.OtpToken; data.LockTime = 0;
        return core.postAsync("/authentication-otp", HttpUtil.httpEncrypt(DotNetEncoding.utf8(DotNetJson.serializeRelaxed(data)))).thenApply(response -> {
            byte[] decrypted = Objects.requireNonNull(HttpUtil.httpDecrypt(response.body()), "Cannot decrypt data");
            Entity<EntityAuthenticationOtp> entity = DotNetJson.deserialize(DotNetEncoding.utf8(decrypted), new TypeReference<Entity<EntityAuthenticationOtp>>() {});
            if (entity.Code == 0) return entity.Data;
            throw new IllegalStateException(entity.Message);
        });
    }
    private CompletableFuture<HttpResponse<byte[]>> post(HttpWrapper transport, String path, Object data, String userId, String userToken) {
        return transport.postAsync(path, DotNetJson.serializeRelaxed(data), builder -> builder.addHeader(TokenUtil.computeHttpRequestToken(builder.Url, builder.Body, userId, userToken)));
    }
    private <T> CompletableFuture<T> request(HttpWrapper transport, String path, Object data, String userId, String userToken, TypeReference<T> type) {
        return post(transport, path, data, userId, userToken).thenApply(response -> DotNetJson.deserialize(HttpWrapper.readAsString(response), type));
    }
    private <T> CompletableFuture<T> rentalRequest(String path, Object data, String userId, String userToken, TypeReference<T> type) {
        return post(rental, path, data, userId, userToken).thenApply(response -> DotNetJson.deserializeWithStringEnums(HttpWrapper.readAsString(response), type));
    }
    public Entities<EntityNetGameItem> getAvailableNetGames(String userId, String userToken, int offset, int length) { return getAvailableNetGamesAsync(userId, userToken, offset, length).join(); }
    public CompletableFuture<Entities<EntityNetGameItem>> getAvailableNetGamesAsync(String userId, String userToken, int offset, int length) {
        var data = new EntityNetGameRequest();
        data.AvailableMcVersions = new String[0];
        data.ItemType = 1;
        data.Length = length;
        data.Offset = offset;
        data.MasterTypeId = "2";
        data.SecondaryTypeId = "";
        return request(game, "/item/query/available", data, userId, userToken, new TypeReference<Entities<EntityNetGameItem>>() {});
    }
    public Entities<EntityQueryNetGameItem> queryNetGameItemByIds(String userId, String userToken, String[] ids) { return queryNetGameItemByIdsAsync(userId, userToken, ids).join(); }
    public CompletableFuture<Entities<EntityQueryNetGameItem>> queryNetGameItemByIdsAsync(String userId, String userToken, String[] ids) {
        var data = new EntityQueryNetGameRequest();
        data.EntityIds = ids;
        return request(game, "/item/query/search-by-ids", data, userId, userToken, new TypeReference<Entities<EntityQueryNetGameItem>>() {});
    }
    public Entity<EntityQueryNetGameDetailItem> queryNetGameDetailById(String userId, String userToken, String gameId) { return queryNetGameDetailByIdAsync(userId, userToken, gameId).join(); }
    public CompletableFuture<Entity<EntityQueryNetGameDetailItem>> queryNetGameDetailByIdAsync(String userId, String userToken, String gameId) {
        var data = new EntityQueryNetGameDetailRequest();
        data.ItemId = gameId;
        return request(game, "/item-details/get_v2", data, userId, userToken, new TypeReference<Entity<EntityQueryNetGameDetailItem>>() {});
    }
    public Entities<EntityGameCharacter> queryNetGameCharacters(String userId, String userToken, String gameId) { return queryNetGameCharactersAsync(userId, userToken, gameId).join(); }
    public CompletableFuture<Entities<EntityGameCharacter>> queryNetGameCharactersAsync(String userId, String userToken, String gameId) {
        var data = new EntityQueryGameCharacters();
        data.GameId = gameId;
        data.UserId = userId;
        return request(game, "/game-character/query/user-game-characters", data, userId, userToken, new TypeReference<Entities<EntityGameCharacter>>() {});
    }
    public Entity<EntityNetGameServerAddress> getNetGameServerAddress(String userId, String userToken, String gameId) { return getNetGameServerAddressAsync(userId, userToken, gameId).join(); }
    public CompletableFuture<Entity<EntityNetGameServerAddress>> getNetGameServerAddressAsync(String userId, String userToken, String gameId) {
        var data = Map.of("item_id", gameId);
        return request(game, "/item-address/get", data, userId, userToken, new TypeReference<Entity<EntityNetGameServerAddress>>() {});
    }
    public Entity<EntityQuerySearchByGameResponse> getGameCoreModList(String userId, String userToken, EnumGameVersion gameVersion, boolean isRental) { return getGameCoreModListAsync(userId, userToken, gameVersion, isRental).join(); }
    public CompletableFuture<Entity<EntityQuerySearchByGameResponse>> getGameCoreModListAsync(String userId, String userToken, EnumGameVersion gameVersion, boolean isRental) {
        var data = new EntityQuerySearchByGameRequest();
        data.McVersionId = (int) gameVersion.value();
        data.GameType = isRental ? 8 : 2;
        return request(game, "/game-auth-item-list/query/search-by-game", data, userId, userToken, new TypeReference<Entity<EntityQuerySearchByGameResponse>>() {});
    }
    public Entities<EntityComponentDownloadInfoResponse> getGameCoreModDetailsList(String userId, String userToken, List<BigInteger> gameModList) { return getGameCoreModDetailsListAsync(userId, userToken, gameModList).join(); }
    public CompletableFuture<Entities<EntityComponentDownloadInfoResponse>> getGameCoreModDetailsListAsync(String userId, String userToken, List<BigInteger> gameModList) {
        var data = new EntitySearchByIdsQuery();
        data.ItemIdList = gameModList;
        return request(game, "/user-item-download-v2/get-list", data, userId, userToken, new TypeReference<Entities<EntityComponentDownloadInfoResponse>>() {});
    }
    public Entities<EntitySkin> getFreeSkinList(String userId, String userToken, int offset, int length) { return getFreeSkinListAsync(userId, userToken, offset, length).join(); }
    public CompletableFuture<Entities<EntitySkin>> getFreeSkinListAsync(String userId, String userToken, int offset, int length) {
        var data = new EntityFreeSkinListRequest();
        data.IsHas = true;
        data.ItemType = 2;
        data.Length = length;
        data.MasterTypeId = 10;
        data.Offset = offset;
        data.PriceType = 3;
        data.SecondaryTypeId = 31;
        return request(gateway, "/item/query/available", data, userId, userToken, new TypeReference<Entities<EntitySkin>>() {});
    }
    public Entities<EntitySkin> queryFreeSkinByName(String userId, String userToken, String name) { return queryFreeSkinByNameAsync(userId, userToken, name).join(); }
    public CompletableFuture<Entities<EntitySkin>> queryFreeSkinByNameAsync(String userId, String userToken, String name) {
        var data = new EntityQuerySkinByNameRequest();
        data.IsHas = true;
        data.IsSync = 0;
        data.ItemType = 2;
        data.Keyword = name;
        data.Length = 20;
        data.MasterTypeId = 10;
        data.Offset = 0;
        data.PriceType = 3;
        data.SecondaryTypeId = "31";
        data.SortType = 1;
        data.Year = 0;
        return request(gateway, "/item/query/search-by-keyword", data, userId, userToken, new TypeReference<Entities<EntitySkin>>() {});
    }
    public Entities<EntitySkin> getSkinDetails(String userId, String userToken, Entities<EntitySkin> skinList) { return getSkinDetailsAsync(userId, userToken, skinList).join(); }
    public CompletableFuture<Entities<EntitySkin>> getSkinDetailsAsync(String userId, String userToken, Entities<EntitySkin> skinList) {
        var data = new EntitySkinDetailsRequest();
        data.ChannelId = "11";
        data.EntityIds = Arrays.stream(skinList.Data).map(e -> e.EntityId).toList();
        data.IsHas = 1;
        data.WithPrice = 1;
        data.WithTitleImage = 1;
        return request(gateway, "/item/query/search-by-ids", data, userId, userToken, new TypeReference<Entities<EntitySkin>>() {});
    }
    public String buyItemResult(String userId, String userToken, String orderId, int buyType) { return buyItemResultAsync(userId, userToken, orderId, buyType).join(); }
    public CompletableFuture<String> buyItemResultAsync(String userId, String userToken, String orderId, int buyType) {
        var data = new EntityBuyItemResult();
        data.OrderId = orderId;
        data.BuyType = buyType;
        return post(gateway, "/buy-item-result", data, userId, userToken).thenApply(HttpWrapper::readAsString);
    }
    public Entity<EntityPurchaseResult> purchaseSkin(String userId, String userToken, String entityId) { return purchaseSkinAsync(userId, userToken, entityId).join(); }
    public CompletableFuture<Entity<EntityPurchaseResult>> purchaseSkinAsync(String userId, String userToken, String entityId) {
        var data = new EntitySkinPurchaseRequest();
        data.BatchCount = 1;
        data.BuyPath = "PC_H5_COMPONENT_DETAIL";
        data.Diamond = 0;
        data.EntityId = 0;
        data.ItemId = entityId;
        data.ItemLevel = 0;
        data.LastPlayTime = 0;
        data.PurchaseTime = 0;
        data.TotalPlayTime = 0;
        data.UserId = userId;
        data.ReceiverId = "";
        data.CouponIds = null;
        data.ActivityName = "";
        return request(gateway, "/user-item-purchase", data, userId, userToken, new TypeReference<Entity<EntityPurchaseResult>>() {});
    }
    public Entity<EntityCoreLibResponse> getMinecraftClientLibs(String userId, String userToken, EnumGameVersion gameVersion) { return getMinecraftClientLibsAsync(userId, userToken, gameVersion).join(); }
    public CompletableFuture<Entity<EntityCoreLibResponse>> getMinecraftClientLibsAsync(String userId, String userToken, EnumGameVersion gameVersion) {
        var data = new EntityMcDownloadVersion();
        data.McVersion = (int) (gameVersion == null ? EnumGameVersion.NONE : gameVersion).value();
        return request(client, "/game-patch-info", data, userId, userToken, new TypeReference<Entity<EntityCoreLibResponse>>() {});
    }
    public Entity<EntityComponentDownloadInfoResponse> getNetGameComponentDownloadList(String userId, String userToken, String gameId) { return getNetGameComponentDownloadListAsync(userId, userToken, gameId).join(); }
    public CompletableFuture<Entity<EntityComponentDownloadInfoResponse>> getNetGameComponentDownloadListAsync(String userId, String userToken, String gameId) {
        var data = new EntitySearchByItemIdQuery();
        data.ItemId = gameId;
        data.Length = 0;
        data.Offset = 0;
        return request(client, "/user-item-download-v2", data, userId, userToken, new TypeReference<Entity<EntityComponentDownloadInfoResponse>>() {});
    }
    public Entities<EntityRentalGame> getRentalGameList(String userId, String userToken, int offset) { return getRentalGameListAsync(userId, userToken, offset).join(); }
    public CompletableFuture<Entities<EntityRentalGame>> getRentalGameListAsync(String userId, String userToken, int offset) {
        var data = new EntityQueryRentalGame();
        data.Offset = offset;
        data.SortType = 0;
        return rentalRequest("/rental-server/query/available-public-server", data, userId, userToken, new TypeReference<Entities<EntityRentalGame>>() {});
    }
    public Entities<EntityRentalGamePlayerList> getRentalGameRolesList(String userId, String userToken, String entityId) { return getRentalGameRolesListAsync(userId, userToken, entityId).join(); }
    public CompletableFuture<Entities<EntityRentalGamePlayerList>> getRentalGameRolesListAsync(String userId, String userToken, String entityId) {
        var data = new EntityQueryRentalGamePlayerList();
        data.ServerId = entityId;
        data.Offset = 0;
        data.Length = 10;
        return request(rental, "/rental-server-player/query/search-by-user-server", data, userId, userToken, new TypeReference<Entities<EntityRentalGamePlayerList>>() {});
    }
    public Entity<EntityRentalGamePlayerList> addRentalGameRole(String userId, String userToken, String serverId, String roleName) { return addRentalGameRoleAsync(userId, userToken, serverId, roleName).join(); }
    public CompletableFuture<Entity<EntityRentalGamePlayerList>> addRentalGameRoleAsync(String userId, String userToken, String serverId, String roleName) {
        var data = new EntityAddRentalGameRole();
        data.ServerId = serverId;
        data.UserId = userId;
        data.Name = roleName;
        data.CreateTs = 555555;
        data.IsOnline = false;
        data.Status = 0;
        return request(rental, "/rental-server-player", data, userId, userToken, new TypeReference<Entity<EntityRentalGamePlayerList>>() {});
    }
    public Entity<EntityRentalGamePlayerList> deleteRentalGameRole(String userId, String userToken, String entityId) { return deleteRentalGameRoleAsync(userId, userToken, entityId).join(); }
    public CompletableFuture<Entity<EntityRentalGamePlayerList>> deleteRentalGameRoleAsync(String userId, String userToken, String entityId) {
        var data = new EntityDeleteRentalGameRole();
        data.EntityId = entityId;
        return request(rental, "/rental-server-player/delete", data, userId, userToken, new TypeReference<Entity<EntityRentalGamePlayerList>>() {});
    }
    public Entity<EntityRentalGameServerAddress> getRentalGameServerAddress(String userId, String userToken, String entityId, String pwd) { return getRentalGameServerAddressAsync(userId, userToken, entityId, pwd).join(); }
    public CompletableFuture<Entity<EntityRentalGameServerAddress>> getRentalGameServerAddressAsync(String userId, String userToken, String entityId, String pwd) {
        var data = new EntityQueryRentalGameServerAddress();
        data.ServerId = entityId;
        data.Password = pwd == null ? "none" : pwd;
        return request(rental, "/rental-server-world-enter/get", data, userId, userToken, new TypeReference<Entity<EntityRentalGameServerAddress>>() {});
    }
    public Entity<EntityRentalGameDetails> getRentalGameDetails(String userId, String userToken, String entityId) { return getRentalGameDetailsAsync(userId, userToken, entityId).join(); }
    public CompletableFuture<Entity<EntityRentalGameDetails>> getRentalGameDetailsAsync(String userId, String userToken, String entityId) {
        var data = new EntityQueryRentalGameDetail();
        data.ServerId = entityId;
        return rentalRequest("/rental-server-details/get", data, userId, userToken, new TypeReference<Entity<EntityRentalGameDetails>>() {});
    }
    public Entities<EntityRentalGame> searchRentalGameByName(String userId, String userToken, String worldId) { return searchRentalGameByNameAsync(userId, userToken, worldId).join(); }
    public CompletableFuture<Entities<EntityRentalGame>> searchRentalGameByNameAsync(String userId, String userToken, String worldId) {
        var data = new EntityQueryRentalGameById();
        data.Offset = BigInteger.ZERO;
        data.SortType = EnumSortType.General.value();
        data.WorldNameKey = new ArrayList<>(List.of(worldId));
        return rentalRequest("/rental-server/query/available-public-server", data, userId, userToken, new TypeReference<Entities<EntityRentalGame>>() {});
    }
    public Entities<EntitySkin> getFreeSkinList(String userId, String userToken, int offset) { return getFreeSkinList(userId, userToken, offset, 20); }
    public Entity<EntityCoreLibResponse> getMinecraftClientLibs(String userId, String userToken) { return getMinecraftClientLibs(userId, userToken, null); }
    public Entity<EntityRentalGameServerAddress> getRentalGameServerAddress(String userId, String userToken, String entityId) { return getRentalGameServerAddress(userId, userToken, entityId, null); }
    public String setSkin(String userId, String userToken, String entityId) { return setSkinAsync(userId, userToken, entityId).join(); }
    public CompletableFuture<String> setSkinAsync(String userId, String userToken, String entityId) {
        var settings = new ArrayList<EntitySkinSettings>();
        for (int gameType : new int[]{9, 8, 2, 10, 7}) {
            var setting = new EntitySkinSettings(); setting.ClientType = "java"; setting.GameType = gameType; setting.SkinId = entityId; setting.SkinMode = 0; setting.SkinType = 31; settings.add(setting);
        }
        return post(gateway, "/user-game-skin-multi", Map.of("skin_settings", settings), userId, userToken).thenApply(HttpWrapper::readAsString);
    }
    public List<EntityUserGameTexture> getSkinListInGame(String userId, String userToken, EntityUserGameTextureRequest data) { return getSkinListInGameAsync(userId, userToken, data).join(); }
    public CompletableFuture<List<EntityUserGameTexture>> getSkinListInGameAsync(String userId, String userToken, EntityUserGameTextureRequest data) {
        return request(game, "/user-game-skin/query/search-by-type", data, userId, userToken, new TypeReference<Entities<EntityUserGameTexture>>() {}).thenApply(response -> new ArrayList<>(Arrays.asList(response.Data)));
    }
    public void createCharacter(String userId, String userToken, String gameId, String roleName) { createCharacterAsync(userId, userToken, gameId, roleName).join(); }
    public CompletableFuture<Void> createCharacterAsync(String userId, String userToken, String gameId, String roleName) {
        var data = new EntityCreateCharacter(); data.GameId = gameId; data.UserId = userId; data.Name = roleName;
        return post(game, "/game-character", data, userId, userToken).thenAccept(response -> {
            if (!success(response)) throw new IllegalStateException("Failed to create character");
        });
    }
    public CompletableFuture<EntityAuthenticationUpdate> authenticationUpdateAsync(String userId, String userToken) {
        var data = new EntityAuthenticationUpdate(); data.EntityId = userId; data.IsRegister = true;
        String entity = DotNetJson.serializeRelaxed(data); byte[] body = HttpUtil.httpEncrypt(DotNetEncoding.utf8(entity));
        return core.postAsync("/authentication/update", body, builder -> builder.addHeader(TokenUtil.computeHttpRequestToken(builder.Url, entity, userId, userToken))).thenApply(response -> {
            byte[] decrypted = HttpUtil.httpDecrypt(response.body());
            if (success(response)) {
                try { return DotNetJson.deserialize(DotNetEncoding.utf8(decrypted), new TypeReference<Entity<EntityAuthenticationUpdate>>() {}).Data; }
                catch (RuntimeException e) { System.getLogger(WPFLauncher.class.getName()).log(System.Logger.Level.ERROR, "Exception while deserializing authentication update", e); }
            }
            System.getLogger(WPFLauncher.class.getName()).log(System.Logger.Level.ERROR, "Failed to update authentication"); return null;
        });
    }
    public Entities<EntityNetGameItem> queryNetGameWithKeyword(String userId, String userToken, String keyword) { return queryNetGameWithKeywordAsync(userId, userToken, keyword).join(); }
    public CompletableFuture<Entities<EntityNetGameItem>> queryNetGameWithKeywordAsync(String userId, String userToken, String keyword) {
        var data = new EntityNetGameKeyword(); data.Keyword = keyword;
        return post(game, "/item/query/search-by-keyword", data, userId, userToken).thenApply(response -> {
            if (!success(response)) { System.getLogger(WPFLauncher.class.getName()).log(System.Logger.Level.ERROR, "Failed to query net game with keyword, response: " + HttpWrapper.readAsString(response)); return null; }
            return DotNetJson.deserialize(HttpWrapper.readAsString(response), new TypeReference<Entities<EntityNetGameItem>>() {});
        });
    }
    private static boolean success(HttpResponse<?> response) { return response.statusCode() >= 200 && response.statusCode() < 300; }

}
