package net.kayn.fallen_gems_affixes.attachment.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ItemStackCakyGetter;
import net.rtxyd.fallen.lib.runtime.forgemod.util.NBTFingerprints;

import java.util.*;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@SuppressWarnings({"ConstantConditions", "rawtypes"})
public class AugmentHelper {
    public static LiveAugments getAugments(ItemStack stack) {
        return ItemStackCakyGetter.resolve(stack, AUGMENT_CACHED_OBJECT, AugmentHelper::getAugmentsA, NBTFingerprints.subTag(AUGMENT_DATA));
    }

    public static void applyAugment(ItemStack stack, AugmentInstance inst) {
        var augments = getAugments(stack);
        Set<AugmentInstance> newAugments = new HashSet<>(augments.instances());
        newAugments.add(inst);
        setAugments(stack, newAugments);
    }

    public static void applyAugmentRaw(ItemStack stack, IAugment augment) {
        applyAugment(stack, new AugmentInstance(augment, IAugmentInnerData.EMPTY));
    }

    public static boolean canApplyTo(LootCategory category, IAugment augment) {
        AugmentMeta meta = Fallen.Registries.AUGMENT_REGISTRY.getMetaData(augment);
        if (meta != null) {
            return meta.canApplyTo(category);
        }
        return false;
    }

    public static void setAugments(ItemStack stack, Set<AugmentInstance> instances) {
        CompoundTag tag1 = stack.getOrCreateTagElement(AUGMENT_DATA);
        var listTag = new ListTag();
        for (AugmentInstance instance : instances) {
            CompoundTag tag = new CompoundTag();
            tag.putString(TYPE, instance.getAugment().getId().toString());
            tag.put(INNER_DATA, instance.getData().serializeNBT());
            listTag.add(tag);
        }
        tag1.put(AUGMENTS, listTag);
    }

    public static boolean hasAugment(ItemStack stack, IAugment augment) {
        return getAugments(stack).get(augment) != null;
    }

    private static LiveAugments getAugmentsA(ItemStack stack) {
        Map<IAugment, AugmentInstance> map = new HashMap<>();
        if (!stack.isEmpty() && stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            ListTag listTag = stack.getTagElement(Fallen.AugmentMisc.AUGMENT_DATA).getList(AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                IAugment augment = Fallen.Registries.AUGMENT_REGISTRY.getValue(tag.getString(TYPE));
                if (augment != null) {
                    CompoundTag inner = tag.getCompound(INNER_DATA);
                    if (inner.isEmpty()) continue;
                    map.put(augment, new AugmentInstance(augment, augment.deserializeInnerData(inner)));
                }
            }
        }
        return new LiveAugments(map);
    }
}
