package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;

/**
 * Deletes a person identified using its displayed index from the address book or its unique client reference.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
                    + ": Deletes a person by displayed index or client reference.\n"
                    + "Parameters: INDEX or r/REFERENCE\n"
                    + "Example: " + COMMAND_WORD + " 1\n"
                    + "         " + COMMAND_WORD + " r/C0001";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";

    private final Index targetIndex;
    private final ClientReference targetReference;

    /**
     * Creates {@code DeleteCommand} object for {@code Person} deletion via index
     * @param targetIndex Index of {@code Person} to delete
     */
    public DeleteCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.targetReference = null;
    }

    /**
     * Creates {@code DeleteCommand} object for {@code Person} deletion via {@code ClientReference}
     * @param targetReference {@code ClientReference} of {@code Person} to delete
     */
    public DeleteCommand(ClientReference targetReference) {
        this.targetIndex = null;
        this.targetReference = requireNonNull(targetReference);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToDelete;

        if (targetIndex != null) {
            personToDelete = getPersonByIndex(model);
        } else {
            personToDelete = getPersonByReference(model);
        }

        model.deletePerson(personToDelete);

        return CommandResult.preserveSelectionOrFirst(
                String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    private Person getPersonByIndex(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        return lastShownList.get(targetIndex.getZeroBased());
    }

    private Person getPersonByReference(Model model) throws CommandException {
        return ClientLookup.requireClient(model, targetReference);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherDeleteCommand.targetIndex)
                && Objects.equals(targetReference, otherDeleteCommand.targetReference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetIndex, targetReference);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("targetReference", targetReference)
                .toString();
    }
}
