package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.affix.*;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.*;
import net.kayn.fallen_gems_affixes.util.CodecUtil;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

public class InitNewCodecs {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {

            //Gem Bonus
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "multi_effect"), MultiEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "attribute_effect"), AttributeEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "permanent_effect"), PermanentEffectBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "boss_slayer"), BossSlayerBonus.CODEC);
            GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "con_cat_bonus"), CodecUtil.CONDITIONAL_CAT_CODEC);

            //Affixes
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "con_affix_type"), CodecUtil.CONDITIONAL_AFFIX_TYPE_CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "adaptive"), AdaptiveAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "afflicted"), AfflictedAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "berserker"), BerserkerAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "chain_shot"), ChainShotAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "homing"), HomingAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "momentum"), MomentumAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "multi_shot"), MultiShotAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "piercing_arrow"), PiercingArrowAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "true_shot"), TrueShotAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "feast"), FeastAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "prospector"), ProspectorAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "shield_guard"), ShieldGuardAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "socket_bonus"), SocketBonusAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "soulbound"), SoulboundAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "enchant_boost"), EnchantBoostAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "fortify"), FortifyAffix.CODEC);

            //ISS
            if (ModList.get().isLoaded("irons_spellbooks")) {
                GemBonus.CODEC.register(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "spell_effect"), SpellEffectBonus.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "adaptive_spell_power"), AdaptiveSpellPowerAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "spell_effect"), SpellEffectAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "spell_cast"), SpellCastAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "cooldown_reset"), CooldownResetAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "chain_cast"), ChainCastAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "mana_cost"), ManaCostAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "mana_block"), ManaBlockAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "mana_damage"), ManaDamageAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "mana_leech"), ManaLeechAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "mana_return"), ManaReturnAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "mana_shield"), ManaShieldAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "spell_focus"), SpellFocusAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "spellblade"), SpellbladeAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "concentration"), ConcentrationAffix.CODEC);
            }
        });
    }
}