package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AppUtilTest {

    @Test
    public void checkArgument_true_nothingHappens() {
        AppUtil.checkArgument(true);
        AppUtil.checkArgument(true, "");
    }

    @Test
    public void checkArgument_falseWithoutErrorMessage_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> AppUtil.checkArgument(false));
    }

    @Test
    public void checkArgument_falseWithErrorMessage_throwsIllegalArgumentException() {
        String errorMessage = "error message";
        assertThrows(IllegalArgumentException.class, errorMessage, () -> AppUtil.checkArgument(false, errorMessage));
    }
    @Test
    public void checkArgument_messageIsPreserved() {
        IllegalArgumentException failure = org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class, () -> AppUtil.checkArgument(false, "specific reason"));
        assertEquals("specific reason", failure.getMessage());
        AppUtil.checkArgument(true, null);
    }

    @Test
    public void checkArgument_nullCondition_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> AppUtil.checkArgument(null));
        assertThrows(NullPointerException.class, () -> AppUtil.checkArgument(null, "message"));
    }

}
