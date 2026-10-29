package su.nightexpress.excellentenchants.enchantment.gamai;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GamaiMathTest {

    private static final double EPS = 1e-6;

    @Test
    void armorPassFollowsVanillaFormula() {
        // 20 брони, 8 твёрдости, удар 10: 20 - 10 / 4 = 17.5 → проходит 30 %.
        assertEquals(0.3D, GamaiMath.armorPass(10, 20, 8), EPS);
        // Броня сверх 20 очков срезает не больше 80 %.
        assertEquals(0.2D, GamaiMath.armorPass(1, 30, 20), EPS);
        assertEquals(1D, GamaiMath.armorPass(10, 0, 0), EPS);
    }

    @Test
    void dawnStrikeFactorAgainstNetherite() {
        // Фулл незерит: 20 брони, 12 твёрдости. 30 % брони в обход → удар в ~1.86 раза сильнее.
        double factor = GamaiMath.armorPierceFactor(10, 20, 12, 0.3, 3);
        assertEquals(0.52D / 0.28D, factor, EPS);
        assertTrue(factor > 1D && factor <= 3D);
    }

    @Test
    void dawnStrikeFactorIsClampedAndNeutralWithoutArmor() {
        assertEquals(1D, GamaiMath.armorPierceFactor(10, 0, 0, 0.3, 3), EPS);
        assertEquals(1D, GamaiMath.armorPierceFactor(10, 20, 8, 0, 3), EPS);
        assertEquals(1.5D, GamaiMath.armorPierceFactor(10, 20, 8, 0.3, 1.5), EPS);
    }

    @Test
    void cutDamage() {
        assertEquals(8.4D, GamaiMath.cutDamage(10, 16), EPS);
        assertEquals(10D, GamaiMath.cutDamage(10, -5), EPS);
        assertEquals(0D, GamaiMath.cutDamage(10, 150), EPS);
    }

    @Test
    void shortenDuration() {
        assertEquals(120, GamaiMath.shortenDuration(200, 40));
        assertEquals(170, GamaiMath.shortenDuration(200, 15));
        assertEquals(1, GamaiMath.shortenDuration(1, 40));
        assertEquals(1, GamaiMath.shortenDuration(100, 100));
        assertEquals(-1, GamaiMath.shortenDuration(-1, 40)); // Бесконечный эффект не трогаем.
    }

    @Test
    void launchReachesRequestedHeight() {
        for (int height : new int[]{3, 4, 5}) {
            double velocity = GamaiMath.launchVelocity(height);
            assertEquals(height, GamaiMath.apexHeight(velocity), 1e-3, "height " + height);
        }
        assertEquals(0D, GamaiMath.launchVelocity(0), EPS);
        // Обычный прыжок (0.42) — около 1.25 блока: проверка, что физика совпадает с ванильной.
        assertEquals(1.25D, GamaiMath.apexHeight(0.42D), 0.01D);
    }

    @Test
    void rolledChance() {
        assertTrue(GamaiMath.rolled(0.1499, 15));
        assertFalse(GamaiMath.rolled(0.15, 15));
        assertFalse(GamaiMath.rolled(0, 0));
        assertTrue(GamaiMath.rolled(0.999, 100));
    }

    @Test
    void byLevelClampsToList() {
        int[] values = {15, 25, 40};
        assertEquals(15D, GamaiMath.byLevel(values, 1), EPS);
        assertEquals(40D, GamaiMath.byLevel(values, 3), EPS);
        assertEquals(40D, GamaiMath.byLevel(values, 5), EPS);
        assertEquals(15D, GamaiMath.byLevel(values, 0), EPS);
        assertEquals(0D, GamaiMath.byLevel(new int[0], 2), EPS);
    }

    @Test
    void breakSpeedBonus() {
        assertEquals(0.5D, GamaiMath.breakSpeedBonus(50), EPS);
        assertEquals(0D, GamaiMath.breakSpeedBonus(-10), EPS);
    }
}
