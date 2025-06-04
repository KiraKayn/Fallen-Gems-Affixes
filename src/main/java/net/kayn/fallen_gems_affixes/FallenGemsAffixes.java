package net.kayn.fallen_gems_affixes;

import net.kayn.fallen_gems_affixes.attributes.AAAttributes;
import net.kayn.fallen_gems_affixes.attributes.MaxHealthDamageHandler;
import net.kayn.fallen_gems_affixes.init.loot.ModLootModifier;
import net.kayn.fallen_gems_affixes.loot.CelestialLootCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(FallenGemsAffixes.MOD_ID)
public class FallenGemsAffixes {
    public static final String MOD_ID = "fallen_gems_affixes";
    private static final Logger LOGGER = LogManager.getLogger();

    public FallenGemsAffixes(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Loading Fallen Gems & Affixes");

        CelestialLootCategory.CELESTIAL_WEAPONS.toString();

        AALootCategories.init();
        AAAttributes.ATTRIBUTES.register(modEventBus);
        new MaxHealthDamageHandler();



        ModLootModifier.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }


    public static ResourceLocation loc(String id) {
        return ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, id);
    }
}
