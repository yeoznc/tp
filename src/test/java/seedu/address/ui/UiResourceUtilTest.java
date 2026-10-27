package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class UiResourceUtilTest {
    @Test
    public void getImage_existingImage_returnsImage() {
        assertNotNull(UiResourceUtil.getImage("/images/address_book_32.png"));
    }

    @Test
    public void getImage_nullPath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> UiResourceUtil.getImage(null));
    }
}
