package net.kayn.fallen_gems_affixes.adventure.entity.affix.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.EntityAffix;
import net.kayn.fallen_gems_affixes.adventure.entity.affix.ScaledValue;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class CursedAffix extends EntityAffix {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath("fallen_gems_affixes", "cursed");

    public static final Codec<CursedAffix> CODEC = RecordCodecBuilder.create(inst -> inst.group(ScaledValue.CODEC
            .fieldOf("chance").forGetter(a -> a.chance), Codec.INT.optionalFieldOf("duration_ticks", 80)
            .forGetter(a -> a.duration), Codec.INT.optionalFieldOf("amplifier", 0).forGetter(a -> a.amplifier), ResourceLocation.CODEC
            .listOf().optionalFieldOf("blacklist", List.of(ResourceLocation.fromNamespaceAndPath("minecraft", "bad_omen")))
            .forGetter(a -> a.blacklist)).apply(inst, CursedAffix::new));

    private final ScaledValue chance;
    private final int duration;
    private final int amplifier;
    private final List<ResourceLocation> blacklist;

    private volatile List<MobEffect> cachedPool = null;

    public CursedAffix(ScaledValue chance, int duration, int amplifier, List<ResourceLocation> blacklist) {
        this.chance = chance;
        this.duration = duration;
        this.amplifier = amplifier;
        this.blacklist = blacklist;
    }

    private List<MobEffect> getPool() {
        if (cachedPool != null) return cachedPool;

        synchronized (this) {
            if (cachedPool == null) {
                List<MobEffect> pool = new ArrayList<>();
                for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
                    if (effect.isBeneficial()) continue;
                    ResourceLocation key = ForgeRegistries.MOB_EFFECTS.getKey(effect);
                    if (key == null || blacklist.contains(key)) continue;

                    pool.add(effect);
                }
                cachedPool = List.copyOf(pool);
            }
        }
        return cachedPool;
    }

    @Override
    public void doPostAttack(LivingEntity entity, Entity target, float level) {
        if (!(target instanceof LivingEntity living)) return;
        if (entity.getRandom().nextFloat() >= chance.get(level)) return;

        List<MobEffect> pool = getPool();
        if (pool.isEmpty()) return;
        MobEffect effect = pool.get(entity.getRandom().nextInt(pool.size()));
        living.addEffect(new MobEffectInstance(effect, duration, amplifier));
    }

    @Override
    public ResourceLocation getType() {
        return TYPE;
    }

    @Override
    public Codec<? extends EntityAffix> getCodec() {
        return CODEC;
    }
}