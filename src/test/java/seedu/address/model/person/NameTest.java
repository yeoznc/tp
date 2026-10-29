package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertFalse(Name.isValidName("12345")); // numbers only
        assertFalse(Name.isValidName("peter the 2nd")); // digits are outside US01
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("Mary O'Connor-Smith")); // apostrophes and hyphens
        assertTrue(Name.isValidName("A".repeat(100)));
        assertFalse(Name.isValidName("A".repeat(101)));
        assertFalse(Name.isValidName("-'")); // requires at least one letter
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
    @Test
    public void valueContracts_equalCopiesHaveSameHashAndPreserveText() {
        Name value = new Name("Alice");
        Name copy = new Name("Alice");
        assertEquals(value, copy);
        assertEquals(copy, value);
        assertEquals(value.hashCode(), copy.hashCode());
        assertEquals("Alice", value.toString());
    }

    @Test
    public void constructor_lengthBoundariesAndUnicode() {
        for (int length : new int[] {1, 2, 99, 100}) {
            assertEquals("A".repeat(length), new Name("A".repeat(length)).fullName);
        }
        assertEquals("\u674e\u660e", new Name("\u674e\u660e").fullName);
        for (String invalid : new String[] {"", "A".repeat(101), "Alice\tBob", "Alice\nBob", "---"}) {
            assertThrows(IllegalArgumentException.class, () -> new Name(invalid));
        }
    }

}
