package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.event.GetItemSocketsEvent;
import net.kayn.fallen_gems_affixes.adventure.affix.SocketBonusAffix;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.StreamSupport;

public class FallenEventHandler {

    @SubscribeEvent
    public static void hookAddSocketsAffix(GetItemSocketsEvent event) {
        ItemStack stack = event.getStack();

        if (!AffixHelper.hasAffixes(stack)) {
            return;
        }

        int affixBonus = StreamSupport.stream(AffixHelper.streamAffixes(stack).spliterator(), false)
                .filter(inst -> inst.affix().isBound() && inst.affix().get() instanceof SocketBonusAffix)
                .mapToInt(inst -> {
                    SocketBonusAffix affix = (SocketBonusAffix) inst.affix().get();
                    return affix.getBonusSockets(inst.rarity().get(), inst.level());
                })
                .sum();

        if (affixBonus > 0) {
            event.setSockets(event.getSockets() + affixBonus);
        }
    }
}