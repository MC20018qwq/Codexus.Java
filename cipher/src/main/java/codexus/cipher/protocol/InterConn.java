package codexus.cipher.protocol;

import codexus.csharpsup.DotNetJson;
import codexus.cipher.entities.interconn.InterConnGameStart;
import codexus.cipher.utils.cipher.TokenUtil;
import codexus.cipher.utils.http.HttpWrapper;
import java.util.*;
import java.util.concurrent.*;

public final class InterConn {
    private InterConn() { }
    private static final HttpWrapper CORE = new HttpWrapper("https://x19obtcore.nie.netease.com:8443", builder -> builder.userAgent(WPFLauncher.getUserAgent()));
    public static CompletableFuture<Void> loginStart(String entityId, String entityToken) {
        return CORE.postAsync("/interconn/web/game-play-v2/login-start", "{\"strict_mode\":true}", builder -> builder.addHeader(TokenUtil.computeHttpRequestToken(builder.Url, builder.Body, entityId, entityToken)))
                .thenAccept(response -> System.getLogger(InterConn.class.getName()).log(System.Logger.Level.DEBUG, "LoginStart response: " + HttpWrapper.readAsString(response)));
    }
    public static CompletableFuture<Void> gameStart(String entityId, String entityToken, String gameId) {
        var data = new InterConnGameStart(); data.GameId = gameId; data.ItemList = new String[]{"10000"};
        return CORE.postAsync("/interconn/web/game-play-v2/start", DotNetJson.serialize(data), builder -> builder.addHeader(TokenUtil.computeHttpRequestToken(builder.Url, builder.Body, entityId, entityToken)))
                .thenAccept(response -> System.getLogger(InterConn.class.getName()).log(System.Logger.Level.DEBUG, "GameStart response: " + HttpWrapper.readAsString(response)));
    }
}
