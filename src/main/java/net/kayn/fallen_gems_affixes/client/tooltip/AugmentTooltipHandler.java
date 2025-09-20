package net.kayn.fallen_gems_affixes.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.kayn.fallen_gems_affixes.attachment.AugmentCapability;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AugmentTooltipHandler {

    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().isEmpty()) return;

        // Only add to items that have augment capability
        var capability = event.getItemStack().getCapability(AugmentCapability.CAPABILITY).resolve();
        if (capability.isPresent()) {
            event.getTooltipElements().add(Either.right(new AugmentTooltipComponent()));
        }
    }
}