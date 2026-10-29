package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PhoneTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Phone(null));
    }

    @Test
    public void constructor_invalidPhone_throwsIllegalArgumentException() {
        String invalidPhone = "";
        assertThrows(IllegalArgumentException.class, () -> new Phone(invalidPhone));
    }

    @Test
    public void isValidPhone() {
        // null phone number
        assertThrows(NullPointerException.class, () -> Phone.isValidPhone(null));

        // invalid phone numbers
        assertFalse(Phone.isValidPhone("")); // empty string
        assertFalse(Phone.isValidPhone(" ")); // spaces only
        assertFalse(Phone.isValidPhone("91")); // less than 3 numbers
        assertFalse(Phone.isValidPhone("phone")); // non-numeric
        assertFalse(Phone.isValidPhone("9011p041")); // alphabets within digits
        assertFalse(Phone.isValidPhone("9312 1534")); // spaces within digits

        assertFalse(Phone.isValidPhone("1".repeat(16))); // maximum length is 15
        assertFalse(Phone.isValidPhone("123-456")); // symbols are not accepted

        // valid phone numbers
        assertTrue(Phone.isValidPhone("911")); // exactly 3 numbers
        assertTrue(Phone.isValidPhone("93121534"));
        assertTrue(Phone.isValidPhone("124293842033123")); // long phone numbers
    }

    @Test
    public void equals() {
        Phone phone = new Phone("999");

        // same values -> returns true
        assertTrue(phone.equals(new Phone("999")));

        // same object -> returns true
        assertTrue(phone.equals(phone));

        // null -> returns false
        assertFalse(phone.equals(null));

        // different types -> returns false
        assertFalse(phone.equals(5.0f));

        // different values -> returns false
        assertFalse(phone.equals(new Phone("995")));
    }
    @Test
    public void valueContracts_equalCopiesHaveSameHashAndPreserveText() {
        Phone value = new Phone("00123");
        Phone copy = new Phone("00123");
        assertEquals(value, copy);
        assertEquals(copy, value);
        assertEquals(value.hashCode(), copy.hashCode());
        assertEquals("00123", value.toString());
    }

    @Test
    public void constructor_digitLengthBoundaries() {
        for (int length : new int[] {3, 4, 14, 15}) {
            assertEquals("0".repeat(length), new Phone("0".repeat(length)).value);
        }
        for (String invalid : new String[] {"12", "1".repeat(16), "+123", "\uff11\uff12\uff13", "123\n"}) {
            assertThrows(IllegalArgumentException.class, () -> new Phone(invalid));
        }
    }

}
