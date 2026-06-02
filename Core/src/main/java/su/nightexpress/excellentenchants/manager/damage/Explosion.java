package su.nightexpress.excellentenchants.manager.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@NullMarked
public class Explosion {

    private final LivingEntity owner;

    @Nullable
    private Consumer<EntityExplodeEvent>        onExplode;
    @Nullable
    private Consumer<EntityDamageByEntityEvent> onDamage;

    public Explosion(LivingEntity owner) {
        this.owner = owner;
    }

    public void handleExplosion(EntityExplodeEvent event) {
        if (this.onExplode != null) {
            this.onExplode.accept(event);
        }
    }

    public void handleDamage(EntityDamageByEntityEvent event) {
        if (this.onDamage != null) {
            this.onDamage.accept(event);
        }
    }


    public LivingEntity getOwner() {
        return this.owner;
    }

    public void setOnExplode(Consumer<EntityExplodeEvent> onExplode) {
        this.onExplode = onExplode;
    }

    public void setOnDamage(Consumer<EntityDamageByEntityEvent> onDamage) {
        this.onDamage = onDamage;
    }
}
