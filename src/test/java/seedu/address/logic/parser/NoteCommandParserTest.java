package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.NoteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientReference;
import seedu.address.model.person.ServiceNote;

public class NoteCommandParserTest {
    private final NoteCommandParser parser = new NoteCommandParser();

    @Test
    public void parse_validSetAndClear_normalizesWhitespaceAndReference() throws Exception {
        assertEquals(new NoteCommand(new ClientReference("C0001"), new ServiceNote("Hello there!")),
                parser.parse(" r/c0001 s/  Hello  there!  "));
        assertEquals(new NoteCommand(new ClientReference("C0001"), new ServiceNote("First Second")),
                parser.parse("r/  C0001 s/First\nSecond"));
        assertEquals(new NoteCommand(new ClientReference("C0001"), new ServiceNote("Morning")),
                parser.parse(" s/Morning r/c0001"));
        assertEquals(new NoteCommand(new ClientReference("C0001"), null), parser.parse(" r/C0001 s/  "));
    }

    @Test
    public void parse_invalidParameters_reportsSpecifiedOrder() {
        assertFailure(" s/Hello", NoteCommand.MESSAGE_MISSING_REFERENCE);
        assertFailure(" r/invalid! s/Hello", ClientReference.MESSAGE_CONSTRAINTS);
        assertFailure(" r/C0001 n/Alex s/Hello s/Again", "Unknown parameter prefix: n/.");
        assertFailure(" r/C0001 s/Discuss x/foo", "Unknown parameter prefix: x/.");
        assertFailure(" r/C0001 s/First s/Second",
                String.format(NoteCommand.MESSAGE_REPEATED_PREFIX, "s/"));
        assertFailure(" r/C0001 s/First r/C0002",
                String.format(NoteCommand.MESSAGE_REPEATED_PREFIX, "r/"));
        assertFailure(" unexpected r/C0001 s/First", NoteCommand.MESSAGE_INVALID_FORMAT);
        assertFailure(" r/C0001", NoteCommand.MESSAGE_MISSING_NOTE);
        assertFailure(" r/C0001 s/" + "a".repeat(201), ServiceNote.MESSAGE_CONSTRAINTS);
    }

    private void assertFailure(String arguments, String expectedMessage) {
        assertThrows(ParseException.class, expectedMessage, () -> parser.parse(arguments));
    }
}
