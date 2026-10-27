package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.FindPersonPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.testutil.PersonBuilder;

public class HashCodeContractTest {
    @Test
    public void equalObjects_workInHashCollections() {
        assertEqualHashes(Index.fromOneBased(2), Index.fromZeroBased(1));
        assertEqualHashes(new NameContainsKeywordsPredicate(List.of("Alice")),
                new NameContainsKeywordsPredicate(List.of("Alice")));

        FindPersonPredicate firstPredicate = new FindPersonPredicate(List.of("Alice"), List.of());
        FindPersonPredicate secondPredicate = new FindPersonPredicate(List.of("Alice"), List.of());
        assertEqualHashes(firstPredicate, secondPredicate);
        assertEqualHashes(new FindCommand(firstPredicate), new FindCommand(secondPredicate));
        assertEqualHashes(new AddCommand(new PersonBuilder().withClientReference("C0001").build()),
                new AddCommand(new PersonBuilder().withClientReference("C0001").build()));
        assertEqualHashes(new DeleteCommand(Index.fromOneBased(2)), new DeleteCommand(Index.fromZeroBased(1)));

        EditPersonDescriptor firstDescriptor = new EditPersonDescriptor();
        EditPersonDescriptor secondDescriptor = new EditPersonDescriptor();
        assertEqualHashes(firstDescriptor, secondDescriptor);
        assertEqualHashes(new EditCommand(Index.fromOneBased(2), firstDescriptor),
                new EditCommand(Index.fromZeroBased(1), secondDescriptor));
        assertEqualHashes(new ModelManager(), new ModelManager());
    }

    private static void assertEqualHashes(Object first, Object second) {
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertTrue(new HashSet<>(List.of(first)).contains(second));
    }
}
