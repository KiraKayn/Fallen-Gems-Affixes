package net.kayn.fallen_gems_affixes.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.kayn.fallen_gems_affixes.types.util.Indexed;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

public class CodecUtil {
    public static final Codec<Holder<MobEffect>> HOLDER_MOB_EFFECT_CODEC = BuiltInRegistries.MOB_EFFECT.holderByNameCodec();

    public static <T> Codec<Indexed<T>> toDefaultIndexedCodec(Codec<T> codec) {
        Codec<Pair<T, Integer>> entryCodec = Codec.pair(
                codec,
                Codec.INT
        );

        return new Codec<>() {
            @Override
            public <R> DataResult<R> encode(Indexed<T> input, DynamicOps<R> ops, R prefix) {
                return entryCodec.encode(Pair.of(input.get(), input.getId()), ops, prefix);
            }

            @Override
            public <R> DataResult<Pair<Indexed<T>, R>> decode(DynamicOps<R> ops, R input) {
                return entryCodec.decode(ops, input).map(pair -> {
                    Pair<T, Integer> pairFirst = pair.getFirst();
                    return Pair.of(Indexed.simple(pairFirst.getSecond(), pairFirst.getFirst()), pair.getSecond());
                });
            }
        };
    }
}
