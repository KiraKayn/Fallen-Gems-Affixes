package net.kayn.fallen_gems_affixes.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffect;

public record PermanentEffectRoot(String slot, Holder<MobEffect> effect){
    public static final Codec<PermanentEffectRoot> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    Codec.STRING.fieldOf("slot").forGetter(a->a.slot),
                    BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(a->a.effect)
            )
            .apply(inst, PermanentEffectRoot::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PermanentEffectRoot> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(RegistryFriendlyByteBuf buf, PermanentEffectRoot value) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value.slot());
            ByteBufCodecs.holderRegistry(BuiltInRegistries.MOB_EFFECT.key()).encode(buf, value.effect);
        }

        @Override
        public PermanentEffectRoot decode(RegistryFriendlyByteBuf buf) {
            String slot = ByteBufCodecs.STRING_UTF8.decode(buf);
            Holder<MobEffect> effect = ByteBufCodecs.holderRegistry(BuiltInRegistries.MOB_EFFECT.key()).decode(buf);
            return new PermanentEffectRoot(slot, effect);
        }
    };
}
