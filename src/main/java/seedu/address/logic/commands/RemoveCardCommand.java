package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;

/** Removes the card details from a client identified by reference. */
public class RemoveCardCommand extends Command {
    public static final String COMMAND_WORD = "removecard";
    public static final String MESSAGE_USAGE = "Format: removecard r/REFERENCE";
    public static final String MESSAGE_SUCCESS = "Removed card ending in %1$s from client %2$s";
    public static final String MESSAGE_CARD_NOT_FOUND = "Client %1$s is not associated with a card.";

    private final ClientReference reference;

    public RemoveCardCommand(ClientReference reference) {
        this.reference = requireNonNull(reference);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person client = ClientLookup.requireClient(model, reference);
        if (client.getCardDetails() == null) {
            throw new CommandException(String.format(MESSAGE_CARD_NOT_FOUND, reference));
        }

        String cardNumber = client.getCardDetails().getCardNumber();
        String cardEnding = cardNumber.substring(Math.max(0, cardNumber.length() - 4));
        Person updatedClient = client.withoutCardDetails();
        model.setPerson(client, updatedClient);
        return new CommandResult(String.format(MESSAGE_SUCCESS, cardEnding, reference));
    }

    @Override
    public boolean isDataChanging() {
        return true;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof RemoveCardCommand command
                && reference.equals(command.reference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reference);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("reference", reference).toString();
    }
}
