package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.ClientReferenceNotFoundException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Address;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.TypicalPersons;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);

        assertCommandSuccess(deleteCommand, model,
                CommandResult.preserveSelectionOrFirst(expectedMessage), expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToDelete = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        DeleteCommand deleteCommand = new DeleteCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(personToDelete));

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deletePerson(personToDelete);
        showNoPerson(expectedModel);

        assertCommandSuccess(deleteCommand, model,
                CommandResult.preserveSelectionOrFirst(expectedMessage), expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteCommand deleteCommand = new DeleteCommand(outOfBoundIndex);

        assertCommandFailure(deleteCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_unknownReference_throwsClientReferenceNotFoundException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        ClientReference missingReference = new ClientReference("C9999");

        DeleteCommand command = new DeleteCommand(missingReference);

        ClientReferenceNotFoundException exception = assertThrows(
                ClientReferenceNotFoundException.class, () -> command.execute(model));

        assertEquals(
                "No person found with client reference: C9999",
                exception.getMessage());

        assertEquals(
                TypicalPersons.getTypicalPersons(),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_validReference_deletesMatchingPerson() throws Exception {
        Person target = new PersonBuilder(TypicalPersons.ALICE)
                .withClientReference("C0001")
                .build();

        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(target);
        Model model = new ModelManager(addressBook, new UserPrefs());

        DeleteCommand command =
                new DeleteCommand(target.getClientReference().orElse(null));

        CommandResult result = command.execute(model);

        assertEquals(
                String.format(
                        DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                        Messages.format(target)),
                result.getFeedbackToUser());

        assertFalse(model.getAddressBook().getPersonList().contains(target));
    }

    @Test
    public void execute_referenceLookup_skipsPersonWithNullReference()
            throws Exception {
        Person legacyPerson = new Person(
                new Name("Legacy Client"),
                new Phone("91234567"),
                new Email("legacy@example.com"),
                new Address("Legacy Street"),
                new HashSet<>());

        Person referencedPerson = new PersonBuilder(TypicalPersons.ALICE)
                .withClientReference("C0001")
                .build();

        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(legacyPerson);
        addressBook.addPerson(referencedPerson);

        Model model = new ModelManager(addressBook, new UserPrefs());

        DeleteCommand command = new DeleteCommand(referencedPerson.getClientReference().orElse(null));

        command.execute(model);

        assertFalse(model.getAddressBook().getPersonList().contains(referencedPerson));
        assertTrue(model.getAddressBook().getPersonList().contains(legacyPerson));
    }

    @Test
    public void equals() {
        DeleteCommand deleteFirstCommand = new DeleteCommand(INDEX_FIRST_PERSON);
        DeleteCommand deleteSecondCommand = new DeleteCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteCommand deleteFirstCommandCopy = new DeleteCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));

        // same client reference -> returns true
        DeleteCommand first = new DeleteCommand(new ClientReference("C0001"));
        DeleteCommand second = new DeleteCommand(new ClientReference("c0001"));
        assertEquals(first, second);
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteCommand deleteCommand = new DeleteCommand(targetIndex);
        String expected = DeleteCommand.class.getCanonicalName()
                + "{targetIndex=" + targetIndex + ", targetReference=null}";
        assertEquals(expected, deleteCommand.toString());
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoPerson(Model model) {
        model.updateFilteredPersonList(p -> false);

        assertTrue(model.getFilteredPersonList().isEmpty());
    }
    @Test
    public void execute_emptyList_rejectsFirstIndex() {
        Model empty = new ModelManager(new AddressBook(), new UserPrefs());
        assertCommandFailure(new DeleteCommand(INDEX_FIRST_PERSON), empty,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertTrue(empty.getAddressBook().getPersonList().isEmpty());
    }

    @Test
    public void execute_lastValidIndex_removesOnlyLastPerson() throws Exception {
        List<Person> before = List.copyOf(model.getAddressBook().getPersonList());
        new DeleteCommand(Index.fromOneBased(before.size())).execute(model);
        assertEquals(before.subList(0, before.size() - 1), model.getAddressBook().getPersonList());
    }

}
