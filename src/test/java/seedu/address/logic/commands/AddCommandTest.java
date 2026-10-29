package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.SortOrder;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class AddCommandTest {

    @Test
    public void constructor_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddCommand(null));
    }

    @Test
    public void execute_personAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingPersonAdded modelStub = new ModelStubAcceptingPersonAdded();
        Person validPerson = new PersonBuilder().withClientReference("C0001").build();

        CommandResult commandResult = new AddCommand(validPerson).execute(modelStub);

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS,
                validPerson.getClientReference().orElse(null), validPerson.getName()),
                commandResult.getFeedbackToUser());
        assertEquals(List.of(validPerson), modelStub.personsAdded);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person validPerson = new PersonBuilder().withClientReference("C0001").build();
        AddCommand addCommand = new AddCommand(validPerson);
        ModelStub modelStub = new ModelStubWithPerson(validPerson);

        assertThrows(CommandException.class,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, validPerson.getClientReference().orElse(null)), ()
                    -> addCommand.execute(modelStub));
    }

    @Test
    public void equals() {
        Person alice = new PersonBuilder().withClientReference("C0001").withName("Alice").build();
        Person bob = new PersonBuilder().withClientReference("C0002").withName("Bob").build();
        AddCommand addAliceCommand = new AddCommand(alice);
        AddCommand addBobCommand = new AddCommand(bob);

        // same object -> returns true
        assertTrue(addAliceCommand.equals(addAliceCommand));

        // same values -> returns true
        AddCommand addAliceCommandCopy = new AddCommand(alice);
        assertTrue(addAliceCommand.equals(addAliceCommandCopy));
        assertEquals(addAliceCommand.hashCode(), addAliceCommandCopy.hashCode());

        // different types -> returns false
        assertFalse(addAliceCommand.equals(1));

        // null -> returns false
        assertFalse(addAliceCommand.equals(null));

        // different person -> returns false
        assertFalse(addAliceCommand.equals(addBobCommand));
    }

    @Test
    public void toStringMethod() {
        Person alice = new PersonBuilder(ALICE).withClientReference("C0001").build();
        AddCommand addCommand = new AddCommand(alice);
        String expected = AddCommand.class.getCanonicalName() + "{toAdd=" + alice
                + ", clashConfirmed=false}";
        assertEquals(expected, addCommand.toString());
    }

    /**
     * A default model stub that has all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public ReadOnlyUserPrefs getUserPrefs() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GuiSettings getGuiSettings() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setGuiSettings(GuiSettings guiSettings) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setAddressBook(ReadOnlyAddressBook newData) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public boolean hasPerson(Person person) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void deletePerson(Person target) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setPerson(Person target, Person editedPerson) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public SortOrder getSortOrder() {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void setSortOrder(SortOrder sortOrder) {
            throw new AssertionError("This method should not be called.");
        }
    }

    /**
     * A Model stub that contains a single person.
     */
    private class ModelStubWithPerson extends ModelStub {
        private final Person person;

        ModelStubWithPerson(Person person) {
            requireNonNull(person);
            this.person = person;
        }

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return this.person.isSamePerson(person);
        }
    }

    /**
     * A Model stub that always accepts the person being added.
     */
    private class ModelStubAcceptingPersonAdded extends ModelStub {
        final ArrayList<Person> personsAdded = new ArrayList<>();

        @Override
        public boolean hasPerson(Person person) {
            requireNonNull(person);
            return personsAdded.stream().anyMatch(person::isSamePerson);
        }

        @Override
        public void addPerson(Person person) {
            requireNonNull(person);
            personsAdded.add(person);
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

    @Test
    public void execute_clashes_requireConfirmationBeforeAdding() throws Exception {
        Person existing = new PersonBuilder().withClientReference("C0001").build();
        Person proposed = new PersonBuilder(existing).withClientReference("C0002").build();
        AddressBook book = new AddressBook();
        book.addPerson(existing);
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Model model = new ModelManager(book, new UserPrefs(), Clock.fixed(now, ZoneOffset.UTC));
        CommandResult warning = new AddCommand(proposed).execute(model);
        assertTrue(warning.requiresClashConfirmation());
        assertEquals(model.findClientClashes(proposed, null), warning.getClientClashes());
        assertEquals(List.of(existing), model.getAddressBook().getPersonList());
        CommandResult success = new AddCommand(proposed, true).execute(model);
        assertFalse(success.requiresClashConfirmation());
        assertEquals(List.of(existing, proposed.withTimestamps(now, now)), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_confirmedDuplicate_stillFailsWithoutMutation() {
        Person existing = new PersonBuilder().withClientReference("C0001").build();
        Person changed = new PersonBuilder(existing).withName("Changed Name").build();
        AddressBook book = new AddressBook();
        book.addPerson(existing);
        Model model = new ModelManager(book, new UserPrefs());
        assertThrows(CommandException.class,
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, existing.getClientReference().orElseThrow()), ()
                    -> new AddCommand(changed, true).execute(model));
        assertEquals(List.of(existing), model.getAddressBook().getPersonList());
    }

    @Test
    public void equals_confirmationState_distinguishesCommands() {
        Person person = new PersonBuilder().withClientReference("C0001").build();
        AddCommand confirmed = new AddCommand(person, true);
        AddCommand copy = new AddCommand(person, true);
        assertEquals(confirmed, copy);
        assertEquals(confirmed.hashCode(), copy.hashCode());
        assertFalse(confirmed.equals(new AddCommand(person, false)));
    }

}
