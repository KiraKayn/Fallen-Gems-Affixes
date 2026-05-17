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
import net.minecraftforge.fml.ModList;

public class TricksterWeaponAffix extends SetAffix {
    public static final Codec<TricksterWeaponAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ResourceLocation.CODEC.fieldOf("set").forGetter(a -> a.setId),
            Codec.FLOAT.fieldOf("damage_multiplier").forGetter(a -> a.damageMultiplier),
            Codec.INT.fieldOf("cooldown_ticks").forGetter(a -> a.cooldownTicks),
            Codec.FLOAT.fieldOf("three_piece_speed_per_clone").forGetter(a -> a.threePieceSpeedPerClone),
            Codec.FLOAT.fieldOf("three_piece_dodge_per_clone").forGetter(a -> a.threePieceDodgePerClone),
            Codec.FLOAT.fieldOf("five_piece_kill_refresh_chance").forGetter(a -> a.fivePieceKillRefreshChance)
    ).apply(inst, TricksterWeaponAffix::new));

    private final float damageMultiplier;
    private final int cooldownTicks;
    private final float threePieceSpeedPerClone;
    private final float threePieceDodgePerClone;
    private final float fivePieceKillRefreshChance;

    public TricksterWeaponAffix(ResourceLocation setId, float damageMultiplier, int cooldownTicks,
                                float threePieceSpeedPerClone, float threePieceDodgePerClone,
                                float fivePieceKillRefreshChance) {
        super(setId);
        this.damageMultiplier = damageMultiplier;
        this.cooldownTicks = cooldownTicks;
        this.threePieceSpeedPerClone = threePieceSpeedPerClone;
        this.threePieceDodgePerClone = threePieceDodgePerClone;
        this.fivePieceKillRefreshChance = fivePieceKillRefreshChance;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public float getThreePieceSpeedPerClone() {
        return threePieceSpeedPerClone;
    }

    public float getThreePieceDodgePerClone() {
        return threePieceDodgePerClone;
    }

    public float getFivePieceKillRefreshChance() {
        return fivePieceKillRefreshChance;
    }

    @Override
    public Component getName(boolean prefix) {
        return Component.translatable(prefix ? "set_affix.fallen_gems_affixes.trickster_weapon" : "set_affix.fallen_gems_affixes.trickster_weapon.suffix");
    }

    @Override
    public ResourceLocation getTypeId() {
        return null;
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        Component value = Component.literal(SetAffix.fmt(damageMultiplier * 100f) + "%")
                .withStyle(ChatFormatting.DARK_RED);

        return Component.translatable("set_affix.fallen_gems_affixes.trickster_weapon.desc", value)
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;

        if (cat == dev.shadowsoffire.apotheosis.adventure.loot.LootCategory.SWORD
                || cat == dev.shadowsoffire.apotheosis.adventure.loot.LootCategory.HEAVY_WEAPON) {
            return true;
        }

        if (ModList.get().isLoaded("celestisynth")) {
            dev.shadowsoffire.apotheosis.adventure.loot.LootCategory celestialMelee =
                    dev.shadowsoffire.apotheosis.adventure.loot.LootCategory.byId("celestial_melee");
            return celestialMelee != null && cat == celestialMelee;
        }

        return false;
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
    public int[] getBonusThresholds() {
        return TricksterSetConstants.BONUS_THRESHOLDS;
    }

    @Override
    public Codec<? extends SetAffix> getCodec() {
        return CODEC;
    }
}