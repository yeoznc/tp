package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.scene.image.Image;
import seedu.address.MainApp;

/** Loads resources used by the JavaFX UI. */
public final class UiResourceUtil {
    private UiResourceUtil() { }

    public static Image getImage(String imagePath) {
        requireNonNull(imagePath);
        return new Image(MainApp.class.getResourceAsStream(imagePath));
    }
}
