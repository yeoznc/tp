package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class FindPersonPredicateTest {
    private final Person alice = new PersonBuilder().withName("Alice Bob").withPhone("00123456").build();

    @Test
    public void test_noCriteria_returnsFalse() {
        assertFalse(new FindPersonPredicate(List.of(), List.of()).test(alice));
    }

    @Test
    public void test_onlyNameKeywords_matchesAnyPartialNameIgnoringCase() {
        assertTrue(new FindPersonPredicate(List.of("missing", "lIC"), List.of()).test(alice));
        assertFalse(new FindPersonPredicate(List.of("Carol"), List.of()).test(alice));
        assertFalse(new FindPersonPredicate(List.of("00123456"), List.of()).test(alice));
    }

    @Test
    public void test_onlyPhoneNumbers_matchesAnyFullPhoneNumber() {
        assertTrue(new FindPersonPredicate(List.of(), List.of(new Phone("00123456"))).test(alice));
        assertTrue(new FindPersonPredicate(List.of(),
                List.of(new Phone("99999999"), new Phone("00123456"))).test(alice));
        assertFalse(new FindPersonPredicate(List.of(), List.of(new Phone("99999999"))).test(alice));

        // Partial numbers and numbers with different leading zeros do not match.
        assertFalse(new FindPersonPredicate(List.of(), List.of(new Phone("001"))).test(alice));
        assertFalse(new FindPersonPredicate(List.of(), List.of(new Phone("123456"))).test(alice));
    }

    @Test
    public void test_bothFields_requiresBothToMatch() {
        assertTrue(new FindPersonPredicate(List.of("lic"), List.of(new Phone("00123456"))).test(alice));
        assertTrue(new FindPersonPredicate(List.of("missing", "lic"),
                List.of(new Phone("99999999"), new Phone("00123456"))).test(alice));
        assertFalse(new FindPersonPredicate(List.of("lic"), List.of(new Phone("99999999"))).test(alice));
        assertFalse(new FindPersonPredicate(List.of("Carol"), List.of(new Phone("00123456"))).test(alice));
        assertFalse(new FindPersonPredicate(List.of("Carol"), List.of(new Phone("99999999"))).test(alice));
    }

    @Test
    public void test_phoneMatchesOtherFields_returnsFalse() {
        Person person = new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("91234567@example.com").withAddress("91234567 Main Street").withTags("91234567").build();
        assertFalse(new FindPersonPredicate(List.of(), List.of(new Phone("91234567"))).test(person));
    }

    @Test
    public void test_cardNumber_requiresFullExactMatch() {
        Person person = new PersonBuilder().withCardDetails("0012  3456", "123", "12/28", "Visa").build();
        assertTrue(new FindPersonPredicate(List.of(), List.of(), "0012  3456").test(person));
        assertTrue(new FindPersonPredicate(List.of(), List.of(), "0012 3456").test(person));
        for (String number : List.of("3456", "12  3456", "00123456", "99999999")) {
            assertFalse(new FindPersonPredicate(List.of(), List.of(), number).test(person));
        }
    }

    @Test
    public void test_cardNumber_doesNotMatchOtherCardOrContactFields() {
        Person person = new PersonBuilder().withPhone("91234567")
                .withCardDetails("4111111111111111", "91234567", "12/28", "91234567").build();
        assertFalse(new FindPersonPredicate(List.of(), List.of(), "91234567").test(person));
        assertFalse(new FindPersonPredicate(List.of(), List.of(), "12/28").test(person));
    }

    @Test
    public void test_legacyProfileWithoutCard_doesNotMatchCardSearch() {
        Person legacy = new Person(alice.getName(), alice.getPhone(), alice.getEmail(), alice.getAddress(),
                alice.getTags());
        assertFalse(new FindPersonPredicate(List.of(), List.of(), PersonBuilder.DEFAULT_CARD_NUMBER).test(legacy));
        assertTrue(new FindPersonPredicate(List.of("lic"), List.of()).test(legacy));
    }

    @Test
    public void test_allFields_requiresEachSuppliedFieldToMatch() {
        String card = PersonBuilder.DEFAULT_CARD_NUMBER;
        assertTrue(new FindPersonPredicate(List.of("lic"), List.of(new Phone("00123456")), card).test(alice));
        assertFalse(new FindPersonPredicate(List.of("Carol"), List.of(new Phone("00123456")), card).test(alice));
        assertFalse(new FindPersonPredicate(List.of("lic"), List.of(new Phone("99999999")), card).test(alice));
        assertFalse(new FindPersonPredicate(List.of("lic"), List.of(new Phone("00123456")), "9999").test(alice));
    }

    @Test
    public void constructor_copiesSearchCriteria() {
        List<String> names = new ArrayList<>(List.of("lic"));
        List<Phone> phones = new ArrayList<>(List.of(new Phone("00123456")));
        FindPersonPredicate predicate = new FindPersonPredicate(names, phones);
        names.set(0, "Carol");
        phones.set(0, new Phone("99999999"));
        assertTrue(predicate.test(alice));
    }

    @Test
    public void equals() {
        FindPersonPredicate predicate = new FindPersonPredicate(List.of("lic"), List.of(new Phone("00123456")));
        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new FindPersonPredicate(List.of("lic"), List.of(new Phone("00123456")))));
        assertFalse(predicate.equals(null));
        assertFalse(predicate.equals("lic"));
        assertFalse(predicate.equals(new FindPersonPredicate(List.of("Carol"), List.of(new Phone("00123456")))));
        assertFalse(predicate.equals(new FindPersonPredicate(List.of("lic"), List.of(new Phone("99999999")))));
        FindPersonPredicate withCard = new FindPersonPredicate(List.of("lic"), List.of(), "00123456");
        assertTrue(withCard.equals(new FindPersonPredicate(List.of("lic"), List.of(), "00123456")));
        assertFalse(withCard.equals(new FindPersonPredicate(List.of("lic"), List.of(), "123456")));
        assertFalse(withCard.equals(new FindPersonPredicate(List.of("lic"), List.of())));
    }

    @Test
    public void toStringMethod() {
        List<String> names = List.of("lic");
        List<Phone> phones = List.of(new Phone("00123456"));
        FindPersonPredicate predicate = new FindPersonPredicate(names, phones);
        String expected = FindPersonPredicate.class.getCanonicalName()
                + "{nameKeywords=" + names + ", phoneNumbers=" + phones + ", cardNumber=null}";
        assertEquals(expected, predicate.toString());
    }
}
