package net.kayn.fallen_gems_affixes;

import com.mojang.serialization.Codec;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2.PermanentEffectCapability;
import net.kayn.fallen_gems_affixes.attachment.permanent_effect_v2.PermanentEffectContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class Fallen {
    public static final DeferredHelper R = DeferredHelper.create(FallenGemsAffixes.MOD_ID);
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, FallenGemsAffixes.MOD_ID);


    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PermanentEffectContainer>> PE_CONTAINER =
            ATTACHMENT_TYPES.register("pe_container", () ->
                    AttachmentType.serializable(PermanentEffectContainer::new)
                            .copyOnDeath()
                            .build()
            );

    public static void bootstrap(IEventBus bus) {

        ATTACHMENT_TYPES.register(bus);
        Capabilities.bootstrap();
    }

    public static class Capabilities {
        public static final ResourceLocation LOC_PE_CAP = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "pe_capability");

        public static final EntityCapability<PermanentEffectCapability, Void> PE_CAP =
                EntityCapability.createVoid(LOC_PE_CAP, PermanentEffectCapability.class);

        public static void bootstrap() {};
    }
}
