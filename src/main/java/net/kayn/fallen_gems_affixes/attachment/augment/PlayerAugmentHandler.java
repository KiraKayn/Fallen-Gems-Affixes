package net.kayn.fallen_gems_affixes.attachment.augment;

import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

/**
 * When an augment affects the player, it creates a global effect
 * from the player's perspective.
 * <p>
 * For example, if GemPower affects the player rather than the item
 * it is attached to, the event will trigger the corresponding logic.
 * As a result, all gems on the client will appear boosted.
 */
@Mod.EventBusSubscriber
public class PlayerAugmentHandler {
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
     * This is the main logic to update player Augment data
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        // check capability and access fields to see if everything works as intended
        Optional<IAugmentAccessor> capOpt = player.getCapability(AugmentCapability.CAPABILITY).resolve();
        if (capOpt.isPresent()) {
            IAugmentAccessor accessor = capOpt.get();
            IAugmentHandler handler = accessor.getHandler();

            ItemStack from = event.getFrom();
            addOrRemoveAugment(from, handler, true);

            ItemStack to = event.getTo();
            addOrRemoveAugment(to, handler, false);
        }
    }

    public static void addOrRemoveAugment(ItemStack stack, IAugmentHandler handler, boolean isRemove) {
        // this method needs a rewrite, not now
    }


}
