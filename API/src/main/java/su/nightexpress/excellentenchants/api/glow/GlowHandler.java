package su.nightexpress.excellentenchants.api.glow;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Подсветка сущностей, видимая только одному игроку (пакетами, на сервере сущности не светятся).
 * Нужна «Чутью»: соседи не должны видеть, кого подсветили, а цели — светиться для всех.
 */
@NullMarked
public interface GlowHandler {

    void setup();

    void shutdown();

    /**
     * Подсветить цели для {@code viewer}. Прошлая подсветка этого игрока снимается.
     *
     * @param playerColor цвет для игроков; null — без команды, обычное белое свечение
     * @param mobColor    цвет для мобов; null — белое свечение
     * @return номер сеанса для {@link #hide(Player, long)}
     */
    long show(Player viewer, Collection<? extends Entity> targets, @Nullable NamedTextColor playerColor,
              @Nullable NamedTextColor mobColor);

    /** Снять подсветку, если сеанс {@code session} ещё текущий. */
    void hide(Player viewer, long session);

    /** Игрок вышел — забыть его сеанс без пакетов. */
    void forget(Player viewer);
}
