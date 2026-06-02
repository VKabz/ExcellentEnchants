package su.nightexpress.excellentenchants.external;

import org.bukkit.entity.Entity;
import org.jspecify.annotations.NullMarked;

import io.lumine.mythic.bukkit.MythicBukkit;

@NullMarked
public class MythicMobsHook {

    private MythicMobsHook() {
    }

    private static final MythicBukkit MYTHIC_MOBS = MythicBukkit.inst();

    public static boolean isMythicMob(Entity entity) {
        return MYTHIC_MOBS.getAPIHelper().isMythicMob(entity);
    }
}
