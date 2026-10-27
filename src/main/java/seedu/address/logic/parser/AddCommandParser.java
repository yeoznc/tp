package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARD_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CREDIT_SCORE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CVV;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DEBT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EXPIRY_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PROVIDER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REFERENCE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenizeStrict(args, PREFIX_REFERENCE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                        PREFIX_ADDRESS, PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER,
                        PREFIX_CREDIT_SCORE, PREFIX_DEBT, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_REFERENCE, PREFIX_NAME, PREFIX_ADDRESS, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER, PREFIX_CREDIT_SCORE, PREFIX_DEBT)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_REFERENCE, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_ADDRESS, PREFIX_CARD_NUMBER, PREFIX_CVV, PREFIX_EXPIRY_DATE, PREFIX_PROVIDER,
                PREFIX_CREDIT_SCORE, PREFIX_DEBT);
        ClientReference reference = ParserUtil.parseClientReference(argMultimap.getValue(PREFIX_REFERENCE).get());
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        CardDetails cardDetails = new CardDetails(
                ParserUtil.parseCardNumber(argMultimap.getValue(PREFIX_CARD_NUMBER).get()),
                ParserUtil.parseCvv(argMultimap.getValue(PREFIX_CVV).get()),
                ParserUtil.parseExpiryDate(argMultimap.getValue(PREFIX_EXPIRY_DATE).get()),
                ParserUtil.parseProvider(argMultimap.getValue(PREFIX_PROVIDER).get()));
        CreditScore creditScore = ParserUtil.parseCreditScore(argMultimap.getValue(PREFIX_CREDIT_SCORE).get());
        Debt debt = ParserUtil.parseDebt(argMultimap.getValue(PREFIX_DEBT).get());
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Person person = new Person(reference, name, phone, email, address, cardDetails, creditScore, debt, tagList);

        return new AddCommand(person);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
