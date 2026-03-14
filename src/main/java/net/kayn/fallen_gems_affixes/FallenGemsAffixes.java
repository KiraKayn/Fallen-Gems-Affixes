package net.kayn.fallen_gems_affixes;

import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveSpellPowerAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.CooldownResetAffix;
import net.kayn.fallen_gems_affixes.adventure.boss.UniversalBossEventHandler;
import net.kayn.fallen_gems_affixes.attributes.AAAttributes;
import net.kayn.fallen_gems_affixes.attributes.MaxHealthDamageHandler;
import net.kayn.fallen_gems_affixes.augment.DualityCritModifierHandler;
import net.kayn.fallen_gems_affixes.augment.GemBonusModifier;
import net.kayn.fallen_gems_affixes.augment.GenesisEventHandler;
import net.kayn.fallen_gems_affixes.commands.AugmentCommands;
import net.kayn.fallen_gems_affixes.compat.*;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.event.*;
import net.kayn.fallen_gems_affixes.init.loot.ModLootModifier;
import net.kayn.fallen_gems_affixes.loot.CelestialLootCategory;
import net.kayn.fallen_gems_affixes.loot.StaffLootCategory;
import net.kayn.fallen_gems_affixes.registry.ModCreativeTabs;
import net.kayn.fallen_gems_affixes.registry.ModItems;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
    public static boolean curiosLoaded = false;

    public FallenGemsAffixes(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        LOGGER.info("Loading Fallen Gems & Affixes");

        // Ensure required library is available
        try {
            isLibAvailable();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // Config & setup listeners
        context.registerConfig(Type.COMMON, ModConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(InitNewCodecs::init);

        // Registries
        ModLootModifier.LOOT_MODIFIERS.register(modEventBus);
        AAAttributes.ATTRIBUTES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        // Bootstraps
        Fallen.bootstrap(modEventBus);
        GemBonusModifier.bootstrap(MinecraftForge.EVENT_BUS);
        GenesisEventHandler.bootstrap(MinecraftForge.EVENT_BUS);
        AALootCategories.init();

        // Attributes / handlers
        new MaxHealthDamageHandler();

        // Event bus
        MinecraftForge.EVENT_BUS.register(SoulboundEventHandler.class);
        MinecraftForge.EVENT_BUS.register(FallenEventHandler.class);
        MinecraftForge.EVENT_BUS.register(BowEventHandler.class);
        MinecraftForge.EVENT_BUS.register(new HurtEventHandler());
        MinecraftForge.EVENT_BUS.register(new DualityCritModifierHandler());
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.register(UniversalBossEventHandler.class);
        MinecraftForge.EVENT_BUS.register(AffixScrollAnvilHandler.class);

        // Mod integrations
        curiosLoaded = ModList.get().isLoaded("curios");

        if (ModList.get().isLoaded("irons_spellbooks")) {
            StaffLootCategory.STAFF.toString();
            modEventBus.addListener(AdaptiveSpellPowerAffix::loadingIronsItemsFromConfig);
            MinecraftForge.EVENT_BUS.register(SpellEventHandler.class);
            MinecraftForge.EVENT_BUS.register(CooldownResetAffix.class);
        }

        if (ModList.get().isLoaded("celestisynth")) {
            CelestialLootCategory.CELESTIAL_MELEE.toString();
            CelestialLootCategory.CELESTIAL_RANGED.toString();
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

    private void isLibAvailable() throws ClassNotFoundException {
        try {
            Class.forName("net.rtxyd.fallen.lib.service.FallenBootstrap");
        } catch (ClassNotFoundException e) {
            final String libId = "fallen_lib";
            final String version = "1.2.0-hotfix";
            throw new ClassNotFoundException(MiscUtil.missingModMessage(MOD_ID, libId, version));
        }
    }

    public void commonSetup(final FMLCommonSetupEvent event) {
        // Any common setup logic
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        AugmentCommands.register(event.getDispatcher(), event.getBuildContext());
    }

    public static ResourceLocation id(@NotNull String path) {
        return new ResourceLocation(FallenGemsAffixes.MOD_ID, path);
    }
}