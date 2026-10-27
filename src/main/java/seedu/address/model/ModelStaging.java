package seedu.address.model;

import java.time.Clock;

/** Creates an isolated writable snapshot of a model. */
public final class ModelStaging {
    private ModelStaging() { }

    public static StagedModel snapshot(Model source, Clock clock) {
        return new ModelManager(source.getAddressBook(), source.getUserPrefs(), clock);
    }
}
