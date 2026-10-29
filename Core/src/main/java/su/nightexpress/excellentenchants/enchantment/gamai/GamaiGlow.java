package su.nightexpress.excellentenchants.enchantment.gamai;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.glow.GlowHandler;
import su.nightexpress.excellentenchants.glow.PacketGlowHandler;
import su.nightexpress.nightcore.util.Plugins;

import java.util.function.Supplier;

/**
 * Подсветка «только для одного игрока» для «Чутья». Работает через PacketEvents; без него
 * «Чутьё» молчит — серверное свечение видели бы все, а это уже не чутьё, а прожектор.
 */
@NullMarked
public final class GamaiGlow {

    private static final String PACKET_EVENTS = "packetevents";

    private static @Nullable GlowHandler handler;

    private GamaiGlow() {
    }

    public static void setup(EnchantsPlugin plugin) {
        if (handler != null) return;
        if (!Plugins.isInstalled(PACKET_EVENTS)) {
            plugin.warn("PacketEvents not found: the Instinct enchantment will not highlight anything.");
            return;
        }

        // Ссылка на метод, а не new: класс из модуля PacketEvents грузится только здесь.
        Supplier<GlowHandler> factory = PacketGlowHandler::new;
        handler = factory.get();
        handler.setup();
    }

    public static void shutdown() {
        if (handler != null) {
            handler.shutdown();
            handler = null;
        }
    }

    public static @Nullable GlowHandler get() {
        return handler;
    }
}
