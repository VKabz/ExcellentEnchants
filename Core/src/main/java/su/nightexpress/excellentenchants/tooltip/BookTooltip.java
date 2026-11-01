package su.nightexpress.excellentenchants.tooltip;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

/** Packet-only title: anvil inputs never acquire a persistent custom_name. */
final class BookTooltip {
    private BookTooltip() {}

    static boolean shouldTitle(boolean book, boolean renamed, int storedCount, int customCount) {
        return book && !renamed && storedCount == 1 && customCount == 1;
    }

    static Component title(Component name, int level) {
        return Component.empty().decoration(TextDecoration.ITALIC, false).append(name)
            .append(Component.text(" " + roman(level), TextColor.color(0xD4D4D4)));
    }

    static String roman(int level) {
        int[] values = {100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] digits = {"C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < values.length; i++) while (level >= values[i]) {
            out.append(digits[i]);
            level -= values[i];
        }
        return out.toString();
    }
}
