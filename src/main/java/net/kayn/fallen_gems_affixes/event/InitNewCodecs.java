package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveSpellPowerAffix;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.*;
import net.kayn.fallen_gems_affixes.util.GemBonusUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class InitNewCodecs {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "multi_effect"), MultiEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "attribute_effect"), AttributeEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "permanent_effect"), PermanentEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "boss_slayer"), BossSlayerBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "con_cat_bonus"), GemBonusUtil.CONDITIONAL_CAT_CODEC);
            if (ModList.get().isLoaded("irons_spellbooks")) {
                GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "spell_effect"), SpellEffectBonus.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "adaptive_spell_power"), AdaptiveSpellPowerAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "spell_effect"), SpellEffectAffix.CODEC);
            }
        });
    }
}