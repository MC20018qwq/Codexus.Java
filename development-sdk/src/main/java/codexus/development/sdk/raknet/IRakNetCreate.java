package codexus.development.sdk.raknet;
public interface IRakNetCreate {
    IRakNet create(String remoteAddress, String nexusToken, String serverId, long userId, String userToken,
                  String serverName, String roleName, int port, int ipv6Port, boolean isRental, boolean isDomainGame);
}
