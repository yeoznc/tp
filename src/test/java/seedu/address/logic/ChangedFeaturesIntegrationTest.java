package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.SortOrder;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientClash;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ServiceNote;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises command routing, staged model changes, display state, and persistence together. */
public class ChangedFeaturesIntegrationTest {
    private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant MODIFIED = Instant.parse("2026-02-01T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(MODIFIED, ZoneOffset.UTC);

    @TempDir
    public Path temporaryFolder;

    private Path dataPath;
    private JsonAddressBookStorage jsonStorage;
    private RecordingStorage storage;
    private ModelManager model;
    private LogicManager logic;
    private Person alex;
    private Person mary;
    private Person minimal;

    @BeforeEach
    public void setUp() throws Exception {
        alex = new PersonBuilder().withClientReference("C1").withName("Alex Yeoh").withPhone("123")
                .withEmail("alex@example.com").withCardDetails("0012  3456", "001", "01/00", "Visa")
                .withTags("friend").build().withTimestamps(CREATED, CREATED);
        mary = new PersonBuilder().withClientReference("C2").withName("Mary Yu").withPhone("456")
                .withEmail("mary@example.com").withCardDetails("0099", "002", "12/99", "Mastercard")
                .build().withTimestamps(CREATED, CREATED);
        minimal = new Person(new ClientReference("C3"), new Name("Alex Missing"),
                null, null, null, null, null, null, Set.of()).withTimestamps(CREATED, CREATED);
        AddressBook book = new AddressBook();
        book.setPersons(List.of(mary, alex, minimal));
        dataPath = temporaryFolder.resolve("clients.json");
        jsonStorage = new JsonAddressBookStorage(dataPath);
        jsonStorage.saveAddressBook(book);
        storage = new RecordingStorage(jsonStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        model = new ModelManager(book, new UserPrefs(), CLOCK);
        logic = new LogicManager(model, storage, "", false, CLOCK);
    }

    @Test
    public void execute_addClash_warnsThenConfirmedRetrySavesOnce() throws Exception {
        byte[] original = Files.readAllBytes(dataPath);
        String command = "add r/C4 n/Other Client p/123";
        CommandResult warning = logic.execute(command);
        assertEquals(command, warning.getCommandToConfirm());
        assertEquals(List.of(new ClientClash(ClientClash.Field.PHONE, "123", alex)), warning.getClientClashes());
        assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList());
        assertEquals(0, storage.snapshots.size());
        assertArrayEquals(original, Files.readAllBytes(dataPath));

        CommandResult result = logic.execute(warning.getCommandToConfirm() + " --confirm-clash");
        Person added = new Person(new ClientReference("C4"), new Name("Other Client"), new Phone("123"),
                null, null, null, null, null, Set.of()).withTimestamps(MODIFIED, MODIFIED);
        assertFalse(result.requiresClashConfirmation());
        assertEquals(List.of(mary, alex, minimal, added), model.getAddressBook().getPersonList());
        assertEquals(CommandResult.SelectionRequest.SELECT_PERSON, result.getSelectionRequest());
        assertTrue(result.getPersonToSelect().isSamePerson(added));
        assertSavedOnce();
    }

    @Test
    public void execute_editClash_preservesFilterUntilConfirmed() throws Exception {
        logic.execute("find n/Mary");
        byte[] original = Files.readAllBytes(dataPath);
        CommandResult warning = logic.execute("edit 1 p/123");
        assertEquals("edit 1 p/123", warning.getCommandToConfirm());
        assertEquals(List.of(new ClientClash(ClientClash.Field.PHONE, "123", alex)), warning.getClientClashes());
        assertEquals(List.of(mary), logic.getFilteredPersonList());
        assertEquals(0, storage.snapshots.size());
        assertArrayEquals(original, Files.readAllBytes(dataPath));

        logic.execute(warning.getCommandToConfirm() + " --confirm-clash");
        Person edited = new PersonBuilder(mary).withPhone("123").build().withTimestamps(CREATED, MODIFIED);
        assertEquals(List.of(edited, alex, minimal), logic.getFilteredPersonList());
        assertSavedOnce();
    }

    @Test
    public void execute_confirmedDuplicateReference_stillRejectedAcrossLayers() throws Exception {
        byte[] original = Files.readAllBytes(dataPath);
        CommandException failure = assertThrows(CommandException.class, () ->
                logic.execute("add r/c1 n/Other Client --confirm-clash"));
        assertEquals(String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, new ClientReference("C1")),
                failure.getMessage());
        assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList());
        assertEquals(0, storage.snapshots.size());
        assertArrayEquals(original, Files.readAllBytes(dataPath));
    }

    @Test
    public void execute_findCombinedFilters_replacesFiltersWithoutSaving() throws Exception {
        byte[] original = Files.readAllBytes(dataPath);
        logic.execute("sort desc");
        assertFind("find n/aLeX p/123 cn/0012  3456", List.of(alex));
        assertFind("find n/Alex p/456 cn/0012  3456", List.of());
        assertFind("find n/Alex p/123 cn/3456", List.of());
        assertFind("find n/Alex", List.of(alex, minimal));
        assertFind("find p/123 456", List.of(mary, alex));
        assertFind("find cn/0099", List.of(mary));
        assertEquals(SortOrder.DESC, model.getSortOrder());
        assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList());
        assertEquals(0, storage.snapshots.size());
        assertArrayEquals(original, Files.readAllBytes(dataPath));
    }

    @Test
    public void execute_sortedFilteredIndexEditAndDelete_targetDisplayedClients() throws Exception {
        logic.execute("sort desc");
        logic.execute("find n/Alex");
        assertEquals(List.of(alex, minimal), logic.getFilteredPersonList());
        CommandResult edit = logic.execute("edit 2 n/Zara Missing");
        Person changed = new PersonBuilder(minimal).withName("Zara Missing").build()
                .withTimestamps(CREATED, MODIFIED);
        assertEquals(List.of(changed, mary, alex), logic.getFilteredPersonList());
        assertTrue(edit.getPersonToSelect().isSamePerson(changed));
        logic.execute("sort asc");
        assertEquals(List.of(alex, mary, changed), logic.getFilteredPersonList());
        logic.execute("delete 2");
        assertEquals(List.of(alex, changed), logic.getFilteredPersonList());
        assertEquals(List.of(alex, changed), model.getAddressBook().getPersonList());
        assertEquals(2, storage.snapshots.size());
        assertReloadMatchesModel();
    }

    @Test
    public void execute_noteAndRemoveCardWhileFilteredAndSorted_preservesOtherFields() throws Exception {
        logic.execute("sort desc");
        logic.execute("find n/Mary");
        CommandResult note = logic.execute("note r/c1 s/Call tomorrow");
        Person noted = alex.withServiceNote(new ServiceNote("Call tomorrow")).withTimestamps(CREATED, MODIFIED);
        assertEquals(new ClientReference("C1"), note.getSelectedClientReference().orElseThrow());
        assertEquals(List.of(mary, noted, minimal), logic.getFilteredPersonList());
        assertEquals(List.of(mary, noted, minimal), jsonStorage.readAddressBook().orElseThrow().getPersonList());
        logic.execute("find cn/0012  3456");
        logic.execute("removecard r/c1");
        Person removed = noted.withoutCardDetails();
        assertTrue(logic.getFilteredPersonList().isEmpty());
        assertEquals(List.of(mary, removed, minimal), model.getAddressBook().getPersonList());
        assertFind("find cn/0012  3456", List.of());
        assertFind("find n/Alex p/123", List.of(removed));
        assertEquals(SortOrder.DESC, model.getSortOrder());
        assertEquals(2, storage.snapshots.size());
        assertReloadMatchesModel();
    }

    @Test
    public void execute_noteLengthBoundaryAndClear_persistWithoutInventingOptionalDetails() throws Exception {
        logic.execute("note r/C3 s/" + "A".repeat(200));
        Person noted = minimal.withServiceNote(new ServiceNote("A".repeat(200)))
                .withTimestamps(CREATED, MODIFIED);
        assertEquals(noted, jsonStorage.readAddressBook().orElseThrow().getPersonList().getLast());
        byte[] saved = Files.readAllBytes(dataPath);
        ParseException failure = assertThrows(ParseException.class, () ->
                logic.execute("note r/C3 s/" + "A".repeat(201)));
        assertEquals(ServiceNote.MESSAGE_CONSTRAINTS, failure.getMessage());
        assertEquals(1, storage.snapshots.size());
        assertArrayEquals(saved, Files.readAllBytes(dataPath));
        logic.execute("note r/C3 s/");
        assertEquals(minimal.withTimestamps(CREATED, MODIFIED),
                jsonStorage.readAddressBook().orElseThrow().getPersonList().getLast());
        assertEquals(2, storage.snapshots.size());
    }

    @Test
    public void execute_escapedNote_preservesDecodedTextAcrossJsonBoundary() throws Exception {
        logic.execute("note r/C3 s/Call \\n/next \\\\ desk \"quoted\"");
        ServiceNote expected = new ServiceNote("Call n/next \\ desk \"quoted\"");
        assertEquals(expected, model.findPersonByReference(new ClientReference("C3")).orElseThrow()
                .getServiceNote().orElseThrow());
        assertEquals(expected, jsonStorage.readAddressBook().orElseThrow().getPersonList().getLast()
                .getServiceNote().orElseThrow());
        assertSavedOnce();
    }

    @Test
    public void execute_multipleClashingFields_reportsEveryAffectedClientWithoutSaving() throws Exception {
        byte[] original = Files.readAllBytes(dataPath);
        CommandResult warning = logic.execute("add r/C4 n/Clashed Client p/123 e/mary@example.com"
                + " cn/0012  3456 cvv/123 exp/12/28 provider/Visa");
        assertEquals(List.of(new ClientClash(ClientClash.Field.EMAIL, "mary@example.com", mary),
                new ClientClash(ClientClash.Field.PHONE, "123", alex),
                new ClientClash(ClientClash.Field.CARD_NUMBER, "0012  3456", alex)), warning.getClientClashes());
        assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList());
        assertEquals(0, storage.snapshots.size());
        assertArrayEquals(original, Files.readAllBytes(dataPath));
    }

    @Test
    public void execute_referenceEdit_normalizesIdentityAndPersistsEqualNameClients() throws Exception {
        logic.execute("find n/Alex Missing");
        // Both keywords are alternatives; select the minimal profile by the displayed index.
        CommandResult result = logic.execute("edit 2 r/c4 n/Alex Yeoh");
        Person edited = new PersonBuilder(minimal).withClientReference("C4").withName("Alex Yeoh").build()
                .withTimestamps(CREATED, MODIFIED);
        assertFalse(minimal.isSamePerson(edited));
        assertNotEquals(minimal, edited);
        assertTrue(model.findPersonByReference(new ClientReference("C3")).isEmpty());
        assertEquals(edited, model.findPersonByReference(new ClientReference("c4")).orElseThrow());
        assertEquals(List.of(mary, alex, edited), model.getAddressBook().getPersonList());
        assertEquals(CommandResult.SelectionRequest.SELECT_PERSON, result.getSelectionRequest());
        assertTrue(edited.isSamePerson(result.getPersonToSelect()));
        assertSavedOnce();
        assertEquals(model.getAddressBook().hashCode(),
                jsonStorage.readAddressBook().orElseThrow().hashCode());
    }

    @Test
    public void execute_referenceEditToHiddenDuplicate_failsEvenWhenConfirmed() throws Exception {
        logic.execute("sort desc");
        logic.execute("find n/Mary");
        byte[] original = Files.readAllBytes(dataPath);
        CommandException failure = assertThrows(CommandException.class, () ->
                logic.execute("edit 1 r/c1 --confirm-clash"));
        assertEquals(EditCommand.MESSAGE_DUPLICATE_PERSON, failure.getMessage());
        assertEquals(List.of(mary), logic.getFilteredPersonList());
        assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList());
        assertEquals(SortOrder.DESC, model.getSortOrder());
        assertEquals(0, storage.snapshots.size());
        assertArrayEquals(original, Files.readAllBytes(dataPath));
    }

    @Test
    public void execute_malformedChangedCommands_doNotReachStorageOrChangeDisplay() throws Exception {
        logic.execute("sort desc");
        logic.execute("find n/Alex");
        byte[] original = Files.readAllBytes(dataPath);
        for (String command : List.of("add r/C4 n/Client p/12", "edit 1 p/12", "find",
                "find n/Alex n/Mary", "sort sideways", "note r/C1", "removecard r/C1 r/C2")) {
            ParseException failure = assertThrows(ParseException.class, () -> logic.execute(command), command);
            assertFalse(failure.getMessage().isBlank(), command);
            assertEquals(List.of(alex, minimal), logic.getFilteredPersonList(), command);
            assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList(), command);
            assertEquals(SortOrder.DESC, model.getSortOrder(), command);
            assertEquals(0, storage.snapshots.size(), command);
            assertArrayEquals(original, Files.readAllBytes(dataPath), command);
        }
    }

    @Test
    public void execute_missingReferencesAndCard_doNotSave() throws Exception {
        byte[] original = Files.readAllBytes(dataPath);
        for (String command : List.of("note r/C9 s/Hello", "removecard r/C9", "removecard r/C3", "delete 4")) {
            assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList(), command);
            assertEquals(0, storage.snapshots.size(), command);
            assertArrayEquals(original, Files.readAllBytes(dataPath), command);
        }
    }

    @Test
    public void execute_failedSaveForEachMutation_preservesAllLiveAndSavedState() throws Exception {
        for (String command : List.of("add r/C4 n/New Client", "edit 1 n/Zara Yu",
                "note r/C1 s/Hello", "removecard r/C1", "delete 1", "clear",
                "add r/C4 n/New Client p/123 --confirm-clash", "edit 1 p/123 --confirm-clash")) {
            setUp();
            logic.execute("sort desc");
            logic.execute("find n/Mary");
            byte[] original = Files.readAllBytes(dataPath);
            IOException ioFailure = new IOException("synthetic write failure");
            storage.failure = ioFailure;
            CommandException failure = assertThrows(CommandException.class, () -> logic.execute(command), command);
            assertEquals(LogicManager.SAVE_FAILURE_MESSAGE, failure.getMessage(), command);
            assertSame(ioFailure, failure.getCause().getCause(), command);
            assertEquals(List.of(mary, alex, minimal), model.getAddressBook().getPersonList(), command);
            assertEquals(List.of(mary), logic.getFilteredPersonList(), command);
            assertEquals(SortOrder.DESC, model.getSortOrder(), command);
            assertEquals(1, storage.snapshots.size(), command);
            assertNotEquals(model.getAddressBook(), storage.snapshots.getFirst(), command);
            assertArrayEquals(original, Files.readAllBytes(dataPath), command);
        }
    }

    private void assertFind(String command, List<Person> expected) throws Exception {
        CommandResult result = logic.execute(command);
        assertEquals(String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, expected.size()),
                result.getFeedbackToUser(), command);
        assertEquals(expected, logic.getFilteredPersonList(), command);
    }

    private void assertSavedOnce() throws Exception {
        assertEquals(1, storage.snapshots.size());
        assertEquals(model.getAddressBook(), storage.snapshots.getFirst());
        assertReloadMatchesModel();
    }

    private void assertReloadMatchesModel() throws Exception {
        ReadOnlyAddressBook reloaded = jsonStorage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), reloaded);
        assertEquals(model.getAddressBook().hashCode(), reloaded.hashCode());
    }

    /** Captures snapshots at the real storage boundary, optionally failing before any disk write. */
    private static class RecordingStorage extends StorageManager {
        private final List<AddressBook> snapshots = new ArrayList<>();
        private IOException failure;

        RecordingStorage(JsonAddressBookStorage addressBookStorage, JsonUserPrefsStorage userPrefsStorage) {
            super(addressBookStorage, userPrefsStorage);
        }

        @Override
        public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
            snapshots.add(new AddressBook(addressBook));
            if (failure != null) {
                throw failure;
            }
            super.saveAddressBook(addressBook);
        }
    }
}
