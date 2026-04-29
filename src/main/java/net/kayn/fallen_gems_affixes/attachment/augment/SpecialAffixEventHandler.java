package net.kayn.fallen_gems_affixes.attachment.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import dev.shadowsoffire.placebo.util.CachedObject;
import net.kayn.fallen_gems_affixes.Fallen;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixApplyEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixCacheRefreshEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.addon.apotheosis.event.AffixGetFinishEvent;
import net.rtxyd.fallen.lib.runtime.forgemod.util.GameLifecycleHelper;
import net.rtxyd.fallen.lib.runtime.forgemod.util.ItemStackCakyHandler;
import net.rtxyd.fallen.lib.util.IObjectCaky;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static net.kayn.fallen_gems_affixes.Fallen.AugmentMisc.AFFIXES_REFRESH_MARKER;
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
        if (NOT_REFRESH.get()) {
            var affixes = GameLifecycleHelper.callAndRemoveIfPresent(Fallen.ContextKeys.AFFIXES_HOLER, GameLifecycleHelper.EMPTY_EX_CONSUMER);
            if (affixes != null) {
                event.setCacheAffixes(affixes);
            }
            NOT_REFRESH.set(false);
            return;
        }
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
            try {
                NOT_REFRESH.set(true);
                GameLifecycleHelper.submitContextCall(Fallen.ContextKeys.AFFIXES_HOLER, () -> inses);
                getAffixesManualRefresh(stack, inses,1);
            } finally {
                NOT_REFRESH.set(false);
            }
            event.setTransientAffixes(getAffixesManualRefresh(stack, inses, 0));
        }
    }

    private static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesManualRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> inses, int refresh) {
        if (refresh == 1) {
            stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA).putBoolean(AFFIXES_REFRESH_MARKER, true);
            return CachedObject.CachedObjectSource.getOrCreate(stack, AffixHelper.AFFIX_CACHED_OBJECT,
                    AffixHelper::getAffixesImpl, CachedObject.hashSubkey(AffixHelper.AFFIX_DATA));
        } else {
            stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA).remove(AFFIXES_REFRESH_MARKER);
            return CachedObject.CachedObjectSource.getOrCreate(stack, AffixHelper.AFFIX_CACHED_OBJECT,
                    AffixHelper::getAffixesImpl, CachedObject.hashSubkey(AffixHelper.AFFIX_DATA));
        }
    }

    private static Map<DynamicHolder<? extends Affix>, AffixInstance> getAffixesCC(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> inses, LiveAugments augs, int refresh) {
        if (refresh == 1) {
            return inses;
        } else {
            return getToModifyAffixesManualRefresh(stack, inses, augs).output();
        }
    }

    public static ToModifyAffixes getToModifyAffixes(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_OBJECT, IObjectCaky.Type.MANUAL, s -> getToModifyAffixesA(s, affixes), i -> REFRESHER_TMA.get().get());
    }

    private static ToModifyAffixes getToModifyAffixesRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_OBJECT, IObjectCaky.Type.MANUAL, s -> getToModifyAffixesA(s, affixes), i -> REFRESHER_TMA.get().incrementAndGet());
    }

    private static ToModifyAffixes getToModifyAffixesA(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        if (stack.isEmpty()) return ToModifyAffixes.EMPTY;
        LiveAugments augments = AugmentHelper.getAugments(stack);
        if (affixes.isEmpty() && augments.isEmpty()) return ToModifyAffixes.EMPTY;
        return new ToModifyAffixes(affixes, augments);
    }

    private static ToModifyAffixes getToModifyAffixesManualRefresh(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes, LiveAugments augments) {
        return ItemStackCakyHandler.resolveWith(stack, TO_MODIFY_AFFIXES_OBJECT, IObjectCaky.Type.MANUAL, s -> new ToModifyAffixes(affixes, augments), i -> REFRESHER_TMA.get().incrementAndGet());
    }
}
