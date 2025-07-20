package net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import net.kayn.fallen_gems_affixes.util.BossUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = "fallen_gems_affixes")
public class BossSlayerHandler {

    private static final TagKey<EntityType<?>> BOSS_TAG = TagKey.create(
            ForgeRegistries.ENTITY_TYPES.getRegistryKey(),
            new ResourceLocation("fallen_gems_affixes", "boss_slayer")
    );

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;
        if (!BossUtil.isBoss(target, BOSS_TAG)) return;

        ItemStack weapon = attacker.getMainHandItem();
        LootCategory category = LootCategory.forItem(weapon);

        SocketHelper.getGems(weapon).forEach(gemInstance -> {
            if (!gemInstance.isValid()) return;
            if (!gemInstance.rarity().isBound()) return;

            LootRarity rarity = gemInstance.rarity().get();
            gemInstance.gem().get().getBonus(category, rarity).ifPresent(bonus -> {
                if (bonus instanceof BossSlayerBonus slayer && slayer.supports(rarity)) {
                    double mod = slayer.values.get(rarity).get(0);
                    event.setAmount((float) (event.getAmount() * (1.0 + mod)));
                }
            });
        });
    }
}