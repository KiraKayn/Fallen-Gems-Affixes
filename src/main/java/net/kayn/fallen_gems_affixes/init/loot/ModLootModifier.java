package net.kayn.fallen_gems_affixes.init.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModLootModifier {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<GemLootModifier>> GEM_MODIFIER =
            LOOT_MODIFIERS.register("gem_modifier", GemLootModifier.CODEC);

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SocketGemModifier>> SOCKET_GEM_MODIFIER =
            LOOT_MODIFIERS.register("socket_gem_modifier", () -> SocketGemModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}