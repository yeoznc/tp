package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.RemoveCardCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.CliSyntax;
import seedu.address.logic.parser.RemoveCardCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.SortOrder;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ServiceNote;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Baseline success and rejection cases for every command, executed through the complete logic chain. */
public class CommandHybridIntegrationTest {
    private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant MODIFIED = Instant.parse("2026-02-01T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(MODIFIED, ZoneOffset.UTC);

    @TempDir
    public Path temporaryFolder;

    private Person alex;
    private Person mary;
    private Person minimal;
    private List<Person> initialPersons;
    private byte[] initialJson;
    private Path dataPath;
    private JsonAddressBookStorage jsonStorage;
    private RecordingStorage storage;
    private ModelManager model;
    private LogicManager logic;

    @BeforeEach
    public void setUp() throws Exception {
        alex = new PersonBuilder().withClientReference("C1").withName("Alex Yeoh").withPhone("123")
                .withEmail("alex@example.com").withCardDetails("0012  3456", "001", "01/00", "Visa")
                .build().withTimestamps(CREATED, CREATED);
        mary = new PersonBuilder().withClientReference("C2").withName("Mary Yu").withPhone("456")
                .withEmail("mary@example.com").withCardDetails("0099", "002", "12/99", "Mastercard")
                .build().withTimestamps(CREATED, CREATED);
        minimal = new Person(new ClientReference("C3"), new Name("Alex Missing"),
                null, null, null, null, null, null, Set.of()).withTimestamps(CREATED, CREATED);
        initialPersons = List.of(minimal, mary, alex);
        AddressBook book = new AddressBook();
        book.setPersons(initialPersons);
        dataPath = temporaryFolder.resolve("clients.json");
        jsonStorage = new JsonAddressBookStorage(dataPath);
        jsonStorage.saveAddressBook(book);
        initialJson = Files.readAllBytes(dataPath);
        storage = new RecordingStorage(jsonStorage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
        model = new ModelManager(book, new UserPrefs(), CLOCK);
        logic = new LogicManager(model, storage, "", false, CLOCK);
        logic.execute("sort desc");
    }

    @Test
    public void executeAdd_validClient_addsAndSavesClient() throws Exception {
        CommandResult result = logic.execute("add r/c4 n/Aaron");
        Person added = new Person(new ClientReference("C4"), new Name("Aaron"),
                null, null, null, null, null, null, Set.of()).withTimestamps(MODIFIED, MODIFIED);
        assertNormalResult(result, String.format(AddCommand.MESSAGE_SUCCESS, "C4", "Aaron"),
                CommandResult.SelectionRequest.SELECT_PERSON);
        assertTrue(added.isSamePerson(result.getPersonToSelect()));
        assertMutation(List.of(minimal, mary, alex, added), List.of(mary, alex, minimal, added));
    }

    @Test
    public void executeAdd_invalidPhone_rejectsWithoutChanges() throws Exception {
        assertParseFailure("add r/C4 n/Aaron p/12", Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void executeAdd_duplicateReference_rejectsWithoutChanges() throws Exception {
        assertExecutionFailure("add r/c1 n/Aaron",
                String.format(AddCommand.MESSAGE_DUPLICATE_PERSON, "C1"));
    }

    @Test
    public void executeEdit_validPhone_updatesAndSavesDisplayedClient() throws Exception {
        CommandResult result = logic.execute("edit 1 p/789");
        Person edited = new PersonBuilder(mary).withPhone("789").build().withTimestamps(CREATED, MODIFIED);
        assertNormalResult(result, String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(edited)),
                CommandResult.SelectionRequest.SELECT_PERSON);
        assertTrue(edited.isSamePerson(result.getPersonToSelect()));
        assertMutation(List.of(minimal, edited, alex), List.of(edited, alex, minimal));
    }

    @Test
    public void executeEdit_invalidPhone_rejectsWithoutChanges() throws Exception {
        assertParseFailure("edit 1 p/12", Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void executeEdit_hiddenDuplicateReference_rejectsWithoutChanges() throws Exception {
        assertExecutionFailure("edit 1 r/c1", EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void executeDelete_validIndex_deletesAndSavesDisplayedClient() throws Exception {
        CommandResult result = logic.execute("delete 1");
        assertNormalResult(result, String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(mary)),
                CommandResult.SelectionRequest.PRESERVE_OR_FIRST);
        assertMutation(List.of(minimal, alex), List.of(alex, minimal));
    }

    @Test
    public void executeDelete_invalidIndexSyntax_rejectsWithoutChanges() throws Exception {
        assertParseFailure("delete 0", invalidFormat(DeleteCommand.MESSAGE_USAGE));
    }

    @Test
    public void executeDelete_indexBeyondDisplayedList_rejectsWithoutChanges() throws Exception {
        assertExecutionFailure("delete 2", Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void executeFind_combinedFilters_displaysMatchesWithoutSaving() throws Exception {
        CommandResult result = logic.execute("find n/aLeX p/123 cn/0012  3456");
        assertNormalResult(result, String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, 1),
                CommandResult.SelectionRequest.PRESERVE_OR_FIRST);
        assertDisplayOnly(List.of(alex), SortOrder.DESC);
    }

    @Test
    public void executeFind_missingCriteria_rejectsWithoutChanges() throws Exception {
        assertParseFailure("find", invalidFormat(FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void executeFind_repeatedPrefix_rejectsWithoutChanges() throws Exception {
        assertParseFailure("find n/Alex n/Mary",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_NAME));
    }

    @Test
    public void executeList_filteredClients_restoresAllClientsWithoutSaving() throws Exception {
        logic.execute("find n/Mary");
        CommandResult result = logic.execute("list");
        assertNormalResult(result, ListCommand.MESSAGE_SUCCESS, CommandResult.SelectionRequest.PRESERVE_OR_FIRST);
        assertDisplayOnly(List.of(mary, alex, minimal), SortOrder.DESC);
    }

    @Test
    public void executeList_unexpectedArgument_rejectsWithoutChanges() throws Exception {
        assertParseFailure("list extra", invalidFormat(ListCommand.COMMAND_WORD));
    }

    @Test
    public void executeSort_ascendingOrder_changesDisplayWithoutSaving() throws Exception {
        CommandResult result = logic.execute("sort asc");
        assertNormalResult(result, String.format(SortCommand.MESSAGE_ASC, 3),
                CommandResult.SelectionRequest.PRESERVE_OR_FIRST);
        assertDisplayOnly(List.of(minimal, alex, mary), SortOrder.ASC);
    }

    @Test
    public void executeSort_unsupportedOrder_rejectsWithoutChanges() throws Exception {
        assertParseFailure("sort sideways", String.format(SortCommand.MESSAGE_INVALID_ORDER, "sideways"));
    }

    @Test
    public void executeSort_extraArgument_rejectsWithoutChanges() throws Exception {
        assertParseFailure("sort asc extra", SortCommand.MESSAGE_INVALID_COMMAND);
    }

    @Test
    public void executeNote_validNote_updatesAndSavesTargetClient() throws Exception {
        logic.execute("find n/Mary");
        CommandResult result = logic.execute("note r/c1 s/Call tomorrow");
        Person noted = alex.withServiceNote(new ServiceNote("Call tomorrow")).withTimestamps(CREATED, MODIFIED);
        assertNormalResult(result, "Added service note for C1 Alex Yeoh.",
                CommandResult.SelectionRequest.SELECT_REFERENCE);
        assertEquals(new ClientReference("C1"), result.getSelectedClientReference().orElseThrow());
        assertMutation(List.of(minimal, mary, noted), List.of(mary, noted, minimal));
    }

    @Test
    public void executeNote_missingNotePrefix_rejectsWithoutChanges() throws Exception {
        assertParseFailure("note r/C1", NoteCommand.MESSAGE_MISSING_NOTE);
    }

    @Test
    public void executeNote_unknownReference_rejectsWithoutChanges() throws Exception {
        assertExecutionFailure("note r/C9 s/Hello", "No client found with reference C9.");
    }

    @Test
    public void executeRemoveCard_existingCard_removesAndSavesOnlyTargetCard() throws Exception {
        CommandResult result = logic.execute("removecard r/c1");
        Person removed = alex.withoutCardDetails().withTimestamps(CREATED, MODIFIED);
        assertNormalResult(result, String.format(RemoveCardCommand.MESSAGE_SUCCESS, "3456", "C1"),
                CommandResult.SelectionRequest.PRESERVE_OR_FIRST);
        assertMutation(List.of(minimal, mary, removed), List.of(mary, removed, minimal));
    }

    @Test
    public void executeRemoveCard_repeatedReference_rejectsWithoutChanges() throws Exception {
        assertParseFailure("removecard r/C1 r/C2", RemoveCardCommandParser.MESSAGE_REPEATED_PARAMETER);
    }

    @Test
    public void executeRemoveCard_missingCard_rejectsWithoutChanges() throws Exception {
        assertExecutionFailure("removecard r/C3", String.format(RemoveCardCommand.MESSAGE_CARD_NOT_FOUND, "C3"));
    }

    @Test
    public void executeClear_existingClients_clearsAndSavesEmptyBook() throws Exception {
        CommandResult result = logic.execute("clear");
        assertNormalResult(result, ClearCommand.MESSAGE_SUCCESS, CommandResult.SelectionRequest.PRESERVE_OR_FIRST);
        assertMutation(List.of(), List.of());
    }

    @Test
    public void executeClear_unexpectedArgument_rejectsWithoutChanges() throws Exception {
        assertParseFailure("clear extra", invalidFormat(ClearCommand.COMMAND_WORD));
    }

    @Test
    public void executeClear_storageFailure_preservesLiveAndSavedState() throws Exception {
        IOException ioFailure = new IOException("synthetic save failure");
        storage.failure = ioFailure;
        CommandException failure = assertRejected("clear", CommandException.class,
                LogicManager.SAVE_FAILURE_MESSAGE, 1);
        assertSame(ioFailure, failure.getCause().getCause());
        assertTrue(storage.snapshots.getFirst().getPersonList().isEmpty());
    }

    @Test
    public void executeHelp_noArguments_returnsHelpFlagWithoutSaving() throws Exception {
        logic.execute("find n/Mary");
        CommandResult result = logic.execute("help");
        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, result.getFeedbackToUser());
        assertTrue(result.isShowHelp());
        assertFalse(result.isExit());
        assertFalse(result.requiresClashConfirmation());
        assertEquals(CommandResult.SelectionRequest.PRESERVE_OR_FIRST, result.getSelectionRequest());
        assertDisplayOnly(List.of(mary), SortOrder.DESC);
    }

    @Test
    public void executeHelp_unexpectedArgument_rejectsWithoutChanges() throws Exception {
        assertParseFailure("help extra", invalidFormat(HelpCommand.MESSAGE_USAGE));
    }

    @Test
    public void executeExit_noArguments_returnsExitFlagWithoutSaving() throws Exception {
        logic.execute("find n/Mary");
        CommandResult result = logic.execute("exit");
        assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT, result.getFeedbackToUser());
        assertFalse(result.isShowHelp());
        assertTrue(result.isExit());
        assertFalse(result.requiresClashConfirmation());
        assertEquals(CommandResult.SelectionRequest.PRESERVE_OR_FIRST, result.getSelectionRequest());
        assertDisplayOnly(List.of(mary), SortOrder.DESC);
    }

    @Test
    public void executeExit_unexpectedArgument_rejectsWithoutChanges() throws Exception {
        assertParseFailure("exit extra", invalidFormat(ExitCommand.COMMAND_WORD));
    }

    private void assertNormalResult(CommandResult result, String feedback, CommandResult.SelectionRequest selection) {
        assertEquals(feedback, result.getFeedbackToUser());
        assertEquals(selection, result.getSelectionRequest());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertFalse(result.requiresClashConfirmation());
    }

    private void assertMutation(List<Person> expectedStored, List<Person> expectedDisplayed) throws Exception {
        assertEquals(expectedStored, model.getAddressBook().getPersonList());
        assertEquals(expectedDisplayed, logic.getFilteredPersonList());
        assertEquals(SortOrder.DESC, model.getSortOrder());
        assertEquals(1, storage.snapshots.size());
        assertEquals(model.getAddressBook(), storage.snapshots.getFirst());
        ReadOnlyAddressBook loaded = jsonStorage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), loaded);
        assertEquals(model.getAddressBook().hashCode(), loaded.hashCode());
    }

    private void assertDisplayOnly(List<Person> expectedDisplayed, SortOrder expectedSort) throws Exception {
        assertEquals(initialPersons, model.getAddressBook().getPersonList());
        assertEquals(expectedDisplayed, logic.getFilteredPersonList());
        assertEquals(expectedSort, model.getSortOrder());
        assertEquals(0, storage.snapshots.size());
        assertArrayEquals(initialJson, Files.readAllBytes(dataPath));
    }

    private void assertParseFailure(String command, String message) throws Exception {
        assertRejected(command, ParseException.class, message, 0);
    }

    private void assertExecutionFailure(String command, String message) throws Exception {
        assertRejected(command, CommandException.class, message, 0);
    }

    private <T extends Exception> T assertRejected(String command, Class<T> exceptionType, String message,
            int expectedSaveAttempts) throws Exception {
        logic.execute("find n/Mary");
        AddressBook before = new AddressBook(model.getAddressBook());
        List<Person> displayedBefore = List.copyOf(logic.getFilteredPersonList());
        SortOrder sortBefore = model.getSortOrder();
        byte[] jsonBefore = Files.readAllBytes(dataPath);
        T failure = assertThrows(exceptionType, () -> logic.execute(command));
        assertEquals(message, failure.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals(displayedBefore, logic.getFilteredPersonList());
        assertEquals(sortBefore, model.getSortOrder());
        assertEquals(expectedSaveAttempts, storage.snapshots.size());
        assertArrayEquals(jsonBefore, Files.readAllBytes(dataPath));
        return failure;
    }

    private String invalidFormat(String usage) {
        return String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, usage);
    }

    /** Counts saves at the storage interface while using real JSON persistence on successful writes. */
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
