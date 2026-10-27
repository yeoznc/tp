package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_REFERENCE;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.ServiceNote;

/** Parses the two single-valued parameters of the note command. */
public class NoteCommandParser implements Parser<NoteCommand> {
    private static final Prefix PREFIX_NOTE = new Prefix("s/");

    @Override
    public NoteCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeStrict(args, PREFIX_REFERENCE, PREFIX_NOTE);
        String referenceValue = arguments.getValue(PREFIX_REFERENCE).orElse("");
        if (referenceValue.isEmpty()) {
            throw new ParseException(NoteCommand.MESSAGE_MISSING_REFERENCE);
        }
        String[] referenceParts = referenceValue.split(" ", 2);
        ClientReference reference = ParserUtil.parseClientReference(referenceParts[0]);
        if (!arguments.getPreamble().isEmpty() || referenceParts.length > 1) {
            throw new ParseException(NoteCommand.MESSAGE_INVALID_FORMAT);
        }
        for (Prefix prefix : new Prefix[] { PREFIX_REFERENCE, PREFIX_NOTE }) {
            if (arguments.getAllValues(prefix).size() > 1) {
                throw new ParseException(String.format(NoteCommand.MESSAGE_REPEATED_PREFIX, prefix));
            }
        }
        String noteValue = arguments.getValue(PREFIX_NOTE)
                .orElseThrow(() -> new ParseException(NoteCommand.MESSAGE_MISSING_NOTE));
        if (!noteValue.isEmpty() && !ServiceNote.isValidServiceNote(noteValue)) {
            throw new ParseException(ServiceNote.MESSAGE_CONSTRAINTS);
        }
        return new NoteCommand(reference, noteValue.isEmpty() ? null : new ServiceNote(noteValue));
    }

}
