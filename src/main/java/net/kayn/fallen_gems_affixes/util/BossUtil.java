package net.kayn.fallen_gems_affixes.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;

public class BossUtil {

    private static final TagKey<EntityType<?>> FORGE_BOSSES =
            TagKey.create(Registries.ENTITY_TYPE, new ResourceLocation("forge:bosses"));

    public static boolean isBoss(LivingEntity entity, TagKey<EntityType<?>> extraBossTag) {
        return entity.getType().is(FORGE_BOSSES)
                || entity.getType().is(extraBossTag)
                || isApotheosisBoss(entity);
    }

    public static boolean isApotheosisBoss(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        return tag.getBoolean("apoth.boss");
    }
}
