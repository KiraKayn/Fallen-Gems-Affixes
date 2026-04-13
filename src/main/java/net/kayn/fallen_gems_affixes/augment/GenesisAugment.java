package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentHelper;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRecipe;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Augment of Genesis.
 *
 * <p>On application: snapshots the raw affix levels from {@code affix_data.affixes} NBT,
 * then multiplies every non-durable affix by {@code defaultPower}
 * (e.g. 0.5 makes a 0.9 affix become 0.45).
 *
 * <p>On each unique boss kill: {@code affixPower} and {@code gemPower} grow by their
 * respective boost values. Affixes are re-applied as {@code originalLevel * affixPower}.
 * Gem power is read passively by {@link GemBonusModifier}.
 *
 * <p>Affix NBT is manipulated directly to avoid key-format mismatches that occur when
 * going through {@link AffixHelper#getAffixes} during recipe assembly.
 */
public class GenesisAugment implements IAugment {

    private static final ResourceLocation GENESIS_ID =
            ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "genesis");
    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(GenesisData.CODEC);

    /** NBT key inside affix_data where affix levels live. */
    private static final String AFFIX_DATA   = "affix_data";
    private static final String AFFIXES_KEY  = "affixes";

    public static final float MAX_AFFIX_LEVEL = SupremacyAugment.MAX_AFFIX_LEVEL;

    // -------------------------------------------------------------------------
    // IAugment identity
    // -------------------------------------------------------------------------

    @Override public ResourceLocation getId()   { return GENESIS_ID; }
    @Override public boolean isUnique()          { return true; }
    @Override public boolean shouldAttachToPlayer()     { return false; }

    @Override
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        GenesisData data = new GenesisData();
        data.originalAffixLevels = new CompoundTag();
        data.defaultPower    = 0.5f;
        data.affixPowerBoost = 0.1f;
        data.gemPowerBoost   = 0.1f;
        data.affixPower      = 0.5f;
        data.gemPower        = 0.5f;
        data.bossKillCount   = 0;
//        data.killedBossIds     = new HashSet<>();
        data.originalAffixLevels     = new CompoundTag();
        return data;
    }

    @Override
    public String toString() {
        return IAugment.string(this);
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        GenesisData data = new GenesisData();
        data.deserializeNBT(tag);
        return data;
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui,
                            IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        ItemStack displayStack = AugmentItem.createAugment(Fallen.Augments.GENESIS);
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(displayStack, 0, 0);
        pose.popPose();
    }

    // -------------------------------------------------------------------------
    // Tooltips
    // -------------------------------------------------------------------------

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof GenesisData data) {
            return data.combineText().withStyle(ChatFormatting.YELLOW);
        }
        return Component.empty();
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.genesis.type")
                .withStyle(ChatFormatting.GOLD));

        AugmentMeta meta = AugmentItem.getAugmentData(stack);
        float defaultPower;
        float affixBoost;
        float gemBoost;
        GenesisData data;
        if (meta == null) {
            data = (GenesisData) Fallen.Augments.GENESIS.fallbackInnerData();
        } else {
            data = (GenesisData) meta.getDefaultData();
        }
        defaultPower = data.defaultPower;
        affixBoost = data.affixPowerBoost;
        gemBoost = data.gemPowerBoost;

        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.default", defaultPower)
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.affix_boost", affixBoost)
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.gem_boost", gemBoost)
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.genesis.desc.augmentation_block", gemBoost)
                        .withStyle(ChatFormatting.YELLOW)));
    }


    /**
     * Called from {@link AugmentRecipe} after genesis
     * NBT has been committed to {@code result}.
     *
     * <p>Reads affix levels directly from {@code affix_data.affixes} NBT (bypassing
     * AffixHelper which is unreliable during recipe assembly), stores them as the permanent
     * baseline, then immediately applies {@code defaultPower} as the starting multiplier.
     */
    public static void apply(ItemStack result, ItemStack augmentItem) {
        AugmentMeta meta = AugmentItem.getAugmentData(augmentItem);

        GenesisData data1;
        if (meta == null) {
            data1 = (GenesisData) Fallen.Augments.GENESIS.fallbackInnerData();
        } else {
            data1 = (GenesisData) meta.getDefaultData();
        }

        // Snapshot raw affix values now, before we touch anything
        CompoundTag originalLevels = snapshotAffixLevels(result);

        // Write genesis config + state into inner_data
        AugmentInstance inst = AugmentHelper.getAugments(result).get(Fallen.Augments.GENESIS);
        GenesisData data = (GenesisData) inst.getData();
        data.defaultPower = data1.defaultPower;
        data.affixPowerBoost = data1.affixPowerBoost;
        data.gemPowerBoost = data1.gemPowerBoost;
        data.affixPower = data1.defaultPower;
        data.gemPower = data1.defaultPower;
        data.bossKillCount = 0;
        data.originalAffixLevels = originalLevels;

        AugmentHelper.applyAugment(result, inst);

        // Apply starting multiplier: every affix becomes originalLevel * defaultPower
        applyAffixPower(result, data1.defaultPower);
    }

    /**
     * Reads {@code affix_data.affixes} directly from NBT and returns a copy.
     * Keys match exactly what {@link #applyAffixPower} will iterate, making the
     * snapshot reliable regardless of AffixHelper state.
     */
    private static CompoundTag snapshotAffixLevels(ItemStack stack) {
        CompoundTag affixData = stack.getTagElement(AFFIX_DATA);
        if (affixData == null || !affixData.contains(AFFIXES_KEY)) return new CompoundTag();
        return affixData.getCompound(AFFIXES_KEY).copy();
    }

    /**
     * Applies {@code multiplier} to every non-durable affix on {@code stack}.
     *
     * <p>Formula: {@code newLevel = originalLevel * multiplier}, clamped to
     * {@code [0, MAX_AFFIX_LEVEL]}.
     *
     * <p>Reads and writes {@code affix_data.affixes} directly in NBT so the keys always
     * match the stored {@code originalAffixLevels} snapshot, no matter when this is called.
     */
    public static void applyAffixPower(ItemStack stack, float multiplier) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(AFFIX_DATA)) return;

        CompoundTag affixData = tag.getCompound(AFFIX_DATA);
        if (!affixData.contains(AFFIXES_KEY)) return;

        CompoundTag originalLevels = getStoredOriginalLevels(stack);
        Set<String>  durableKeys   = getDurableAffixKeys(stack);

        CompoundTag affixes    = affixData.getCompound(AFFIXES_KEY);
        CompoundTag newAffixes = new CompoundTag();

        for (String key : affixes.getAllKeys()) {
            if (durableKeys.contains(key)) {
                // Leave durable affixes untouched
                newAffixes.putFloat(key, affixes.getFloat(key));
            } else {
                // Always multiply from the original level, never from the current level
                float base = (originalLevels != null && originalLevels.contains(key))
                        ? originalLevels.getFloat(key)
                        : affixes.getFloat(key); // fallback: first call before snapshot written
                newAffixes.putFloat(key, Mth.clamp(base * multiplier, 0f, MAX_AFFIX_LEVEL));
            }
        }

        affixData.put(AFFIXES_KEY, newAffixes);
        tag.put(AFFIX_DATA, affixData);
    }

    /**
     * Returns the {@code originalAffixLevels} compound from inside genesis inner_data,
     * or {@code null} if the item has no genesis augment or the snapshot isn't written yet.
     */
    @Nullable
    private static CompoundTag getStoredOriginalLevels(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return null;
        ListTag augments = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA)
                .getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < augments.size(); i++) {
            CompoundTag entry = augments.getCompound(i);
            if (!GENESIS_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) continue;
            CompoundTag inner = entry.getCompound(Fallen.AugmentMisc.INNER_DATA);
            if (inner.contains("originalAffixLevels")) {
                return inner.getCompound("originalAffixLevels");
            }
        }
        return null;
    }

    /**
     * Uses AffixHelper to find which affix keys correspond to DurableAffix, so those
     * can be skipped during scaling. Returns an empty set if AffixHelper gives nothing
     * (durable affixes will then be scaled too, which is a minor acceptable fallback).
     */
    private static Set<String> getDurableAffixKeys(ItemStack stack) {
        Set<String> keys = new HashSet<>();
        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        if (affixes == null) return keys;
        for (var entry : affixes.entrySet()) {
            if (entry.getValue().affix().get() instanceof DurableAffix) {
                keys.add(entry.getKey().getId().toString());
            }
        }
        return keys;
    }

    /**
     * Returns the Genesis gem-power multiplier for {@code stack}, or {@code null} if
     * the item carries no Genesis augment.
     */
    @Nullable
    public static Float getGenesisGemPower(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return null;

        ListTag list = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA)
                .getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!GENESIS_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) continue;
            return entry.getCompound(Fallen.AugmentMisc.INNER_DATA).getFloat("gemPower");
        }
        return null;
    }

    public static class GenesisData implements IAugmentInnerData, IGemPowerProvider, IAffixPowerProvider {
        public static final Codec<GenesisData> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.FLOAT.fieldOf("default_power").forGetter(d -> d.defaultPower),
                        Codec.FLOAT.fieldOf("affix_power_boost").forGetter(d -> d.affixPowerBoost),
                        Codec.FLOAT.fieldOf("gem_power_boost").forGetter(d -> d.gemPowerBoost)
                ).apply(inst, (defaultPower, affixPowerBoost, gemPowerBoost) -> {
                    GenesisData data = (GenesisData) Fallen.Augments.GENESIS.fallbackInnerData();
                    data.defaultPower = defaultPower;
                    data.affixPowerBoost = affixPowerBoost;
                    data.gemPowerBoost = gemPowerBoost;
                    return data;
                })
        );

        float defaultPower    = 0.5f;
        float affixPowerBoost = 0.1f;
        float gemPowerBoost   = 0.1f;
        float affixPower      = 0.5f;
        float gemPower        = 0.5f;
        int   bossKillCount   = 0;
        final Set<String> killedBossIds     = new HashSet<>();
        CompoundTag originalAffixLevels     = new CompoundTag();

        @Override
        public float getAffixPower()    { return affixPower; }
        @Override
        public float getGemPower()      { return gemPower; }
        public int   getBossKillCount() { return bossKillCount; }

        @Override public void enable()  {}
        @Override public void disable() {}
        @Override public boolean isFunctional() { return true; }

        @Override
        public MutableComponent combineText() {
            return Component.translatable(
                    "fallen_gems_affixes.augment.genesis.socket_desc",
                    bossKillCount,
                    String.format("%.2f", affixPower),
                    String.format("%.2f", gemPower));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("defaultPower",    defaultPower);
            tag.putFloat("affixPowerBoost", affixPowerBoost);
            tag.putFloat("gemPowerBoost",   gemPowerBoost);
            tag.putFloat("affixPower",      affixPower);
            tag.putFloat("gemPower",        gemPower);
            tag.putInt("bossKillCount",     bossKillCount);
            tag.put("originalAffixLevels",  originalAffixLevels);
            ListTag bosslist = new ListTag();
            for (String id : killedBossIds) bosslist.add(StringTag.valueOf(id));
            tag.put("killedBosses", bosslist);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            defaultPower    = tag.getFloat("defaultPower");
            affixPowerBoost = tag.getFloat("affixPowerBoost");
            gemPowerBoost   = tag.getFloat("gemPowerBoost");
            affixPower      = tag.getFloat("affixPower");
            gemPower        = tag.getFloat("gemPower");
            bossKillCount   = tag.getInt("bossKillCount");
            originalAffixLevels = tag.contains("originalAffixLevels")
                    ? tag.getCompound("originalAffixLevels")
                    : new CompoundTag();
            killedBossIds.clear();
            for (Tag t : tag.getList("killedBosses", Tag.TAG_STRING))
                killedBossIds.add(t.getAsString());
        }

        @Override
        public Codec<GenesisData> getCodec() {
            return CODEC;
        }
    }
}