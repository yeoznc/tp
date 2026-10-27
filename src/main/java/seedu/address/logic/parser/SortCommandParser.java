package seedu.address.logic.parser;

import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.SortOrder;

/** Parses the optional order argument of the sort command. */
public class SortCommandParser implements Parser<SortCommand> {
    @Override
    public SortCommand parse(String args) throws ParseException {
        String trimmed = ArgumentTokenizer.tokenizeStrict(args).getPreamble();
        if (trimmed.isEmpty()) {
            return new SortCommand(SortOrder.ASC);
        }
        String[] tokens = trimmed.split(" ");
        if (tokens.length > 1) {
            throw new ParseException(SortCommand.MESSAGE_INVALID_COMMAND);
        }
        return switch (tokens[0]) {
            case "asc" -> new SortCommand(SortOrder.ASC);
            case "desc" -> new SortCommand(SortOrder.DESC);
            case "reset" -> new SortCommand(SortOrder.ORIGINAL);
            default -> throw new ParseException(String.format(SortCommand.MESSAGE_INVALID_ORDER, tokens[0]));
        };
    }
}
