package net.kayn.fallen_gems_affixes.adventure.set.trickster;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import net.kayn.fallen_gems_affixes.adventure.set.SetAffix;
import net.kayn.fallen_gems_affixes.adventure.set.trickster.bonus.TricksterSetBonusHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TricksterHelmetAffix extends SetAffix {
    public static final Codec<TricksterHelmetAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("spawn_chance").forGetter(a -> a.spawnChance)
    ).apply(inst, TricksterHelmetAffix::new));

    private final float spawnChance;

    public TricksterHelmetAffix(ResourceLocation setId, float spawnChance) {
        super(setId);
        this.spawnChance = spawnChance;
    }

    public float getSpawnChance() { return spawnChance; }

    @Override
    public Component getName(boolean prefix) {
        return Component.translatable(prefix ? "set_affix.fallen_gems_affixes.trickster_helmet" : "set_affix.fallen_gems_affixes.trickster_helmet.suffix");
    }

    @Override
    public ResourceLocation getTypeId() {
        return null;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component value = Component.literal(SetAffix.fmt(spawnChance * 100f) + "%")
                .withStyle(ChatFormatting.DARK_RED);

        return Component.translatable("set_affix.fallen_gems_affixes.trickster_helmet.desc", value)
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return !cat.isNone() && cat == dev.shadowsoffire.apotheosis.adventure.loot.LootCategory.HELMET;
    }

    @Override
    public void applySetBonus(Player player, int pieceCount) {
        TricksterSetBonusHandler.onPieceCountChanged(player, pieceCount);
    }

    @Override
    public void removeSetBonus(Player player) {
        TricksterSetBonusHandler.onPieceCountChanged(player, 0);
    }

    @Override
    public int[] getBonusThresholds() { return TricksterSetConstants.BONUS_THRESHOLDS; }

    @Override
    public Codec<? extends SetAffix> getCodec() { return CODEC; }
}