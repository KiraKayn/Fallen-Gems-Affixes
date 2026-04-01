package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.adventure.affix.*;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.*;
import net.kayn.fallen_gems_affixes.util.GemBonusUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class InitNewCodecs {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "multi_effect"), MultiEffectBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "attribute_effect"), AttributeEffectBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "permanent_effect"), PermanentEffectBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "boss_slayer"), BossSlayerBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "boss_resistance"), BossResistanceBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "dragon_resistance"), DragonResistanceBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "dragon_slayer"), DragonSlayerBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "hydra"), HydraBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "wet_damage"), WetDamageBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "wet_resistance"), WetResistanceBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "con_cat_bonus"), GemBonusUtil.CONDITIONAL_CAT_CODEC);

            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "socket_bonus"), SocketBonusAffix.CODEC);

            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "soulbound"), SoulboundAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "momentum"), MomentumAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "multi_shot"), MultiShotAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "piercing_arrow"), PiercingArrowAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "chain_shot"), ChainShotAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "true_shot"), TrueShotAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "homing"), HomingAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "afflicted"), AfflictedAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "berserker"), BerserkerAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "feast"), FeastAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "adaptive"), AdaptiveAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "fortify"), FortifyAffix.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "prospector"), ProspectorAffix.CODEC);

            if (ModList.get().isLoaded("irons_spellbooks")) {
                GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "spell_effect"), SpellEffectBonus.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "adaptive_spell_power"), AdaptiveSpellPowerAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "spell_effect"), SpellEffectAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "spell_cast"), SpellCastAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "autocast"), AutocastAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "mana_cost"), ManaCostAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "mana_return"), ManaReturnAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "mana_damage"), ManaDamageAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "spell_focus"), SpellFocusAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "mana_shield"), ManaShieldAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "cooldown_reset"), CooldownResetAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "mana_leech"), ManaLeechAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "spellblade"), SpellbladeAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "mana_block"), ManaBlockAffix.CODEC);
                AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "concentration"), ConcentrationAffix.CODEC);
                GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "void_hunter"), VoidHunterBonus.CODEC);
                GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "spell_echo"), SpellEchoBonus.CODEC);
                GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "arrow_teleport"), ArrowTeleportBonus.CODEC);
            }
//            ExtraGemBonusRegistry.INSTANCE.registerToBus();
        });
    }
}