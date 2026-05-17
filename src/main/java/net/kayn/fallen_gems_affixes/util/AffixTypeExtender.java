package net.kayn.fallen_gems_affixes.util;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;

public final class AffixTypeExtender {
    public static AffixType SET_AFFIX;

    public static void init() {
        try {
            SET_AFFIX = EnumExtender.addEnumValue(AffixType.class, "SET_AFFIX");
            FallenGemsAffixes.LOGGER.info("[FGA] Registered SET_AFFIX in AffixType at ordinal {}", SET_AFFIX.ordinal());
        } catch (Exception e) {
            FallenGemsAffixes.LOGGER.error("[FGA] Failed to extend AffixType", e);
            throw new RuntimeException(e);
        }
    }

    private AffixTypeExtender() {}
}