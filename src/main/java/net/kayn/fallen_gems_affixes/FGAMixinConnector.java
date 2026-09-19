package net.kayn.fallen_gems_affixes;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class FGAMixinConnector implements IMixinConnector {

    @Override
    public void connect() {

        Mixins.addConfiguration("fallen_gems_affixes.mixins.json");

        boolean isISSExist = getClass().getClassLoader().getResource(
                "io/redspace/ironsspellbooks/IronsSpellbooks.class") != null;
        if (isISSExist) {
            Mixins.addConfiguration("fallen_gems_affixes.iss.mixins.json");
        }

        boolean isCelestisynthExist = getClass().getClassLoader().getResource(
                "org/thecelestialworkshop/celestisynth/Celestisynth.class") != null;
        if (isCelestisynthExist) {
            Mixins.addConfiguration("fallen_gems_affixes.celestisynth.mixins.json");
        }
    }
}