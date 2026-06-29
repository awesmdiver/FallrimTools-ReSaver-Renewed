package resaver.gui;

import java.io.IOException;
import java.net.URI;
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

    /**
     * Returns the directory where log files are written.
     * For the jpackage exe this resolves to the exe's own directory.
     * For dev (maven -jar) this resolves to the project root.
     */
    public static Path getLogDir() {
        try {
            URI uri = resaver.ReSaver_Renewed.class
                    .getProtectionDomain().getCodeSource().getLocation().toURI();
            Path codePath = Paths.get(uri);
            // classes dir (IDE/exec plugin) → go up one level to project root
            if (Files.isDirectory(codePath)) {
                Path parent = codePath.getParent();
                return parent != null ? parent : codePath;
            }
            // .jar file: jar → app/ → exe dir (jpackage) or project root (dev)
            Path parent = codePath.getParent();
            Path grandparent = parent != null ? parent.getParent() : null;
            return grandparent != null ? grandparent : (parent != null ? parent : codePath);
        } catch (Exception ex) {
            return Paths.get(System.getProperty("user.dir", "."));
        }
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
