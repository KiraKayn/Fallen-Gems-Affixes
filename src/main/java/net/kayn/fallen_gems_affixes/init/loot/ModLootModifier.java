package net.kayn.fallen_gems_affixes.init.loot;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;

public class ModLootModifier {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FallenGemsAffixes.MOD_ID);

    public static final RegistryObject<Codec<GemLootModifier>> GEM_MODIFIER =
            LOOT_MODIFIERS.register("gem_modifier", GemLootModifier.CODEC);

    public static final RegistryObject<Codec<SocketGemModifier>> SOCKET_GEM_MODIFIER =
            LOOT_MODIFIERS.register("socket_gem_modifier", () -> SocketGemModifier.CODEC);

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}