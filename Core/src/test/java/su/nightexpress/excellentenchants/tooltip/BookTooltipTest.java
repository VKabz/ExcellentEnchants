package su.nightexpress.excellentenchants.tooltip;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookTooltipTest {
    @Test void onlySingleCustomUnrenamedBookGetsTitle() {
        assertTrue(BookTooltip.shouldTitle(true, false, 1, 1));
        assertFalse(BookTooltip.shouldTitle(true, true, 1, 1));
        assertFalse(BookTooltip.shouldTitle(true, false, 2, 1));
        assertFalse(BookTooltip.shouldTitle(true, false, 2, 2));
        assertFalse(BookTooltip.shouldTitle(true, false, 1, 0));
        assertFalse(BookTooltip.shouldTitle(false, false, 1, 1));
    }
    @Test void titleAlwaysHasCurrentRomanLevelEvenForMaxLevelOne() {
        Component name = MiniMessage.miniMessage().deserialize("<gradient:#FFD700:#FFA500>Бомбардировщик</gradient>");
        var plain = PlainTextComponentSerializer.plainText();
        assertEquals("Бомбардировщик I", plain.serialize(BookTooltip.title(name, 1)));
        assertEquals("Бомбардировщик III", plain.serialize(BookTooltip.title(name, 3)));
        assertEquals("CCLV", BookTooltip.roman(255));
    }
}
