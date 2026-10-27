package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;
import seedu.address.model.SortOrder;

public class SortCommandParserTest {
    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_whitespaceAndUnknownPrefix() {
        assertParseSuccess(parser, " \n desc\t ", new SortCommand(SortOrder.DESC));
        assertParseFailure(parser, "desc x/foo", "Unknown parameter prefix: x/.");
    }
}
