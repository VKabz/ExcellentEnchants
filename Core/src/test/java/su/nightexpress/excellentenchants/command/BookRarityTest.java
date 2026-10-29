package su.nightexpress.excellentenchants.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookRarityTest {

    @Test
    void weightToRarity() {
        assertEquals(BookRarity.COMMON, BookRarity.ofWeight(10));
        assertEquals(BookRarity.COMMON, BookRarity.ofWeight(20));
        assertEquals(BookRarity.UNCOMMON, BookRarity.ofWeight(7));
        assertEquals(BookRarity.UNCOMMON, BookRarity.ofWeight(5));
        assertEquals(BookRarity.RARE, BookRarity.ofWeight(2));
        assertEquals(BookRarity.LEGENDARY, BookRarity.ofWeight(1));
        assertEquals(BookRarity.LEGENDARY, BookRarity.ofWeight(0));
    }

    @Test
    void parse() {
        assertEquals(BookRarity.LEGENDARY, BookRarity.parse("Legendary").orElseThrow());
        assertEquals(BookRarity.UNCOMMON, BookRarity.parse(" uncommon ").orElseThrow());
        assertTrue(BookRarity.parse("epic").isEmpty());
    }
}
