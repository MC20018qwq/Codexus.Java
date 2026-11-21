package codexus.gateway.nexus;

import codexus.csharpsup.DotNetJson;
import codexus.gateway.entities.nexus.NexusUser;
import java.io.*;
import java.nio.file.*;

public final class NexusStorage {
    public static final NexusStorage Instance = new NexusStorage(Path.of("nexus-user.json"));
    private final Path file;
    private NexusUser user;
    public NexusStorage() { this(Path.of("nexus-user.json")); }
    public NexusStorage(Path file) {
        this.file = file;
        try { user = Files.exists(file) ? DotNetJson.deserialize(Files.readString(file), NexusUser.class) : null; }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public synchronized void checkout(NexusUser user) {
        try { Files.writeString(file, DotNetJson.serialize(user)); this.user = user; }
        catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public synchronized boolean isRequireAuthorization() { return user == null; }
}
