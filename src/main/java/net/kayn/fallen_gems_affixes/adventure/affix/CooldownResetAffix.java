package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CooldownResetAffix extends Affix {

    public static final Codec<CooldownResetAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    affixDef(),
                    LootRarity.mapCodec(StepFunction.CODEC).fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, CooldownResetAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    private record PendingReset(LivingEntity caster, String spellId) {}
    private static final Queue<PendingReset> PENDING = new ConcurrentLinkedQueue<>();

    public CooldownResetAffix(AffixDefinition def, Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(def);
        this.values = values;
        this.types  = types;
    }

    public void onSpellCast(LivingEntity caster, String spellId, LootRarity rarity, float level) {
        float chance = values.get(rarity).get(level);
        if (caster.getRandom().nextFloat() >= chance) return;
        PENDING.add(new PendingReset(caster, spellId));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTick(ServerTickEvent.Post event) {
        PendingReset reset;
        while ((reset = PENDING.poll()) != null) {
            MagicData magicData = MagicData.getPlayerMagicData(reset.caster());
            if (magicData == null) continue;
            magicData.getPlayerCooldowns().removeCooldown(reset.spellId());
            if (reset.caster() instanceof ServerPlayer sp) {
                magicData.getPlayerCooldowns().syncToPlayer(sp);
            }
        }
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        if (cat.isNone()) return false;
        return (types.isEmpty() || types.contains(cat)) && values.containsKey(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        float chance = values.get(inst.getRarity()).get(inst.level());
        return Component.translatable(
                "affix.fallen_gems_affixes.cooldown_reset.desc",
                Affix.fmt(chance * 100f)
        ).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }
}