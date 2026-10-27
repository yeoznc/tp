package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.ServiceNote;
import seedu.address.testutil.PersonBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_nullPerson_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("nullPerson.json");
        Files.writeString(filePath, "{\"persons\":[null]}");

        assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(filePath).readAddressBook());
    }

    @Test
    public void readAddressBook_nullTag_throwsDataLoadingException() throws Exception {
        Path filePath = testFolder.resolve("nullTag.json");
        AddressBook book = new AddressBook();
        book.addPerson(new PersonBuilder().withTags("friend").build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(book);
        String withNullTag = Files.readString(filePath).replaceFirst("\"tags\"\\s*:\\s*\\[", "\"tags\": [null,");
        Files.writeString(filePath, withNullTag);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_referencedClientsWithSameName_preservesReferences() throws Exception {
        Path filePath = testFolder.resolve("ReferencedAddressBook.json");
        AddressBook original = new AddressBook();
        Person first = new PersonBuilder().withName("Alex Yeoh").withClientReference("C0001").build();
        Person second = new PersonBuilder().withName("Alex Yeoh").withClientReference("C0002").build();
        original.addPerson(first);
        original.addPerson(second);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);

        assertEquals(original, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAndSaveAddressBook_clientWithServiceNote_preservesNote() throws Exception {
        Path filePath = testFolder.resolve("ServiceNoteAddressBook.json");
        Person base = new PersonBuilder().withClientReference("C0001").build();
        Person withNote = new Person(base.getClientReference(), base.getName(), base.getPhone(), base.getEmail(),
                base.getAddress(), base.getCardDetails(), base.getCreditScore(), base.getDebt(), base.getTags(),
                new ServiceNote("Called about renewal"));
        AddressBook original = new AddressBook();
        original.addPerson(withNote);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);

        assertEquals(original, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void readAddressBook_serviceNotePresence_distinguishesMissingFromNull() throws Exception {
        Path filePath = testFolder.resolve("notes.json");
        Person base = new PersonBuilder().withClientReference("C0001").build();
        AddressBook book = new AddressBook();
        book.addPerson(base);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(book);
        String json = Files.readString(filePath);
        assertEquals(book, storage.readAddressBook().orElseThrow());

        String withNull = json.replaceFirst("\"name\"\\s*:", "\"serviceNote\": null, \"name\":");
        Files.writeString(filePath, withNull);
        assertThrows(DataLoadingException.class, storage::readAddressBook);

        Files.writeString(filePath, json.replaceFirst("\"name\"\\s*:",
                "\"serviceNote\": 12, \"name\":"));
        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void saveAddressBook_replacementFails_keepsExistingTarget() throws Exception {
        Path target = testFolder.resolve("existing.json");
        Files.createDirectory(target);
        Path sentinel = target.resolve("sentinel.txt");
        Files.writeString(sentinel, "keep this");

        assertThrows(IOException.class, () -> new JsonAddressBookStorage(target).saveAddressBook(new AddressBook()));
        assertTrue(Files.isDirectory(target));
        assertEquals("keep this", Files.readString(sentinel));
        try (var files = Files.list(testFolder)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
