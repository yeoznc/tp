package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.ServiceNote;
import seedu.address.testutil.PersonBuilder;

/** Verifies the complete model/JSON/model boundary, including invalid data in otherwise valid books. */
public class ChangedFieldsStorageIntegrationTest {
    private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant MODIFIED = Instant.parse("2026-02-01T00:00:00Z");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndReload_mixedProfiles_preservesAllFieldsAndMissingValues() throws Exception {
        Person complete = completeProfile();
        Person minimal = new Person(new ClientReference("C2"), new Name("Minimal"),
                null, null, null, null, null, null, Set.of());
        Person legacy = new Person(new Name("Legacy"), null, null, null, Set.of());
        AddressBook book = new AddressBook();
        book.setPersons(List.of(complete, minimal, legacy));
        Path path = temporaryFolder.resolve("mixed.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        storage.saveAddressBook(book);

        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
        assertEquals(book, loaded);
        assertEquals(book.hashCode(), loaded.hashCode());
        for (int index = 0; index < book.getPersonList().size(); index++) {
            Person before = book.getPersonList().get(index);
            Person after = loaded.getPersonList().get(index);
            assertEquals(before, after);
            assertEquals(before.hashCode(), after.hashCode());
            assertTrue(before.isSamePerson(after));
        }
        JsonNode persons = JsonUtil.readJsonFile(path, JsonNode.class).orElseThrow().get("persons");
        assertEquals("0000  1234", persons.get(0).get("cardDetails").get("cardNumber").asText());
        assertEquals("001", persons.get(0).get("cardDetails").get("cvv").asText());
        assertEquals(complete.getServiceNote().orElseThrow().value, persons.get(0).get("serviceNote").asText());
        assertEquals(CREATED.toString(), persons.get(0).get("createdAt").asText());
        assertEquals(MODIFIED.toString(), persons.get(0).get("lastModifiedAt").asText());
        for (String absent : List.of("phone", "email", "address", "cardDetails", "creditScore", "debt",
                "createdAt", "lastModifiedAt", "serviceNote")) {
            assertFalse(persons.get(1).has(absent), absent);
            assertFalse(persons.get(2).has(absent), absent);
        }
        assertFalse(persons.get(2).has("clientReference"));
        assertTrue(persons.get(1).get("tags").isArray());
        assertEquals(0, persons.get(1).get("tags").size());
        storage.saveAddressBook(loaded);
        assertEquals(book, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void read_invalidChangedField_rejectsEntireBookWithoutRewritingFile() throws Exception {
        List<Consumer<ObjectNode>> corruptions = List.of(
                node -> node.put("phone", "12"),
                node -> node.put("email", "a@b.c"),
                node -> node.put("address", "A".repeat(201)),
                node -> node.put("creditScore", "-1"),
                node -> node.put("debt", "1.234"),
                node -> ((ObjectNode) node.get("cardDetails")).remove("cvv"),
                node -> ((ObjectNode) node.get("cardDetails")).put("expiryDate", "13/28"),
                node -> node.put("createdAt", "not-an-instant"),
                node -> node.put("lastModifiedAt", "not-an-instant"),
                node -> node.put("serviceNote", "A".repeat(201)),
                node -> node.put("serviceNote", 123),
                node -> node.putArray("tags").add("invalid tag"),
                node -> node.putArray("tags").addNull());
        Path path = temporaryFolder.resolve("invalid.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(path);
        for (int index = 0; index < corruptions.size(); index++) {
            // A valid leading record must not cause a partial load of the invalid second record.
            ObjectNode root = validBookJson();
            corruptions.get(index).accept((ObjectNode) root.get("persons").get(1));
            Files.writeString(path, JsonUtil.toJsonString(root));
            byte[] original = Files.readAllBytes(path);
            DataLoadingException failure = assertThrows(DataLoadingException.class,
                    storage::readAddressBook, "corruption " + index);
            assertNotNull(failure.getCause());
            assertArrayEquals(original, Files.readAllBytes(path), "corruption " + index);
        }
    }

    @Test
    public void read_normalizedDuplicateReferencesWithDifferentDetails_rejectsEntireBook() throws Exception {
        ObjectNode root = validBookJson();
        ObjectNode duplicate = (ObjectNode) root.get("persons").get(1);
        duplicate.put("clientReference", "c2");
        Path path = temporaryFolder.resolve("duplicate.json");
        Files.writeString(path, JsonUtil.toJsonString(root));
        byte[] original = Files.readAllBytes(path);
        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(path).readAddressBook());
        assertArrayEquals(original, Files.readAllBytes(path));
    }

    private ObjectNode validBookJson() throws Exception {
        AddressBook book = new AddressBook();
        Person minimal = new Person(new ClientReference("C2"), new Name("Other Client"),
                null, null, null, null, null, null, Set.of());
        book.setPersons(List.of(minimal, completeProfile()));
        ObjectNode root = (ObjectNode) JsonUtil.fromJsonString(
                JsonUtil.toJsonString(new JsonSerializableAddressBook(book)), JsonNode.class);
        assertEquals(2, ((ArrayNode) root.get("persons")).size());
        return root;
    }

    private Person completeProfile() {
        return new PersonBuilder().withClientReference("C1").withName("\u674e Alex O'Connor")
                .withAddress("\u6771\u4eac #01-02, \"Main\" \\ Road")
                .withCardDetails("0000  1234", "001", "01/00", "Some Provider")
                .withCreditScore("0").withDebt("0.01").withTags("friend", "VIP1")
                .build().withServiceNote(new ServiceNote("Call \"Alex\" \\ desk n/next"))
                .withTimestamps(CREATED, MODIFIED);
    }
}
