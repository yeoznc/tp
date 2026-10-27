package seedu.address.storage;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.CardDetails;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.CreditScore;
import seedu.address.model.person.Debt;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.ServiceNote;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String INVALID_TIMESTAMP_MESSAGE = "Timestamp information is invalid for this profile.";
    public static final String MESSAGE_NULL_TAG = "Tags list contains a null tag.";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String clientReference;
    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final JsonAdaptedCardDetails cardDetails;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String creditScore;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String debt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String createdAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String lastModifiedAt;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final JsonNode serviceNote;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("clientReference") String clientReference,
            @JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("cardDetails") JsonAdaptedCardDetails cardDetails,
            @JsonProperty("creditScore") String creditScore, @JsonProperty("debt") String debt,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("lastModifiedAt") String lastModifiedAt,
            @JsonProperty("serviceNote") JsonNode serviceNote) {
        this.clientReference = clientReference;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cardDetails = cardDetails;
        this.creditScore = creditScore;
        this.debt = debt;
        this.serviceNote = serviceNote;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        if (tags != null) {
            this.tags.addAll(tags);
        }
    }

    /** Creates a person in the older JSON format without timestamps. */
    public JsonAdaptedPerson(String clientReference, String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags) {
        this(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags, null, null, null);
    }

    /** Creates a person with timestamps and no service note. */
    public JsonAdaptedPerson(String clientReference, String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags,
            String createdAt, String lastModifiedAt) {
        this(clientReference, name, phone, email, address, cardDetails, creditScore, debt, tags,
                createdAt, lastModifiedAt, null);
    }

    /** Creates an unreferenced person from the fields used by older data files. */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            JsonAdaptedCardDetails cardDetails, String creditScore, String debt, List<JsonAdaptedTag> tags) {
        this(null, name, phone, email, address, cardDetails, creditScore, debt, tags, null, null, null);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        clientReference = source.getClientReference() == null ? null : source.getClientReference().value;
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        cardDetails = source.getCardDetails() == null ? null : new JsonAdaptedCardDetails(source.getCardDetails());
        creditScore = source.getCreditScore() == null ? null : source.getCreditScore().toString();
        debt = source.getDebt() == null ? null : source.getDebt().toString();
        createdAt = source.getCreatedAt() == null ? null : source.getCreatedAt().toString();
        lastModifiedAt = source.getLastModifiedAt() == null ? null : source.getLastModifiedAt().toString();
        serviceNote = source.getServiceNote() == null ? null : TextNode.valueOf(source.getServiceNote().value);
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
    }

    /** Validates a field whose absence and explicit JSON null have different meanings. */
    static void validateServiceNoteField(JsonNode person) throws IllegalValueException {
        if (person.has("serviceNote") && !person.get("serviceNote").isTextual()) {
            throw new IllegalValueException(ServiceNote.MESSAGE_CONSTRAINTS);
        }
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        ServiceNote modelNote = toServiceNote();
        Instant modelCreatedAt = parseTimestamp(createdAt);
        Instant modelLastModifiedAt = parseTimestamp(lastModifiedAt);
        ClientReference modelReference = toClientReference();
        Set<Tag> modelTags = toTags();
        ContactFields contact = toContactFields();
        return toPerson(modelReference, contact, modelTags, modelNote, modelCreatedAt, modelLastModifiedAt);
    }

    private ServiceNote toServiceNote() throws IllegalValueException {
        // Jackson also supplies NullNode when an optional creator property is absent.
        // validateServiceNoteField rejects explicit JSON null before this conversion.
        if (serviceNote == null || serviceNote.isNull()) {
            return null;
        }
        if (!serviceNote.isTextual() || !ServiceNote.isValidServiceNote(serviceNote.textValue())) {
            throw new IllegalValueException(ServiceNote.MESSAGE_CONSTRAINTS);
        }
        return new ServiceNote(serviceNote.textValue());
    }

    private ClientReference toClientReference() throws IllegalValueException {
        if (clientReference == null) {
            return null;
        }
        if (!ClientReference.isValidClientReference(clientReference)) {
            throw new IllegalValueException(ClientReference.MESSAGE_CONSTRAINTS);
        }
        return new ClientReference(clientReference);
    }

    private Set<Tag> toTags() throws IllegalValueException {
        Set<Tag> personTags = new HashSet<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(MESSAGE_NULL_TAG);
            }
            personTags.add(tag.toModelType());
        }
        return personTags;
    }

    private ContactFields toContactFields() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        Address modelAddress = new Address(address);
        return new ContactFields(modelName, modelPhone, modelEmail, modelAddress);
    }

    private Person toPerson(ClientReference modelReference, ContactFields contact, Set<Tag> modelTags,
            ServiceNote modelNote, Instant modelCreatedAt, Instant modelLastModifiedAt) throws IllegalValueException {
        // Files saved before card and financial details were introduced remain valid.
        if (cardDetails == null && creditScore == null && debt == null) {
            if (modelReference == null) {
                return new Person(contact.name(), contact.phone(), contact.email(), contact.address(),
                        modelTags, modelNote)
                        .withTimestamps(modelCreatedAt, modelLastModifiedAt);
            }
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    CardDetails.class.getSimpleName()));
        }
        if (cardDetails == null && modelReference == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    CardDetails.class.getSimpleName()));
        }
        if (creditScore == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    CreditScore.class.getSimpleName()));
        }
        if (debt == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Debt.class.getSimpleName()));
        }
        try {
            if (modelReference != null) {
                return new Person(modelReference, contact.name(), contact.phone(), contact.email(), contact.address(),
                        cardDetails == null ? null : cardDetails.toModelType(),
                        new CreditScore(creditScore), new Debt(debt), modelTags, modelNote)
                        .withTimestamps(modelCreatedAt, modelLastModifiedAt);
            }
            return new Person(contact.name(), contact.phone(), contact.email(), contact.address(),
                    cardDetails.toModelType(),
                    new CreditScore(creditScore), new Debt(debt), modelTags, modelNote)
                    .withTimestamps(modelCreatedAt, modelLastModifiedAt);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    private record ContactFields(Name name, Phone phone, Email email, Address address) { }

    private static Instant parseTimestamp(String value) throws IllegalValueException {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalValueException(INVALID_TIMESTAMP_MESSAGE);
        }
    }

}
