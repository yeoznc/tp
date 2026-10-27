package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CARD_NUMBER;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.FindPersonPredicate;
import seedu.address.model.person.Phone;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_nameKeywords_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new FindPersonPredicate(List.of("Alice", "Bob"), List.of()));
        assertParseSuccess(parser, "n/Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n n/Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_phoneNumbers_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(new FindPersonPredicate(List.of(),
                List.of(new Phone("91234567"), new Phone("00123456"))));
        assertParseSuccess(parser, "p/91234567 00123456", expectedFindCommand);
        assertParseSuccess(parser, " \t p/ 91234567 \n 00123456 \t ", expectedFindCommand);
    }

    @Test
    public void parse_bothFieldsInEitherOrder_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(new FindPersonPredicate(List.of("lic", "BO"),
                List.of(new Phone("91234567"), new Phone("98765432"))));
        assertParseSuccess(parser, "n/lic BO p/91234567 98765432", expectedFindCommand);
        assertParseSuccess(parser, "p/91234567 98765432 n/lic BO", expectedFindCommand);
        assertParseSuccess(parser, "\tn/lic BO\np/91234567 98765432", expectedFindCommand);
    }

    @Test
    public void parse_cardNumber_preservesFullValue() {
        for (String card : List.of("0012345678901234", "4111  1111 1111 1111", "4111-1111-1111-1111")) {
            FindCommand expected = new FindCommand(new FindPersonPredicate(List.of(), List.of(),
                    card.replaceAll("\\s+", " ")));
            assertParseSuccess(parser, "cn/" + card, expected);
            assertParseSuccess(parser, " \t cn/ " + card + " \n ", expected);
        }
    }

    @Test
    public void parse_cardWithOtherFields_inAnyOrder() {
        FindCommand expected = new FindCommand(new FindPersonPredicate(List.of("lic"),
                List.of(new Phone("91234567")), "4111111111111111"));
        for (String args : List.of("n/lic p/91234567 cn/4111111111111111",
                "cn/4111111111111111 n/lic p/91234567", "p/91234567 cn/4111111111111111 n/lic",
                "\tn/lic\np/91234567\tcn/4111111111111111")) {
            assertParseSuccess(parser, args, expected);
        }
        assertParseSuccess(parser, "n/lic cn/4111111111111111",
                new FindCommand(new FindPersonPredicate(List.of("lic"), List.of(), "4111111111111111")));
        assertParseSuccess(parser, "cn/4111111111111111 p/91234567",
                new FindCommand(new FindPersonPredicate(List.of(), List.of(new Phone("91234567")),
                        "4111111111111111")));
    }

    @Test
    public void parse_emptyCard_throwsParseException() {
        assertParseFailure(parser, "cn/ \t ", CardDetails.CARD_NUMBER_MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "cn/ n/Alice", CardDetails.CARD_NUMBER_MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_missingPrefixOrUnexpectedPreamble_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "Alice Bob", expectedMessage);
        assertParseFailure(parser, "91234567", expectedMessage);
        assertParseFailure(parser, "Alice p/91234567", expectedMessage);
        assertParseFailure(parser, "91234567 n/Alice", expectedMessage);
    }

    @Test
    public void parse_emptyField_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/", expectedMessage);
        assertParseFailure(parser, "p/ \t ", expectedMessage);
        assertParseFailure(parser, "n/ p/91234567", expectedMessage);
        assertParseFailure(parser, "n/Alice p/", expectedMessage);
        assertParseFailure(parser, "n/ p/", expectedMessage);
    }

    @Test
    public void parse_duplicatePrefix_throwsParseException() {
        assertParseFailure(parser, "n/Alice n/Bob", getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, "p/91234567 p/98765432", getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, "n/Alice p/91234567 n/Bob p/98765432",
                getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_PHONE));
        assertParseFailure(parser, "cn/4111111111111111 cn/5555555555554444",
                getErrorMessageForDuplicatePrefixes(PREFIX_CARD_NUMBER));
    }

    @Test
    public void parse_unsupportedPrefix_throwsParseException() {
        assertParseFailure(parser, "e/alice@example.com", "Unknown parameter prefix: e/.");
        assertParseFailure(parser, "n/Alice t/friends", "Unknown parameter prefix: t/.");
        assertParseFailure(parser, "p/91234567 e/alice@example.com", "Unknown parameter prefix: e/.");
        assertParseFailure(parser, "n/Alice p/91234567 t/friends", "Unknown parameter prefix: t/.");
        assertParseFailure(parser, "cn/4111111111111111 cvv/123", "Unknown parameter prefix: cvv/.");
        assertParseFailure(parser, "cn/4111111111111111\te/alice@example.com",
                "Unknown parameter prefix: e/.");
    }

    @Test
    public void parse_invalidPhoneNumber_throwsParseException() {
        assertParseFailure(parser, "p/12", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "p/1234567890123456", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "p/9123abcd", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "p/+6591234567", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "p/91234567 invalid", Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "n/Alice p/9123-4567", Phone.MESSAGE_CONSTRAINTS);
    }

}
