package net.kayn.fallen_gems_affixes.event;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.kayn.fallen_gems_affixes.adventure.affix.SpellEffectAffix;
import net.kayn.fallen_gems_affixes.adventure.socket.gem.bonus.SpellEffectBonus;
import net.kayn.fallen_gems_affixes.util.EquipmentSlotUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.function.BiConsumer;

public class SpellEventHandler {
    public static final BiConsumer<LivingEntity, LivingEntity> triggerCurio;

    static {
        if (ModList.get().isLoaded("curios")) {
            triggerCurio = (caster, target) -> {
                ICuriosItemHandler handler = CuriosApi.getCuriosInventory(caster).orElse(null);
                if (handler != null) {
                    for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                        ICurioStacksHandler slotHandler = entry.getValue();
                        try {
                            for (int i = 0; i < slotHandler.getSlots(); i++) {
                                ItemStack equippedStack = slotHandler.getStacks().getStackInSlot(i);
                                if (!EquipmentSlotUtil.simpleMatchesCurioSlot(caster, equippedStack, entry.getKey())) continue;
                                applyAllEffects(equippedStack, caster, target);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else return;
            };
        } else {
            triggerCurio = (a,b)-> {};
        }
    }

    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;
        SpellDamageSource source = event.getSpellDamageSource();
        Entity entity = source.getEntity();
        if (!(entity instanceof LivingEntity caster)) return;
        for (ItemStack stack : caster.getAllSlots()) {
            if (!EquipmentSlotUtil.simpleMatchesSlot(stack, stack.getEquipmentSlot())) continue;
            applyAllEffects(stack, caster, target);
        }
        triggerCurio.accept(caster, target);
    }

    @SubscribeEvent
    public static void onSpellHeal(SpellHealEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        LivingEntity caster = event.getEntity();
        LivingEntity target = event.getTargetEntity();
        for (ItemStack stack : event.getEntity().getAllSlots()) {
            if (!EquipmentSlotUtil.simpleMatchesSlot(stack, stack.getEquipmentSlot())) continue;
            applyAllEffects(stack, caster, target);
        }
        triggerCurio.accept(event.getEntity(), event.getTargetEntity());
    }

    public static void applyAllEffects(ItemStack stack, LivingEntity caster, LivingEntity target) {
        AffixHelper.streamAffixes(stack).forEach(inst -> {
            if (inst.affix().get() instanceof SpellEffectAffix affix) {
                switch(affix.target) {
                    case SPELL_DAMAGE_SELF, SPELL_HEAL_SELF -> {affix.applyEffect(caster, inst.rarity().get(), inst.level());}
                    case SPELL_DAMAGE_TARGET, SPELL_HEAL_TARGET -> {affix.applyEffect(target, inst.rarity().get(), inst.level());}
                }
            }
        });
        checkGemBonus(stack, (gem, bonus) -> {
            switch(bonus.target) {
                case SPELL_DAMAGE_SELF, SPELL_HEAL_SELF -> {bonus.applyEffect(gem, caster);}
                case SPELL_DAMAGE_TARGET, SPELL_HEAL_TARGET -> {bonus.applyEffect(gem, target);}
            }
        });
    }

    public static void checkGemBonus(ItemStack itemStack, BonusProcessor processor) {
        if (itemStack.isEmpty()) return;
        LootCategory cat = LootCategory.forItem(itemStack);
        for (GemInstance g : SocketHelper.getGems(itemStack)) {
            if (!g.isValid()) continue;
            Purity purity = g.purity();
            Gem gem = g.gem().get();
            gem.getBonus(cat, purity)
                    .filter(b -> b instanceof SpellEffectBonus)
                    .map(b -> (SpellEffectBonus) b)
                    .ifPresent(bonus -> processor.accept(g, bonus));
        }
    }

    @FunctionalInterface
    public interface BonusProcessor {
        void accept(GemInstance gem, SpellEffectBonus bonus);
    }
}
