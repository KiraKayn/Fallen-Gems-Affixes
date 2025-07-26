package net.kayn.fallen_gems_affixes;

import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveSpellPowerAffix;
import net.kayn.fallen_gems_affixes.attributes.AAAttributes;
import net.kayn.fallen_gems_affixes.attributes.MaxHealthDamageHandler;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.event.InitNewCodecs;
import net.kayn.fallen_gems_affixes.event.SpellEventHandler;
import net.kayn.fallen_gems_affixes.init.loot.ModLootModifier;
import net.kayn.fallen_gems_affixes.loot.LootCategories;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

@Mod(FallenGemsAffixes.MOD_ID)
public class FallenGemsAffixes {
    public static final String MOD_ID = "fallen_gems_affixes";
    public static final DeferredHelper R = DeferredHelper.create(MOD_ID);
    public static final Logger LOGGER = LogManager.getLogger();

    public FallenGemsAffixes(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Loading Fallen Gems & Affixes");

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(InitNewCodecs::init);

        ModLootModifier.LOOT_MODIFIERS.register(modEventBus);
        AAAttributes.ATTRIBUTES.register(modEventBus);
        modEventBus.register(R);
        LootCategories.bootstrap();
//        AALootCategories.init();
        new MaxHealthDamageHandler();

        if (ModList.get().isLoaded("irons_spellbooks")) {
//            StaffLootCategory.STAFF.toString();
            modEventBus.addListener(AdaptiveSpellPowerAffix::loadingIronsItemsFromConfig);
            NeoForge.EVENT_BUS.addListener(SpellEventHandler::onSpellHeal);
            NeoForge.EVENT_BUS.addListener(SpellEventHandler::onSpellDamage);
        }
        if (ModList.get().isLoaded("celestisynth")) {
//            CelestialLootCategory.CELESTIAL_WEAPONS.toString();
//            NeoForge.EVENT_BUS.register(SolarisSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(CrescentiaSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(BreezebreakerSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(KeresSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(AquafloraSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(PoltergeistSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(RainfallSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(FrostboundSpellPowerPatch.class);
//            NeoForge.EVENT_BUS.register(CelestisynthAttributeHandler.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, path);
    }
}