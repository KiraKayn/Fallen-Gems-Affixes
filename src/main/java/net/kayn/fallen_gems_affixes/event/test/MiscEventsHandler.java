package net.kayn.fallen_gems_affixes.event.test;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.AugmentCapability;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber
public class MiscEventsHandler {
    private static final ResourceLocation ID = new ResourceLocation(FallenGemsAffixes.MOD_ID, "augment_cap");

    /**
     * This event is fired on both client and server
     */
    @SubscribeEvent
    public static void onDataAttachment(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(ID, new AugmentCapability(player));
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
}
