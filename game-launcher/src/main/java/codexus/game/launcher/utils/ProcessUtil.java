package codexus.game.launcher.utils;

import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

/** The Windows Process.CloseMainWindow operation; Process.destroy terminates instead. */
public final class ProcessUtil {
    private ProcessUtil() { }
    public static boolean closeMainWindow(Process process) {
        if (!System.getProperty("os.name").startsWith("Windows")) return false;
        boolean[] found = {false};
        User32.INSTANCE.EnumWindows((window, argument) -> {
            var owner = new IntByReference(); User32.INSTANCE.GetWindowThreadProcessId(window, owner);
            if (Integer.toUnsignedLong(owner.getValue()) == process.pid() && User32.INSTANCE.IsWindowVisible(window)
                    && User32.INSTANCE.GetWindow(window, new WinDef.DWORD(4)) == null) {
                User32.INSTANCE.PostMessage(window, WinUser.WM_CLOSE, null, null); found[0] = true; return false;
            }
            return true;
        }, null);
        return found[0];
    }
}
