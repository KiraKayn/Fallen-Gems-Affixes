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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.rtxyd.fallen.lib.util.ins_attr.InsAttributeModifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GemPowerAugment implements IAugment {
    private static final ResourceLocation GEM_POWER_ID = ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "gem_power");
    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(GemPowerData.CODEC);
    @Override
    public ResourceLocation getId() {
        return GEM_POWER_ID;
    }

    @Override
    public String toString() {
        return IAugment.string(this);
    }

    @Override
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        GemPowerData data = new GemPowerData();
        data.power = 0.5f;
        return data;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean shouldAttachToEntity() {
        return false;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);

        var data = AugmentItem.getAugmentData(GEM_POWER_ID);
        if (data == null) return;

        ItemStack stack = AugmentItem.createAugment(Fallen.Augments.GEM_POWER);

        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(stack, 0, 0);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof GemPowerData data) {
            return data.combineText().withStyle(ChatFormatting.YELLOW);
        }

        return Component.empty();
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        GemPowerData data = new GemPowerData();
        data.deserializeNBT(tag);

        return data;
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level, java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.gem_power.type")
                .withStyle(ChatFormatting.GOLD));

        float power = 1.0f;

        String id = AugmentItem.getAugmentId(stack);
        AugmentMeta data = Fallen.Registries.AUGMENT_REGISTRY.getMetaData(ResourceLocation.parse(id));
        if (data == null) {
            return;
        }

        power = ((GemPowerData) data.newDefaultData()).getGemPower();

        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.gem_power.desc", power + 1)
                        .withStyle(ChatFormatting.YELLOW)));
    }

    public static class GemPowerData implements IAugmentInnerData, IGemPowerProvider {
        public static final Codec<GemPowerData> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.FLOAT.fieldOf("power").forGetter(d -> d.power)
                ).apply(inst, (power) -> {
                    GemPowerData data = new GemPowerData();
                    data.power = power;
                    return data;
                })
        );
        float power;

        @Override
        public void enable() {}

        @Override
        public void disable() {}

        @Override
        public boolean isFunctional() {
            return true;
        }

        @Override
        public IAugmentInnerData copy() {
            var data = new GemPowerData();
            data.power = power;
            return data;
        }

        @Override
        public MutableComponent combineText() {
            return Component.translatable(Fallen.Augments.GEM_POWER.getDescString(), power + 1);
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

        @Override
        public float getGemPower() {
            return power;
        }

        @Override
        public Codec<GemPowerData> getCodec() {
            return CODEC;
        }

        @Override
        public InsAttributeModifier getModifier() {
            return InsAttributeModifier.EMPTY;
        }
    }
}