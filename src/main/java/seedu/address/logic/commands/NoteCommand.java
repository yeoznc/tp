package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;
import seedu.address.model.person.ServiceNote;

/** Replaces or clears the service note on a client identified by reference. */
public class NoteCommand extends Command {
    public static final String COMMAND_WORD = "note";
    public static final String FORMAT = "note r/REFERENCE s/NOTE";
    public static final String MESSAGE_MISSING_REFERENCE =
            "Missing client reference. Format: " + FORMAT;
    public static final String MESSAGE_MISSING_NOTE =
            "Missing required parameter: s/. Format: " + FORMAT;
    public static final String MESSAGE_INVALID_FORMAT = "Invalid note command. Format: " + FORMAT;
    public static final String MESSAGE_REPEATED_PREFIX = "Parameter %s was specified more than once.";

    private final ClientReference reference;
    private final ServiceNote note;

    /** Creates a request to set or clear the note for one referenced client. */
    public NoteCommand(ClientReference reference, ServiceNote note) {
        this.reference = requireNonNull(reference);
        this.note = note;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person client = ClientLookup.requireClient(model, reference);
        if (note == null && client.getServiceNote() == null) {
            throw new CommandException("Client " + reference + " has no service note to clear.");
        }

        String action = note == null ? "Cleared" : client.getServiceNote() == null ? "Added" : "Updated";
        Person updated = client.withServiceNote(note);
        model.setPerson(client, updated);
        return new CommandResult(action + " service note for " + reference + " " + client.getName() + ".",
                reference);
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof NoteCommand command
                && reference.equals(command.reference) && Objects.equals(note, command.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference, note);
    }
}
