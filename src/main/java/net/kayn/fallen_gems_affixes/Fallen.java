package net.kayn.fallen_gems_affixes;

import net.kayn.fallen_gems_affixes.attachment.AugmentRecipeSerializer;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.augment.GemPowerAugment;
import net.kayn.fallen_gems_affixes.augment.SoulboundAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Fallen {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    public static void bootstrap(IEventBus bus) {
        RecipeSerializers.bootstrap(bus);
    }
    public static class RecipeSerializers {
        public static final RegistryObject<AugmentRecipeSerializer> ADD_AUGMENT =
                SERIALIZERS.register("add_augment", AugmentRecipeSerializer::new);

        private static void bootstrap(IEventBus bus) {
            SERIALIZERS.register(bus);
            Augments.bootstrap();
            AugmentMisc.bootstrap();
        }
    }
    public static class AugmentMisc {
        public static final ResourceLocation AUGMENT_CAP_ID = new ResourceLocation(FallenGemsAffixes.MOD_ID, "augment_cap");
        // This is the root node.
        public static final String AUGMENT_DATA = "fallen_gems_affixes:augment_data";
        // The following are nodes inside root node, so without namespace.
        public static final String AUGMENTS = "augments";
        public static final String TYPE = "type";
        public static final String UNIQUE_ID = "uuid";
        public static final String INNER_DATA = "inner_data";
        public static void bootstrap() {}
    }

    public static class Augments {
        public static final IAugment SOUL_BOUND = AugmentRegistry.register(new SoulboundAugment());
        public static final IAugment GEM_POWER = AugmentRegistry.register(new GemPowerAugment());
        public static void bootstrap() {}
    }
}
