package seedu.address.model.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class TagTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Tag(null));
    }

    @Test
    public void constructor_invalidTagName_throwsIllegalArgumentException() {
        String invalidTagName = "";
        assertThrows(IllegalArgumentException.class, () -> new Tag(invalidTagName));
    }

    @Test
    public void isValidTagName() {
        // null tag name
        assertThrows(NullPointerException.class, () -> Tag.isValidTagName(null));
    }

    @Test
    public void valueContracts_equalCopiesHaveSameHashAndPreserveText() {
        Tag value = new Tag("Friends1");
        Tag copy = new Tag("Friends1");
        assertEquals(value, copy);
        assertEquals(copy, value);
        assertEquals(value.hashCode(), copy.hashCode());
        assertEquals("[Friends1]", value.toString());
    }

    @Test
    public void equals_distinguishesValuesTypesAndNull() {
        Tag tag = new Tag("A");
        assertTrue(tag.equals(tag));
        assertFalse(tag.equals(new Tag("a")));
        assertFalse(tag.equals("A"));
        assertFalse(tag.equals(null));
    }

    @Test
    public void constructor_alphanumericPartitions() {
        for (String valid : new String[] {"A", "0", "Friends123", "a".repeat(201)}) {
            assertEquals(valid, new Tag(valid).tagName);
        }
        for (String invalid : new String[] {"", " ", "a b", "a-b", "a_b", "a\n"}) {
            assertThrows(IllegalArgumentException.class, () -> new Tag(invalid));
        }
    }

}
