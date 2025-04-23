package dev.davey.apotheosis_ascended;

import dev.davey.apotheosis_ascended.attributes.AAAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(ApotheosisAscended.MODID)
public class ApotheosisAscended
{
    public static final String MODID = "apotheosis_ascended";
    private static final Logger LOGGER = LogManager.getLogger();
    public ApotheosisAscended(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ApotheosisAscended.LOGGER.info("Loading Apotheosis Ascended");

        AALootCategories.init();
        AAAttributes.ATTRIBUTES.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event) {}

    public static ResourceLocation loc(String id) {
        return ResourceLocation.fromNamespaceAndPath(ApotheosisAscended.MODID, id);
    }
}