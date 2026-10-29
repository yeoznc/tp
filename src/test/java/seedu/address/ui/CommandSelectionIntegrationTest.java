package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.CommandResult;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Feeds real command results and real displayed lists into the UI's headless selection resolver. */
public class CommandSelectionIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private LogicManager logic;
    private Person alice;
    private Person bob;

    @BeforeEach
    public void setUp() {
        alice = client("C1", "Alice");
        bob = client("C2", "Bob");
        AddressBook book = new AddressBook();
        book.setPersons(List.of(bob, alice));
        model = new ModelManager(book, new UserPrefs());
        StorageManager storage = new StorageManager(
                new JsonAddressBookStorage(temporaryFolder.resolve("clients.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_addAndSort_selectsAddedClientAndPreservesSelection() throws Exception {
        logic.execute("sort desc");
        CommandResult add = logic.execute("add r/C3 n/Aaron");
        assertEquals(2, PersonListPanel.indexToSelect(logic.getFilteredPersonList(), add, bob));
        Person added = logic.getFilteredPersonList().get(2);
        assertEquals(new ClientReference("C3"), added.getClientReference().orElseThrow());
        CommandResult sort = logic.execute("sort asc");
        assertEquals(0, PersonListPanel.indexToSelect(logic.getFilteredPersonList(), sort, added));
    }

    @Test
    public void execute_referenceAndNameEdit_selectsUpdatedClientAtItsNewPosition() throws Exception {
        logic.execute("sort asc");
        logic.execute("find n/Alice");
        CommandResult edit = logic.execute("edit 1 r/c3 n/Zara");
        assertEquals(List.of("Bob", "Zara"),
                logic.getFilteredPersonList().stream().map(person -> person.getName().fullName).toList());
        assertEquals(1, PersonListPanel.indexToSelect(logic.getFilteredPersonList(), edit, alice));
        Person selected = logic.getFilteredPersonList().get(1);
        assertEquals(new ClientReference("C3"), selected.getClientReference().orElseThrow());
        assertTrue(model.findPersonByReference(new ClientReference("C1")).isEmpty());
    }

    @Test
    public void execute_noteThenDeleteAndClear_resolvesSelectionFallbacks() throws Exception {
        logic.execute("sort asc");
        logic.execute("find n/Bob");
        CommandResult note = logic.execute("note r/C1 s/Hello");
        assertEquals(0, PersonListPanel.indexToSelect(logic.getFilteredPersonList(), note, bob));
        Person selected = logic.getFilteredPersonList().getFirst();
        assertTrue(selected.isSamePerson(alice));
        CommandResult delete = logic.execute("delete 1");
        assertEquals(List.of(bob), logic.getFilteredPersonList());
        assertEquals(0, PersonListPanel.indexToSelect(logic.getFilteredPersonList(), delete, selected));
        CommandResult clear = logic.execute("clear");
        assertEquals(-1, PersonListPanel.indexToSelect(logic.getFilteredPersonList(), clear, bob));
    }

    private Person client(String reference, String name) {
        return new Person(new ClientReference(reference), new Name(name),
                null, null, null, null, null, null, Set.of());
    }
}
