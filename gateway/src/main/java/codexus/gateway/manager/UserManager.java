package codexus.gateway.manager;

import codexus.cipher.entities.wpflauncher.EntityAuthenticationOtp;
import codexus.cipher.protocol.WPFLauncher;
import codexus.csharpsup.*;
import codexus.development.sdk.entities.EntityAvailableUser;
import codexus.development.sdk.manager.IUserManager;
import codexus.gateway.entities.web.EntityUser;
import codexus.gateway.entities.web.gateway.EntityAccount;
import codexus.gateway.enums.Platform;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public final class UserManager implements IUserManager, AutoCloseable {
    public static final UserManager Instance = new UserManager(Path.of("users.json"), WPFLauncher::new);
    private static final System.Logger LOG = System.getLogger(UserManager.class.getName());
    private final ConcurrentMap<String, EntityUser> users = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, EntityAvailableUser> available = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, Thread.ofPlatform().daemon(true).name("gateway-users-", 0).factory());
    private final Path file;
    private final Supplier<WPFLauncher> launcherFactory;
    private volatile WPFLauncher maintenanceLauncher;
    private volatile boolean closed;
    private final Object saveLock = new Object();
    private final AtomicLong revision = new AtomicLong();
    private volatile long savedRevision;
    private ScheduledFuture<?> pendingSave;
    public UserManager(Path file, Supplier<WPFLauncher> launcherFactory) {
        this.file = file.toAbsolutePath().normalize(); this.launcherFactory = Objects.requireNonNull(launcherFactory);
        IUserManager.Current.Instance = this;
        scheduler.scheduleWithFixedDelay(this::maintain, 2, 2, TimeUnit.SECONDS);
    }
    @Override public EntityAvailableUser getAvailableUser(String id) { return available.get(id); }
    public List<EntityAccount> getAvailableUsers() {
        return available.values().stream().map(value -> {
            var account = new EntityAccount(); account.UserId = value.UserId;
            var user = users.get(value.UserId); account.Alias = user == null ? "" : user.Alias; return account;
        }).toList();
    }
    public List<EntityUser> getUsersNoDetails() {
        return users.values().stream().map(user -> {
            var copy = new EntityUser(); copy.UserId = user.UserId; copy.Authorized = user.Authorized; copy.AutoLogin = false;
            copy.Channel = user.Channel; copy.Type = user.Type; copy.Details = ""; copy.Platform = user.Platform; copy.Alias = user.Alias; return copy;
        }).toList();
    }
    public EntityUser getUserByEntityId(String id) { return users.get(id); }
    public EntityAvailableUser getLastAvailableUser() { return available.values().stream().reduce((first, last) -> last).orElse(null); }
    public void addUserToMaintain(EntityAuthenticationOtp authentication) {
        Objects.requireNonNull(authentication);
        available.compute(authentication.EntityId, (id, existing) -> {
            var user = existing == null ? new EntityAvailableUser() : existing;
            user.UserId = id; user.AccessToken = authentication.Token; user.LastLoginTime = System.currentTimeMillis(); return user;
        });
    }
    public void addUser(EntityUser user) { addUser(user, true, Platform.Desktop); }
    public void addUser(EntityUser user, boolean saveToDisk) { addUser(user, saveToDisk, Platform.Desktop); }
    public void addUser(EntityUser user, boolean saveToDisk, Platform platform) {
        Objects.requireNonNull(user); user.setPlatform(platform);
        users.compute(user.UserId, (id, existing) -> { if (existing == null) return user; existing.Authorized = true; return existing; });
        if (saveToDisk) markDirtyAndScheduleSave();
    }
    public void removeUser(String id) { if (users.remove(id) != null) markDirtyAndScheduleSave(); }
    public void removeAvailableUser(String id) {
        available.remove(id); var user = users.get(id);
        if (user != null) { user.Authorized = false; markDirtyAndScheduleSave(); }
    }
    public void readUsersFromDisk() {
        try {
            if (!Files.exists(file)) return;
            var loaded = DotNetJson.deserialize(Files.readString(file), new TypeReference<List<EntityUser>>() { });
            users.clear();
            if (loaded != null) for (var user : loaded) { user.Authorized = false; users.putIfAbsent(user.UserId, user); }
        } catch (Exception e) { users.clear(); LOG.log(System.Logger.Level.ERROR, "Error reading users from disk", e); }
    }
    public CompletableFuture<Void> readUsersFromDiskAsync() { return CompletableFuture.runAsync(this::readUsersFromDisk, Thread::startVirtualThread); }
    public synchronized void markDirtyAndScheduleSave() {
        if (closed) return;
        revision.incrementAndGet(); if (pendingSave != null) pendingSave.cancel(false);
        pendingSave = scheduler.schedule(() -> { try { saveIfDirty(); } catch (Exception e) { LOG.log(System.Logger.Level.ERROR, "Error saving users", e); } }, 1, TimeUnit.SECONDS);
    }
    private void saveIfDirty() {
        synchronized (saveLock) {
            long snapshot = revision.get(); if (snapshot == savedRevision) return;
            try {
                Files.createDirectories(file.getParent());
                Path temporary = Files.createTempFile(file.getParent(), ".users-", ".json");
                try {
                    Files.writeString(temporary, DotNetJson.serializeRelaxedIndented(List.copyOf(users.values())));
                    try { Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
                    catch (AtomicMoveNotSupportedException e) { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING); }
                } finally { Files.deleteIfExists(temporary); }
                savedRevision = snapshot;
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }
    public void saveUsersToDisk() { revision.incrementAndGet(); saveIfDirty(); }
    public CompletableFuture<Void> saveUsersToDiskAsync() { return CompletableFuture.runAsync(this::saveUsersToDisk, Thread::startVirtualThread); }

    private void maintain() {
        if (closed) return;
        long expiration = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30);
        for (var user : List.copyOf(available.values())) {
            if (closed) return;
            if (user.LastLoginTime >= expiration) continue;
            try {
                if (maintenanceLauncher == null) {
                    var created = launcherFactory.get();
                    synchronized (this) {
                        if (closed) { created.close(); return; }
                        maintenanceLauncher = created;
                    }
                }
                String oldToken = user.AccessToken;
                var update = maintenanceLauncher.authenticationUpdateAsync(user.UserId, oldToken).get();
                if (!closed && update != null && update.Token != null) available.computeIfPresent(user.UserId, (id, current) -> {
                    if (current == user && Objects.equals(current.AccessToken, oldToken)) {
                        current.AccessToken = update.Token; current.LastLoginTime = System.currentTimeMillis();
                    }
                    return current;
                });
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            catch (Exception e) { if (!closed) LOG.log(System.Logger.Level.ERROR, "Unable to refresh user " + user.UserId, e); }
        }
    }
    @Override public void close() {
        WPFLauncher launcher;
        synchronized (this) {
            if (closed) return; closed = true; scheduler.shutdownNow(); launcher = maintenanceLauncher;
        }
        try { saveIfDirty(); }
        finally {
            if (launcher != null) launcher.close();
            try { scheduler.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            if (IUserManager.Current.Instance == this) IUserManager.Current.Instance = null;
        }
    }
}
