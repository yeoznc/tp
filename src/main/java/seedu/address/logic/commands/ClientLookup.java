package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.ClientReferenceNotFoundException;
import seedu.address.model.Model;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.Person;

/** Resolves a referenced client while letting each command retain its own error. */
final class ClientLookup {
    private ClientLookup() { }

    static Person requireClient(Model model, ClientReference reference) throws ClientReferenceNotFoundException {
        requireNonNull(model);
        requireNonNull(reference);
        return model.findPersonByReference(reference)
                .orElseThrow(() -> new ClientReferenceNotFoundException(reference));
    }
}
