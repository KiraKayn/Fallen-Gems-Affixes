package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.attachment.augment.LiveAugments;
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
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
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
            ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "duality");
    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(DualityData.CODEC);

    @Override public ResourceLocation getId()   { return DUALITY_ID; }
    @Override public boolean isUnique()          { return true; }
    @Override public boolean shouldAttachToEntity()     { return false; }

    @Override
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        DualityData data = new DualityData();
        data.critChanceMultiplier = 2.0f;
        data.critDamageReduction  = 0.3f;
        data.physicalRatio        = 0.5f;
        data.magicRatio           = 0.5f;
        return data;
    }

    @Override
    public String toString() {
        return IAugment.string(this);
    }

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
        ItemStack displayStack = AugmentItem.createAugment(Fallen.Augments.DUALITY);
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
            return data.combineText().withStyle(ChatFormatting.YELLOW);
        }
        return Component.empty();
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.duality.type")
                .withStyle(ChatFormatting.GOLD));

        AugmentMeta meta = AugmentItem.getAugmentData(stack);
        float critChanceMult;
        float critDmgReduce;
        float physRatio;
        float magicRatio;
        if (meta != null) {
            DualityData data = (DualityData) meta.newDefaultData();
            critChanceMult  =  data.critChanceMultiplier;
            critDmgReduce   =  data.critDamageReduction;
            physRatio       =  data.physicalRatio;
            magicRatio      =  data.magicRatio;
        } else {
            critChanceMult  =  2.0f;
            critDmgReduce   =  0.3f;
            physRatio       =  0.5f;
            magicRatio      =  0.5f;
        }

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

    public static class DualityData implements IAugmentInnerData {
        public static final Codec<DualityData> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.FLOAT.fieldOf("crit_chance_multiplier").forGetter(d -> d.critChanceMultiplier),
                        Codec.FLOAT.fieldOf("crit_damage_reduction").forGetter(d -> d.critDamageReduction),
                        Codec.FLOAT.fieldOf("physical_ratio").forGetter(d -> d.physicalRatio),
                        Codec.FLOAT.fieldOf("magic_ratio").forGetter(d -> d.magicRatio)
                ).apply(inst, (critChanceMultiplier, critDamageReduction, physicalRatio, magicRatio) -> {
                    DualityData data = (DualityData) Fallen.Augments.DUALITY.fallbackInnerData();
                    data.critChanceMultiplier = critChanceMultiplier;
                    data.critDamageReduction = critDamageReduction;
                    data.physicalRatio = physicalRatio;
                    data.magicRatio = magicRatio;
                    return data;
                })
        );

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
        public IAugmentInnerData copy() {
            var data = new DualityData();
            data.critChanceMultiplier = critChanceMultiplier;
            data.critDamageReduction = critDamageReduction;
            data.physicalRatio = physicalRatio;
            data.magicRatio = magicRatio;
            return data;
        }

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

        @Override
        public Codec<DualityData> getCodec() {
            return CODEC;
        }
    }
}