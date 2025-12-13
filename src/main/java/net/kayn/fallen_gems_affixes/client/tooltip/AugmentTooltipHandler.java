package net.kayn.fallen_gems_affixes.client.tooltip;

import com.mojang.datafixers.util.Either;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentSlotHelper;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentInnerData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AugmentTooltipHandler {

    @SubscribeEvent
    @SuppressWarnings("ConstantConditions")
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        int augmentSlots = AugmentSlotHelper.getAugmentSlots(stack);
        if (augmentSlots <= 0) return;

        ListTag listTag = new ListTag();
        if (stack.hasTag() && stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
                CompoundTag augmentData = stack.getTag().getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
                listTag = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
        }

        int filled = listTag.size();
        int empty = Math.max(0, augmentSlots - filled);

        for (int i = 0; i < empty; i++) {
            event.getTooltipElements().add(Either.right(new AugmentTooltipComponent(null, null)));
        }

        for (int i = 0; i < filled; i++) {
            CompoundTag tag = listTag.getCompound(i);

            ResourceLocation typeId = ResourceLocation.tryParse(tag.getString(TYPE));
            IAugment augment = AugmentRegistry.get(typeId);

            if (augment != null) {
                IAugmentInnerData inner = augment.deserializeInnerData(tag.getCompound(INNER_DATA));
                event.getTooltipElements().add(Either.right(new AugmentTooltipComponent(augment, inner)));
            } else {
                event.getTooltipElements().add(Either.right(new AugmentTooltipComponent(null, null)));
            }
        }
    }
}