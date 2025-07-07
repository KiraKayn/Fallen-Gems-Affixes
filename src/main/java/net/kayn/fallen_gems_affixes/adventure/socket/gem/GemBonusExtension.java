package net.kayn.fallen_gems_affixes.adventure.socket.gem;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;

public interface GemBonusExtension {
    void fallen_gems_affixes$appendExtraBonus(GemBonus bonus);
    void fallen_gems_affixes$clearExtraBonuses();
}