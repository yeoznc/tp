package seedu.address.model.person;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Matches partial name keywords, full phone numbers, and a full card number.
 * All supplied fields must match. An empty search matches nobody.
 */
public class FindPersonPredicate implements Predicate<Person> {
    private final List<String> nameKeywords;
    private final List<Phone> phoneNumbers;
    private final String cardNumber;
    private final NameContainsKeywordsPredicate namePredicate;

    /**
     * Creates a search predicate. An empty list omits the corresponding field from the search.
     */
    public FindPersonPredicate(List<String> nameKeywords, List<Phone> phoneNumbers) {
        this(nameKeywords, phoneNumbers, null);
    }

    /**
     * Creates a search predicate. A null card number omits that field from the search.
     */
    public FindPersonPredicate(List<String> nameKeywords, List<Phone> phoneNumbers, String cardNumber) {
        this.nameKeywords = List.copyOf(nameKeywords);
        this.phoneNumbers = List.copyOf(phoneNumbers);
        this.cardNumber = cardNumber;
        namePredicate = new NameContainsKeywordsPredicate(this.nameKeywords);
    }

    @Override
    public boolean test(Person person) {
        if (nameKeywords.isEmpty() && phoneNumbers.isEmpty() && cardNumber == null) {
            return false;
        }
        return (nameKeywords.isEmpty() || namePredicate.test(person))
                && (phoneNumbers.isEmpty() || phoneNumbers.contains(person.getPhone()))
                && (cardNumber == null || person.getCardDetails() != null
                        && StringUtil.normalizeWhitespace(cardNumber).equals(
                                StringUtil.normalizeWhitespace(person.getCardDetails().getCardNumber())));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FindPersonPredicate otherPredicate)) {
            return false;
        }
        return nameKeywords.equals(otherPredicate.nameKeywords) && phoneNumbers.equals(otherPredicate.phoneNumbers)
                && Objects.equals(cardNumber, otherPredicate.cardNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameKeywords, phoneNumbers, cardNumber);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("nameKeywords", nameKeywords).add("phoneNumbers", phoneNumbers)
                .add("cardNumber", cardNumber).toString();
    }
}
