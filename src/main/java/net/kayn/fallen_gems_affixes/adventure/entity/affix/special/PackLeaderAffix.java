package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PackLeaderAffix extends EntityAffix {

    public static final ResourceLocation TYPE =
            ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "pack_leader");

    private static final UUID   ATK_UUID = UUID.fromString("bb000002-0000-0000-0002-000000000001");
    private static final UUID   SPD_UUID = UUID.fromString("bb000002-0000-0000-0002-000000000002");
    private static final String ATK_NAME = "fga:pack_leader_attack";
    private static final String SPD_NAME = "fga:pack_leader_speed";

    private static final Map<UUID, Set<UUID>> BUFFED_MOBS = new HashMap<>();

    public static final Codec<PackLeaderAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.optionalFieldOf("radius", 16.0f).forGetter(a -> a.radius),
            ScaledValue.CODEC.fieldOf("attack_boost").forGetter(a -> a.attackBoost),
            ScaledValue.CODEC.fieldOf("speed_boost").forGetter(a -> a.speedBoost),
            Codec.INT.optionalFieldOf("interval", 40).forGetter(a -> a.interval)
    ).apply(inst, PackLeaderAffix::new));

    private final float radius;
    private final ScaledValue attackBoost;
    private final ScaledValue speedBoost;
    private final int interval;

    public PackLeaderAffix(float radius, ScaledValue attackBoost,
                           ScaledValue speedBoost, int interval) {
        this.radius      = radius;
        this.attackBoost = attackBoost;
        this.speedBoost  = speedBoost;
        this.interval    = interval;
    }

    @Override
    public void onTick(LivingEntity entity, float level) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        UUID leaderId = entity.getUUID();
        Set<UUID> prevBuffed = BUFFED_MOBS.getOrDefault(leaderId, Collections.emptySet());

        List<Mob> inRange = serverLevel.getEntitiesOfClass(
                Mob.class,
                entity.getBoundingBox().inflate(radius),
                m -> m != entity && m.getType() == entity.getType() && m.isAlive());

        Set<UUID> nowBuffed = new HashSet<>();
        for (Mob mob : inRange) {
            applyOrRefresh(mob, Attributes.ATTACK_DAMAGE, ATK_UUID, ATK_NAME, attackBoost.get(level));
            applyOrRefresh(mob, Attributes.MOVEMENT_SPEED, SPD_UUID, SPD_NAME, speedBoost.get(level));
            nowBuffed.add(mob.getUUID());
        }

        for (UUID mobId : prevBuffed) {
            if (nowBuffed.contains(mobId)) continue;
            Entity mob = serverLevel.getEntity(mobId);
            if (mob instanceof Mob m) {
                removeModifier(m, Attributes.ATTACK_DAMAGE, ATK_UUID);
                removeModifier(m, Attributes.MOVEMENT_SPEED, SPD_UUID);
            }
        }

        if (nowBuffed.isEmpty()) {
            BUFFED_MOBS.remove(leaderId);
        } else {
            BUFFED_MOBS.put(leaderId, nowBuffed);
        }
    }

    public static void onLeaderDeath(LivingEntity leader) {
        Set<UUID> buffed = BUFFED_MOBS.remove(leader.getUUID());
        if (buffed == null || buffed.isEmpty()) return;
        if (!(leader.level() instanceof ServerLevel serverLevel)) return;

        for (UUID mobId : buffed) {
            Entity mob = serverLevel.getEntity(mobId);
            if (mob instanceof Mob m) {
                removeModifier(m, Attributes.ATTACK_DAMAGE, ATK_UUID);
                removeModifier(m, Attributes.MOVEMENT_SPEED, SPD_UUID);
            }
        }
    }

    private static void applyOrRefresh(Mob mob, Attribute attr,
                                       UUID uuid, String name, double value) {
        AttributeInstance inst = mob.getAttribute(attr);
        if (inst == null) return;
        if (inst.getModifier(uuid) == null)
            inst.addTransientModifier(
                    new AttributeModifier(uuid, name, value,
                            AttributeModifier.Operation.MULTIPLY_BASE));
    }

    private static void removeModifier(Mob mob, Attribute attr, UUID uuid) {
        AttributeInstance inst = mob.getAttribute(attr);
        if (inst != null) inst.removeModifier(uuid);
    }

    @Override public int getTickInterval()                    { return interval; }
    @Override public ResourceLocation getType()               { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec()  { return CODEC; }
}