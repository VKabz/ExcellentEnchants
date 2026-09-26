package su.nightexpress.excellentenchants.enchantment.gamai;

import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GamaiUtils {

    private GamaiUtils() {}

    /**
     * «Удар принят на щит»: жертва держит щит поднятым, а источник урона перед ней —
     * та же проверка, что в ванильном isDamageSourceBlocked (полусфера по взгляду).
     */
    public static boolean isBlockedByShield(LivingEntity victim, EntityDamageByEntityEvent event) {
        if (!isBlocking(victim)) return false;

        DamageSource source = event.getDamageSource();
        Entity direct = source.getDirectEntity();
        Location from = direct != null ? direct.getLocation() : source.getSourceLocation();
        if (from == null) return false;

        return isInFront(victim, from);
    }

    /** Щит есть только у людей; мобы isBlocking не умеют. */
    public static boolean isBlocking(LivingEntity entity) {
        return entity instanceof HumanEntity human && human.isBlocking();
    }

    public static boolean isInFront(LivingEntity entity, Location source) {
        Vector look = horizontal(entity.getLocation().getDirection());
        Vector to = horizontal(source.toVector().subtract(entity.getLocation().toVector()));
        if (look == null || to == null) return true;

        return look.dot(to) > 0D;
    }

    /** Атакующий за спиной жертвы (в конусе ~105° позади). */
    public static boolean isBehind(LivingEntity victim, LivingEntity attacker) {
        Vector look = horizontal(victim.getLocation().getDirection());
        Vector to = horizontal(attacker.getLocation().toVector().subtract(victim.getLocation().toVector()));
        if (look == null || to == null) return false;

        return look.dot(to) < -0.25D;
    }

    /** Горизонтальная нормаль вектора; null, если вектор вертикальный/нулевой. */
    @Nullable
    public static Vector horizontal(Vector vector) {
        Vector flat = vector.clone().setY(0);
        if (flat.lengthSquared() < 1.0E-6) return null;

        return flat.normalize();
    }

    /**
     * Ответный урон от щита. Тип THORNS выбран намеренно: EnchantListener пропускает его и для
     * атакующих, и для защитных зачарований, так что ответка не запускает цепочку «щит → меч → щит».
     */
    public static void retaliate(LivingEntity target, LivingEntity source, double amount) {
        if (amount <= 0D || target.isDead() || !target.isValid()) return;

        DamageSource damageSource = DamageSource.builder(DamageType.THORNS)
            .withCausingEntity(source)
            .withDirectEntity(source)
            .build();
        target.damage(amount, damageSource);
    }
}
