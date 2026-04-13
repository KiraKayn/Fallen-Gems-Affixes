package net.kayn.fallen_gems_affixes;

import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRecipeSerializer;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.augment.*;
import net.kayn.fallen_gems_affixes.network.ClientLikeSyncAugmentPacket;
import net.kayn.fallen_gems_affixes.recipe.*;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.rtxyd.fallen.lib.runtime.forgemod.network.Connection;

public class Fallen {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    public static void bootstrap(IEventBus bus) {
        RecipeSerializers.bootstrap(bus);
        Augments.bootstrap();
        AugmentMisc.bootstrap();
        Registries.bootstrap();
    }

    public static class Registries {
        public static final AugmentRegistry AUGMENT_REGISTRY = new AugmentRegistry();
        public static void bootstrap() {
            Connection.registerRegistryBoundPacketPayloads(AUGMENT_REGISTRY, ClientLikeSyncAugmentPacket.BUF_CODEC,
                    ClientLikeSyncAugmentPacket.Begin.class,    ClientLikeSyncAugmentPacket.Begin::new,     ClientLikeSyncAugmentPacket.Begin::handle,
                    ClientLikeSyncAugmentPacket.class,          ClientLikeSyncAugmentPacket::new,           ClientLikeSyncAugmentPacket::handle,
                    ClientLikeSyncAugmentPacket.End.class,      ClientLikeSyncAugmentPacket.End::new,       ClientLikeSyncAugmentPacket.End::handle);

            for (IAugment augment : AUGMENT_REGISTRY.registryView().values()) {
                AUGMENT_REGISTRY.registerCodec(augment.getId(), augment.getMetaDataCodec());
            }
        }
    }

    public static class RecipeSerializers {
        public static final RegistryObject<AugmentRecipeSerializer> ADD_AUGMENT =
                SERIALIZERS.register("add_augment",
                        AugmentRecipeSerializer::new);

        public static final RegistryObject<RecipeSerializer<AugmentCraftingRecipe>> AUGMENT_CRAFTING =
                SERIALIZERS.register("augment_crafting",
                        AugmentCraftingRecipe.Serializer::new);

        public static final RegistryObject<RecipeSerializer<SocketConversionRecipe>> SOCKET_CONVERSION =
                SERIALIZERS.register("socket_conversion",
                        MiscUtil.simpleRecipeSerializer(SocketConversionRecipe::new));

        public static final RegistryObject<RecipeSerializer<TransmutationRecipe>> TRANSMUTATION =
                SERIALIZERS.register("transmutation",
                        MiscUtil.simpleRecipeSerializer(TransmutationRecipe::new));

        public static final RegistryObject<RecipeSerializer<SeveranceRecipe>> SEVERANCE =
                SERIALIZERS.register("severance",
                        MiscUtil.simpleRecipeSerializer(SeveranceRecipe::new));

        public static final RegistryObject<RecipeSerializer<ErasureRecipe>> ERASURE =
                SERIALIZERS.register("erasure",
                        MiscUtil.simpleRecipeSerializer(ErasureRecipe::new));

        private static void bootstrap(IEventBus bus) {
            SERIALIZERS.register(bus);
        }
    }

    public static class AugmentMisc {
        public static final ResourceLocation AUGMENT_CAP_ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "augment_cap");
        public static final String AUGMENT_CACHED_OBJECT = "fallen_gems_affixes:augments";
        public static final String AUGMENT_DATA = "fallen_gems_affixes:augment_data";
        public static final String AUGMENT_ID_TAG = "AugmentId";
        public static final String AUGMENT_SLOTS = "augment_slots";
        public static final String AUGMENTS = "augments";
        public static final String TYPE = "type";
        public static final String UNIQUE_ID = "uuid";
        public static final String INNER_DATA = "inner_data";
        public static final String CATEGORIES = "categories";

        public static void bootstrap() {
        }
    }

    public static class Augments {
        public static final IAugment GEM_POWER = Registries.AUGMENT_REGISTRY.register(new GemPowerAugment());
        public static final IAugment SUPREMACY = Registries.AUGMENT_REGISTRY.register(new SupremacyAugment());
        public static final IAugment GENESIS = Registries.AUGMENT_REGISTRY.register(new GenesisAugment());
        public static final IAugment CASCADE = Registries.AUGMENT_REGISTRY.register(new CascadeAugment());
        public static final IAugment DUALITY = Registries.AUGMENT_REGISTRY.register(new DualityAugment());
        public static final IAugment MALICE = Registries.AUGMENT_REGISTRY.register(new MaliceAugment());

        public static final String GEM_POWER_STRING = GEM_POWER.getId().toString();
        public static final String SUPREMACY_STRING = SUPREMACY.getId().toString();
        public static final String GENESIS_STRING = GENESIS.getId().toString();
        public static final String CASCADE_STRING = CASCADE.getId().toString();
        public static final String DUALITY_STRING = DUALITY.getId().toString();
        public static final String MALICE_STRING = MALICE.getId().toString();

        public static void bootstrap() {

        }
    }
}