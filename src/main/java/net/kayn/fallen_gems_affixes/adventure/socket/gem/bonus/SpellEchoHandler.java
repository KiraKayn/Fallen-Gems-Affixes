package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.kayn.fallen_gems_affixes.util.SpellCastUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix.isCurrentlyTriggering;
import static net.kayn.fallen_gems_affixes.adventure.affix.SpellCastAffix.setTriggering;

public class SpellEchoHandler {

    public static final Map<UUID, Long> LAST_ECHO_TICK = new ConcurrentHashMap<>();

    public static void handle(LivingEntity caster, AbstractSpell spell, int spellLevel, LivingEntity target) {
        if (caster.level().isClientSide()) return;
        if (isCurrentlyTriggering(caster)) return;

        if (spell.getCastType() == CastType.CONTINUOUS) return;

        SchoolType spellSchool = spell.getSchoolType();
        if (spellSchool == null) return;

        MagicData originalMagic = MagicData.getPlayerMagicData(caster);
        ICastData originalCastData = originalMagic.getAdditionalCastData();

        SpellEchoBonus bonus = null;
        LootRarity rarity = null;

        outer:
        for (ItemStack stack : caster.getAllSlots()) {
            if (stack.isEmpty()) continue;
            for (GemInstance inst : SocketHelper.getGems(stack).gems()) {
                if (!inst.isValid()) continue;
                Optional<?> opt = inst.gem().get().getBonus(inst.cat(), inst.rarity().get());
                if (opt.isPresent() && opt.get() instanceof SpellEchoBonus found) {
                    SchoolType echoSchool = found.getSchool();
                    if (echoSchool != null && echoSchool.equals(spellSchool)) {
                        bonus = found;
                        rarity = inst.rarity().get();
                        break outer;
                    }
                }
            }
        }

        if (bonus == null || rarity == null) return;

        long now = caster.level().getGameTime();
        long lastEcho = LAST_ECHO_TICK.getOrDefault(caster.getUUID(), -1000L);
        int delay = bonus.getDelayTicks(rarity);

        if (now - lastEcho < delay) return;
        LAST_ECHO_TICK.put(caster.getUUID(), now);

        DelayedTaskScheduler.schedule(caster.level(), delay,
                () -> waitThenEcho(caster, spell, spellLevel, target, originalCastData));
    }

    private static void waitThenEcho(LivingEntity caster, AbstractSpell spell, int level,
                                     LivingEntity target, ICastData originalCastData) {
        if (caster.isRemoved() || caster.isDeadOrDying()) return;

        MagicData data = MagicData.getPlayerMagicData(caster);
        if (data.isCasting()) {
            DelayedTaskScheduler.schedule(caster.level(), 1,
                    () -> waitThenEcho(caster, spell, level, target, data.getAdditionalCastData()));
            return;
        }

        setTriggering(caster, true);
        try {
            if (originalCastData != null) {
                data.setAdditionalCastData(originalCastData);
            }
            if (target != null && !target.isRemoved()) {
                target.invulnerableTime = 0;
            }
            SpellCastUtil.castSpell(caster, spell, level, target);
        } catch (Exception e) {
            FallenGemsAffixes.LOGGER.warn("Echo failed for spell {}", spell.getSpellId(), e);
        } finally {

            var server = caster.level().getServer();
            if (server != null) {
                server.execute(() -> setTriggering(caster, false));
            } else {
                setTriggering(caster, false);
            }
        }
    }
}