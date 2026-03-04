package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.Fallen;
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

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.*;

/**
 * This class should manage the logic on both client and server, must be thread-safe.
 *
 * <p>Genesis priority: if the item carries a Genesis augment, its {@code gemPower} value
 * is used exclusively. No hasGems guard — Genesis always controls gem scaling once applied.
 * Only if Genesis is absent does it fall through to the GemPower augment.
 */
public class GemBonusModifier {
    private static ObjectModifierFactory FACTORY;
    private static final ThreadLocal<Boolean> isKeyValid = ThreadLocal.withInitial(() -> false);
    public static final ThreadLocal<ItemStack> currentSuspendedItemStack = ThreadLocal.withInitial(() -> ItemStack.EMPTY);

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

    public static void bootstrap(IEventBus eventBus) {
        if (FACTORY == null) {
            FACTORY = new ObjectModifierFactory(s -> {
                String name = s.toLowerCase();
                if (name.startsWith("cool") || name.endsWith("cool")) return true;
                return name.startsWith("cost") || name.endsWith("cost");
            });
        }
        eventBus.addListener(EventPriority.HIGHEST, GemBonusModifier::onTooltipEvent);
    }

    public static void onTooltipEvent(ItemTooltipEvent event) {
        suspendItemStack(event.getItemStack());
    }

    public static void suspendItemStack(ItemStack stack) {
        currentSuspendedItemStack.set(stack);
    }

    @Deprecated
    public static void keyCheck(Object obj) {
        isKeyValid.set(obj instanceof LootRarity);
    }

    public static boolean shouldNotModify() {
        return currentSuspendedItemStack.get() == ItemStack.EMPTY;
    }

    public static boolean clazzCheck(Class<?> clazz) {
        if (Number.class.isAssignableFrom(clazz)) {
            return true;
        }
        Class<?> host = clazz.getNestHost();
        return GemBonus.class.isAssignableFrom(host) && !FACTORY.isInBlackList(clazz);
    }

    public static Object modifyL(Object obj, float multiplied) {
        return FACTORY.copyAndModifyNumbers(obj, multiplied);
    }

    public static Object modifyLPre(Object obj) {
        if (shouldNotModify()) return obj;
        ItemStack stack = currentSuspendedItemStack.get();
        float multiplied;
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
     * Returns the gem-power multiplier for {@code stack}.
     *
     * <p>Priority:
     * <ol>
     *   <li><b>Genesis</b> — if present, its {@code gemPower} is returned unconditionally.</li>
     *   <li><b>GemPower augment</b> — classic single-value power.</li>
     *   <li><b>Default</b> — 1.0 (no change).</li>
     * </ol>
     */
    private static float getGemPower(ItemStack stack) {
        CompoundTag itemTag = stack.getTag();
        if (itemTag == null || !itemTag.contains(Fallen.AugmentMisc.AUGMENT_DATA)) return 1F;

        CompoundTag augmentData = itemTag.getCompound(Fallen.AugmentMisc.AUGMENT_DATA);
        ListTag listTag = augmentData.getList(AUGMENTS, Tag.TAG_COMPOUND);
        float currentGemPower = 1F;

        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            ResourceLocation typeId = ResourceLocation.tryParse(tag.getString(TYPE));

            if (GemPowerAugment.augmentId().equals(typeId)) {
                GemPowerAugment.GemPowerData data = (GemPowerAugment.GemPowerData)
                        Fallen.Augments.GEM_POWER.deserializeInnerData(tag.getCompound(INNER_DATA));
                currentGemPower = data.getPower();
            }
            else if (GenesisAugment.augmentId().equals(typeId)) {
                // Genesis present — always return its gemPower, no further checks needed
                Float genesisGemPower = GenesisAugment.getGenesisGemPower(stack);
                currentGemPower = genesisGemPower != null ? genesisGemPower * currentGemPower : currentGemPower;
            }

        }

        return currentGemPower;
    }
}