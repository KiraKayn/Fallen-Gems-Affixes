package net.kayn.fallen_gems_affixes.client;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.AugmentModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onBakeModels(ModelEvent.BakingCompleted event) {
        event.getModels().forEach((resourceLocation, model) -> {
            if (resourceLocation.getNamespace().equals(FallenGemsAffixes.MOD_ID) &&
                    resourceLocation.getPath().startsWith("item/augments/")) {

                BakedModel wrappedModel = new AugmentModel(model);
                event.getModels().put(resourceLocation, wrappedModel);
            }
        });
    }
}