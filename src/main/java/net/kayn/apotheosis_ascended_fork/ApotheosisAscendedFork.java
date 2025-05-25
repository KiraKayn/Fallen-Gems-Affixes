package net.kayn.apotheosis_ascended_fork;

import net.kayn.apotheosis_ascended_fork.attributes.AAAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(ApotheosisAscendedFork.MOD_ID)
public class ApotheosisAscendedFork
{
    public static final String MOD_ID = "apotheosis_ascended_fork";
    private static final Logger LOGGER = LogManager.getLogger();
    public ApotheosisAscendedFork(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ApotheosisAscendedFork.LOGGER.info("Loading Apotheosis Ascended Fork");

        AALootCategories.init();
        AAAttributes.ATTRIBUTES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {}

    public static ResourceLocation loc(String id) {
        return ResourceLocation.fromNamespaceAndPath(ApotheosisAscendedFork.MOD_ID, id);
    }
}