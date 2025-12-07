package net.kayn.fallen_gems_affixes.event.test;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.attachment.AugmentCapability;
import net.kayn.fallen_gems_affixes.attachment.AugmentInstance;
import net.kayn.fallen_gems_affixes.augment.AugmentRegistry;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentAccessor;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentContainer;
import net.kayn.fallen_gems_affixes.types.augment.IAugmentHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

@Mod.EventBusSubscriber
public class MiscEventsHandler {
    /**
     * This event is fired on both client and server
     */
    @SubscribeEvent
    public static void onDataAttachment(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            event.addCapability(AUGMENT_CAP_ID, new AugmentCapability(player));
        }
    }

    /**
     * This event is fired on both client and server
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        // check capability and access fields to see if everything works as intended
        Optional<IAugmentAccessor> capOpt = player.getCapability(AugmentCapability.CAPABILITY).resolve();
        if (capOpt.isPresent()) {
            IAugmentAccessor accessor = capOpt.get();
            IAugmentHandler handler = accessor.getHandler();
            IAugmentContainer container = accessor.getContainer();
        }
    }

    /**
     * This is the main logic to update player Augment data
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        // check capability and access fields to see if everything works as intended
        Optional<IAugmentAccessor> capOpt = player.getCapability(AugmentCapability.CAPABILITY).resolve();
        if (capOpt.isPresent()) {
            IAugmentAccessor accessor = capOpt.get();
            IAugmentHandler handler = accessor.getHandler();

            ItemStack from = event.getFrom();
            addOrRemoveAugment(from, handler, true);

            ItemStack to = event.getTo();
            addOrRemoveAugment(to, handler, false);
        }
    }

    public static void addOrRemoveAugment(ItemStack stack, IAugmentHandler handler, boolean isRemove) {
        if (!stack.isEmpty() && stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
                ListTag listTag = Objects.requireNonNull(stack.getTagElement(AUGMENT_DATA)).getList(AUGMENTS, Tag.TAG_COMPOUND);
                for (int i = 0; i < listTag.size(); i++) {
                    CompoundTag tag = listTag.getCompound(i);
                    String type = tag.getString(TYPE);
                    ResourceLocation loc = ResourceLocation.tryParse(type);
                    IAugment augment = (IAugment) AugmentRegistry.get(loc);
                    if (augment != null) {
                        if (!isRemove) {
                            AugmentInstance instance = augment.createInstanceFromStack(stack);
                            if (instance == null) {
                                return;
                            }
                            instance.enable();
                            if (instance.isFunctional()) {
                                UUID uuid = instance.generateUniqueUUID();
                                handler.addAugment(instance);
                                AugmentInstance.store(instance.getUuid(), instance);
                                tag.putUUID(UNIQUE_ID, uuid);
                            }
                        } else {
                            if (tag.contains(UNIQUE_ID)) {
                                UUID uuid = tag.getUUID(UNIQUE_ID);
                                AugmentInstance instance1 = AugmentInstance.get(uuid);
                                if (instance1 != null) {
                                    handler.removeAugment(instance1);
                                }
                                AugmentInstance.delete(uuid);
                            }
                            tag.remove(UNIQUE_ID);
                        }
                    }
                }
            }
        }
    }

    /**
     * Rendering custom tooltip by augments
     */
    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.hasTag()) {
            assert stack.getTag() != null;
            if (stack.getTag().contains(AUGMENT_DATA)) {
                CompoundTag tag = stack.getTag();
                ListTag listTag = tag.getCompound(AUGMENT_DATA).getList(AUGMENTS, Tag.TAG_COMPOUND);
                for (Tag tag1 : listTag) {
                    ((CompoundTag) tag1).getString(TYPE);
                    net.kayn.fallen_gems_affixes.augment.SoulboundAugment.getAugmentId().toString();
                }
            }
        }
    }
}
