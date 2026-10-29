package su.nightexpress.excellentenchants.enchantment.gamai;

/**
 * Чистая математика новых книг — без Bukkit, чтобы её можно было проверить тестами.
 */
public final class GamaiMath {

    /** Гравитация и сопротивление воздуха игрока по вертикали (ванилла, блоков за тик). */
    static final double GRAVITY = 0.08D;
    static final double DRAG    = 0.98D;

    private GamaiMath() {}

    /**
     * Доля урона, которая проходит сквозь броню (ванильная формула Java Edition 1.21).
     * Зачарования защиты считаются отдельно, после брони.
     */
    public static double armorPass(double damage, double armor, double toughness) {
        if (armor <= 0D) return 1D;
        double reduction = Math.min(20D, Math.max(armor / 5D, armor - damage / (2D + toughness / 4D)));
        return clamp(1D - reduction / 25D, 0D, 1D);
    }

    /**
     * Во сколько раз поднять урон удара, чтобы после брони он был таким, будто брони у цели
     * на {@code ignoreShare} меньше. Множитель ограничен сверху, чтобы голая цель с огромной
     * бронёй от модов не получила бесконечный урон.
     */
    public static double armorPierceFactor(double damage, double armor, double toughness, double ignoreShare, double maxFactor) {
        if (damage <= 0D || armor <= 0D || ignoreShare <= 0D) return 1D;

        double full = armorPass(damage, armor, toughness);
        if (full <= 0D) return 1D;

        double pierced = armorPass(damage, armor * (1D - clamp(ignoreShare, 0D, 1D)), toughness);
        return clamp(pierced / full, 1D, maxFactor);
    }

    /** Урон после среза в процентах; срез ограничен 0..100. */
    public static double cutDamage(double damage, double cutPercent) {
        return Math.max(0D, damage * (1D - clamp(cutPercent, 0D, 100D) / 100D));
    }

    /** Длительность эффекта, укороченная на {@code percent} процентов. Не меньше 1 тика. */
    public static int shortenDuration(int ticks, double percent) {
        if (ticks <= 0) return ticks;

        int shortened = (int) (ticks * (1D - clamp(percent, 0D, 100D) / 100D));
        return Math.max(1, Math.min(ticks, shortened));
    }

    /**
     * Начальная вертикальная скорость (блоков за тик), с которой игрок поднимется на
     * {@code height} блоков. Подбирается по ванильной физике: v' = (v - g) * drag.
     */
    public static double launchVelocity(double height) {
        if (height <= 0D) return 0D;

        double low = 0D;
        double high = 5D;
        for (int i = 0; i < 40; i++) {
            double mid = (low + high) / 2D;
            if (apexHeight(mid) < height) low = mid;
            else high = mid;
        }
        return high;
    }

    /** Высота подъёма при начальной вертикальной скорости {@code velocity}. */
    public static double apexHeight(double velocity) {
        double y = 0D;
        double v = velocity;
        for (int tick = 0; tick < 400 && v > 0D; tick++) {
            y += v;
            v = (v - GRAVITY) * DRAG;
        }
        return y;
    }

    /** Сработал ли шанс: {@code roll} — случайное число 0..1, {@code chancePercent} — 0..100. */
    public static boolean rolled(double roll, double chancePercent) {
        return roll * 100D < chancePercent;
    }

    /** Множитель скорости копания: +percent % к ванильной скорости. */
    public static double breakSpeedBonus(double percent) {
        return Math.max(0D, percent) / 100D;
    }

    /** Значение для уровня из списка «по уровням»; уровни выше списка берут последнее значение. */
    public static double byLevel(int[] values, int level) {
        if (values.length == 0) return 0D;

        int index = Math.max(1, Math.min(level, values.length)) - 1;
        return values[index];
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
