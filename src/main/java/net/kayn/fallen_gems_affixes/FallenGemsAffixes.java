package net.kayn.fallen_gems_affixes;

import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveSpellPowerAffix;
import net.kayn.fallen_gems_affixes.attributes.AAAttributes;
import net.kayn.fallen_gems_affixes.attributes.MaxHealthDamageHandler;
import net.kayn.fallen_gems_affixes.compat.*;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.event.CelestisynthAttributeHandler;
import net.kayn.fallen_gems_affixes.event.InitNewCodecs;
import net.kayn.fallen_gems_affixes.event.SpellEventHandler;
import net.kayn.fallen_gems_affixes.init.loot.ModLootModifier;
import net.kayn.fallen_gems_affixes.loot.CelestialLootCategory;
import net.kayn.fallen_gems_affixes.loot.StaffLootCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(FallenGemsAffixes.MOD_ID)
public class FallenGemsAffixes {
    public static final String MOD_ID = "fallen_gems_affixes";
    public static final Logger LOGGER = LogManager.getLogger();

    public FallenGemsAffixes(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        LOGGER.info("Loading Fallen Gems & Affixes");

        context.registerConfig(Type.COMMON, ModConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(InitNewCodecs::init);

        ModLootModifier.LOOT_MODIFIERS.register(modEventBus);
        AAAttributes.ATTRIBUTES.register(modEventBus);

        AALootCategories.init();
        new MaxHealthDamageHandler();

        if (ModList.get().isLoaded("irons_spellbooks")) {
            StaffLootCategory.STAFF.toString();
            modEventBus.addListener(AdaptiveSpellPowerAffix::loadingIronsItemsFromConfig);
            MinecraftForge.EVENT_BUS.addListener(SpellEventHandler::onSpellHeal);
            MinecraftForge.EVENT_BUS.addListener(SpellEventHandler::onSpellDamage);
        }
        if (ModList.get().isLoaded("celestisynth")) {
            CelestialLootCategory.CELESTIAL_WEAPONS.toString();
            MinecraftForge.EVENT_BUS.register(SolarisSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(CrescentiaSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(BreezebreakerSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(KeresSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(AquafloraSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(PoltergeistSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(RainfallSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(FrostboundSpellPowerPatch.class);
            MinecraftForge.EVENT_BUS.register(CelestisynthAttributeHandler.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(FallenGemsAffixes.MOD_ID, path);
    }
}