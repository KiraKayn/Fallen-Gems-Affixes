package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class EnrageAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "enrage");
    private static final UUID   ATK_UUID = UUID.fromString("aa000001-0000-0000-0001-000000000001");
    private static final UUID   SPD_UUID = UUID.fromString("aa000001-0000-0000-0001-000000000002");
    private static final String ATK_NAME = "fga:enrage_attack";
    private static final String SPD_NAME = "fga:enrage_speed";

    public static final Codec<EnrageAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.FLOAT.fieldOf("threshold").forGetter(a -> a.threshold),
            ScaledValue.CODEC.fieldOf("attack_boost").forGetter(a -> a.attackBoost),
            ScaledValue.CODEC.fieldOf("speed_boost").forGetter(a -> a.speedBoost)
    ).apply(inst, EnrageAffix::new));

    private final float threshold;
    private final ScaledValue attackBoost;
    private final ScaledValue speedBoost;

    public EnrageAffix(float threshold, ScaledValue attackBoost, ScaledValue speedBoost) {
        this.threshold   = threshold;
        this.attackBoost = attackBoost;
        this.speedBoost  = speedBoost;
    }

    @Override
    public void onTick(LivingEntity entity, float level) {
        boolean enraged = (entity.getHealth() / entity.getMaxHealth()) <= threshold;

        AttributeInstance atk = entity.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance spd = entity.getAttribute(Attributes.MOVEMENT_SPEED);

        if (enraged) {
            if (atk != null && atk.getModifier(ATK_UUID) == null)
                atk.addTransientModifier(new AttributeModifier(ATK_UUID, ATK_NAME, attackBoost.get(level), AttributeModifier.Operation.MULTIPLY_BASE));
            if (spd != null && spd.getModifier(SPD_UUID) == null)
                spd.addTransientModifier(new AttributeModifier(SPD_UUID, SPD_NAME, speedBoost.get(level), AttributeModifier.Operation.MULTIPLY_BASE));
        } else {
            if (atk != null) atk.removeModifier(ATK_UUID);
            if (spd != null) spd.removeModifier(SPD_UUID);
        }
    }

    @Override public int getTickInterval()                             { return 10; }
    @Override public ResourceLocation getType()                        { return TYPE; }
    @Override public Codec<? extends EntityAffix> getCodec()           { return CODEC; }
}