package seedu.address.model;

import java.time.Instant;
import java.util.Optional;

/** A writable snapshot whose changes can be saved before reaching the live model. */
public interface StagedModel extends Model {
    /** The kind of timestamp change made by a command. */
    enum TimestampAction { ADD, EDIT }

    /** Timestamp values to log after a successful save. */
    record TimestampChange(TimestampAction action, String reference,
            Instant createdAt, Instant lastModifiedAt) { }

    Optional<TimestampChange> getTimestampChange();
}
