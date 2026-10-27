package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARD_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.ArrayList;
import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.FindPersonPredicate;
import seedu.address.model.person.Phone;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenizeStrict(args,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_CARD_NUMBER);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_CARD_NUMBER);
        List<String> nameKeywords = parseKeywords(argMultimap, PREFIX_NAME);
        List<String> phoneKeywords = parseKeywords(argMultimap, PREFIX_PHONE);
        String cardNumber = null;
        if (argMultimap.getValue(PREFIX_CARD_NUMBER).isPresent()) {
            String value = argMultimap.getValue(PREFIX_CARD_NUMBER).get();
            if (value.contains("/")) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            cardNumber = ParserUtil.parseCardNumber(value);
        }
        if (nameKeywords.isEmpty() && phoneKeywords.isEmpty() && cardNumber == null) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        List<Phone> phoneNumbers = new ArrayList<>();
        for (String phoneKeyword : phoneKeywords) {
            phoneNumbers.add(ParserUtil.parsePhone(phoneKeyword));
        }
        return new FindCommand(new FindPersonPredicate(nameKeywords, phoneNumbers, cardNumber));
    }

    /**
     * Returns a field's keywords, or an empty list if that field was omitted.
     * Rejects empty fields and unrecognised prefixes instead of treating them as keywords.
     */
    private List<String> parseKeywords(ArgumentMultimap argMultimap, Prefix prefix) throws ParseException {
        if (argMultimap.getValue(prefix).isEmpty()) {
            return List.of();
        }
        String value = argMultimap.getValue(prefix).get();
        if (value.isEmpty() || value.contains("/")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        return List.of(value.split("\\s+"));
    }
}
