package seedu.address.commons.core;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/** Resolves application files beside the distributable JAR. */
public final class AppPaths {
    private AppPaths() { }

    /** Returns the JAR directory, or the working directory for development runs. */
    public static Path getHomeDirectory() {
        URL location = AppPaths.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            return resolveHome(Path.of(location.toURI()), Path.of("").toAbsolutePath());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Unable to resolve the application location", e);
        }
    }

    /** Resolves a home directory from a code location and development working directory. */
    static Path resolveHome(Path codeLocation, Path workingDirectory) {
        Path absoluteCodeLocation = codeLocation.toAbsolutePath().normalize();
        if (Files.isRegularFile(absoluteCodeLocation)
                && absoluteCodeLocation.getFileName().toString().endsWith(".jar")) {
            return absoluteCodeLocation.getParent();
        }
        return workingDirectory.toAbsolutePath().normalize();
    }
}
