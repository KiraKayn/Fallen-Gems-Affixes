package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.affix.AdaptiveSpellPowerAffix;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.ExtraGemBonusRegistry;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.AttributeEffectBonus;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.MultiEffectBonus;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.PermanentEffectBonus;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class InitNewCodecs {
    @SubscribeEvent
    public static void init(FMLCommonSetupEvent e) {
        e.enqueueWork(() -> {
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "multi_effect"), MultiEffectBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "attribute_effect"), AttributeEffectBonus.CODEC);
            GemBonus.CODEC.register(new ResourceLocation("fallen_gems_affixes", "permanent_effect"), PermanentEffectBonus.CODEC);
            AffixRegistry.INSTANCE.registerCodec(new ResourceLocation("fallen_gems_affixes", "adaptive_spell_power"), AdaptiveSpellPowerAffix.CODEC);
            ExtraGemBonusRegistry.INSTANCE.registerToBus();
        });
    }
}