package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.event.test.MiscEventsHandler;
import net.kayn.fallen_gems_affixes.mixin.SocketHelperMixin;
import net.kayn.fallen_gems_affixes.util.ReflectionFactoryModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

/**
 * This class should manage the logic on both client and server, must be thread-safe
 */
public class GemBonusModifier {
    private static final Path BLACKLIST_PATH = Path.of("./fallen/fallen_ref_blacklist.txt");
    private static final Path MAINS_PATH = Path.of("./fallen/fallen_ref_mains.txt");
    private static final Path TARGETS_PATH = Path.of("./fallen/fallen_ref_targets.txt");
    private static final ThreadLocal<Boolean> isKeyValid = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<ItemStack> currentSuspendedItemStack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

    static {
        ReflectionFactoryModifier.fieldNameFilter.add("cost");
        ReflectionFactoryModifier.fieldNameFilter.add("cool");
        try {
            String[] blacklist = Files.readString(BLACKLIST_PATH).trim().split("\n");
            for (String string : blacklist) {
                try {
                    if (string.isEmpty()) continue;
                    Class<?> cls = Class.forName(string);
                    ReflectionFactoryModifier.blackList.add(cls);
                } catch (Exception e) {
                    FallenGemsAffixes.LOGGER.error("class not found: {}", string);
                }
            }
        } catch (Exception e) {
            FallenGemsAffixes.LOGGER.error("fallen files reading failed");
        }
    }

    public static void bootstrap() {};

    /**
     * The main logic for key check, it accepts the Object from a LootCategory-driven GemBonus,
     * which has a {@link Map} structure , must be {@literal Map<LootCategory, ?>}, ? could be primitive type,
     * <br>
     * which defines an inner class for data serialization, and the ? must be this class
     * @param obj the key when we use method get/getOrDefault from the map
     */
    public static void keyCheck(Object obj) {
        isKeyValid.set(obj instanceof LootRarity);
    }

    /**
     * Must check if the class is what we want, if not, should not do anything.
     * @return whether the whole logic run.
     */
    public static boolean shouldNotModify() {
        return !isKeyValid.get() && currentSuspendedItemStack.get() == ItemStack.EMPTY;
    }

    /**
     * Check if its inner class, and check if the nest host is GemBonus,
     * this check will stricten the structure of this GemBonus,
     * only works when the data class is inner class of the class extending GemBonus
     * @param clazz the object class we accepted
     * @return whether the class is valid
     */
    public static boolean clazzCheck(Class<?> clazz) {
        Class<?> host = clazz.getNestHost();
//        return GemBonus.class.isAssignableFrom(host) && !ReflectionFactoryModifier.blackList.contains(clazz);
        return GemBonus.class.isAssignableFrom(host);
    }

    /**
     * Modify the value by creating a new instance
     * @param obj the value we get from the map
     * @return new object having the same class with obj
     */
    public static Object modifyL(Object obj, float multiplied) {
        return ReflectionFactoryModifier.copyAndModifyNumbers(obj, multiplied);
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
        }
        else if (clazzCheck(obj.getClass())) {
            multiplied = getGemPower(stack);
            return modifyL(obj, multiplied);
        }
        return obj;
    }

    /**
     * Get GemPower by reading tags on the suspended item
     * @param stack the item which was suspended by our hooks,
     *              see {@link SocketHelperMixin} {@link MiscEventsHandler#onTooltipEvent(ItemTooltipEvent)}
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

    /**
     * For unusual Map which's value is int
     * @param value int
     * @return int
     */
    public static int modifyI(int value) {
        if (shouldNotModify()) return value;
        ItemStack stack = currentSuspendedItemStack.get();
        int multiplied = (int) getGemPower(stack);
        return value * multiplied;
    }

    /**
     * For unusual Map which's value is float
     * @param value float
     * @return float
     */
    public static float modifyF(float value) {
        if (shouldNotModify()) return value;
        ItemStack stack = currentSuspendedItemStack.get();
        float multiplied = (float) getGemPower(stack);
        return value * multiplied;
    }

    /**
     * For unusual Map which's value is double
     * @param value double
     * @return double
     */
    public static double modifyD(double value) {
        if (shouldNotModify()) return value;
        ItemStack stack = currentSuspendedItemStack.get();
        double multiplied = (double) getGemPower(stack);
        return value * multiplied;
    }

    /**
     * For unusual Map which's value is long
     * @param value long
     * @return long
     */
    public static long modifyJ(long value) {
        if (shouldNotModify()) return value;
        ItemStack stack = currentSuspendedItemStack.get();
        long multiplied = (long) getGemPower(stack);
        return value * multiplied;
    }

    /**
     * For unusual Map which's value is short
     * @param value short
     * @return short
     */
    public static short modifyS(short value) {
        if (shouldNotModify()) return value;
        ItemStack stack = currentSuspendedItemStack.get();
        short multiplied = (short) getGemPower(stack);
        return (short) (value * multiplied);
    }

    /**
     * For unusual Map which's value is byte
     * @param value byte
     * @return byte
     */
    public static byte modifyB(byte value) {
        if (shouldNotModify()) return value;
        ItemStack stack = currentSuspendedItemStack.get();
        byte multiplied = (byte) getGemPower(stack);
        return (byte) (value * multiplied);
    }
}
