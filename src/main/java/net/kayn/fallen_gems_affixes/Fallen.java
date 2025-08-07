package net.kayn.fallen_gems_affixes;

import com.mojang.serialization.Codec;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.placebo.registry.DeferredHelper;
import net.kayn.fallen_gems_affixes.component.PermanentEffectRoot;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Fallen {
    public static final DeferredHelper R = DeferredHelper.create(FallenGemsAffixes.MOD_ID);
    public static class Components {
        public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
                DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, FallenGemsAffixes.MOD_ID);

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<PermanentEffectRoot>> PERMANENT_EFFECT =
                DATA_COMPONENT_TYPES.register("permanent_effect", () ->
                        DataComponentType.<PermanentEffectRoot>builder()
                                .persistent(PermanentEffectRoot.CODEC)
                                .networkSynchronized(PermanentEffectRoot.STREAM_CODEC)
                                .build()
                );
    }
}
