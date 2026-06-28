package resaver.gui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class DebugLogger {

    public static DebugLogger getInstance() {
        return INSTANCE;
    }

    public static Path getLogDir() {
        String appData = System.getenv("APPDATA");
        if (appData == null) appData = System.getProperty("user.home");
        return Paths.get(appData, "ReSaver");
    }

    public synchronized void setEnabled(boolean enable) {
        if (enable && handler == null) {
            try {
                Path dir = getLogDir();
                Files.createDirectories(dir);
                String pattern = dir.resolve("debug_%g.log").toString();
                handler = new FileHandler(pattern, 10_000_000, 5, true);
                handler.setFormatter(new SimpleFormatter());
                Logger.getLogger("").addHandler(handler);
            } catch (IOException ex) {
                Logger.getLogger(DebugLogger.class.getName())
                      .warning("Could not open debug log: " + ex.getMessage());
            }
        } else if (!enable && handler != null) {
            Logger.getLogger("").removeHandler(handler);
            handler.close();
            handler = null;
        }
    }

    public synchronized boolean isEnabled() {
        return handler != null;
    }

    private FileHandler handler;

    private DebugLogger() {}

    private static final DebugLogger INSTANCE = new DebugLogger();
}
