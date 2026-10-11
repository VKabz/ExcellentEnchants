package su.nightexpress.excellentenchants.tooltip.handler;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemEnchantments;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetCursorItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPlayerInventory;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;

import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import su.nightexpress.excellentenchants.api.tooltip.TooltipController;
import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;

public class PacketTooltipHandler implements TooltipHandler {

    private final TooltipController controller;

    private Listener listener;

    public PacketTooltipHandler(TooltipController controller) {
        this.controller = controller;
    }

    @Override
    public void setup() {
        if (this.listener == null) {
            this.listener = new Listener(this.controller);
            this.listener.register();
        }
    }

    @Override
    public void shutdown() {
        if (this.listener != null) {
            this.listener.unregister();
            this.listener = null;
        }
    }

    private static class Listener implements PacketListener {

        private final TooltipController controller;

        private PacketListenerCommon backend;

        public Listener(@NonNull TooltipController controller) {
            this.controller = controller;
        }

        public void register() {
            this.backend = PacketEvents.getAPI().getEventManager().registerListener(this,
                PacketListenerPriority.NORMAL);
        }

        public void unregister() {
            PacketEvents.getAPI().getEventManager().unregisterListener(this.backend);
        }

        @Override
        public void onPacketSend(PacketSendEvent event) {
            PacketTypeCommon type = event.getPacketType();
            Player player = event.getPlayer();
            if (player == null) return;
            if (!this.controller.isReadyForTooltipUpdate(player)) return;

            // Раньше каждый предмет каждого пакета инвентаря гонялся в Bukkit и обратно (полное кодирование
            // через NMS со всем вложенным содержимым), а пакет всегда перекодировался. Шалкеры и мешки
            // с тяжёлыми предметами забивали так сетевой поток на 100 %, и сидевших на нём игроков кикало
            // по keepalive (прод, 11.10.2026). Теперь в Bukkit идут только предметы с зачарованиями,
            // а пакет перекодируется, только если описание реально добавилось.
            boolean changed = false;
            switch (type) {
                case PacketType.Play.Server.SET_SLOT -> {
                    WrapperPlayServerSetSlot setSlot = new WrapperPlayServerSetSlot(event);

                    var decorated = this.decorate(setSlot.getItem());
                    if (decorated != null) {
                        setSlot.setItem(decorated);
                        changed = true;
                    }
                }
                case PacketType.Play.Server.WINDOW_ITEMS -> {
                    WrapperPlayServerWindowItems windowItems = new WrapperPlayServerWindowItems(event);

                    var items = windowItems.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        var decorated = this.decorate(items.get(i));
                        if (decorated != null) {
                            items.set(i, decorated);
                            changed = true;
                        }
                    }
                }
                case PacketType.Play.Server.SET_PLAYER_INVENTORY -> {
                    WrapperPlayServerSetPlayerInventory setPlayerInventory = new WrapperPlayServerSetPlayerInventory(event);

                    var decorated = this.decorate(setPlayerInventory.getStack());
                    if (decorated != null) {
                        setPlayerInventory.setStack(decorated);
                        changed = true;
                    }
                }
                case PacketType.Play.Server.SET_CURSOR_ITEM -> {
                    WrapperPlayServerSetCursorItem setCursorItem = new WrapperPlayServerSetCursorItem(event);

                    var decorated = this.decorate(setCursorItem.getStack());
                    if (decorated != null) {
                        setCursorItem.setStack(decorated);
                        changed = true;
                    }
                }
                case PacketType.Play.Server.MERCHANT_OFFERS -> {
                    WrapperPlayServerMerchantOffers offers = new WrapperPlayServerMerchantOffers(event);

                    for (var offer : offers.getMerchantOffers()) {
                        var decorated = this.decorate(offer.getOutputItem());
                        if (decorated != null) {
                            offer.setOutputItem(decorated);
                            changed = true;
                        }
                    }
                }
                default -> {
                    return;
                }
            }

            if (changed) event.markForReEncode(true);
        }

        /** Предмет с добавленным описанием зачарований или null, если менять нечего. */
        private com.github.retrooper.packetevents.protocol.item.@Nullable ItemStack decorate(
            com.github.retrooper.packetevents.protocol.item.@Nullable ItemStack pooperStack) {
            if (pooperStack == null || pooperStack.isEmpty() || !hasEnchantments(pooperStack)) return null;

            ItemStack bukkit = SpigotConversionUtil.toBukkitItemStack(pooperStack);
            ItemStack described = this.controller.addDescription(bukkit);
            if (described == bukkit) return null; // addDescription вернул исходник — описание не нужно

            return SpigotConversionUtil.fromBukkitItemStack(described);
        }

        private static boolean hasEnchantments(com.github.retrooper.packetevents.protocol.item.ItemStack stack) {
            return notEmpty(stack.getComponentOr(ComponentTypes.ENCHANTMENTS, null))
                || notEmpty(stack.getComponentOr(ComponentTypes.STORED_ENCHANTMENTS, null));
        }

        private static boolean notEmpty(@Nullable ItemEnchantments enchantments) {
            return enchantments != null && !enchantments.isEmpty();
        }
    }
}
