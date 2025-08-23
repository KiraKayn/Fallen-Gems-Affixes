package net.kayn.fallen_gems_affixes.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.mixin.accessor.DynamicRegistryAccessor;
import net.kayn.fallen_gems_affixes.types.util.Indexed;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

import java.util.*;
import java.util.function.Function;

public class CodecUtil {
    public static final Codec<Holder<MobEffect>> HOLDER_MOB_EFFECT_CODEC = BuiltInRegistries.MOB_EFFECT.holderByNameCodec();

    public static final Codec<GemBonus> CONDITIONAL_CAT_CODEC = new Codec<>() {

        @Override
        public <T> DataResult<T> encode(GemBonus input, DynamicOps<T> ops, T prefix) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> DataResult<Pair<GemBonus, T>> decode(DynamicOps<T> ops, T input) {
            Dynamic<T> dynamic = new Dynamic<>(ops, input);
            Optional<Dynamic<T>> conCatOpt = dynamic.get("con_cat").result();
            if (conCatOpt.isEmpty()) {
                return DataResult.error(() -> "Missing 'con_cat' field");
            }
            Dynamic<T> conCatDyn = conCatOpt.get();
            Optional<Pair<Set<ConditionalLootCategory>, T>> conCats = ConditionalLootCategory.SET_CODEC.decode(conCatDyn).result();
            if (conCats.isEmpty()) {
                return DataResult.error(() -> "Can't parse conditional loot category");
            }
            Optional<Dynamic<T>> bonusOpt = dynamic.get("bonus").result();
            if (bonusOpt.isEmpty()) {
                return DataResult.error(() -> "Missing 'bonus' field");
            }
            Optional<String> type = bonusOpt.get().get("type").asString().result();
            if (type.isEmpty()) {
                return DataResult.error(() -> "Missing 'type' field");
            }
            Dynamic<T> bonusDyn = bonusOpt.get();
            Optional<Dynamic<T>> unmodifiedGemClassOpt = bonusDyn.get("gem_class").result();
            if (unmodifiedGemClassOpt.isEmpty()) {
                return DataResult.error(() -> "Missing 'gem_class' field");
            }
            Dynamic<T> unmodifiedGemClass = unmodifiedGemClassOpt.get();
            Optional<Dynamic<T>> unmodifiedTypesOpt = unmodifiedGemClass.get("types").result();
            if (unmodifiedTypesOpt.isEmpty()) {
                return DataResult.error(() -> "Missing 'types' field");
            }
            Dynamic<T> unmodifiedTypesDyn = unmodifiedTypesOpt.get();
            List<Dynamic<T>> tempList = new ArrayList<>(unmodifiedTypesDyn.asList(Function.identity()));
            for (ConditionalLootCategory conCat : conCats.get().getFirst()) {
                if (conCat.test()) {
                    tempList.add(unmodifiedTypesDyn.createString(conCat.cat()));
                }
            }
            Dynamic<T> modifiedBonus = bonusDyn.set("gem_class", unmodifiedGemClass.set("types", unmodifiedTypesDyn.createList(tempList.stream())));
            Codec<? extends GemBonus> defaultCodec = GemBonus.CODEC.getValue(ResourceLocation.parse(type.get()));
            if (defaultCodec == null) {
                return DataResult.error(() -> "Unknown GemBonus type: " + type.get());
            }
            return defaultCodec.decode(modifiedBonus).map(p -> Pair.of(p.getFirst(), p.getSecond()));
        }
    };

    @SuppressWarnings("unchecked")
    public static final Codec<Affix> CONDITIONAL_AFFIX_TYPE_CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Affix, T>> decode(DynamicOps<T> ops, T input) {
            Dynamic<T> dyn = new Dynamic<>(ops, input);

            Optional<Dynamic<T>> affixOpt = dyn.get("affix").result();
            if (affixOpt.isEmpty()) {
                return DataResult.error(() -> "Missing 'affix' field");
            }
            Dynamic<T> affix = affixOpt.get();
            Optional<Dynamic<T>> conCatOpt = affix.get("con_cat").result();
            if (!conCatOpt.isEmpty()) {
                Optional<Pair<Set<ConditionalLootCategory>, T>> conCats = ConditionalLootCategory.SET_CODEC.decode(conCatOpt.get()).result();
                if (conCats.isEmpty()) {
                    return DataResult.error(() -> "Can't parse conditional loot category");
                }
                Optional<Dynamic<T>> unmodifiedTypesOpt = affix.get("types").result();
                if (unmodifiedTypesOpt.isEmpty()) {
                    return DataResult.error(() -> "Missing 'types' field");
                }
                Dynamic<T> unmodifiedTypes = unmodifiedTypesOpt.get();
                List<Dynamic<T>> tempList = new ArrayList<>(unmodifiedTypes.asList(Function.identity()));
                for (ConditionalLootCategory conCat : conCats.get().getFirst()) {
                    if (conCat.test()) {
                        tempList.add(unmodifiedTypes.createString(conCat.cat()));
                    }
                }
                dyn = dyn.set("affix", affix.set("types", unmodifiedTypes.createList(tempList.stream())));
            }
            affixOpt = dyn.get("affix").result();
            if (affixOpt.isEmpty()) {
                return DataResult.error(() -> "Missing 'affix' field");
            }
            affix = affixOpt.get();

            Optional<String> defaultAffixTypeOpt = affix.get("type").asString().result();
            if (defaultAffixTypeOpt.isEmpty()) {
                return DataResult.error(() -> "Can't find valid affix type, no default type provided");
            }
            Codec<? extends Affix> defaultCodec = (Codec<? extends Affix>) ((DynamicRegistryAccessor) AffixRegistry.INSTANCE).getCodecs().getValue(ResourceLocation.tryParse(defaultAffixTypeOpt.get()));

            Optional<Dynamic<T>> conTypeOpt = dyn.get("con_type").result();
            if (conTypeOpt.isEmpty()) {
                return DataResult.error(() -> "Missing 'con_type' field");
            }

            Optional<Pair<Set<ConditionalType>, T>> conAffixesOpt = ConditionalType.SET_CODEC.decode(conTypeOpt.get()).result();
            if (conAffixesOpt.isEmpty()) {
                return DataResult.error(() -> "Can't decode conditional bonus");
            }
            Set<ConditionalType> conTypes = conAffixesOpt.get().getFirst();

            String realType = "";
            for (ConditionalType conType : conTypes) {
                if (conType.test()) {
                    realType = conType.type();
                }
            }

            Codec<? extends Affix> actualCodec = (Codec<? extends Affix>) ((DynamicRegistryAccessor) AffixRegistry.INSTANCE).getCodecs().getValue(ResourceLocation.tryParse(realType));
            if (defaultCodec == null) {
                final String finalDefaultType = defaultAffixTypeOpt.get();
                return DataResult.error(() -> "Unknown GemBonus type: " + finalDefaultType);
            }
            if (Objects.equals(realType, "") || actualCodec == null) {
                FallenGemsAffixes.LOGGER.warn("Can't find valid bonus type, use default");

                return defaultCodec.decode(affix).map(p -> Pair.of(p.getFirst(), p.getSecond()));
            }
            return actualCodec.decode(affix).map(p -> Pair.of(p.getFirst(), p.getSecond()));
        }

        @Override
        public <T> DataResult<T> encode(Affix input, DynamicOps<T> ops, T prefix) {
            throw new UnsupportedOperationException();
        }
    };

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

    public static <T> Optional<Dynamic<T>> getNested(Dynamic<T> root, String[] path) {
        if (path.length == 0) return Optional.of(root);

        String key = path[0];
        if (path.length == 1) {
            return root.get(key).result();
        } else {
            Optional<Dynamic<T>> dynOpt = root.get(key).result();
            if (dynOpt.isEmpty()) {
                return Optional.empty();
            }
            return getNested(dynOpt.get(), Arrays.copyOfRange(path, 1, path.length));
        }
    }

    public static <T> Optional<Dynamic<T>> updateNested(Dynamic<T> root, String[] path, Dynamic<T> newValue) {
        if (path.length == 0) return Optional.of(root);

        String key = path[0];
        if (path.length == 1) {
            Optional<Dynamic<T>> dynOpt = root.get(key).result();
            if (dynOpt.isEmpty()) {
                return Optional.empty();
            }
            root.set(key, newValue);
        } else {
            Optional<Dynamic<T>> dynOpt = root.get(key).result();
            if (dynOpt.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(root.set(key, updateNested(dynOpt.get(), Arrays.copyOfRange(path, 1, path.length), newValue).get()));
        }
        return Optional.of(root);
    }
}
