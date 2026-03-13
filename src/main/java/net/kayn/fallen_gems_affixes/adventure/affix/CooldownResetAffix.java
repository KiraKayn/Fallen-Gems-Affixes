package net.kayn.fallen_gems_affixes.adventure.affix;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.util.StepFunction;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CooldownResetAffix extends Affix {

    public static final Codec<CooldownResetAffix> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                    GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values),
                    LootCategory.SET_CODEC.fieldOf("types").forGetter(a -> a.types))
            .apply(inst, CooldownResetAffix::new));

    protected final Map<LootRarity, StepFunction> values;
    protected final Set<LootCategory> types;

    private record PendingReset(LivingEntity caster, String spellId) {}
    private static final Queue<PendingReset> PENDING = new ConcurrentLinkedQueue<>();

    public CooldownResetAffix(Map<LootRarity, StepFunction> values, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.values = values;
        this.types  = types;
    }

    public void onSpellCast(LivingEntity caster, String spellId, LootRarity rarity, float level) {
        float chance = values.get(rarity).get(level);
        if (caster.getRandom().nextFloat() >= chance) return;
        PENDING.add(new PendingReset(caster, spellId));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        float chance = values.get(rarity).get(level);
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