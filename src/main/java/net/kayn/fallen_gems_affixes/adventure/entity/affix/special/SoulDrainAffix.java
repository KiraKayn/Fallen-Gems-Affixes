package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class SoulDrainAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "soul_drain");

    private static final UUID   DRAIN_UUID = UUID.fromString("cc000003-0000-0000-0003-000000000001");
    private static final String DRAIN_NAME = "fga:soul_drain_hp";

    public static final Codec<SoulDrainAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            ScaledValue.CODEC.fieldOf("steal_amount").forGetter(a -> a.stealAmount),
            Codec.INT.optionalFieldOf("duration_ticks", 200).forGetter(a -> a.duration),
            ScaledValue.CODEC.fieldOf("chance").forGetter(a -> a.chance)
    ).apply(inst, SoulDrainAffix::new));

    private final ScaledValue stealAmount;
    private final int duration;
    private final ScaledValue chance;

    public SoulDrainAffix(ScaledValue stealAmount, int duration, ScaledValue chance) {
        this.stealAmount = stealAmount;
        this.duration    = duration;
        this.chance      = chance;
    }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        if (!(target instanceof Player player)) return;
        if (entity.getRandom().nextFloat() >= chance.get(level)) return;

        AttributeInstance hp = player.getAttribute(Attributes.MAX_HEALTH);
        if (hp == null) return;

        double drain = stealAmount.get(level);

        // Remove any existing drain first so multiple hits don't stack infinitely
        hp.removeModifier(DRAIN_UUID);
        hp.addTransientModifier(new AttributeModifier(DRAIN_UUID, DRAIN_NAME, -drain, AttributeModifier.Operation.ADDITION));

        // Cap health if now exceeds new max
        if (player.getHealth() > player.getMaxHealth())
            player.setHealth(player.getMaxHealth());

        // Schedule removal after duration
        net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler.schedule(entity.level(), duration, () -> {
            if (!player.isRemoved()) {
                AttributeInstance inst = player.getAttribute(Attributes.MAX_HEALTH);
                if (inst != null) inst.removeModifier(DRAIN_UUID);
            }
        });
    }

    @Override public ResourceLocation getType()              { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec() { return CODEC; }
}
