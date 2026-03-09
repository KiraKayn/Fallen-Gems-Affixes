package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Augment of Duality.
 *
 * <p>Passive (via {@link DualityCritModifierHandler} and
 * {@link net.minecraftforge.event.ItemAttributeModifierEvent}):
 * <ul>
 *   <li>Multiplies the wearer's CRIT_CHANCE by {@code critChanceMultiplier}.</li>
 *   <li>Reduces the wearer's CRIT_DAMAGE by {@code critDamageReduction} (fraction).</li>
 * </ul>
 *
 * <p>On critical hit (intercepted via
 * {@link net.kayn.fallen_gems_affixes.mixin.CritMixin}):
 * <ul>
 *   <li>{@code physicalRatio} of the total hit damage remains as physical damage.</li>
 *   <li>{@code magicRatio} of the total hit damage is dealt as a separate magic hit.</li>
 *   <li>{@code magicRatio + physicalRatio} must equal {@code 1.0}; otherwise the augment
 *       is skipped during JSON loading and an error is logged.</li>
 */
public class DualityAugment implements IAugment {

    private static final ResourceLocation DUALITY_ID =
            new ResourceLocation(FallenGemsAffixes.MOD_ID, "duality");

    public static ResourceLocation augmentId() { return DUALITY_ID; }

    @Override public ResourceLocation getId()   { return DUALITY_ID; }
    @Override public boolean isUnique()          { return true; }
    @Override public boolean needsInstance()     { return false; }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        DualityData data = new DualityData();
        data.deserializeNBT(tag);
        return data;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui,
                            IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        ItemStack displayStack = AugmentItem.createAugment(DUALITY_ID);
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(displayStack, 0, 0);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof DualityData data) {
            return Component.translatable(
                    "fallen_gems_affixes.augment.duality.socket_desc",
                    String.format("%.1f", data.critChanceMultiplier),
                    String.format("%.0f%%", data.critDamageReduction * 100f),
                    String.format("%.0f%%", data.physicalRatio * 100f),
                    String.format("%.0f%%", data.magicRatio * 100f)
            ).withStyle(ChatFormatting.YELLOW);
        }
        return Component.translatable("fallen_gems_affixes.augment.duality.socket_desc")
                .withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.duality.type")
                .withStyle(ChatFormatting.GOLD));

        AugmentItem.AugmentData config = AugmentItem.getAugmentData(stack);
        float critChanceMult  = config != null ? config.getCritChanceMultiplier()  : 2.0f;
        float critDmgReduce   = config != null ? config.getCritDamageReduction()   : 0.3f;
        float physRatio       = config != null ? config.getPhysicalRatio()         : 0.5f;
        float magicRatio      = config != null ? config.getMagicRatio()            : 0.5f;

        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.duality.desc.crit_chance",
                                String.format("%.1f", critChanceMult))
                        .withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.RED)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.duality.desc.crit_damage",
                                String.format("%.0f%%", critDmgReduce * 100f))
                        .withStyle(ChatFormatting.RED)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.duality.desc.split",
                                String.format("%.0f%%", physRatio * 100f),
                                String.format("%.0f%%", magicRatio * 100f))
                        .withStyle(ChatFormatting.YELLOW)));
    }

    @Nullable
    public static DualityData getDualityDataFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return null;
        ListTag augments = tag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA)
                .getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < augments.size(); i++) {
            CompoundTag entry = augments.getCompound(i);
            if (DUALITY_ID.toString().equals(entry.getString(Fallen.AugmentMisc.TYPE))) {
                DualityData data = new DualityData();
                data.deserializeNBT(entry.getCompound(Fallen.AugmentMisc.INNER_DATA));
                return data;
            }
        }
        return null;
    }

    public static class DualityData implements IAugmentInnerData {

        float critChanceMultiplier = 2.0f;
        float critDamageReduction  = 0.3f;
        float physicalRatio        = 0.5f;
        float magicRatio           = 0.5f;

        public float getCritChanceMultiplier() { return critChanceMultiplier; }
        public float getCritDamageReduction()  { return critDamageReduction; }
        public float getPhysicalRatio()        { return physicalRatio; }
        public float getMagicRatio()           { return magicRatio; }

        @Override public void enable()  {}
        @Override public void disable() {}
        @Override public boolean isFunctional() { return true; }

        @Override
        public MutableComponent combineText() {
            return Component.translatable(
                    "fallen_gems_affixes.augment.duality.socket_desc",
                    String.format("%.0f%%", critChanceMultiplier * 100f),
                    String.format("%.0f%%", critDamageReduction * 100f),
                    String.format("%.0f%%", physicalRatio * 100f),
                    String.format("%.0f%%", magicRatio * 100f));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("critChanceMultiplier", critChanceMultiplier);
            tag.putFloat("critDamageReduction",  critDamageReduction);
            tag.putFloat("physicalRatio",        physicalRatio);
            tag.putFloat("magicRatio",           magicRatio);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            critChanceMultiplier = tag.getFloat("critChanceMultiplier");
            critDamageReduction  = tag.getFloat("critDamageReduction");
            physicalRatio        = tag.getFloat("physicalRatio");
            magicRatio           = tag.getFloat("magicRatio");
        }
    }

    @Override
    public String toString() {
        return "DualityAugment{id=" + augmentId() + "}";
    }
}