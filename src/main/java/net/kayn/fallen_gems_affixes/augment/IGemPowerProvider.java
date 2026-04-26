package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.rtxyd.fallen.lib.util.IEither;
import net.rtxyd.fallen.lib.util.ins_attr.IFilterableIAMProvider;
import net.rtxyd.fallen.lib.util.ins_attr.InsAttributeModifier;

public interface IGemPowerProvider extends IFilterableIAMProvider<IEither<DynamicHolder<? extends Affix>, GemBonus>> {
    float getGemPower();
    @Override
    default InsAttributeModifier getModifierBy(IEither<DynamicHolder<? extends Affix>, GemBonus> a){
        if (test(a)) {
            return getModifier();
        }
        return InsAttributeModifier.EMPTY;
    }
    @Override
    default boolean test(IEither<DynamicHolder<? extends Affix>, GemBonus> a) {
        return true;
    }
}
