package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_REFERENCE;

import seedu.address.logic.commands.RemoveCardCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the single client reference required by {@code removecard}. */
public class RemoveCardCommandParser implements Parser<RemoveCardCommand> {
    public static final String MESSAGE_MISSING_REFERENCE =
            "Missing client reference. Format: removecard r/REFERENCE";
    public static final String MESSAGE_REPEATED_PARAMETER = "Parameter was specified more than once.";
    public static final String MESSAGE_INVALID_COMMAND = "Invalid removecard command. Format: removecard r/REFERENCE";

    @Override
    public RemoveCardCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeStrict(args, PREFIX_REFERENCE);
        if (arguments.getAllValues(PREFIX_REFERENCE).size() > 1) {
            throw new ParseException(MESSAGE_REPEATED_PARAMETER);
        }
        String referenceValue = arguments.getValue(PREFIX_REFERENCE).orElse("");
        if (referenceValue.isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_REFERENCE);
        }
        if (!arguments.getPreamble().isEmpty() || referenceValue.contains(" ")) {
            throw new ParseException(MESSAGE_INVALID_COMMAND);
        }
        return new RemoveCardCommand(ParserUtil.parseClientReference(referenceValue));
    }
}
