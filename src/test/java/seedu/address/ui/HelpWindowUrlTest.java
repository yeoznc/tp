package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class HelpWindowUrlTest {
    @Test
    public void guideUrl_pointsToTeamDocumentation() {
        assertEquals("https://ay2627s1-cs2103t-f08-4.github.io/tp/UserGuide.html",
                HelpWindow.USERGUIDE_URL);
        assertEquals("Refer to the user guide: " + HelpWindow.USERGUIDE_URL, HelpWindow.HELP_MESSAGE);
    }
}
