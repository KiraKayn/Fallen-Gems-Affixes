package net.kayn.fallen_gems_affixes;

import com.google.gson.JsonObject;
import net.kayn.fallen_gems_affixes.attachment.AugmentRecipeSerializer;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.augment.GemPowerAugment;
import net.kayn.fallen_gems_affixes.augment.SoulboundAugment;
import net.kayn.fallen_gems_affixes.augment.SupremacyAugment;
import net.kayn.fallen_gems_affixes.recipe.SeveranceRecipe;
import net.kayn.fallen_gems_affixes.recipe.SocketConversionRecipe;
import net.kayn.fallen_gems_affixes.recipe.TransmutationRecipe;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class Fallen {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    public static void bootstrap(IEventBus bus) {
        RecipeSerializers.bootstrap(bus);
        Augments.bootstrap();
        AugmentMisc.bootstrap();
    }

    public static class RecipeSerializers {
        public static final RegistryObject<AugmentRecipeSerializer> ADD_AUGMENT =
                SERIALIZERS.register("add_augment", AugmentRecipeSerializer::new);

        public static final RegistryObject<RecipeSerializer<SocketConversionRecipe>> SOCKET_CONVERSION =
                SERIALIZERS.register("socket_conversion", () -> new RecipeSerializer<>() {
                    @Override
                    public @NotNull SocketConversionRecipe fromJson(ResourceLocation id, JsonObject json) {
                        return new SocketConversionRecipe();
                    }

                    @Override
                    public SocketConversionRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                        return new SocketConversionRecipe();
                    }

                    @Override
                    public void toNetwork(FriendlyByteBuf buf, SocketConversionRecipe recipe) {
                    }
                });

        public static final RegistryObject<RecipeSerializer<TransmutationRecipe>> TRANSMUTATION =
                SERIALIZERS.register("transmutation", () -> new RecipeSerializer<>() {
                    @Override
                    public @NotNull TransmutationRecipe fromJson(ResourceLocation id, JsonObject json) {
                        return new TransmutationRecipe();
                    }

                    @Override
                    public TransmutationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                        return new TransmutationRecipe();
                    }

                    @Override
                    public void toNetwork(FriendlyByteBuf buf, TransmutationRecipe recipe) {
                    }
                });

        public static final RegistryObject<RecipeSerializer<SeveranceRecipe>> SEVERANCE =
                SERIALIZERS.register("severance", () -> new RecipeSerializer<>() {
                    @Override
                    public @NotNull SeveranceRecipe fromJson(ResourceLocation id, JsonObject json) {
                        return new SeveranceRecipe();
                    }

                    @Override
                    public SeveranceRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
                        return new SeveranceRecipe();
                    }

                    @Override
                    public void toNetwork(FriendlyByteBuf buf, SeveranceRecipe recipe) {}
                });

        private static void bootstrap(IEventBus bus) {
            SERIALIZERS.register(bus);
        }
    }

    public static class AugmentMisc {
        public static final ResourceLocation AUGMENT_CAP_ID = new ResourceLocation(FallenGemsAffixes.MOD_ID, "augment_cap");
        public static final String AUGMENT_DATA = "fallen_gems_affixes:augment_data";
        public static final String AUGMENTS = "augments";
        public static final String TYPE = "type";
        public static final String UNIQUE_ID = "uuid";
        public static final String INNER_DATA = "inner_data";
        public static final String CATEGORIES = "categories";
        public static void bootstrap() {}
    }

    public static class Augments {
        public static final IAugment SOUL_BOUND = AugmentRegistry.register(new SoulboundAugment());
        public static final IAugment GEM_POWER = AugmentRegistry.register(new GemPowerAugment());
        public static final IAugment SUPREMACY = AugmentRegistry.register(new SupremacyAugment());

        public static final String SOUL_BOUND_STRING = SoulboundAugment.augmentId().toString();
        public static final String GEM_POWER_STRING = GemPowerAugment.augmentId().toString();
        public static final String SUPREMACY_STRING = SupremacyAugment.augmentId().toString();
        public static void bootstrap() {}
    }
}