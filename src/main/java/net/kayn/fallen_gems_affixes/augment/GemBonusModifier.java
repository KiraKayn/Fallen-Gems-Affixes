package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.mixin.SocketHelperMixin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.rtxyd.fallen.lib.api.annotation.FallenInserter;
import net.rtxyd.fallen.lib.type.util.patch.IInserterContext;
import net.rtxyd.fallen.lib.util.ObjectModifierFactory;
import net.rtxyd.fallen.lib.util.patch.InserterType;

import java.util.Map;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

/**
 * This class should manage the logic on both client and server, must be thread-safe
 */
public class GemBonusModifier {
    private static ObjectModifierFactory FACTORY;
    private static final ThreadLocal<Boolean> isKeyValid = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<ItemStack> currentSuspendedItemStack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

    /**
     * FallenInserter, where the real point to function GemPowerAugment
     * @param ctx Context which contains method receiver (maybe this) and return.
     * @param args Parameters of the target method.
     * @return Original object or a new object.
     */
    @FallenInserter(type = InserterType.STANDARD)
    public static Object modifier(IInserterContext<Object, Object> ctx, Object... args) {
        if (args.length > 0 && args[0] instanceof LootRarity) {
            Object ret = ctx.ret();
            if (ret != null) {
                return modifyLPre(ctx.ret());
            }
        }
        return ctx.ret();
    }

    /**
     * Hook {@link FallenGemsAffixes}, then register necessary events.
     * @param eventBus
     */
    public static void bootstrap(IEventBus eventBus) {
        if (FACTORY == null) {
            FACTORY = new ObjectModifierFactory(s -> {
                String name = s.toLowerCase();
                if (name.startsWith("cool") || name.endsWith("cool")) return true;
                return name.startsWith("cost") || name.endsWith("cost");
            });
        }
        eventBus.addListener(EventPriority.HIGHEST, GemBonusModifier::onTooltipEvent);
    };

    /**
     * Suspend current ItemStack on client
     */
    public static void onTooltipEvent(ItemTooltipEvent event) {
        suspendItemStack(event.getItemStack());
    }

    /**
     * Suspend the given ItemStack on current thread (client/server).
     * This is the only way to reach the item's data we want to read.
     * @param stack the ItemStack need to be stored for following logic
     */
    public static void suspendItemStack(ItemStack stack) {
        currentSuspendedItemStack.set(stack);
    }

    /**
     * The main logic for key check, it accepts the Object from a LootCategory-driven GemBonus,
     * which has a {@link Map} structure , must be {@literal Map<LootCategory, ?>}, ? could be primitive type,
     * <br>
     * which defines an inner class for data serialization, and the ? must be this class
     * @param obj the key when we use method get/getOrDefault from the map
     */
    @Deprecated
    public static void keyCheck(Object obj) {
        isKeyValid.set(obj instanceof LootRarity);
    }

    /**
     * Must check if the class is what we want, if not, should not do anything.
     * @return whether the whole logic run.
     */
    public static boolean shouldNotModify() {
        return currentSuspendedItemStack.get() == ItemStack.EMPTY;
    }

    /**
     * Check if its inner class, and check if the nest host is GemBonus,
     * this check will stricten the structure of this GemBonus,
     * only works when the data class is inner class of the class extending GemBonus
     * @param clazz the object class we accepted
     * @return whether the class is valid
     */
    public static boolean clazzCheck(Class<?> clazz) {
        if (Number.class.isAssignableFrom(clazz)) {
            return true;
        }
        Class<?> host = clazz.getNestHost();
        return GemBonus.class.isAssignableFrom(host) && !FACTORY.isInBlackList(clazz);
        // when we debug, the blacklist could be ignored.
        // return GemBonus.class.isAssignableFrom(host);
    }

    /**
     * Modify the value by creating a new instance
     * @param obj the value we get from the map
     * @return new object having the same class with obj
     */
    public static Object modifyL(Object obj, float multiplied) {
        return FACTORY.copyAndModifyNumbers(obj, multiplied);
    }

    /**
     *
     * @param obj the key we caught
     * @return the value we output
     */
    public static Object modifyLPre(Object obj) {
        if (shouldNotModify()) return obj;
        ItemStack stack = currentSuspendedItemStack.get();
        float multiplied;
        // StepFunction
        if (obj instanceof StepFunction sf) {
            multiplied = getGemPower(stack);
            return new StepFunction(sf.min() * multiplied, sf.steps(), sf.step() * multiplied);
        } else if (obj instanceof Number) {
            multiplied = getGemPower(stack);
            return ObjectModifierFactory.modifyNumber((Number) obj, multiplied);
        } else if (clazzCheck(obj.getClass())) {
            multiplied = getGemPower(stack);
            return modifyL(obj, multiplied);
        }
        return obj;
    }

    /**
     * Get GemPower by reading tags on the suspended item
     * @param stack the item which was suspended by our hooks,
     *              see {@link SocketHelperMixin} {@link GemBonusModifier#onTooltipEvent(ItemTooltipEvent)}
     * @return
     */
    private static float getGemPower(ItemStack stack) {
        float value = 1F;
        // it is certain the stack is not empty, and we want .
        CompoundTag itemTag = stack.getTag();
        if (itemTag != null && itemTag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) {
            CompoundTag augmentData = itemTag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
            ListTag listTag = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag tag = listTag.getCompound(i);
                ResourceLocation typeId = ResourceLocation.tryParse(tag.getString(TYPE));
                if (GemPowerAugment.augmentId().equals(typeId)) {
                    GemPowerAugment.GemPowerData data = (GemPowerAugment.GemPowerData) Fallen.Augments.GEM_POWER.deserializeInnerData(tag.getCompound(INNER_DATA));
                    return data.getPower();
                }
            }
        }
        return value;
    }
}
