package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
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
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PersonUtil;

public class AddCommandParserTest {
    private static final List<String> FIELDS = List.of("r/C0001", "n/Alex Yeoh", "p/87438807",
            "e/alex@example.com", "a/Blk 30 Geylang Street 29, #06-40", "cn/4111111111111111", "cvv/123",
            "exp/12/28", "provider/Visa", "cs/700", "d/0.00");
    private static final String VALID_ARGUMENTS = " " + String.join(" ", FIELDS);
    private static final String FINANCIAL_ARGUMENTS = " " + String.join(" ", FIELDS.subList(5, FIELDS.size()));
    private static final String MINIMAL_ARGUMENTS = " r/C0001 n/Alex Yeoh";
    private final AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_minimalClient_success() {
        Person expected = new Person(new ClientReference("C0001"), new Name("Alex Yeoh"),
                null, null, null, null, null, null, Collections.emptySet());
        assertParseSuccess(parser, " r/c0001 n/Alex Yeoh", new AddCommand(expected));
    }

    @Test
    public void parse_optionalDetails_acceptsEveryCombinationAndCommandHelperOutput() {
        List<String> optionalArguments = List.of(FIELDS.get(2), FIELDS.get(3), FIELDS.get(4),
                String.join(" ", FIELDS.subList(5, 9)), "cs/0", "d/0.00");
        for (int mask = 0; mask < 64; mask++) {
            StringBuilder args = new StringBuilder(MINIMAL_ARGUMENTS);
            for (int field = 0; field < optionalArguments.size(); field++) {
                if ((mask & (1 << field)) != 0) {
                    args.append(" ").append(optionalArguments.get(field));
                }
            }
            Phone phone = (mask & 1) == 0 ? null : new Phone("87438807");
            Email email = (mask & 2) == 0 ? null : new Email("alex@example.com");
            Address address = (mask & 4) == 0 ? null : new Address("Blk 30 Geylang Street 29, #06-40");
            CardDetails card = (mask & 8) == 0 ? null : new CardDetails("4111111111111111", "123", "12/28", "Visa");
            CreditScore score = (mask & 16) == 0 ? null : new CreditScore("0");
            Debt debt = (mask & 32) == 0 ? null : new Debt("0.00");
            Person expected = new Person(new ClientReference("C0001"), new Name("Alex Yeoh"),
                    phone, email, address, card, score, debt, Collections.emptySet());

            assertParseSuccess(parser, args.toString(), new AddCommand(expected));
            assertParseSuccess(parser, " " + PersonUtil.getPersonDetails(expected), new AddCommand(expected));
        }
    }

    @Test
    public void parse_partialCardGroup_reportsAllFourRequiredFields() {
        List<String> cardFields = FIELDS.subList(5, 9);
        for (int mask = 1; mask < 15; mask++) {
            StringBuilder args = new StringBuilder(MINIMAL_ARGUMENTS);
            for (int field = 0; field < cardFields.size(); field++) {
                if ((mask & (1 << field)) != 0) {
                    args.append(" ").append(cardFields.get(field));
                }
            }
            assertParseFailure(parser, args.toString(), AddCommandParser.MESSAGE_INCOMPLETE_CARD_DETAILS);
        }
        assertParseFailure(parser, MINIMAL_ARGUMENTS + " cn/", AddCommandParser.MESSAGE_INCOMPLETE_CARD_DETAILS);
    }

    @Test
    public void parse_blankOptionalValueWithoutOtherDetails_reportsFieldConstraint() {
        List<String> fields = List.of("p/", "e/", "a/", "cs/", "d/");
        List<String> messages = List.of(Phone.MESSAGE_CONSTRAINTS, Email.MESSAGE_CONSTRAINTS,
                Address.MESSAGE_CONSTRAINTS, CreditScore.MESSAGE_CONSTRAINTS, Debt.MESSAGE_CONSTRAINTS);
        for (int field = 0; field < fields.size(); field++) {
            assertParseFailure(parser, MINIMAL_ARGUMENTS + " " + fields.get(field), messages.get(field));
        }
    }

    @Test
    public void parse_referenceWithAllCommittedFields_success() {
        Person expected = new PersonBuilder().withClientReference("C0001").withName("Alex Yeoh")
                .withPhone("87438807").withEmail("alex@example.com")
                .withAddress("Blk 30 Geylang Street 29, #06-40").build();
        assertParseSuccess(parser, VALID_ARGUMENTS, new AddCommand(expected));
    }

    @Test
    public void parse_reorderedFields_normalizesReferenceAndNameSpacing() {
        Person expected = new PersonBuilder().withClientReference("SG-1024").withName("Mary O'Connor")
                .withPhone("91234567").withEmail("Mary.oconnor@example.com").withAddress("10  Main Street").build();
        assertParseSuccess(parser, "  n/  Mary   O'Connor  e/ Mary.oconnor@example.com "
                + "r/ sg-1024  a/ 10  Main Street  p/91234567  " + FINANCIAL_ARGUMENTS, new AddCommand(expected));
    }

    @Test
    public void parse_missingRequiredField_reportsInvalidFormat() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        for (int i = 0; i < 2; i++) {
            List<String> fields = new ArrayList<>(FIELDS);
            fields.remove(i);
            assertParseFailure(parser, " " + String.join(" ", fields), expectedMessage);
        }
        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, " n/Alex Yeoh", expectedMessage);
        assertParseFailure(parser, " r/C0001", expectedMessage);
    }

    @Test
    public void parse_repeatedSingleValuedField_reportsItsPrefix() {
        for (String field : FIELDS) {
            assertParseFailure(parser, VALID_ARGUMENTS + " " + field,
                    Messages.getErrorMessageForDuplicatePrefixes(new Prefix(prefixOf(field))));
        }
        assertParseFailure(parser, VALID_ARGUMENTS + " p/91234567 n/Mary",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_PHONE));
    }

    @Test
    public void parse_optionalTags_preservesExistingSupport() {
        Person expected = new PersonBuilder().withClientReference("C0001").withName("Alex Yeoh")
                .withPhone("87438807").withEmail("alex@example.com")
                .withAddress("Blk 30 Geylang Street 29, #06-40").withTags("friends", "owesMoney").build();
        assertParseSuccess(parser, VALID_ARGUMENTS + " t/friends t/owesMoney t/friends", new AddCommand(expected));
        assertParseFailure(parser, VALID_ARGUMENTS + " t/#invalid", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownPrefix_remainsPartOfValue() {
        for (String prefix : List.of("x/", "R/", "future/")) {
            assertParseFailure(parser, VALID_ARGUMENTS + " " + prefix + "value",
                    Debt.MESSAGE_CONSTRAINTS);
            String address = "Blk 30 Geylang Street 29, #06-40 " + prefix + "value";
            Person expected = new PersonBuilder().withClientReference("C0001").withName("Alex Yeoh")
                    .withPhone("87438807").withEmail("alex@example.com").withAddress(address).build();
            assertParseSuccess(parser, VALID_ARGUMENTS.replace(FIELDS.get(4), "a/" + address),
                    new AddCommand(expected));
        }
    }

    @Test
    public void parse_nonEmptyPreamble_reportsInvalidFormat() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "extra-text" + VALID_ARGUMENTS, expectedMessage);
        assertParseFailure(parser, "extra-text", expectedMessage);
    }

    @Test
    public void parse_prefixWithoutPrecedingSpace_reportsInvalidFormat() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, VALID_ARGUMENTS.stripLeading(), expectedMessage);
    }

    @Test
    public void parse_blankSuppliedValues_reportsFieldConstraint() {
        List<String> messages = List.of(ClientReference.MESSAGE_CONSTRAINTS, Name.MESSAGE_CONSTRAINTS,
                Phone.MESSAGE_CONSTRAINTS, Email.MESSAGE_CONSTRAINTS, Address.MESSAGE_CONSTRAINTS,
                CardDetails.CARD_NUMBER_MESSAGE_CONSTRAINTS, CardDetails.CVV_MESSAGE_CONSTRAINTS,
                CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS, CardDetails.PROVIDER_MESSAGE_CONSTRAINTS,
                CreditScore.MESSAGE_CONSTRAINTS, Debt.MESSAGE_CONSTRAINTS);
        for (int i = 0; i < FIELDS.size(); i++) {
            List<String> fields = new ArrayList<>(FIELDS);
            fields.set(i, prefixOf(fields.get(i)));
            assertParseFailure(parser, " " + String.join(" ", fields), messages.get(i));
        }
    }

    @Test
    public void parse_invalidContactValues_reportsSpecConstraint() {
        assertParseFailure(parser, VALID_ARGUMENTS.replace("C0001", "1C0001"), ClientReference.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGUMENTS.replace("Alex Yeoh", "Alex2"), Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGUMENTS.replace("87438807", "123-456"), Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGUMENTS.replace("alex@example.com", "alex@example"),
                Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_ARGUMENTS.replace("Blk 30 Geylang Street 29, #06-40", "Main\nStreet"),
                Address.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidCardOrFinancialValues_preservesCommittedConstraints() {
        for (String expiry : List.of("13/28", "00/28", "1/28", "12/2028")) {
            assertParseFailure(parser, VALID_ARGUMENTS.replace("exp/12/28", "exp/" + expiry),
                    CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS);
        }
        for (String score : List.of("-1", "7.5")) {
            assertParseFailure(parser, VALID_ARGUMENTS.replace("cs/700", "cs/" + score),
                    CreditScore.MESSAGE_CONSTRAINTS);
        }
        for (String debt : List.of("-1", "1.234")) {
            assertParseFailure(parser, VALID_ARGUMENTS.replace("d/0.00", "d/" + debt), Debt.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_multipleInvalidValues_reportsFirstInFieldValidationOrder() {
        assertParseFailure(parser, " p/12 n/Alex2 r/C0001 e/alex@example.com a/Main Street" + FINANCIAL_ARGUMENTS,
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Alex2 p/12 r/C0001 e/alex@example.com a/Main Street" + FINANCIAL_ARGUMENTS,
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " a/ n/Alex Yeoh r/1Invalid p/123 e/alex@example.com" + FINANCIAL_ARGUMENTS,
                ClientReference.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " t/friends " + VALID_ARGUMENTS.replace("87438807", "12") + " t/#invalid",
                Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " cs/-1 " + VALID_ARGUMENTS.replace("cs/700", "").replace("Alex Yeoh", "Alex2"),
                Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " t/#invalid " + VALID_ARGUMENTS.replace("87438807", "12") + " t/friends",
                Phone.MESSAGE_CONSTRAINTS);
    }

    private String prefixOf(String field) {
        return field.substring(0, field.indexOf('/') + 1);
    }
    @Test
    public void parse_confirmationFlag_setsConfirmationState() {
        Person expected = new Person(new ClientReference("C0001"), new Name("Alex Yeoh"),
                null, null, null, null, null, null, Collections.emptySet());
        assertParseSuccess(parser, MINIMAL_ARGUMENTS + " --confirm-clash", new AddCommand(expected, true));
        assertParseSuccess(parser, MINIMAL_ARGUMENTS, new AddCommand(expected, false));
    }

    @Test
    public void parse_flagLikeNameSuffix_isNotConfirmation() {
        Person expected = new Person(new ClientReference("C0001"), new Name("Alex Yeoh --confirm-clashExtra"),
                null, null, null, null, null, null, Collections.emptySet());
        assertParseSuccess(parser, MINIMAL_ARGUMENTS + " --confirm-clashExtra", new AddCommand(expected, false));
        assertParseFailure(parser, MINIMAL_ARGUMENTS + " /confirm", Name.MESSAGE_CONSTRAINTS);
    }

}
