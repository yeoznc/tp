package seedu.address.storage;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;

/**
 * A class to access AddressBook data stored as a JSON file on the hard disk.
 */
public class JsonAddressBookStorage {

    private static final Logger logger = LogsCenter.getLogger(JsonAddressBookStorage.class);

    private Path filePath;

    public JsonAddressBookStorage(Path filePath) {
        this.filePath = filePath;
    }

    public Path getAddressBookFilePath() {
        return filePath;
    }

    /**
     * Returns AddressBook data as a {@link ReadOnlyAddressBook}.
     * Returns {@code Optional.empty()} if storage file is not found.
     *
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        return readAddressBook(filePath);
    }

    /**
     * Similar to {@link #readAddressBook()}.
     *
     * @param filePath location of the data. Cannot be null.
     * @throws DataLoadingException if loading the data from storage failed.
     */
    public Optional<ReadOnlyAddressBook> readAddressBook(Path filePath) throws DataLoadingException {
        requireNonNull(filePath);

        // Preserve field presence in the tree for validation before adapting it once.
        Optional<JsonNode> rawAddressBook = JsonUtil.readJsonFile(filePath, JsonNode.class);
        if (rawAddressBook.isEmpty()) {
            return Optional.empty();
        }
        try {
            for (JsonNode person : rawAddressBook.get().path("persons")) {
                JsonAdaptedPerson.validateServiceNoteField(person);
            }
            JsonSerializableAddressBook adapted = JsonUtil.fromJsonNode(rawAddressBook.get(),
                    JsonSerializableAddressBook.class);
            return Optional.of(adapted.toModelType());
        } catch (IllegalValueException ive) {
            logger.info("Illegal values found in " + filePath + ": " + ive.getMessage());
            throw new DataLoadingException(ive);
        } catch (IOException ioe) {
            throw new DataLoadingException(ioe);
        }
    }

    /**
     * Saves the given {@link ReadOnlyAddressBook} to the storage.
     * @param addressBook cannot be null.
     * @throws IOException if there was any problem writing to the file.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        saveAddressBook(addressBook, filePath);
    }

    /**
     * Similar to {@link #saveAddressBook(ReadOnlyAddressBook)}.
     *
     * @param filePath location of the data. Cannot be null.
     */
    public void saveAddressBook(ReadOnlyAddressBook addressBook, Path filePath) throws IOException {
        requireNonNull(addressBook);
        requireNonNull(filePath);

        Path absolutePath = filePath.toAbsolutePath();
        Path parent = absolutePath.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "addressbook-", ".tmp");
        try {
            Files.writeString(temporaryFile, JsonUtil.toJsonString(new JsonSerializableAddressBook(addressBook)),
                    StandardCharsets.UTF_8);
            Files.move(temporaryFile, absolutePath, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            throw new IOException("Atomic file replacement is unavailable for " + filePath, e);
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

}
