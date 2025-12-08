package net.kayn.fallen_gems_affixes.client;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.AugmentModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    private static final Logger LOGGER = LogManager.getLogger("ClientSetup");

    @SubscribeEvent
    public static void registerAugmentModels(ModelEvent.RegisterAdditional event) {
        Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager()
                .listResources("models", loc -> FallenGemsAffixes.MOD_ID.equals(loc.getNamespace())
                        && loc.getPath().contains("items/augments/")
                        && loc.getPath().endsWith(".json"))
                .keySet();

        for (ResourceLocation s : locs) {
            String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());

            ResourceLocation rl = new ResourceLocation(s.getNamespace(), path);
            ModelResourceLocation mrl = new ModelResourceLocation(rl, "inventory");
            event.register(mrl);
            LOGGER.debug("Registered augment model: {}", mrl);
        }
    }

    @SubscribeEvent
    public static void replaceAugmentModels(ModelEvent.ModifyBakingResult event) {
        event.getModels().forEach((resourceLocation, bakedModel) -> {
            if (resourceLocation.getNamespace().equals(FallenGemsAffixes.MOD_ID)
                    && resourceLocation.getPath().startsWith("items/augments/")
                    && resourceLocation instanceof ModelResourceLocation mrl
                    && mrl.getVariant().equals("inventory")) {

                BakedModel wrappedModel = new AugmentModel(bakedModel);
                event.getModels().put(resourceLocation, wrappedModel);
                LOGGER.debug("Wrapped augment model with AugmentModel: {}", resourceLocation);
            }
        });
    }
}