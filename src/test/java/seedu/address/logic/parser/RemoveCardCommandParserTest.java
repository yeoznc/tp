package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemoveCardCommand;
import seedu.address.model.person.ClientReference;

public class RemoveCardCommandParserTest {
    private final RemoveCardCommandParser parser = new RemoveCardCommandParser();

    @Test
    public void parse_validReference_normalizesToUppercase() {
        assertParseSuccess(parser, "  r/c0001  ", new RemoveCardCommand(new ClientReference("C0001")));
        assertParseSuccess(parser, "  r/  c0001  ", new RemoveCardCommand(new ClientReference("C0001")));
    }

    @Test
    public void parse_missingReference_reportsMissingReference() {
        assertParseFailure(parser, "", RemoveCardCommandParser.MESSAGE_MISSING_REFERENCE);
        assertParseFailure(parser, " r/ ", RemoveCardCommandParser.MESSAGE_MISSING_REFERENCE);
    }

    @Test
    public void parse_repeatedReference_reportsRepeatedParameter() {
        assertParseFailure(parser, "r/C0001 r/C0002", RemoveCardCommandParser.MESSAGE_REPEATED_PARAMETER);
    }

    @Test
    public void parse_extraTextOrUnknownPrefix_reportsInvalidCommand() {
        assertParseFailure(parser, "r/C0001 h/abcde", "Unknown parameter prefix: h/.");
        assertParseFailure(parser, "extra r/C0001", RemoveCardCommandParser.MESSAGE_INVALID_COMMAND);
        assertParseFailure(parser, "h/abcde", "Unknown parameter prefix: h/.");
    }

    @Test
    public void parse_invalidReference_usesSharedConstraint() {
        assertParseFailure(parser, "r/1BAD", ClientReference.MESSAGE_CONSTRAINTS);
    }
}
