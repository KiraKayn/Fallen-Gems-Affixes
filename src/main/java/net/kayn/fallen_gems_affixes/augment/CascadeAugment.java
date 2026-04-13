package net.kayn.fallen_gems_affixes.augment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.attributeslib.api.ALObjects;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.attachment.augment.AugmentMeta;
import net.kayn.fallen_gems_affixes.client.CascadeAugmentClient;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CascadeAugment implements IAugment {

    private static final ResourceLocation CASCADE_ID =
            ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "cascade");

    public static final float DEFAULT_CHANCE       = 0.35f;
    public static final float DEFAULT_DAMAGE_BONUS = 0.40f;

    private static final String KEY_CHANCE       = "chance";
    private static final String KEY_DAMAGE_BONUS = "damage_bonus";
    private static final Codec<AugmentMeta> META_CODEC = AugmentMeta.codecCreate(CascadeData.CODEC);

    @Override public ResourceLocation getId()   { return CASCADE_ID; }
    @Override public boolean isUnique()          { return true; }
    @Override public boolean shouldAttachToPlayer()     { return false; }

    @Override
    public Codec<AugmentMeta> getMetaDataCodec() {
        return META_CODEC;
    }

    @Override
    public IAugmentInnerData fallbackInnerData() {
        CascadeData data = new CascadeData();
        data.damageBonus = DEFAULT_CHANCE;
        data.chance = DEFAULT_DAMAGE_BONUS;
        return data;
    }

    @Override
    public String toString() {
        return IAugment.string(this);
    }

    @Override
    public IAugmentInnerData deserializeInnerData(CompoundTag tag) {
        CascadeData data = new CascadeData();
        data.deserializeNBT(tag);
        return data;
    }

    @Override
    public void renderImage(@NotNull Font font, int x, int y, GuiGraphics gui, IAugmentInnerData innerData) {
        gui.blit(IAugment.AUGMENT_ICON, x, y, 0, 0, 0, 9, 9, 9, 9);
        AugmentMeta data = Fallen.Registries.AUGMENT_REGISTRY.getMetaData(CASCADE_ID);
        if (data == null) return;
        ItemStack stack = AugmentItem.createAugment(data.getAugment());
        var pose = gui.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(0.5F, 0.5F, 1);
        gui.renderFakeItem(stack, 0, 0);
        pose.popPose();
    }

    @Override
    public MutableComponent organizeTooltipText(IAugmentInnerData innerData) {
        // Safe client-only call via DistExecutor — never touches Minecraft class on server
        CascadeData base = (CascadeData) innerData;
        CascadeData[] effective = {null};
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                effective[0] = CascadeAugmentClient.getEffectiveData(base));

        if (effective[0] != null) {
            boolean scaled = effective[0].chance != base.chance || effective[0].damageBonus != base.damageBonus;
            MutableComponent comp = effective[0].combineText().withStyle(ChatFormatting.YELLOW);

            if (scaled) {
                comp = comp.append(Component.literal(
                        " (" + (int)(base.chance * 100) + "% / " + (int)(base.damageBonus * 100) + "% base)"
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
            return comp;
        }

        return Component.translatable(
                "fallen_gems_affixes.augment.cascade.desc_not_hold",
                (int)(DEFAULT_CHANCE * 100), (int)(DEFAULT_DAMAGE_BONUS * 100)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void appendItemTooltip(ItemStack stack, @Nullable Level level,
                                  List<Component> tooltip, TooltipFlag flag) {
        float chance      = getChanceFromItem(stack);
        float damageBonus = getDamageBonusFromItem(stack);

        tooltip.add(Component.translatable("fallen_gems_affixes.augment.cascade.type")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable(
                        "fallen_gems_affixes.augment.cascade.desc",
                        (int)(chance * 100),
                        (int)(damageBonus * 100)
                ).withStyle(ChatFormatting.YELLOW)));
        tooltip.add(Component.literal("• ")
                .withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("fallen_gems_affixes.augment.cascade.scaling_hint")
                        .withStyle(ChatFormatting.YELLOW)));
    }

    public static CascadeData computeEffective(CascadeData base, LivingEntity entity) {
        // Access the suspended ItemStack.
        // Clientside "currentSuspendedItemStack" stores which item is computed for current tooltip event.
        // Serverside it stores which item is accessing Gems for SocketHelper.
        ItemStack stack = GemBonusModifier.currentSuspendedItemStack.get();
        if (!stack.isEmpty() && entity.getMainHandItem() != stack) return null;

        double critChance = entity.getAttributeValue(ALObjects.Attributes.CRIT_CHANCE.get());
        double critDamage = entity.getAttributeValue(ALObjects.Attributes.CRIT_DAMAGE.get());

        float excessChance  = Math.max(0, (float) critChance - 1.0f);
        float excessDamage  = Math.max(0, (float) critDamage - 2.0f);

        CascadeData effective = new CascadeData();
        // Chance should not over 100%
        effective.chance      = Mth.clamp(base.chance, base.chance + excessChance, 1.0f);
        // Max extra damage 100%
        effective.damageBonus = base.damageBonus + Math.min(1.0f, excessDamage);
        return effective;
    }

    @Nullable
    public static CascadeData getCascadeData(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            CascadeData base = readFromWeapon(stack);
            if (base != null) return computeEffective(base, entity);
        }
        return null;
    }

    @Nullable
    private static CascadeData readFromWeapon(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null) return null;
        if (!root.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return null;

        CompoundTag augmentData = root.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < augments.size(); i++) {
            CompoundTag augment = augments.getCompound(i);
            if (!augment.getString(Fallen.AugmentMisc.TYPE).equals(Fallen.Augments.CASCADE_STRING)) continue;

            CompoundTag innerData = augment.getCompound(Fallen.AugmentMisc.INNER_DATA);
            CascadeData data = new CascadeData();
            data.deserializeNBT(innerData);
            return data;
        }
        return null;
    }

    public static void apply(ItemStack augmentItem, ItemStack weaponStack) {
        float chance      = getChanceFromItem(augmentItem);
        float damageBonus = getDamageBonusFromItem(augmentItem);

        CompoundTag root = weaponStack.getOrCreateTag();
        if (!root.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return;

        CompoundTag augmentData = root.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag augments = augmentData.getList(Fallen.AugmentMisc.AUGMENTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < augments.size(); i++) {
            CompoundTag augment = augments.getCompound(i);
            if (!augment.getString(Fallen.AugmentMisc.TYPE).equals(Fallen.Augments.CASCADE_STRING)) continue;

            CompoundTag innerData = augment.getCompound(Fallen.AugmentMisc.INNER_DATA);
            if (!innerData.contains(KEY_CHANCE))       innerData.putFloat(KEY_CHANCE,       chance);
            if (!innerData.contains(KEY_DAMAGE_BONUS)) innerData.putFloat(KEY_DAMAGE_BONUS, damageBonus);

            augment.put(Fallen.AugmentMisc.INNER_DATA, innerData);
            augments.set(i, augment);
            break;
        }

        augmentData.put(Fallen.AugmentMisc.AUGMENTS, augments);
        root.put(Fallen.AugmentMisc.AUGMENT_DATA, augmentData);
    }

    private static float getChanceFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(KEY_CHANCE)) return tag.getFloat(KEY_CHANCE);
        return DEFAULT_CHANCE;
    }

    private static float getDamageBonusFromItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(KEY_DAMAGE_BONUS)) return tag.getFloat(KEY_DAMAGE_BONUS);
        return DEFAULT_DAMAGE_BONUS;
    }

    public static class CascadeData implements IAugmentInnerData {
        public static final Codec<CascadeData> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Codec.FLOAT.fieldOf(KEY_CHANCE).forGetter(d -> d.chance),
                    Codec.FLOAT.fieldOf(KEY_DAMAGE_BONUS).forGetter(d -> d.damageBonus)
            ).apply(inst, (chance, damageBonus) -> {
                CascadeData data = (CascadeData) Fallen.Augments.CASCADE.fallbackInnerData();
                data.chance = chance;
                data.damageBonus = damageBonus;
                return data;
            })
        );

        public float chance      = DEFAULT_CHANCE;
        public float damageBonus = DEFAULT_DAMAGE_BONUS;

        @Override public void enable() {}
        @Override public void disable() {}
        @Override public boolean isFunctional() { return true; }

        @Override
        public MutableComponent combineText() {
            return Component.translatable(
                    "fallen_gems_affixes.augment.cascade.desc",
                    (int)(chance * 100),
                    (int)(damageBonus * 100));
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat(KEY_CHANCE,       chance);
            tag.putFloat(KEY_DAMAGE_BONUS, damageBonus);
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            chance      = tag.contains(KEY_CHANCE)       ? tag.getFloat(KEY_CHANCE)       : DEFAULT_CHANCE;
            damageBonus = tag.contains(KEY_DAMAGE_BONUS) ? tag.getFloat(KEY_DAMAGE_BONUS) : DEFAULT_DAMAGE_BONUS;
        }

        @Override
        public Codec<? extends IAugmentInnerData> getCodec() {
            return CODEC;
        }
    }
}