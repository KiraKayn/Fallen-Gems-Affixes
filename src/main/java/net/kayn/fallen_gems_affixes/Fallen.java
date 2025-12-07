package net.kayn.fallen_gems_affixes;

import net.kayn.fallen_gems_affixes.attachment.AugmentRecipe;
import net.kayn.fallen_gems_affixes.attachment.AugmentRecipeSerializer;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Fallen {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, FallenGemsAffixes.MOD_ID);

    public static void bootstrap(IEventBus bus) {
        RecipeSerializers.bootstrap(bus);
        RecipeTypes.bootstrap(bus);
        Augments.bootstrap();
    }

    public static class RecipeTypes {
        public static final RegistryObject<RecipeType<AugmentRecipe>> ADD_AUGMENT =
                RECIPE_TYPES.register("add_augment", () -> new RecipeType<>() {
                    @Override
                    public String toString() {
                        return "fallen_gems_affixes:add_augment";
                    }
                });

        private static void bootstrap(IEventBus bus) {
            RECIPE_TYPES.register(bus);
        }
    }

    public static class RecipeSerializers {
        public static final RegistryObject<AugmentRecipeSerializer> ADD_AUGMENT =
                SERIALIZERS.register("add_augment", AugmentRecipeSerializer::new);

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

        public static void bootstrap() {
        }
    }

    public static class Augments {

        public static void bootstrap() {
            AugmentRegistry.loadAll();
        }
    }
}