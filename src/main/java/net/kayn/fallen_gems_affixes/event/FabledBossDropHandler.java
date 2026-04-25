package net.kayn.fallen_gems_affixes.event;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.kayn.fallen_gems_affixes.config.ModConfig;
import net.kayn.fallen_gems_affixes.item.augments.AugmentItem;
import net.kayn.fallen_gems_affixes.types.augment.IAugment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = FallenGemsAffixes.MOD_ID)
public class FabledBossDropHandler {

    private static final String BOSS_TAG   = "fga.universal_boss";
    private static final String RARITY_TAG = "fga.universal_boss.rarity";

    @SubscribeEvent
    public static void onBossDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Monster mob)) return;
        if (mob.level().isClientSide()) return;

        CompoundTag data = mob.getPersistentData();
        if (!data.getBoolean(BOSS_TAG)) return;

        String stored = data.getString(RARITY_TAG);
        String path = stored.contains(":") ? stored.substring(stored.indexOf(':') + 1) : stored;
        if (!path.equals("fabled")) return;

        double chance = ModConfig.FABLED_AUGMENT_DROP_CHANCE.get();
        if (chance <= 0.0 || mob.getRandom().nextDouble() >= chance) return;

        List<IAugment> augments = new ArrayList<>(Fallen.Registries.AUGMENT_REGISTRY.registryView().values());
        if (augments.isEmpty()) return;

        IAugment chosen = augments.get(mob.getRandom().nextInt(augments.size()));
        ItemStack drop = AugmentItem.createAugment(chosen);
        if (drop.isEmpty()) return;

        ItemEntity item = new ItemEntity(mob.level(), mob.getX(), mob.getY() + 0.5, mob.getZ(), drop);
        item.setPickUpDelay(10);
        mob.level().addFreshEntity(item);
    }
}