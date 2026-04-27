package net.kayn.fallen_gems_affixes.attachment.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixApplyEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixCacheRefreshEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixGetFinishEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ItemStackCakyHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.TO_MODIFY_AFFIXES_OBJECT;

public class SpecialAffixEventHandler {
    private static final ThreadLocal<Boolean> NOT_REFRESH = ThreadLocal.withInitial(() -> false);
    private static final ThreadLocal<AtomicInteger> REFRESHER_TMA = ThreadLocal.withInitial(AtomicInteger::new);

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(SpecialAffixEventHandler::onAffixApply);
        MinecraftForge.EVENT_BUS.addListener(SpecialAffixEventHandler::inAffixCacheRefresh);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, SpecialAffixEventHandler::onAffixGetFinish);
    }

    private static void onAffixApply(AffixApplyEvent event) {
        NOT_REFRESH.set(true);
    }

    private static void inAffixCacheRefresh(AffixCacheRefreshEvent event) {
        if (NOT_REFRESH.get()) return;
        NOT_REFRESH.set(false);
        ToModifyAffixes toModifyAffixes = getToModifyAffixesRefresh(event.getItem(), event.getAffixes());
        if (toModifyAffixes.getFactor().isEmpty()) return;
        event.setCacheAffixes(toModifyAffixes.output());
    }

    private static void onAffixGetFinish(AffixGetFinishEvent event) {
        ItemStack stack = event.getItem();
        var inses = event.getAffixesView();
        ToModifyAffixes affixes = getToModifyAffixes(stack, inses);
        var augs = AugmentHelper.getAugments(stack);
        if (affixes.getFactor() != augs) {
            getAffixesManualRefresh(stack, inses, augs,1);
            try {
                NOT_REFRESH.set(true);
                event.setTransientAffixes(getAffixesManualRefresh(stack, inses, augs, 0));
            } finally {
                NOT_REFRESH.set(false);
            }
        }
    }

    private static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesManualRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> inses, LiveAugments augs, int refresh) {
        return CachedObject.CachedObjectSource.getOrCreate(stack, AffixHelper.AFFIX_CACHED_OBJECT,
                i -> getAffixesCC(i, inses, augs, refresh),
                i -> refresh + CachedObject.hashSubkey(AffixHelper.AFFIX_DATA).applyAsInt(i));
    }

    private static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesCC(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> inses, LiveAugments augs, int refresh) {
        if (refresh == 1) {
            return inses;
        } else {
            return getToModifyAffixesManualRefresh(stack, inses, augs).output();
        }
    }

    public static ToModifyAffixes getToModifyAffixes(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        return ItemStackCakyHandler.resolve(stack, TO_MODIFY_AFFIXES_OBJECT, s -> getToModifyAffixesA(s, affixes), i -> REFRESHER_TMA.get().get());
    }

    private static ToModifyAffixes getToModifyAffixesRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        return ItemStackCakyHandler.resolve(stack, TO_MODIFY_AFFIXES_OBJECT, s -> getToModifyAffixesA(s, affixes), i -> REFRESHER_TMA.get().incrementAndGet());
    }

    private static ToModifyAffixes getToModifyAffixesA(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        if (stack.isEmpty()) return ToModifyAffixes.EMPTY;
        LiveAugments augments = AugmentHelper.getAugments(stack);
        if (affixes.isEmpty() && augments.isEmpty()) return ToModifyAffixes.EMPTY;
        return new ToModifyAffixes(affixes, augments);
    }

    private static ToModifyAffixes getToModifyAffixesManualRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes, LiveAugments augments) {
        return ItemStackCakyHandler.resolve(stack, TO_MODIFY_AFFIXES_OBJECT, s -> new ToModifyAffixes(affixes, augments), i -> REFRESHER_TMA.get().incrementAndGet());
    }
}
