package net.kayn.fallen_gems_affixes.adventure.entity.affix;

import com.mojang.serialization.Codec;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.special.*;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EntityAffixRegistry {

    private static final Map<ResourceLocation, Codec<? extends EntityAffix>> CODECS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, EntityAffix> INSTANCES = new LinkedHashMap<>();

    private EntityAffixRegistry() {
    }

    static {
        registerType(PackLeaderAffix.TYPE, PackLeaderAffix.CODEC);
        registerType(CursedAffix.TYPE, CursedAffix.CODEC);
        registerType(SoulDrainAffix.TYPE, SoulDrainAffix.CODEC);
        registerType(ShadowStepAffix.TYPE, ShadowStepAffix.CODEC);
        registerType(ArcaneShieldAffix.TYPE, ArcaneShieldAffix.CODEC);
        registerType(EnrageAffix.TYPE, EnrageAffix.CODEC);
        registerType(ThornsAffix.TYPE, ThornsAffix.CODEC);
        registerType(RegenerationAffix.TYPE, RegenerationAffix.CODEC);
        registerType(PhaseAffix.TYPE, PhaseAffix.CODEC);
        registerType(FearAuraAffix.TYPE, FearAuraAffix.CODEC);
        registerType(UndyingAffix.TYPE, UndyingAffix.CODEC);
        registerType(ExecutionerAffix.TYPE, ExecutionerAffix.CODEC);
        registerType(ColossusAffix.TYPE, ColossusAffix.CODEC);
        registerType(OverloadAffix.TYPE, OverloadAffix.CODEC);
        registerType(PredatorAffix.TYPE, PredatorAffix.CODEC);
        registerType(ApexAffix.TYPE, ApexAffix.CODEC);
        registerType(HunterAffix.TYPE, HunterAffix.CODEC);
    }

    public static <T extends EntityAffix> void registerType(ResourceLocation typeId, Codec<T> codec) {
        CODECS.put(typeId, codec);
    }

    @Nullable
    public static Codec<? extends EntityAffix> getCodec(ResourceLocation typeId) {
        return CODECS.get(typeId);
    }

    public static void registerInstance(ResourceLocation id, EntityAffix affix) {
        affix.setId(id);
        INSTANCES.put(id, affix);
    }

    @Nullable
    public static EntityAffix getInstance(ResourceLocation id) {
        return INSTANCES.get(id);
    }

    public static void clearInstances() {
        INSTANCES.clear();
    }

    public static Map<ResourceLocation, EntityAffix> getInstances() {
        return Collections.unmodifiableMap(INSTANCES);
    }

    public static final Codec<EntityAffix> DISPATCH_CODEC = ResourceLocation.CODEC.dispatch("type", EntityAffix::getType, id -> {
        Codec<? extends EntityAffix> codec = getCodec(id);
        if (codec == null) throw new IllegalArgumentException("Unknown entity affix type: " + id);
        return codec;
    });
}