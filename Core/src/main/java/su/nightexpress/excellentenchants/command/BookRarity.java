package su.nightexpress.excellentenchants.command;

import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * Редкость книги по весу зачарования — та же шкала, что в каталоге и на звёздах в описании:
 * 10 — обычная, 5 — необычная, 2 — редкая, 1 — легендарная. Вес между ступенями округляется вниз
 * (7 — необычная), чтобы правка веса в yml не выкидывала книгу из всех редкостей.
 */
@NullMarked
public enum BookRarity {

    COMMON("common", 10),
    UNCOMMON("uncommon", 5),
    RARE("rare", 2),
    LEGENDARY("legendary", 1);

    private final String id;
    private final int    minWeight;

    BookRarity(String id, int minWeight) {
        this.id = id;
        this.minWeight = minWeight;
    }

    public String getId() {
        return this.id;
    }

    public static BookRarity ofWeight(int weight) {
        for (BookRarity rarity : values()) {
            if (weight >= rarity.minWeight) return rarity;
        }
        return LEGENDARY;
    }

    public static Optional<BookRarity> parse(String input) {
        String id = input.trim().toLowerCase();
        for (BookRarity rarity : values()) {
            if (rarity.id.equals(id) || rarity.name().equalsIgnoreCase(id)) return Optional.of(rarity);
        }
        return Optional.empty();
    }
}
