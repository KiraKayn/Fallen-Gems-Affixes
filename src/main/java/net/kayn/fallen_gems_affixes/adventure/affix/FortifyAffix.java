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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FortifyAffix extends Affix {

    public static final UUID FORTIFY_UUID = UUID.fromString("f07f1f00-aaaa-bbbb-cccc-000000000001");
    public static final String FORTIFY_NAME = "fga:fortify_armor";

    public static final Codec<FortifyAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, FortifyAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    private static final Map<UUID, Float> BONUS_CACHE = new HashMap<>();

    public FortifyAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types  = types;
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        BONUS_CACHE.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (player.tickCount % 5 != 0) return;

        AttributeInstance armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        armorAttr.removeModifier(FORTIFY_UUID);

        float cachedBonus = BONUS_CACHE.computeIfAbsent(player.getUUID(), k -> computeBonus(player));
        if (cachedBonus <= 0f) return;

        boolean standing = player.getDeltaMovement().horizontalDistanceSqr() < 0.001
                && !player.isFallFlying()
                && !player.isSwimming();

        if (!standing) return;

        // Remove first so getAttributeValue doesn't include our own modifier
        double armorWithoutBonus = armorAttr.getValue();
        armorAttr.addTransientModifier(new AttributeModifier(
                FORTIFY_UUID, FORTIFY_NAME,
                armorWithoutBonus * cachedBonus,
                AttributeModifier.Operation.ADDITION));
    }

    private static float computeBonus(Player player) {
        float total = 0f;
        for (ItemStack stack : player.getAllSlots()) {
            for (var inst : AffixHelper.getAffixes(stack).values()) {
                if (!inst.isValid()) continue;
                if (!(inst.affix().get() instanceof FortifyAffix affix)) continue;
                total += affix.values.get(inst.rarity().get()).get(inst.level());
            }
        }
        return total;
    }

    public static void clearState(UUID uuid) {
        BONUS_CACHE.remove(uuid);
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