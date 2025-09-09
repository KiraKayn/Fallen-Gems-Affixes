package net.kayn.fallen_gems_affixes.event.test;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentCapability;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@Mod.EventBusSubscriber
public class MiscEventsHandler {
    /**
     * This event is fired on both client and server
     */
    @SubscribeEvent
    public static void onDataAttachment(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(AUGMENT_CAP_ID, new AugmentCapability(player));
        }
    }
    /**
     * This event is fired on both client and server
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        // check capability and access fields to see if everything works as intended
        Optional<IAugmentAccessor> capOpt = player.getCapability(AugmentCapability.CAPABILITY).resolve();
        if (capOpt.isPresent()) {
            IAugmentAccessor accessor = capOpt.get();
            IAugmentHandler handler = accessor.getHandler();
            IAugmentContainer container = accessor.getContainer();
        }
    }

    /**
     * This is the main logic to update player Augment data
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        ItemStack from = event.getFrom();
        ItemStack to = event.getTo();
    }

    /**
     * Rendering custom tooltip by augments
     */
    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.hasTag() && stack.getTag().contains(AUGMENT_DATA)) {
            CompoundTag tag = stack.getTag();
            ListTag listTag = tag.getCompound(AUGMENT_DATA).getList(AUGMENTS, Tag.TAG_COMPOUND);
            for (Tag tag1 : listTag) {
                if (((CompoundTag) tag1).getString(TYPE).equals(Fallen.Augments.SOUL_BOUND.getId().toString())) {
                    event.getToolTip().add(Component.literal("fallen_gems_affixes:test"));
                }
            }
        }
    }
}
