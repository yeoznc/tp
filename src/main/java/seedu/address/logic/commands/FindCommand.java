package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARD_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.person.FindPersonPredicate;

/**
 * Finds persons by partial name keywords, full phone numbers, and/or a full card number.
 * Keywords within a field are alternatives; all supplied fields must match.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds persons by name, phone number and/or card number "
            + "and displays them as a list with index numbers. Names allow case-insensitive partial matches; "
            + "phone and card numbers must match in full. All supplied fields must match.\n"
            + "Parameters: [" + PREFIX_NAME + "KEYWORD [MORE_KEYWORDS]...] [" + PREFIX_PHONE
            + "PHONE_NUMBER [MORE_PHONE_NUMBERS]...] [" + PREFIX_CARD_NUMBER + "CARD_NUMBER]\n"
            + "At least one prefix is required; each prefix may appear only once.\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_NAME + "alice bob\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_PHONE + "91234567\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_CARD_NUMBER + "4111111111111111";

    private final FindPersonPredicate predicate;

    public FindCommand(FindPersonPredicate predicate) {
        this.predicate = requireNonNull(predicate);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean isDataChanging() {
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public int hashCode() {
        return predicate.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("predicate", predicate)
                .toString();
    }
}
