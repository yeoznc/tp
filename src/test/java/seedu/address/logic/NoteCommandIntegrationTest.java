package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ServiceNote;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class NoteCommandIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private Path dataPath;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addPerson(new PersonBuilder().withClientReference("C0001").withName("Alex Yeoh").build());
        dataPath = temporaryFolder.resolve("addressBook.json");
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
    }

    @Test
    public void execute_setReplaceEditAndClear_persistsEachState() throws Exception {
        assertEquals("Added service note for C0001 Alex Yeoh.",
                logic.execute("note r/c0001 s/Prefers mornings").getFeedbackToUser());
        assertEquals("Prefers mornings", loadedNote().value);

        assertEquals("Updated service note for C0001 Alex Yeoh.",
                logic.execute("note s/Prefers afternoons r/C0001").getFeedbackToUser());
        assertEquals("Prefers afternoons", loadedNote().value);
        assertEquals("Updated service note for C0001 Alex Yeoh.",
                logic.execute("note r/C0001 s/Prefers afternoons").getFeedbackToUser());
        logic.execute("edit 1 p/91234567");
        assertEquals("Prefers afternoons", loadedNote().value);

        assertEquals("Cleared service note for C0001 Alex Yeoh.",
                logic.execute("note r/C0001 s/").getFeedbackToUser());
        assertNull(loadedNote());
        assertThrows(CommandException.class, "Client C0001 has no service note to clear.", () ->
                logic.execute("note r/C0001 s/"));
    }

    @Test
    public void execute_noteAndEdit_preserveNoteAndCreationTimeAcrossReloads() throws Exception {
        Instant createdAt = model.getAddressBook().getPersonList().getFirst().getCreatedAt();
        Instant noteAddedAt = createdAt.plusSeconds(60);
        Instant editedAt = createdAt.plusSeconds(120);
        Instant noteUpdatedAt = createdAt.plusSeconds(180);
        StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));

        logic = new LogicManager(model, storage, "", false, Clock.fixed(noteAddedAt, ZoneOffset.UTC));
        logic.execute("note r/C0001 s/Prefers mornings");
        var savedAfterNote = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow()
                .getPersonList().getFirst();
        assertEquals("Prefers mornings", savedAfterNote.getServiceNote().value);
        assertEquals(createdAt, savedAfterNote.getCreatedAt());
        assertEquals(noteAddedAt, savedAfterNote.getLastModifiedAt());

        logic = new LogicManager(model, storage, "", false, Clock.fixed(editedAt, ZoneOffset.UTC));
        logic.execute("edit 1 p/91234567");
        var savedAfterEdit = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow()
                .getPersonList().getFirst();
        assertEquals("Prefers mornings", savedAfterEdit.getServiceNote().value);
        assertEquals(createdAt, savedAfterEdit.getCreatedAt());
        assertEquals(editedAt, savedAfterEdit.getLastModifiedAt());

        logic = new LogicManager(model, storage, "", false, Clock.fixed(noteUpdatedAt, ZoneOffset.UTC));
        logic.execute("note r/C0001 s/Prefers afternoons");
        var savedAfterUpdate = new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow()
                .getPersonList().getFirst();
        assertEquals("Prefers afternoons", savedAfterUpdate.getServiceNote().value);
        assertEquals(createdAt, savedAfterUpdate.getCreatedAt());
        assertEquals(noteUpdatedAt, savedAfterUpdate.getLastModifiedAt());
    }

    @Test
    public void execute_hiddenClient_preservesFilterAndRequestsSelection() throws Exception {
        logic.execute("find n/Nobody");
        var result = logic.execute("note r/C0001 s/Hello");
        assertEquals("C0001", result.getSelectedClientReference().orElseThrow().value);
        assertEquals(0, logic.getFilteredPersonList().size());
    }

    @Test
    public void execute_missingClientAndInvalidNote_leaveSavedStateUnchanged() throws Exception {
        logic.execute("note r/C0001 s/Original");
        String originalJson = Files.readString(dataPath);

        assertThrows(CommandException.class, "No person found with client reference: C9999", () ->
                logic.execute("note r/C9999 s/Other"));
        assertThrows(ParseException.class,
                ServiceNote.MESSAGE_CONSTRAINTS, () -> logic.execute("note r/C0001 s/" + "a".repeat(201)));
        assertEquals("Original", loadedNote().value);
        assertEquals(originalJson, Files.readString(dataPath));
    }

    @Test
    public void execute_failedSave_keepsPreviousNoteAndSavedFile() throws Exception {
        logic.execute("note r/C0001 s/Original");
        String originalJson = Files.readString(dataPath);
        JsonAddressBookStorage failingStorage = new JsonAddressBookStorage(dataPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw new IOException("write failed");
            }
        };
        logic = new LogicManager(model, new StorageManager(failingStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));

        assertThrows(CommandException.class, LogicManager.SAVE_FAILURE_MESSAGE, () ->
                logic.execute("note r/C0001 s/Replacement"));
        assertEquals("Original", model.getFilteredPersonList().getFirst().getServiceNote().value);
        assertEquals(originalJson, Files.readString(dataPath));
    }

    @Test
    public void readAddressBook_missingNoteLoads_nonTextAndInvalidNotesReject() throws Exception {
        new JsonAddressBookStorage(dataPath).saveAddressBook(model.getAddressBook());
        assertNull(loadedNote());
        JsonNode saved = JsonUtil.fromJsonString(Files.readString(dataPath), JsonNode.class);
        ObjectNode client = (ObjectNode) saved.path("persons").get(0);

        client.put("serviceNote", 42);
        assertInvalidSavedNote(saved);
        client.putNull("serviceNote");
        assertInvalidSavedNote(saved);
        client.put("serviceNote", "a".repeat(201));
        assertInvalidSavedNote(saved);
        client.put("serviceNote", "first\nsecond");
        assertInvalidSavedNote(saved);
    }

    @Test
    public void execute_protectedMode_rejectsNote() {
        logic = new LogicManager(model, new StorageManager(new JsonAddressBookStorage(dataPath),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))), "", true);
        assertThrows(CommandException.class, LogicManager.NOTE_LOAD_FAILURE_MESSAGE, () ->
                logic.execute("note r/C0001 s/Hello"));
    }

    private ServiceNote loadedNote() throws DataLoadingException {
        return new JsonAddressBookStorage(dataPath).readAddressBook().orElseThrow()
                .getPersonList().getFirst().getServiceNote();
    }

    private void assertInvalidSavedNote(JsonNode saved) throws Exception {
        Files.writeString(dataPath, JsonUtil.toJsonString(saved));
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(dataPath).readAddressBook());
        assertTrue(model.getFilteredPersonList().getFirst().getServiceNote() == null);
    }
}
