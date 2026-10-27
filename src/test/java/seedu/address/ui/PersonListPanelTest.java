package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.CommandResult;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

class PersonListPanelTest {

    @Test
    void indexToSelect_preservesClientAfterSortAndReplacement() {
        Person alice = new PersonBuilder().withClientReference("C0001").withName("Alice").build();
        Person bob = new PersonBuilder().withClientReference("C0002").withName("Bob").build();
        Person updatedAlice = new PersonBuilder(alice).withName("Zoe").build();
        CommandResult sortResult = CommandResult.preserveSelectionOrFirst("Sorted");

        assertEquals(1, PersonListPanel.indexToSelect(List.of(bob, alice), sortResult, alice));
        assertEquals(1, PersonListPanel.indexToSelect(List.of(bob, updatedAlice), sortResult, alice));
    }

    @Test
    void indexToSelect_honorsExplicitPersonAndReference() {
        Person alice = new PersonBuilder().withClientReference("C0001").withName("Alice").build();
        Person bob = new PersonBuilder().withClientReference("C0002").withName("Bob").build();
        List<Person> displayed = List.of(bob, alice);

        assertEquals(1, PersonListPanel.indexToSelect(displayed,
                CommandResult.selectPerson("Added", alice), bob));
        assertEquals(1, PersonListPanel.indexToSelect(displayed,
                new CommandResult("Note updated", alice.getClientReference()), bob));
    }

    @Test
    void indexToSelect_missingClientFallsBackToFirstOrEmptyState() {
        Person alice = new PersonBuilder().withClientReference("C0001").withName("Alice").build();
        Person bob = new PersonBuilder().withClientReference("C0002").withName("Bob").build();
        CommandResult result = new CommandResult("List updated");

        assertEquals(0, PersonListPanel.indexToSelect(List.of(bob), result, alice));
        assertEquals(-1, PersonListPanel.indexToSelect(List.of(), result, alice));
        assertEquals(0, PersonListPanel.indexToSelect(List.of(bob),
                CommandResult.selectPerson("Added", alice), bob));
        assertEquals(-1, PersonListPanel.indexToSelect(List.of(),
                new CommandResult("Note updated", alice.getClientReference()), bob));
    }
}
