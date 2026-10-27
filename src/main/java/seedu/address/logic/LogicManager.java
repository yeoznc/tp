package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.time.Clock;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.commands.Command;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelStaging;
import seedu.address.model.StagedModel;
import seedu.address.model.person.Person;
import seedu.address.storage.Storage;

/**
 * The main LogicManager of the app.
 */
public class LogicManager implements Logic {
    public static final String LOAD_FAILURE_MESSAGE = "Unable to load client data. Existing data was not changed. "
            + "Add, delete, edit, and clear commands are disabled for this session.";
    public static final String SAVE_FAILURE_MESSAGE = "Unable to save client data. No changes were made.";
    public static final String NOTE_LOAD_FAILURE_MESSAGE = "Client data is unavailable because saved data could not "
            + "be loaded. Restart TellerFlow after resolving the data problem.";
    public static final String FILE_OPS_ERROR_FORMAT = AddressBookSync.FILE_OPS_ERROR_FORMAT;

    public static final String FILE_OPS_PERMISSION_ERROR_FORMAT =
            AddressBookSync.FILE_OPS_PERMISSION_ERROR_FORMAT;

    private final Logger logger = LogsCenter.getLogger(LogicManager.class);

    private final Model model;
    private final AddressBookSync addressBookSync;
    private final AddressBookParser addressBookParser;
    private final String startupMessage;
    private final boolean protectedMode;
    private final Clock clock;
    private final BiFunction<Model, Clock, StagedModel> stagedModelFactory;

    /**
     * Constructs a {@code LogicManager} with the given {@code Model} and {@code Storage}.
     */
    public LogicManager(Model model, Storage storage) {
        this(model, storage, "", false, Clock.systemUTC());
    }

    /** Creates logic with the result of loading client data at startup. */
    public LogicManager(Model model, Storage storage, String startupMessage, boolean protectedMode) {
        this(model, storage, startupMessage, protectedMode, Clock.systemUTC());
    }

    /** Creates logic with a clock shared by each staged model. */
    LogicManager(Model model, Storage storage, String startupMessage, boolean protectedMode, Clock clock) {
        this(model, storage, startupMessage, protectedMode, clock, ModelStaging::snapshot);
    }

    /** Creates logic with an injectable staging factory. */
    public LogicManager(Model model, Storage storage, String startupMessage, boolean protectedMode, Clock clock,
            BiFunction<Model, Clock, StagedModel> stagedModelFactory) {
        this.model = model;
        this.addressBookSync = new AddressBookSync(storage);
        addressBookParser = new AddressBookParser();
        this.startupMessage = startupMessage;
        this.protectedMode = protectedMode;
        this.clock = requireNonNull(clock);
        this.stagedModelFactory = requireNonNull(stagedModelFactory);
    }

    @Override
    public CommandResult execute(String commandText) throws CommandException, ParseException {
        logger.info("----------------[USER COMMAND][" + commandText + "]");

        Command command = addressBookParser.parseCommand(commandText);
        if (command.isDataChanging()) {
            if (protectedMode) {
                throw new CommandException(command instanceof NoteCommand
                        ? NOTE_LOAD_FAILURE_MESSAGE : LOAD_FAILURE_MESSAGE);
            }
            StagedModel stagedModel = requireNonNull(stagedModelFactory.apply(model, clock));
            stagedModel.updateFilteredPersonList(model.getFilteredPersonListPredicate());
            stagedModel.setSortOrder(model.getSortOrder());
            CommandResult result = command.execute(stagedModel);
            try {
                addressBookSync.sync(stagedModel.getAddressBook());
            } catch (CommandException e) {
                throw new CommandException(SAVE_FAILURE_MESSAGE, e);
            }
            model.setAddressBook(stagedModel.getAddressBook());
            model.updateFilteredPersonList(stagedModel.getFilteredPersonListPredicate());
            stagedModel.getTimestampChange().ifPresent(change -> {
                String reference = change.reference() == null ? "unreferenced profile" : change.reference();
                logger.info("Timestamp " + change.action() + " for " + reference
                        + ": createdAt=" + change.createdAt() + ", lastModifiedAt=" + change.lastModifiedAt());
            });
            return result;
        }
        return command.execute(model);
    }

    @Override
    public String getStartupMessage() {
        return startupMessage;
    }

    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return model.getFilteredPersonList();
    }

    @Override
    public GuiSettings getGuiSettings() {
        return model.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        model.setGuiSettings(guiSettings);
    }
}
