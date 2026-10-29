package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void constructor_nullTagElement_throwsNullPointerException() {
        Set<Tag> tags = new HashSet<>();
        tags.add(null);
        assertThrows(NullPointerException.class, () ->
                new Person(new Name("Alex"), null, null, null, tags));
    }

    @Test
    public void constructor_minimalReferencedProfile_hasNoOptionalDetails() {
        Person person = new Person(new ClientReference("c0001"), new Name("John Doe"),
                null, null, null, null, null, null, Collections.emptySet());

        assertEquals(new ClientReference("C0001"), person.getClientReference().orElse(null));
        assertEquals("C0001", person.getClientReference().orElseThrow().value);
        assertEquals(new Name("John Doe"), person.getName());
        assertEquals(Collections.emptySet(), person.getTags());
        assertEquals(Optional.empty(), person.getPhone());
        assertEquals(Optional.empty(), person.getEmail());
        assertEquals(Optional.empty(), person.getAddress());
        assertEquals(Optional.empty(), person.getCardDetails());
        assertEquals(Optional.empty(), person.getCreditScore());
        assertEquals(Optional.empty(), person.getDebt());
    }

    @Test
    public void constructors_optionalDetails_areIndependent() {
        ClientReference reference = new ClientReference("C0001");
        Name name = new Name("John Doe");
        Set<Tag> tags = Set.of(new Tag("friend"));

        // Exercise every combination, including zero financial values without a card or contact details.
        for (int mask = 0; mask < 64; mask++) {
            Phone phone = (mask & 1) == 0 ? null : new Phone(PersonBuilder.DEFAULT_PHONE);
            Email email = (mask & 2) == 0 ? null : new Email(PersonBuilder.DEFAULT_EMAIL);
            Address address = (mask & 4) == 0 ? null : new Address(PersonBuilder.DEFAULT_ADDRESS);
            CardDetails card = (mask & 8) == 0 ? null : new CardDetails(PersonBuilder.DEFAULT_CARD_NUMBER,
                    PersonBuilder.DEFAULT_CVV, PersonBuilder.DEFAULT_EXPIRY_DATE, PersonBuilder.DEFAULT_PROVIDER);
            CreditScore score = (mask & 16) == 0 ? null : new CreditScore("0");
            Debt debt = (mask & 32) == 0 ? null : new Debt("0.00");
            Person referenced = new Person(reference, name, phone, email, address, card, score, debt, tags);
            Person legacy = new Person(name, phone, email, address, card, score, debt, tags);

            assertEquals(reference, referenced.getClientReference().orElse(null));
            assertNull(legacy.getClientReference().orElse(null));
            for (Person person : List.of(referenced, legacy)) {
                String context = "Optional detail combination: " + mask;
                assertEquals(name, person.getName(), context);
                assertEquals(tags, person.getTags(), context);
                assertEquals(Optional.ofNullable(phone), person.getPhone(), context);
                assertEquals(Optional.ofNullable(email), person.getEmail(), context);
                assertEquals(Optional.ofNullable(address), person.getAddress(), context);
                assertEquals(Optional.ofNullable(card), person.getCardDetails(), context);
                assertEquals(Optional.ofNullable(score), person.getCreditScore(), context);
                assertEquals(Optional.ofNullable(debt), person.getDebt(), context);

                Person copy = new PersonBuilder(person).build();
                assertEquals(person, copy, context);
                assertEquals(copy, person, context);
                assertEquals(person.hashCode(), copy.hashCode(), context);
            }

            if (card == null && score == null && debt == null) {
                assertEquals(legacy, new Person(name, phone, email, address, tags));
            }
        }
    }

    @Test
    public void constructors_nullRequiredFields_throwsNullPointerException() {
        ClientReference reference = new ClientReference("C0001");
        Name name = new Name("John Doe");
        Set<Tag> tags = Collections.emptySet();

        assertThrows(NullPointerException.class, () -> new Person(
                null, name, null, null, null, null, null, null, tags));
        assertThrows(NullPointerException.class, () -> new Person(
                reference, null, null, null, null, null, null, null, tags));
        assertThrows(NullPointerException.class, () -> new Person(
                reference, name, null, null, null, null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Person(null, null, null, null, tags));
        assertThrows(NullPointerException.class, () -> new Person(name, null, null, null, null));
        assertThrows(NullPointerException.class, () -> new Person(
                null, null, null, null, null, null, null, tags));
        assertThrows(NullPointerException.class, () -> new Person(
                name, null, null, null, null, null, null, null));
    }

    @Test
    public void equals_missingAndPresentDetails_areDistinctInBothDirections() {
        Person empty = new Person(new ClientReference("C0001"), new Name("John Doe"),
                null, null, null, null, null, null, Collections.emptySet());
        List<Person> populatedProfiles = List.of(
                new PersonBuilder(empty).withPhone(PersonBuilder.DEFAULT_PHONE).build(),
                new PersonBuilder(empty).withEmail(PersonBuilder.DEFAULT_EMAIL).build(),
                new PersonBuilder(empty).withAddress(PersonBuilder.DEFAULT_ADDRESS).build(),
                new PersonBuilder(empty).withCardDetails(PersonBuilder.DEFAULT_CARD_NUMBER, PersonBuilder.DEFAULT_CVV,
                        PersonBuilder.DEFAULT_EXPIRY_DATE, PersonBuilder.DEFAULT_PROVIDER).build(),
                new PersonBuilder(empty).withCreditScore("0").build(),
                new PersonBuilder(empty).withDebt("0.00").build());

        for (Person populated : populatedProfiles) {
            assertFalse(empty.equals(populated));
            assertFalse(populated.equals(empty));
            assertTrue(empty.isSamePerson(populated));
            assertTrue(populated.isSamePerson(empty));
        }
    }

    @Test
    public void isSamePerson_incompleteProfiles_preservesReferenceAndLegacyIdentity() {
        Name name = new Name("John Doe");
        Person client = new Person(new ClientReference("C0001"), name,
                null, null, null, null, null, null, Collections.emptySet());
        Person sameReference = new PersonBuilder(client).withClientReference("c0001").withName("Jane Doe").build();
        Person otherReference = new PersonBuilder(client).withClientReference("C0002").build();
        Person legacy = new Person(name, null, null, null, Collections.emptySet());
        Person legacyWithPhone = new PersonBuilder(legacy).withPhone(PersonBuilder.DEFAULT_PHONE).build();

        assertTrue(client.isSamePerson(sameReference));
        assertTrue(sameReference.isSamePerson(client));
        assertFalse(client.isSamePerson(otherReference));
        assertFalse(otherReference.isSamePerson(client));
        assertFalse(client.isSamePerson(legacy));
        assertFalse(legacy.isSamePerson(client));
        assertTrue(legacy.isSamePerson(legacyWithPhone));
        assertTrue(legacyWithPhone.isSamePerson(legacy));
    }

    @Test
    public void getters_completeProfile_returnPresentOptionals() {
        Person person = new PersonBuilder().build();

        assertEquals(Optional.of(new Phone(PersonBuilder.DEFAULT_PHONE)), person.getPhone());
        assertEquals(Optional.of(new Email(PersonBuilder.DEFAULT_EMAIL)), person.getEmail());
        assertEquals(Optional.of(new Address(PersonBuilder.DEFAULT_ADDRESS)), person.getAddress());
        assertEquals(Optional.of(new CardDetails(PersonBuilder.DEFAULT_CARD_NUMBER, PersonBuilder.DEFAULT_CVV,
                PersonBuilder.DEFAULT_EXPIRY_DATE, PersonBuilder.DEFAULT_PROVIDER)), person.getCardDetails());
        assertEquals(Optional.of(new CreditScore(PersonBuilder.DEFAULT_CREDIT_SCORE)), person.getCreditScore());
        assertEquals(Optional.of(new Debt(PersonBuilder.DEFAULT_DEBT)), person.getDebt());
    }

    @Test
    public void getters_legacyProfile_returnPresentContactsAndEmptyCardAndFinancialOptionals() {
        Phone phone = new Phone(PersonBuilder.DEFAULT_PHONE);
        Email email = new Email(PersonBuilder.DEFAULT_EMAIL);
        Address address = new Address(PersonBuilder.DEFAULT_ADDRESS);
        Person person = new Person(new Name(PersonBuilder.DEFAULT_NAME), phone, email, address, Collections.emptySet());

        assertEquals(Optional.of(phone), person.getPhone());
        assertEquals(Optional.of(email), person.getEmail());
        assertEquals(Optional.of(address), person.getAddress());
        assertEquals(Optional.empty(), person.getCardDetails());
        assertEquals(Optional.empty(), person.getCreditScore());
        assertEquals(Optional.empty(), person.getDebt());
        assertEquals(person, new PersonBuilder(person).build());
    }

    @Test
    public void isSamePerson_referencedClients_usesOnlyNormalizedReference() {
        Person client = new PersonBuilder(ALICE).withClientReference("C0001").build();
        Person sameReference = new PersonBuilder(BOB).withClientReference("c0001").build();
        Person sameDetails = new PersonBuilder(client).withClientReference("C0002").build();
        assertTrue(client.isSamePerson(sameReference));
        assertTrue(sameReference.isSamePerson(client));
        assertFalse(client.isSamePerson(sameDetails));
        assertFalse(client.equals(sameDetails));
        assertFalse(client.isSamePerson(null));
    }

    @Test
    public void isSamePerson_legacyRecordAndReferencedClient_areDistinct() {
        Person client = new PersonBuilder(ALICE).withClientReference("C0001").build();
        assertFalse(client.isSamePerson(ALICE));
        assertFalse(ALICE.isSamePerson(client));
    }

    @Test
    public void equals_referencedClients_includesReferenceInEqualityAndHash() {
        Person client = new PersonBuilder(ALICE).withClientReference("C0001").build();
        Person sameClient = new PersonBuilder(client).withClientReference("c0001").build();
        assertEquals(client, sameClient);
        assertEquals(client.hashCode(), sameClient.hashCode());
        assertFalse(client.equals(new PersonBuilder(client).withClientReference("C0002").build()));
    }

    @Test
    public void timestamps_affectValueEqualityButNotIdentity() {
        Person original = new PersonBuilder(ALICE).withClientReference("C0001").build();
        Person stamped = original.withTimestamps(Instant.parse("2026-03-15T01:42:00Z"),
                Instant.parse("2026-10-12T06:18:00Z"));

        assertTrue(original.isSamePerson(stamped));
        assertFalse(original.equals(stamped));
        assertEquals(stamped, new PersonBuilder(stamped).build());
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different card details -> returns false
        editedAlice = new PersonBuilder(ALICE).withCardDetails("5555555555554444", "456", "11/29", "Mastercard")
                .build();
        assertFalse(ALICE.equals(editedAlice));

        editedAlice = new PersonBuilder(ALICE).withCreditScore("701").build();
        assertFalse(ALICE.equals(editedAlice));

        editedAlice = new PersonBuilder(ALICE).withDebt("1.00").build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName() + "{name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone().orElseThrow() + ", email=" + ALICE.getEmail().orElseThrow()
                + ", address=" + ALICE.getAddress().orElseThrow() + ", tags=" + ALICE.getTags() + "}";
        assertEquals(expected, ALICE.toString());
    }
    @Test
    public void constructor_copiesMutableTagSet() {
        Set<Tag> tags = new HashSet<>(Set.of(new Tag("friend")));
        Person person = new Person(new Name("Alice"), null, null, null, tags);
        Person copy = new PersonBuilder(person).build();
        int originalHash = person.hashCode();
        tags.clear();
        tags.add(new Tag("other"));
        assertEquals(Set.of(new Tag("friend")), person.getTags());
        assertEquals(copy, person);
        assertEquals(originalHash, person.hashCode());
    }

}
