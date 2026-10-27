package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.Instant;

import org.junit.jupiter.api.Test;

/** Tests that focused updates retain unrelated client data. */
public class PersonCopyTest {
    @Test
    public void focusedUpdates_preserveOtherFields() {
        Instant created = Instant.parse("2026-01-01T00:00:00Z");
        Instant modified = Instant.parse("2026-02-01T00:00:00Z");
        ServiceNote note = new ServiceNote("Follow up");
        Person original = ALICE.withServiceNote(note).withTimestamps(created, modified);

        Person edited = original.withContactDetails(new Name("Different Name"), original.getPhone(),
                original.getEmail(), original.getAddress(), original.getTags());
        assertEquals(note, edited.getServiceNote());
        assertEquals(original.getCardDetails(), edited.getCardDetails());
        assertEquals(created, edited.getCreatedAt());
        assertEquals(modified, edited.getLastModifiedAt());

        Person withoutCard = edited.withoutCardDetails();
        assertNull(withoutCard.getCardDetails());
        assertEquals(note, withoutCard.getServiceNote());
        assertEquals(edited.getCreditScore(), withoutCard.getCreditScore());
        assertEquals(edited.getDebt(), withoutCard.getDebt());
        assertEquals(created, withoutCard.getCreatedAt());
        assertEquals(modified, withoutCard.getLastModifiedAt());
        assertEquals(original.getCardDetails(), original.getCardDetails());
    }
}
