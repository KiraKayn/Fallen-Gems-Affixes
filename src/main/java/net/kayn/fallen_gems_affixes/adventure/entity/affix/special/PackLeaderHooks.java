package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.adventure.entity.MobAffixHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PackLeaderHooks {

    @SubscribeEvent
    public static void onLeaderDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (!MobAffixHelper.hasAffixes(entity)) return;

        boolean isPackLeader = MobAffixHelper.getAffixes(entity).stream()
                .anyMatch(r -> r.affix() instanceof PackLeaderAffix);

        if (isPackLeader) {
            PackLeaderAffix.onLeaderDeath(entity);
        }
    }

    private PackLeaderHooks() {}
}