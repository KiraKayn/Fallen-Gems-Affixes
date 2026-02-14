package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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

import java.util.HashMap;
import java.util.Map;

public class SupremacyAugment implements IAugment {
    private static final ResourceLocation SUPREMACY_ID = new ResourceLocation(FallenGemsAffixes.MOD_ID, "supremacy");
    public static final float STANDARD_MAX_LEVEL = 1.0f;
    public static final float MAX_AFFIX_LEVEL = 2.0f;

    public static ResourceLocation augmentId() {
        return SUPREMACY_ID;
    }

    @Override
    public ResourceLocation getId() {
        return SUPREMACY_ID;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean needsInstance() {
        return true;
    }

    @Override
    public AugmentInstance createInstanceFromStack(ItemStack stack) {
        float power = getAugmentPower(stack);

        SupremacyData innerData = new SupremacyData();
        innerData.power = power;

        return new AugmentInstance(this, innerData);
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);

        AugmentItem.AugmentData data = AugmentItem.getAugmentData(SUPREMACY_ID);
        if (data == null) return;

        ItemStack stack = AugmentItem.createAugment(SUPREMACY_ID);

        var pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(stack, 0, 0);
        pose.popPose();
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        SupremacyData data = new SupremacyData();
        data.deserializeNBT(tag);

        if (data.power == 0.0f) {
            AugmentItem.AugmentData jsonData = AugmentItem.getAugmentData(SUPREMACY_ID);
            if (jsonData != null) {
                data.power = jsonData.getPower();
            }
        }

        return data;
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof SupremacyData data) {
            return Component.translatable(
                    "fallen_gems_affixes.augment.supremacy.desc",
                    data.getPower()
            ).withStyle(ChatFormatting.YELLOW);
        }

        return Component.translatable("fallen_gems_affixes.augment.supremacy.desc")
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        float power = getAugmentPower(stack);

        tooltip.add(Component.translatable("fallen_gems_affixes.augment.supremacy.type")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.supremacy.desc", power)
                        .withStyle(ChatFormatting.YELLOW)));
    }

    public static void apply(ItemStack stack) {
        float power = getAugmentPower(stack);

        Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
        if (affixes == null || affixes.isEmpty()) return;

        Map<DynamicHolder<? extends Affix>, AffixInstance> newAffixes = new HashMap<>();
        CompoundTag root = stack.getOrCreateTag();
        root.putBoolean("fallen_gems_affixes:fabled", true);

        if (root.contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            CompoundTag augmentData = root.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
            ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);

            for (int i = 0; i < augments.size(); i++) {
                CompoundTag augment = augments.getCompound(i);
                if (augment.getString(Fallen.AugmentMisc.TYPE).equals(Fallen.Augments.SUPREMACY_STRING)) {
                    CompoundTag innerData = augment.getCompound(Fallen.AugmentMisc.INNER_DATA);
                    if (!innerData.contains("power")) {
                        innerData.putFloat("power", power);
                        augment.put(Fallen.AugmentMisc.INNER_DATA, innerData);
                    }
                    break;
                }
            }

            augmentData.put(Fallen.AugmentMisc.AUGMENTS, augments);
            root.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);
        }

        for (var entry : affixes.entrySet()) {
            var holder = entry.getKey();
            var affixIns = entry.getValue();
            Affix affix = holder.get();

            if (!(affix instanceof DurableAffix)) {
                float oldLevel = affixIns.level();
                float newLevel = Mth.clamp(Math.max(oldLevel, power), 0, MAX_AFFIX_LEVEL);

                newAffixes.put(holder, new AffixInstance(
                        affixIns.affix(),
                        affixIns.stack(),
                        affixIns.rarity(),
                        newLevel
                ));
            } else {
                newAffixes.put(holder, affixIns);
            }
        }

        AffixHelper.setAffixes(stack, newAffixes);
    }

    private static float getAugmentPower(ItemStack stack) {
        AugmentItem.AugmentData data = AugmentItem.getAugmentData(stack);
        if (data != null) {
            return data.getPower();
        }
        return 1.5f;
    }

    public static class SupremacyData implements IAugmentInnerData {
        float power;

        public float getPower() {
            return power;
        }

        @Override
        public void enable() {
        }

        @Override
        public void disable() {
        }

        @Override
        public boolean isFunctional() {
            return true;
        }

        @Override
        public MutableComponent combineText() {
            return Component.translatable("fallen_gems_affixes.augment.supremacy.desc", power);
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("power", power);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            power = tag.getFloat("power");
        }
    }

    @Override
    public String toString() {
        return "SupremacyAugment{" + "id=" + augmentId() + "}";
    }
}