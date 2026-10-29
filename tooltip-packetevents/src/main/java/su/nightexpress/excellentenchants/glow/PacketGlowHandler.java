package su.nightexpress.excellentenchants.glow;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import su.nightexpress.excellentenchants.api.glow.GlowHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Подсветка через PacketEvents: зрителю шлём метаданные цели с флагом свечения, а пока сеанс
 * идёт — дописываем флаг во все метаданные этих целей, которые сервер шлёт ему сам (присел,
 * загорелся, вернулся в прорисовку). Цвет — через фейковую команду, которую видит только зритель.
 *
 * Пакеты читаются в сетевых потоках, поэтому сеансы лежат в ConcurrentHashMap и не меняются.
 */
@NullMarked
public class PacketGlowHandler implements GlowHandler, PacketListener {

    private static final byte FLAG_FIRE      = 0x01;
    private static final byte FLAG_SNEAK     = 0x02;
    private static final byte FLAG_SPRINT    = 0x08;
    private static final byte FLAG_SWIM      = 0x10;
    private static final byte FLAG_INVISIBLE = 0x20;
    private static final byte FLAG_GLOW      = 0x40;
    private static final byte FLAG_GLIDE     = (byte) 0x80;

    private static final String TEAM_PLAYERS = "ee_glow_players";
    private static final String TEAM_MOBS    = "ee_glow_mobs";

    private record Session(long id, Set<Integer> entityIds, List<Entity> entities, List<String> teams) {}

    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();
    private final AtomicLong         counter  = new AtomicLong();

    private @Nullable PacketListenerCommon backend;

    @Override
    public void setup() {
        if (this.backend == null) {
            this.backend = PacketEvents.getAPI().getEventManager().registerListener(this, PacketListenerPriority.HIGH);
        }
    }

    @Override
    public void shutdown() {
        if (this.backend != null) {
            PacketEvents.getAPI().getEventManager().unregisterListener(this.backend);
            this.backend = null;
        }
        this.sessions.clear();
    }

    @Override
    public long show(Player viewer, Collection<? extends Entity> targets, @Nullable NamedTextColor playerColor,
                     @Nullable NamedTextColor mobColor) {
        Session previous = this.sessions.get(viewer.getUniqueId());
        if (previous != null) this.hide(viewer, previous.id());

        Set<Integer> ids = new HashSet<>();
        List<String> players = new ArrayList<>();
        List<String> mobs = new ArrayList<>();
        for (Entity target : targets) {
            ids.add(target.getEntityId());
            if (target instanceof Player player) players.add(player.getName());
            else mobs.add(target.getUniqueId().toString());
        }

        List<String> teams = new ArrayList<>();
        if (playerColor != null && !players.isEmpty()) {
            send(viewer, team(TEAM_PLAYERS, playerColor, players));
            teams.add(TEAM_PLAYERS);
        }
        if (mobColor != null && !mobs.isEmpty()) {
            send(viewer, team(TEAM_MOBS, mobColor, mobs));
            teams.add(TEAM_MOBS);
        }

        long id = this.counter.incrementAndGet();
        this.sessions.put(viewer.getUniqueId(), new Session(id, Set.copyOf(ids), List.copyOf(targets), List.copyOf(teams)));

        for (Entity target : targets) {
            send(viewer, metadata(target.getEntityId(), (byte) (flags(target) | FLAG_GLOW)));
        }
        return id;
    }

    @Override
    public void hide(Player viewer, long session) {
        Session current = this.sessions.get(viewer.getUniqueId());
        if (current == null || current.id() != session) return;

        this.sessions.remove(viewer.getUniqueId());
        if (!viewer.isOnline()) return;

        for (Entity target : current.entities()) {
            if (!target.isValid()) continue;
            send(viewer, metadata(target.getEntityId(), flags(target)));
        }
        for (String team : current.teams()) {
            send(viewer, new WrapperPlayServerTeams(team, WrapperPlayServerTeams.TeamMode.REMOVE,
                Optional.<WrapperPlayServerTeams.ScoreBoardTeamInfo>empty(), List.<String>of()));
        }
    }

    @Override
    public void forget(Player viewer) {
        this.sessions.remove(viewer.getUniqueId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.ENTITY_METADATA) return;
        if (this.sessions.isEmpty()) return;

        UUID viewer = event.getUser().getUUID();
        if (viewer == null) return;

        Session session = this.sessions.get(viewer);
        if (session == null) return;

        WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(event);
        if (!session.entityIds().contains(wrapper.getEntityId())) return;

        for (EntityData<?> data : wrapper.getEntityMetadata()) {
            if (data.getIndex() != 0 || !(data.getValue() instanceof Byte flags)) continue;
            if ((flags & FLAG_GLOW) != 0) return;

            ((EntityData<Byte>) data).setValue((byte) (flags | FLAG_GLOW));
            event.markForReEncode(true);
            return;
        }
    }

    /** Флаги сущности так, как их видит сервер; свечение — только если она светится сама. */
    private static byte flags(Entity entity) {
        byte flags = 0;
        if (entity.getFireTicks() > 0) flags |= FLAG_FIRE;
        if (entity.isSneaking()) flags |= FLAG_SNEAK;
        if (entity instanceof Player player && player.isSprinting()) flags |= FLAG_SPRINT;
        if (entity instanceof LivingEntity living) {
            if (living.isSwimming()) flags |= FLAG_SWIM;
            if (living.isInvisible()) flags |= FLAG_INVISIBLE;
            if (living.isGliding()) flags |= FLAG_GLIDE;
        }
        if (entity.isGlowing()) flags |= FLAG_GLOW;
        return flags;
    }

    private static WrapperPlayServerEntityMetadata metadata(int entityId, byte flags) {
        return new WrapperPlayServerEntityMetadata(entityId, List.of(new EntityData<>(0, EntityDataTypes.BYTE, flags)));
    }

    private static WrapperPlayServerTeams team(String name, NamedTextColor color, Collection<String> entries) {
        WrapperPlayServerTeams.ScoreBoardTeamInfo info = new WrapperPlayServerTeams.ScoreBoardTeamInfo(
            Component.empty(), Component.empty(), Component.empty(),
            WrapperPlayServerTeams.NameTagVisibility.ALWAYS,
            WrapperPlayServerTeams.CollisionRule.ALWAYS,
            color,
            WrapperPlayServerTeams.OptionData.NONE
        );
        return new WrapperPlayServerTeams(name, WrapperPlayServerTeams.TeamMode.CREATE, info, entries);
    }

    private static void send(Player viewer, PacketWrapper<?> packet) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(viewer, packet);
    }
}
