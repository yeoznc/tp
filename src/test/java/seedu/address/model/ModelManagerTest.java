package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private static final Instant CREATED = Instant.parse("2026-03-15T01:42:00Z");
    private static final Instant MODIFIED = Instant.parse("2026-10-12T06:18:00Z");

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void findPersonByReference_matchesNormalizedReference() {
        var client = new PersonBuilder(ALICE).withClientReference("C0001").build();
        modelManager.addPerson(client);
        assertEquals(client.getClientReference(), modelManager.findPersonByReference(
                new ClientReference("c0001")).orElseThrow().getClientReference());
        assertTrue(modelManager.findPersonByReference(new ClientReference("C9999")).isEmpty());
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void addPerson_setsBothTimestamps() {
        ModelManager stampedModel = new ModelManager(new AddressBook(), new UserPrefs(),
                Clock.fixed(CREATED, ZoneOffset.UTC));
        stampedModel.addPerson(new PersonBuilder().withClientReference("C0001").build());

        var saved = stampedModel.getAddressBook().getPersonList().getFirst();
        assertEquals(CREATED, saved.getCreatedAt());
        assertEquals(CREATED, saved.getLastModifiedAt());
    }

    @Test
    public void setPerson_preservesCreationAndRefreshesModificationForUnchangedEdit() {
        var original = new PersonBuilder().withClientReference("C0001").build()
                .withTimestamps(CREATED, CREATED);
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(original);
        ModelManager stampedModel = new ModelManager(addressBook, new UserPrefs(),
                Clock.fixed(MODIFIED, ZoneOffset.UTC));

        stampedModel.setPerson(original, original);

        var saved = stampedModel.getAddressBook().getPersonList().getFirst();
        assertEquals(CREATED, saved.getCreatedAt());
        assertEquals(MODIFIED, saved.getLastModifiedAt());
    }

    @Test
    public void setPerson_legacyRecord_keepsUnknownCreation() {
        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(ALICE);
        ModelManager stampedModel = new ModelManager(addressBook, new UserPrefs(),
                Clock.fixed(MODIFIED, ZoneOffset.UTC));

        stampedModel.setPerson(ALICE, new PersonBuilder(ALICE).withPhone("91234567").build());

        var saved = stampedModel.getAddressBook().getPersonList().getFirst();
        assertNull(saved.getCreatedAt());
        assertEquals(MODIFIED, saved.getLastModifiedAt());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
