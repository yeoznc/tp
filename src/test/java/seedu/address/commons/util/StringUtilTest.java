package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class StringUtilTest {

    //---------------- Tests for isNonZeroUnsignedInteger --------------------------------------

    @Test
    public void isNonZeroUnsignedInteger() {

        // EP: empty strings
        assertFalse(StringUtil.isNonZeroUnsignedInteger("")); // Boundary value
        assertFalse(StringUtil.isNonZeroUnsignedInteger("  "));

        // EP: not a number
        assertFalse(StringUtil.isNonZeroUnsignedInteger("a"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("aaa"));

        // EP: zero
        assertFalse(StringUtil.isNonZeroUnsignedInteger("0"));

        // EP: zero as prefix
        assertTrue(StringUtil.isNonZeroUnsignedInteger("01"));

        // EP: signed numbers
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-1"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("+1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNonZeroUnsignedInteger(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNonZeroUnsignedInteger("1 0")); // Spaces in the middle

        // EP: number larger than Integer.MAX_VALUE
        assertFalse(StringUtil.isNonZeroUnsignedInteger(Long.toString(Integer.MAX_VALUE + 1)));

        // EP: valid numbers, should return true
        assertTrue(StringUtil.isNonZeroUnsignedInteger("1")); // Boundary value
        assertTrue(StringUtil.isNonZeroUnsignedInteger("10"));
    }


    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
            .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

    @Test
    public void isNonZeroUnsignedInteger_integerBoundaries() {
        assertTrue(StringUtil.isNonZeroUnsignedInteger("2147483646"));
        assertTrue(StringUtil.isNonZeroUnsignedInteger("2147483647"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("2147483648"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("-2147483648"));
        assertFalse(StringUtil.isNonZeroUnsignedInteger("00"));
        assertThrows(NullPointerException.class, () -> StringUtil.isNonZeroUnsignedInteger(null));
    }

    @Test
    public void getDetails_nestedCauseAndMissingMessage_preservesDiagnosticInformation() {
        RuntimeException cause = new RuntimeException("root cause");
        String details = StringUtil.getDetails(new IllegalArgumentException("outer", cause));
        assertTrue(details.startsWith("outer\n"));
        assertTrue(details.contains("Caused by: java.lang.RuntimeException: root cause"));
        assertTrue(details.contains("getDetails_nestedCauseAndMissingMessage"));
        assertTrue(StringUtil.getDetails(new RuntimeException()).contains("java.lang.RuntimeException"));
    }

}
