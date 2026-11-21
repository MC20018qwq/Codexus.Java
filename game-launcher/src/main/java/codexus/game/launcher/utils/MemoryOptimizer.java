package codexus.game.launcher.utils;

import com.sun.jna.*;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.win32.StdCallLibrary;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public final class MemoryOptimizer implements AutoCloseable {
    private static final System.Logger LOG = System.getLogger(MemoryOptimizer.class.getName());
    private static MemoryOptimizer instance;
    private final Set<Long> processedIds = new HashSet<>();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(command -> Thread.ofPlatform().daemon().name("codexus-memory-optimizer").unstarted(command));
    private volatile boolean disposed;
    private final Object processLock = new Object();
    private interface Kernel extends StdCallLibrary {
        Kernel API = Native.load("kernel32", Kernel.class);
        Pointer OpenProcess(int access, boolean inherit, int processId);
        boolean CloseHandle(Pointer handle);
        boolean SetProcessWorkingSetSize(Pointer process, SIZE_T minimum, SIZE_T maximum);
    }
    private interface Psapi extends StdCallLibrary {
        Psapi API = Native.load("psapi", Psapi.class);
        boolean EmptyWorkingSet(Pointer process);
        boolean GetProcessMemoryInfo(Pointer process, Counters memory, int bytes);
    }
    @Structure.FieldOrder({"cb", "pageFaultCount", "peakWorkingSet", "workingSet", "peakPagedPool", "pagedPool", "peakNonPagedPool", "nonPagedPool", "pagefileUsage", "peakPagefileUsage"})
    public static class Counters extends Structure {
        public int cb, pageFaultCount;
        public SIZE_T peakWorkingSet = new SIZE_T(), workingSet = new SIZE_T(), peakPagedPool = new SIZE_T(), pagedPool = new SIZE_T(),
                peakNonPagedPool = new SIZE_T(), nonPagedPool = new SIZE_T(), pagefileUsage = new SIZE_T(), peakPagefileUsage = new SIZE_T();
        public Counters() { cb = size(); }
    }
    private MemoryOptimizer() { }
    public static synchronized MemoryOptimizer getInstance() {
        if (instance == null) {
            instance = new MemoryOptimizer();
            instance.timer.scheduleAtFixedRate(instance::optimizeCallback, 0, 7, TimeUnit.MINUTES);
        }
        return instance;
    }
    private void optimizeCallback() {
        if (disposed || !System.getProperty("os.name").startsWith("Windows")) { close(); return; }
        try {
            List<ProcessHandle> processes;
            try (var all = ProcessHandle.allProcesses()) {
                processes = all.filter(process -> process.info().command().map(command -> Path.of(command).getFileName().toString().equalsIgnoreCase("javaw.exe")).orElse(false))
                        .filter(MemoryOptimizer::isMinecraftProcess).toList();
            }
            if (processes.isEmpty()) { LOG.log(System.Logger.Level.INFO, "[Memory Optimizer] No Minecraft processes found, stopping optimizer"); close(); return; }
            for (ProcessHandle process : processes) optimizeProcess(process);
            synchronized (processLock) { processedIds.removeIf(id -> ProcessHandle.of(id).map(process -> !process.isAlive()).orElse(true)); }
        } catch (Exception e) { LOG.log(System.Logger.Level.ERROR, "[Memory Optimizer] Optimization failed", e); }
    }
    private static boolean isMinecraftProcess(ProcessHandle process) {
        String command = commandLine(process).toLowerCase(Locale.ROOT);
        return List.of("minecraft", "net.minecraft", "launchwrapper", "forge", "fabric", "quilt", "optifine", ".minecraft", "versions", "libraries").stream().anyMatch(command::contains);
    }
    private static String commandLine(ProcessHandle process) {
        // Same Win32_Process query as System.Management; use the OS CIM provider when Java cannot read it.
        var direct = process.info().commandLine();
        if (direct.isPresent()) return direct.get();
        try {
            Process query = new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-WindowStyle", "Hidden", "-Command",
                    "[Console]::OutputEncoding=[System.Text.Encoding]::UTF8;(Get-CimInstance Win32_Process -Filter 'ProcessId=" + process.pid() + "').CommandLine")
                    .redirectError(ProcessBuilder.Redirect.DISCARD).start();
            try {
                if (!query.waitFor(5, TimeUnit.SECONDS)) { query.destroyForcibly(); return ""; }
                return new String(query.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } finally { query.destroy(); }
        } catch (Exception e) { return ""; }
    }
    private void optimizeProcess(ProcessHandle process) {
        synchronized (processLock) {
            if (disposed || !process.isAlive()) return;
            Pointer handle = Kernel.API.OpenProcess(1024 | 256, false, (int) process.pid());
            if (handle == null) return;
            try {
                var before = new Counters();
                if (!Psapi.API.GetProcessMemoryInfo(handle, before, before.size())) return;
                if (!Psapi.API.EmptyWorkingSet(handle)) return;
                long workingSet = before.workingSet.longValue(), minimum = Math.max(52_428_800L, workingSet / 4), maximum = Math.max(minimum * 2, workingSet);
                Kernel.API.SetProcessWorkingSetSize(handle, new SIZE_T(minimum), new SIZE_T(maximum));
                Thread.sleep(500);
                var after = new Counters();
                if (Psapi.API.GetProcessMemoryInfo(handle, after, after.size())) LOG.log(System.Logger.Level.INFO,
                        "[Memory Optimizer] Process " + process.pid() + ": " + workingSet / 1_048_576 + " MB -> " + after.workingSet.longValue() / 1_048_576 + " MB");
                processedIds.add(process.pid());
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            finally { Kernel.API.CloseHandle(handle); }
        }
    }
    @Override public void close() {
        synchronized (MemoryOptimizer.class) {
            if (disposed) return;
            disposed = true; timer.shutdownNow(); if (instance == this) instance = null;
        }
    }
}
