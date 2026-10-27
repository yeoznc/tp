package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.ServiceNote;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

public class RemoveCardCommandIntegrationTest {
    private static final String ADD_CLIENT = "add r/C0001 n/Alex Yeoh p/87438807"
            + " e/alex@example.com a/10 Main Street cn/4111111111111111 cvv/123 exp/12/28"
            + " provider/Visa cs/700 d/1250.50 t/friends";

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private LogicManager logic;
    private Path dataPath;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        dataPath = temporaryFolder.resolve("addressbook.json");
        logic = new LogicManager(model, storage(new JsonAddressBookStorage(dataPath)));
    }

    @Test
    public void execute_existingCard_removesCardAndPreservesOtherFieldsAcrossReload() throws Exception {
        logic.execute(ADD_CLIENT);
        logic.execute("note r/C0001 s/Follow up");
        Person before = model.getAddressBook().getPersonList().getFirst();
        logic.execute("find n/Alex");

        CommandResult result = logic.execute("removecard r/c0001");

        assertEquals("Removed card ending in 1111 from client C0001", result.getFeedbackToUser());
        assertEquals(1, model.getFilteredPersonList().size());
        Person after = model.getFilteredPersonList().getFirst();
        assertEquals(before.getClientReference(), after.getClientReference());
        assertEquals(before.getName(), after.getName());
        assertEquals(before.getPhone(), after.getPhone());
        assertEquals(before.getEmail(), after.getEmail());
        assertEquals(before.getAddress(), after.getAddress());
        assertEquals(before.getCreditScore(), after.getCreditScore());
        assertEquals(before.getDebt(), after.getDebt());
        assertEquals(before.getTags(), after.getTags());
        assertEquals(new ServiceNote("Follow up"), after.getServiceNote());
        assertNull(after.getCardDetails());
        assertFalse(Files.readString(dataPath).contains("\"cardDetails\""));
        assertEquals(model.getAddressBook(), new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow());
        assertEquals(new ServiceNote("Follow up"), new JsonAddressBookStorage(dataPath).readAddressBook()
                .orElseThrow().getPersonList().getFirst().getServiceNote());

        logic.execute("edit 1 p/91234567");
        assertNull(new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow()
                .getPersonList().getFirst().getCardDetails());
    }

    @Test
    public void execute_missingClientOrCard_keepsModelAndSavedFile() throws Exception {
        logic.execute(ADD_CLIENT);
        String originalJson = Files.readString(dataPath);
        ReadOnlyAddressBook originalModel = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, "No person found with client reference: C9999", () ->
                logic.execute("removecard r/C9999"));
        assertEquals(originalModel, model.getAddressBook());
        assertEquals(originalJson, Files.readString(dataPath));

        logic.execute("removecard r/C0001");
        String savedJson = Files.readString(dataPath);
        ReadOnlyAddressBook before = new AddressBook(model.getAddressBook());

        assertThrows(CommandException.class, "Client C0001 is not associated with a card.", () ->
                logic.execute("removecard r/C0001"));
        assertEquals(before, model.getAddressBook());
        assertEquals(savedJson, Files.readString(dataPath));
    }

    @Test
    public void execute_matchingCardNumberOnAnotherClient_removesOnlyTargetCard() throws Exception {
        logic.execute(ADD_CLIENT);
        logic.execute(ADD_CLIENT.replace("C0001", "C0002"));

        logic.execute("removecard r/C0001");

        List<Person> clients = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow().getPersonList();
        assertNull(clients.get(0).getCardDetails());
        assertNotNull(clients.get(1).getCardDetails());
    }

    @Test
    public void execute_shortCardNumber_reportsWholeNumber() throws Exception {
        logic.execute(ADD_CLIENT.replace("4111111111111111", "123"));

        assertEquals("Removed card ending in 123 from client C0001",
                logic.execute("removecard r/C0001").getFeedbackToUser());
    }

    @Test
    public void execute_saveFailure_keepsModelFilteredListAndFile() throws Exception {
        logic.execute(ADD_CLIENT);
        logic.execute("find n/Alex");
        String savedJson = Files.readString(dataPath);
        ReadOnlyAddressBook before = new AddressBook(model.getAddressBook());
        List<Person> filteredBefore = List.copyOf(model.getFilteredPersonList());
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("write failed");
            }
        };
        logic = new LogicManager(model, storage(failingStorage));

        assertThrows(CommandException.class, LogicManager.SAVE_FAILURE_MESSAGE, () ->
                logic.execute("removecard r/C0001"));
        assertEquals(before, model.getAddressBook());
        assertEquals(filteredBefore, model.getFilteredPersonList());
        assertEquals(savedJson, Files.readString(dataPath));
    }

    @Test
    public void execute_protectedMode_rejectsRemovalWithoutWriting() throws Exception {
        logic.execute(ADD_CLIENT);
        String savedJson = Files.readString(dataPath);
        ReadOnlyAddressBook before = new AddressBook(model.getAddressBook());
        logic = new LogicManager(model, storage(new JsonAddressBookStorage(dataPath)),
                LogicManager.LOAD_FAILURE_MESSAGE, true);

        assertThrows(CommandException.class, LogicManager.LOAD_FAILURE_MESSAGE, () ->
                logic.execute("removecard r/C0001"));
        assertEquals(before, model.getAddressBook());
        assertEquals(savedJson, Files.readString(dataPath));
        assertNotNull(model.getAddressBook().getPersonList().getFirst().getCardDetails());
    }

    private StorageManager storage(JsonAddressBookStorage addressBookStorage) {
        return new StorageManager(addressBookStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
    }
}
