package net.kayn.fallen_gems_affixes.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.GemBonusExtension;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

@Mixin(value = Gem.class, remap = false)
public class GemMixin implements GemBonusExtension {

    @Final
    @Shadow
    protected transient java.util.Map<LootCategory, GemBonus> bonusMap;

    @Unique
    private final List<GemBonus> fallen_gems_affixes$extraBonuses = new ArrayList<>();

    @Unique
    @Override
    public void fallen_gems_affixes$appendExtraBonus(GemBonus bonus) {
        fallen_gems_affixes$validateBonus(bonus);
        this.fallen_gems_affixes$extraBonuses.add(bonus);

        for (LootCategory cat : bonus.getGemClass().types()) {
            this.bonusMap.put(cat, bonus);
        }
    }

    @Override
    public void fallen_gems_affixes$clearExtraBonuses() {
        this.fallen_gems_affixes$extraBonuses.clear();
    }

    @Unique
    private void fallen_gems_affixes$validateBonus(GemBonus bonus) {
        for (LootCategory category : bonus.getGemClass().types()) {
            if (this.bonusMap.containsKey(category)) {
                GemBonus conflict = this.bonusMap.get(category);
                if (!conflict.equals(bonus)) {
                    throw new IllegalArgumentException("Gem Bonus for class %s conflicts with existing bonus for class %s (categories overlap)"
                            .formatted(bonus.getGemClass().key(), conflict.getGemClass().key()));
                }
            }
        }
    }

    @Inject(method = "addInformation", at = @At("TAIL"), remap = false)
    private void fallen_gems_affixes$addExtraBonusesTooltips(ItemStack gem, LootRarity rarity, Consumer<Component> list, CallbackInfo ci) {
        for (GemBonus bonus : this.fallen_gems_affixes$extraBonuses) {
            if (!bonus.supports(rarity)) continue;

            Component modifyComp = bonus.getSocketBonusTooltip(gem, rarity);
            Component sum = Component.translatable("text.apotheosis.dot_prefix",
                    Component.translatable("%s: %s",
                            Component.translatable("gem_class." + bonus.getGemClass().key()),
                            modifyComp)).withStyle(ChatFormatting.GOLD);
            list.accept(sum);
        }
    }
}