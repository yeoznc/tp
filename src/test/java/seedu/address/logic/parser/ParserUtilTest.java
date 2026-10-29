package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseClientReference_validValue_trimsAndNormalizesCase() throws Exception {
        assertEquals(new ClientReference("SG-1024"), ParserUtil.parseClientReference("  sg-1024  "));
        assertEquals(new ClientReference("SG-1024"), ParserUtil.parseClientReference("\u2003sg-1024\u2003"));
    }

    @Test
    public void parseClientReference_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, ClientReference.MESSAGE_CONSTRAINTS, ()
                -> ParserUtil.parseClientReference("123"));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseClientReference(null));
    }

    @Test
    public void parseName_repeatedSpaces_collapsesSpacingAndPreservesCapitalization() throws Exception {
        assertEquals(new Name("Mary O'Connor-Smith"), ParserUtil.parseName("  Mary   O'Connor-Smith  "));
    }

    @Test
    public void parseExpiryDate_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseExpiryDate(null));
    }

    @Test
    public void parseExpiryDate_invalidMonth_throwsParseException() {
        assertThrows(ParseException.class, CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS, ()
                -> ParserUtil.parseExpiryDate("13/28"));
    }

    @Test
    public void parseExpiryDate_validValueWithWhitespace_success() throws Exception {
        assertEquals("12/28", ParserUtil.parseExpiryDate(WHITESPACE + "12/28" + WHITESPACE));
    }

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("\u20031\u2003"));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }
    @Test
    public void parseIndex_boundaries() throws Exception {
        assertEquals(Index.fromOneBased(1), ParserUtil.parseIndex(" 1 "));
        assertEquals(Index.fromOneBased(Integer.MAX_VALUE), ParserUtil.parseIndex("2147483647"));
        for (String invalid : new String[] {"0", "-1", "+1", "2147483648", "1.0", "1 2", ""}) {
            assertThrows(ParseException.class, ParserUtil.MESSAGE_INVALID_INDEX, ()
                        -> ParserUtil.parseIndex(invalid));
        }
        assertThrows(NullPointerException.class, () -> ParserUtil.parseIndex(null));
    }

    @Test
    public void parseFinancialValues_validPartitions_normalizesValues() throws Exception {
        for (String valid : new String[] {"0", "1", "0001", "999999999999999999999999"}) {
            assertEquals(new CreditScore(valid), ParserUtil.parseCreditScore(" " + valid + " "));
        }
        for (String valid : new String[] {"0", "0.00", "1.2", "1.23", "999999999999999999.99"}) {
            assertEquals(new Debt(valid), ParserUtil.parseDebt(" " + valid + " "));
        }
    }

    @Test
    public void parseFinancialValues_invalidPartitions() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCreditScore(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseDebt(null));
        for (String invalid : new String[] {"", " ", "-1", "+1", "1.0", "1e2", "a"}) {
            assertThrows(ParseException.class, CreditScore.MESSAGE_CONSTRAINTS, ()
                        -> ParserUtil.parseCreditScore(invalid));
        }
        for (String invalid : new String[] {"", " ", "-0.01", "+1", "1.234", ".1", "1.", "1e2"}) {
            assertThrows(ParseException.class, Debt.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseDebt(invalid));
        }
    }

    @Test
    public void parseCardFields_requiredTextAndNullPartitions() throws Exception {
        assertEquals("0012  34", ParserUtil.parseCardNumber(" 0012  34 "));
        assertEquals("001", ParserUtil.parseCvv(" 001 "));
        assertEquals("Some Provider", ParserUtil.parseProvider(" Some Provider "));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCardNumber(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseCvv(null));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseProvider(null));
        for (String blank : new String[] {"", " ", "\t\n"}) {
            assertThrows(ParseException.class, CardDetails.CARD_NUMBER_MESSAGE_CONSTRAINTS, ()
                        -> ParserUtil.parseCardNumber(blank));
            assertThrows(ParseException.class, CardDetails.CVV_MESSAGE_CONSTRAINTS, ()
                        -> ParserUtil.parseCvv(blank));
            assertThrows(ParseException.class, CardDetails.PROVIDER_MESSAGE_CONSTRAINTS, ()
                        -> ParserUtil.parseProvider(blank));
        }
    }

    @Test
    public void parseExpiryDate_monthBoundaries() throws Exception {
        for (String valid : new String[] {"01/00", "02/99", "11/28", "12/99"}) {
            assertEquals(valid, ParserUtil.parseExpiryDate(" " + valid + " "));
        }
        for (String invalid : new String[] {"00/28", "13/28", "1/28", "01/2028", "01-28", ""}) {
            assertThrows(ParseException.class, CardDetails.EXPIRY_DATE_MESSAGE_CONSTRAINTS, ()
                        -> ParserUtil.parseExpiryDate(invalid));
        }
    }

    @Test
    public void parseTags_duplicatesAfterTrimming_collapsesToOneTag() throws Exception {
        assertEquals(Set.of(new Tag("friend")), ParserUtil.parseTags(List.of("friend", " friend ")));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(Arrays.asList("friend", null)));
        assertThrows(NullPointerException.class, () -> ParserUtil.parseClientReference(null));
    }

}
