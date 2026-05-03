package net.kayn.fallen_gems_affixes.augment;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DurableAffix;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentInstance;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentRecipe;
import net.kayn.fallen_gems_affixes.attachment.augment.SpecialAffixEventHandler;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.kayn.fallen_gems_affixes.util.MiscUtil;
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
import net.rtxyd.fallen.lib.util.IEither;
import net.rtxyd.fallen.lib.util.ins_attr.InsAttributeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ConvergenceAugment implements IAugment {

    private static final ResourceLocation CONVERGENCE_ID =
            ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "convergence");
    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(ConvergenceData.CODEC);

    @Override
    public ResourceLocation getId() {
        return CONVERGENCE_ID;
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
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        ConvergenceData data = new ConvergenceData();
        return data;
    }

    @Override
    public String toString() {
        return IAugment.string(this);
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        ConvergenceData data = new ConvergenceData();
        data.deserializeNBT(tag);
        return data;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui,
                            IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        ItemStack displayStack = AugmentItem.createAugment(Fallen.Augments.CONVERGENCE);
        PoseStack pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(displayStack, 0, 0);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        if (innerData instanceof ConvergenceData data) {
            return data.combineText().withStyle(ChatFormatting.YELLOW);
        }
        return Component.empty();
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  java.util.List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("fallen_gems_affixes.augment.convergence.type")
                .withStyle(ChatFormatting.GOLD));

        AugmentMeta meta = AugmentItem.getAugmentData(stack);
        float eachAffixPower;
        float eachGemPower;
        ConvergenceData data;
        if (meta == null) {
            data = (ConvergenceData) Fallen.Augments.CONVERGENCE.fallbackInnerData();
        } else {
            data = (ConvergenceData) meta.newDefaultData();
        }
        eachAffixPower = data.eachAffixPower;
        eachGemPower = data.eachGemPower;

        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                                "fallen_gems_affixes.augment.convergence.desc",
                                MiscUtil.formatFlat(eachGemPower), MiscUtil.formatFlat(eachAffixPower))
                        .withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public boolean onApply(ItemStack stack, Map<IAugment, AugmentInstance> mutableAugMap, AugmentInstance inst) {
        return true;
    }

    public static class ConvergenceData implements IAugmentInnerData, IAffixPowerProvider, IGemPowerProvider {

        public static final Codec<ConvergenceData> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.FLOAT.fieldOf("each_affix_power").forGetter(d -> d.eachAffixPower),
                        Codec.FLOAT.fieldOf("each_gem_power").forGetter(d -> d.eachGemPower)
                ).apply(inst, (eachAffixPower, eachGemPower) -> {
                    ConvergenceData data = (ConvergenceData) Fallen.Augments.CONVERGENCE.fallbackInnerData();
                    data.eachAffixPower = eachAffixPower;
                    data.eachGemPower = eachGemPower;
                    return data;
                })
        );

        public static final String MODIFIER_NAME = "fallen_gems_affixes:convergence_affix_power";

        public float eachAffixPower = 0.1f;
        public float eachGemPower = 0.1f;
        public ItemStack stack = ItemStack.EMPTY;

        @Override
        public float getAffixPower() {
            if (!stack.isEmpty()) {
                return SocketHelper.getGems(stack).streamValidGems().count() * eachAffixPower;
            }
            return 0;
        }

        @Override
        public InsAttributeModifier getModifier() {
            return new InsAttributeModifier(
                    InsAttributeModifier.Type.ADD_FINAL,
                    MODIFIER_NAME,
                    getAffixPower());
        }

        @Override
        public InsAttributeModifier getModifierBy(IEither<DynamicHolder<? extends Affix>, GemBonus> either) {
            if (this.test(either)) {
                return getModifier();
            }
            return InsAttributeModifier.EMPTY;
        }

        @Override
        public boolean test(IEither<DynamicHolder<? extends Affix>, GemBonus> a) {
            return !(a.getA().get() instanceof DurableAffix);
        }

        @Override
        public float getGemPower() {
            if (!stack.isEmpty()) {
                return AffixHelper.getAffixes(stack).size() * eachGemPower;
            }
            return 0;
        }

        @Override
        public void enable() {}

        @Override
        public void disable() {}

        @Override
        public boolean isFunctional() {
            return true;
        }

        @Override
        public void stackContext(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public IAugmentInnerData copy() {
            ConvergenceData data = new ConvergenceData();
            data.eachGemPower = eachGemPower;
            data.eachAffixPower = eachAffixPower;
            return data;
        }

        @Override
        public MutableComponent combineText() {
            return Component.translatable("fallen_gems_affixes.augment.convergence.socket_desc",
                    MiscUtil.formatFlat(getAffixPower()), MiscUtil.formatFlat(getGemPower()));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat("eachAffixPower", eachAffixPower);
            tag.putFloat("eachGemPower", eachGemPower);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            eachAffixPower = nbt.getFloat("eachAffixPower");
            eachGemPower = nbt.getFloat("eachGemPower");
        }

        @Override
        public Codec<? extends IAugmentInnerData> getCodec() {
            return CODEC;
        }
    }
}
