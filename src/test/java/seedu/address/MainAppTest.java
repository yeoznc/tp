package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class MainAppTest {
    @TempDir
    public Path testFolder;

    @Test
    public void load_missingFile_showsSamplesWithoutCreatingFile() {
        Path dataPath = testFolder.resolve("missing.json");
        MainApp.InitialData initialData = load(dataPath);
        assertEquals("No saved client data found. Showing sample clients.", initialData.message());
        assertEquals(SampleDataUtil.getSampleAddressBook(), initialData.model().getAddressBook());
        assertFalse(initialData.protectedMode());
        assertFalse(Files.exists(dataPath));
    }

    @Test
    public void load_missingFile_firstChangeSavesSamplesAndNewClient() throws Exception {
        Path dataPath = testFolder.resolve("missing.json");
        MainApp.InitialData initialData = load(dataPath);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        LogicManager logic = new LogicManager(initialData.model(), storage);

        logic.execute("add r/C0001 n/New Client p/87438807 e/new@example.com a/10 Main Street"
                + " cn/4111111111111111 cvv/123 exp/12/28 provider/Visa cs/700 d/1250.50");

        assertTrue(Files.exists(dataPath));
        assertEquals(SampleDataUtil.getSamplePersons().length + 1,
                new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow().getPersonList().size());
    }

    @Test
    public void load_emptyFile_startsEmpty() throws Exception {
        Path dataPath = testFolder.resolve("empty.json");
        new JsonAddressBookStorage(dataPath).saveAddressBook(new AddressBook());
        MainApp.InitialData initialData = load(dataPath);
        assertEquals("No saved client data found.", initialData.message());
        assertTrue(initialData.model().getAddressBook().getPersonList().isEmpty());
        assertFalse(initialData.protectedMode());
    }

    @Test
    public void load_savedClients_showsCountAndData() throws Exception {
        Path dataPath = testFolder.resolve("saved.json");
        AddressBook saved = new AddressBook();
        Person first = new PersonBuilder().withClientReference("C0001").build();
        Person second = new PersonBuilder().withClientReference("C0002").build();
        saved.addPerson(first);
        saved.addPerson(second);
        new JsonAddressBookStorage(dataPath).saveAddressBook(saved);

        MainApp.InitialData initialData = load(dataPath);
        assertEquals("Loaded 2 client(s).", initialData.message());
        assertEquals(saved, initialData.model().getAddressBook());
        assertFalse(initialData.protectedMode());
    }

    @Test
    public void load_invalidFile_startsProtectedWithoutChangingFile() throws Exception {
        Path dataPath = testFolder.resolve("invalid.json");
        Files.writeString(dataPath, "invalid json");
        MainApp.InitialData initialData = load(dataPath);
        assertEquals(LogicManager.LOAD_FAILURE_MESSAGE, initialData.message());
        assertTrue(initialData.model().getAddressBook().getPersonList().isEmpty());
        assertTrue(initialData.protectedMode());
        assertEquals("invalid json", Files.readString(dataPath));
    }

    @Test
    public void load_invalidTimestamp_startsProtectedWithoutChangingFile() throws Exception {
        Path dataPath = testFolder.resolve("badTimestamp.json");
        AddressBook saved = new AddressBook();
        saved.addPerson(new PersonBuilder().withClientReference("C0001").build()
                .withTimestamps(Instant.parse("2026-03-15T01:42:00Z"),
                        Instant.parse("2026-03-15T01:42:00Z")));
        new JsonAddressBookStorage(dataPath).saveAddressBook(saved);
        String corrupted = Files.readString(dataPath).replace("2026-03-15T01:42:00Z", "bad date");
        Files.writeString(dataPath, corrupted);

        MainApp.InitialData initialData = load(dataPath);

        assertEquals(LogicManager.LOAD_FAILURE_MESSAGE, initialData.message());
        assertTrue(initialData.protectedMode());
        assertTrue(initialData.model().getAddressBook().getPersonList().isEmpty());
        assertEquals(corrupted, Files.readString(dataPath));
    }

    @Test
    public void load_nullPerson_startsProtectedWithoutChangingFile() throws Exception {
        Path dataPath = testFolder.resolve("nullPerson.json");
        String malformed = "{\"persons\":[null]}";
        Files.writeString(dataPath, malformed);

        MainApp.InitialData initialData = load(dataPath);

        assertEquals(LogicManager.LOAD_FAILURE_MESSAGE, initialData.message());
        assertTrue(initialData.protectedMode());
        assertEquals(malformed, Files.readString(dataPath));
    }

    @Test
    public void load_unreadablePath_startsProtectedWithoutChangingPath() throws Exception {
        Path dataPath = testFolder.resolve("directory.json");
        Files.createDirectory(dataPath);
        MainApp.InitialData initialData = load(dataPath);
        assertEquals(LogicManager.LOAD_FAILURE_MESSAGE, initialData.message());
        assertTrue(initialData.protectedMode());
        assertTrue(Files.isDirectory(dataPath));
    }

    private MainApp.InitialData load(Path dataPath) {
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));
        return new MainApp().initModelManager(storage, new UserPrefs());
    }
}
