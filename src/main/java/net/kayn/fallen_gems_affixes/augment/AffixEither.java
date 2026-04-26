package net.kayn.fallen_gems_affixes.augment;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.rtxyd.fallen.lib.util.IEither;

public class AffixEither implements IEither<DynamicHolder<? extends Affix>, GemBonus> {
    private final DynamicHolder<? extends Affix> affix;

    public AffixEither(DynamicHolder<? extends Affix> affix) {
        this.affix = affix;
    }

    @Override
    public DynamicHolder<? extends Affix> getA() {
        return affix;
    }

    @Override
    public GemBonus getB() {
        return null;
    }
}
