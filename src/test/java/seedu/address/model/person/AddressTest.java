package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_invalidAddress_throwsIllegalArgumentException() {
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // invalid addresses
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only
        assertFalse(Address.isValidAddress("\u00a0")); // non-breaking space only

        assertFalse(Address.isValidAddress("A".repeat(201)));
        assertFalse(Address.isValidAddress("Main\nStreet"));
        assertFalse(Address.isValidAddress("Main\tStreet"));
        assertFalse(Address.isValidAddress("Main\u0000Street"));

        // valid addresses
        assertTrue(Address.isValidAddress("A".repeat(200)));
        assertTrue(Address.isValidAddress("10  Main Street / #01-02"));
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
    @Test
    public void valueContracts_equalCopiesHaveSameHashAndPreserveText() {
        Address value = new Address("Main Street");
        Address copy = new Address("Main Street");
        assertEquals(value, copy);
        assertEquals(copy, value);
        assertEquals(value.hashCode(), copy.hashCode());
        assertEquals("Main Street", value.toString());
    }

    @Test
    public void constructor_lengthAndPrintableCharacterBoundaries() {
        for (int length : new int[] {1, 2, 199, 200}) {
            assertEquals("A".repeat(length), new Address("A".repeat(length)).value);
        }
        assertEquals("\u6771\u4eac #01-02", new Address("\u6771\u4eac #01-02").value);
        for (String invalid : new String[] {"", "A".repeat(201), "\u00a0 ", "A\rB", "A\u0000B"}) {
            assertThrows(IllegalArgumentException.class, () -> new Address(invalid));
        }
    }

}
