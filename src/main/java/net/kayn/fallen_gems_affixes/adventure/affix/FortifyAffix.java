package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.kayn.fallen_gems_affixes.adventure.entity.EntityAffixBehavior;
import net.kayn.fallen_gems_affixes.util.DelayedTaskScheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FortifyAffix extends Affix implements EntityAffixBehavior {

    public static final UUID   FORTIFY_UUID = UUID.fromString("f07f1f00-aaaa-bbbb-cccc-000000000001");
    public static final String FORTIFY_NAME = "fga:fortify_armor";

    public static final Codec<FortifyAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, FortifyAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory>             types;

    public FortifyAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types  = types;
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        event.getEntity().getPersistentData().remove(FORTIFY_NAME);
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;
        if (entity.tickCount % 5 != 0) return;

        Vec3 lastPosition = entity.position();
        DelayedTaskScheduler.schedule(entity.level(), 1, () -> {
            if (!entity.isAlive()) return;
            AttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
            if (armorAttr == null) return;
            armorAttr.removeModifier(FORTIFY_UUID);

            CompoundTag tag = entity.getPersistentData();
            float cachedBonus;
            if (!tag.contains(FORTIFY_NAME)) {
                cachedBonus = computeBonus(entity);
                tag.putFloat(FORTIFY_NAME, cachedBonus);
            } else {
                cachedBonus = tag.getFloat(FORTIFY_NAME);
            }
            if (cachedBonus <= 0f) return;

            if (entity.position().distanceToSqr(lastPosition) < 0.001) {
                double armorWithoutBonus = armorAttr.getValue();
                armorAttr.addTransientModifier(new AttributeModifier(
                        FORTIFY_UUID, FORTIFY_NAME,
                        armorWithoutBonus * cachedBonus,
                        AttributeModifier.Operation.ADDITION));
            }
        });
    }

    private static float computeBonus(LivingEntity entity) {
        float total = 0f;
        for (ItemStack stack : entity.getAllSlots()) {
            for (var inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (!(inst.affix().get() instanceof FortifyAffix affix)) continue;
                total += affix.values.get(inst.rarity().get()).get(inst.level());
            }
        }
        return total;
    }

    @Override
    public void tickEntityAffix(LivingEntity entity, LootRarity rarity, float level) {
        Vec3 lastPos = entity.position();
        DelayedTaskScheduler.schedule(entity.level(), 1, () -> {
            if (!entity.isAlive()) return;
            AttributeInstance armorAttr = entity.getAttribute(Attributes.ARMOR);
            if (armorAttr == null) return;

            // Remove first to get clean baseline
            armorAttr.removeModifier(FORTIFY_UUID);
            float bonus = values.get(rarity).get(level);
            if (bonus <= 0f) return;

            if (entity.position().distanceToSqr(lastPos) < 0.001) {
                double base = armorAttr.getValue();
                armorAttr.addTransientModifier(new AttributeModifier(
                        FORTIFY_UUID, FORTIFY_NAME,
                        base * bonus,
                        AttributeModifier.Operation.ADDITION));
            }
        });
    }

    @Override
    public int tickInterval() {
        return 5;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        return Component.translatable(
                "affix.fallen_gems_affixes.fortify.desc",
                Affix.fmt(values.get(rarity).get(level) * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}