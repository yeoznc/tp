package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.FIONA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.FindCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.FindPersonPredicate;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void equals() {
        FindPersonPredicate firstPredicate = new FindPersonPredicate(List.of("first"), List.of());
        FindPersonPredicate secondPredicate = new FindPersonPredicate(List.of("second"), List.of());

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));

        // same names, different phone numbers -> returns false
        FindCommand findByNameAndPhone = new FindCommand(new FindPersonPredicate(List.of("first"),
                List.of(new Phone("91234567"))));
        assertFalse(findFirstCommand.equals(findByNameAndPhone));
    }

    @Test
    public void execute_zeroKeywords_noPersonFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0);
        FindPersonPredicate predicate = preparePredicate(" ");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_multipleKeywords_multiplePersonsFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 3);
        FindPersonPredicate predicate = preparePredicate("Kurz Elle Kunz");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_partialKeyword_multiplePersonsFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2);
        FindPersonPredicate predicate = preparePredicate("eIE");
        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_parsedPartialKeywords_multiplePersonsFound() throws ParseException {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 3);
        FindPersonPredicate predicate = preparePredicate("aRL lL iOn missing");
        FindCommand command = new FindCommandParser().parse(" \t n/aRL  lL \n iOn missing  ");
        expectedModel.updateFilteredPersonList(predicate);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL, ELLE, FIONA), model.getFilteredPersonList());
    }

    @Test
    public void execute_phoneNumbers_multiplePersonsFound() throws ParseException {
        assertSearchResult("p/95352563 9482427", List.of(CARL, FIONA));
    }

    @Test
    public void execute_cardNumber_findsAllProfilesWithFullNumber() throws ParseException {
        Person first = new PersonBuilder(CARL).withCardDetails("0012  3456", "123", "12/28", "Visa").build();
        Person second = new PersonBuilder(FIONA).withCardDetails("0012  3456", "456", "11/29", "Visa").build();
        Person legacy = new Person(ELLE.getName(), ELLE.getPhone().orElse(null), ELLE.getEmail().orElse(null),
                ELLE.getAddress().orElse(null), ELLE.getTags());
        AddressBook addressBook = getTypicalAddressBook();
        addressBook.setPerson(CARL, first);
        addressBook.setPerson(FIONA, second);
        addressBook.setPerson(ELLE, legacy);
        model = new ModelManager(addressBook, new UserPrefs());
        expectedModel = new ModelManager(addressBook, new UserPrefs());

        assertSearchResult("cn/0012  3456", List.of(first, second));
        assertSearchResult("cn/3456", List.of());
        assertSearchResult("cn/12  3456", List.of());
        assertSearchResult("cn/0012  3456 n/arl", List.of(first));
        assertSearchResult("p/9482427 cn/0012  3456", List.of(second));
        assertSearchResult("n/arl p/95352563 cn/0012  3456", List.of(first));
        assertSearchResult("n/arl p/9482427 cn/0012  3456", List.of());
        assertSearchResult("n/arl cn/9999", List.of());
        assertSearchResult("p/95352563", List.of(first));
    }

    @Test
    public void execute_nonMatchingPhone_noPersonFound() throws ParseException {
        assertSearchResult("p/11111111", List.of());
    }

    @Test
    public void execute_partialPhoneNumber_noPersonFound() throws ParseException {
        assertSearchResult("p/953", List.of());
    }

    @Test
    public void execute_bothFields_onlyPersonMatchingBothFound() throws ParseException {
        assertSearchResult("n/eIE p/98765432", List.of(BENSON));
    }

    @Test
    public void execute_alternativesWithinBothFields_multiplePersonsFound() throws ParseException {
        assertSearchResult("p/98765432 87652533 n/meI missing", List.of(BENSON, DANIEL));
    }

    @Test
    public void execute_fieldsMatchDifferentPersons_noPersonFound() throws ParseException {
        assertSearchResult("n/Carl p/98765432", List.of());
    }

    @Test
    public void execute_onlyNameMatches_noPersonFound() throws ParseException {
        assertSearchResult("n/Carl p/11111111", List.of());
    }

    @Test
    public void execute_newSearch_replacesPreviousFilter() throws ParseException {
        new FindCommandParser().parse("n/Carl").execute(model);
        assertSearchResult("p/98765432", List.of(BENSON));
    }

    @Test
    public void toStringMethod() {
        FindPersonPredicate predicate = new FindPersonPredicate(List.of("keyword"), List.of());
        FindCommand findCommand = new FindCommand(predicate);
        String expected = FindCommand.class.getCanonicalName() + "{predicate=" + predicate + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Parses name keywords into a {@code FindPersonPredicate}.
     */
    private FindPersonPredicate preparePredicate(String userInput) {
        return new FindPersonPredicate(List.of(userInput.split("\\s+")), List.of());
    }

    /**
     * Checks a parsed search against independently specified results and feedback.
     */
    private void assertSearchResult(String args, List<Person> expectedPersons) throws ParseException {
        FindCommand command = new FindCommandParser().parse(args);
        expectedModel.updateFilteredPersonList(expectedPersons::contains);
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, expectedPersons.size());
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(expectedPersons, model.getFilteredPersonList());
    }
    @Test
    public void execute_missingOptionalDetails_skipsOnlyWhenTheirFilterIsSupplied() throws Exception {
        Person minimal = new Person(new ClientReference("C0001"), new Name("Alice"),
                null, null, null, null, null, null, Set.of());
        AddressBook book = new AddressBook();
        book.addPerson(minimal);
        model = new ModelManager(book, new UserPrefs());
        expectedModel = new ModelManager(book, new UserPrefs());
        assertSearchResult("n/ali", List.of(minimal));
        assertSearchResult("n/ali p/123", List.of());
        assertSearchResult("n/ali cn/123", List.of());
        assertSearchResult("p/123 cn/123", List.of());
        assertEquals(List.of(minimal), model.getAddressBook().getPersonList());
        assertSearchResult("n/ali", List.of(minimal));
    }

    @Test
    public void equalsAndHashCode_includeCardFilter() {
        FindCommand first = new FindCommand(new FindPersonPredicate(List.of("Alice"), List.of(), "001"));
        FindCommand copy = new FindCommand(new FindPersonPredicate(List.of("Alice"), List.of(), "001"));
        assertEquals(first, copy);
        assertEquals(first.hashCode(), copy.hashCode());
        assertFalse(first.equals(new FindCommand(new FindPersonPredicate(List.of("Alice"), List.of(), "002"))));
        assertFalse(first.equals(new FindCommand(new FindPersonPredicate(List.of("Alice"), List.of()))));
        assertFalse(first.isDataChanging());
    }

}
