//package net.kayn.fallen_gems_affixes.mixin.client;
//
//import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.world.effect.MobEffect;
//import net.minecraft.world.effect.MobEffectInstance;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.inventory.CraftingContainer;
//import net.minecraft.world.inventory.InventoryMenu;
//import net.minecraft.world.inventory.MenuType;
//import net.minecraft.world.inventory.RecipeBookMenu;
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Unique;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;
//
//@Mixin(InventoryMenu.class)
//@OnlyIn(Dist.CLIENT)
//public abstract class InventoryMenuMixin extends RecipeBookMenu<CraftingContainer> {
//    private static final Logger LOGGER = LogManager.getLogger();
//
//    public InventoryMenuMixin(MenuType<?> pMenuType, int pContainerId) {
//        super(pMenuType, pContainerId);
//    }
//
//    @Inject(method = "onEquipItem", at = @At("HEAD"))
//    private static void onEquipItemPrefix(Player pPlayer, EquipmentSlot pSlot, ItemStack pNewItem, ItemStack pOldItem, CallbackInfo ci) {
//        LOGGER.info("into onSetItemSlot");
//        if (!(pPlayer instanceof LocalPlayer player)) return;
////        var currentEffectsMap = player.getActiveEffectsMap();
////        if (currentEffectsMap instanceof ProtectedMobEffectMap<?> map) {
////            map.setRemover(ProtectedMobEffectMap.EffectRemover.ON_EQUIP);
////            try {
////                checkGemBonus(pOldItem, (bonus, rarity) -> {
////                    MobEffect effect = bonus.getEffect();
////                    int amplifier = bonus.getAmplifier(rarity);
////                    MobEffectInstance effectInst = new MobEffectInstance(effect, -1, amplifier);
////                    LOGGER.info("remove {}", effect);
////                    player.removeEffect(effect);
////                    map.tryRemovePermanentEffect(effect, amplifier);
////                    if (map.containsPermanent(effect)) {
////                        player.addEffect(map.getLastPotentialEffectInst(effect));
////                    }
////                });
////                checkGemBonus(pNewItem, (bonus, rarity) -> {
////                    MobEffect effect = bonus.getEffect();
////                    int amplifier = bonus.getAmplifier(rarity);
////                    MobEffectInstance inst = new MobEffectInstance(effect, -1, amplifier);
////                    player.addEffect(inst);
////                    map.addPermanentEffect(effect, amplifier);
////                });
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
////            LOGGER.info("effect map {}", map);
////            map.setRemover(ProtectedMobEffectMap.EffectRemover.EXTERNAL);
////        }
//    }
//
////    @Unique
////    private static ItemStack extractOldItemStack_$1(ItemStack old) {
////        if (old.isEmpty()) {
////            ItemStack itemstack = new ItemStack(Items.DIAMOND, old.getCount());
////            itemstack.setPopTime(old.getPopTime());
////            if (old.getTag() != null) {
////                itemstack.setTag(old.getTag().copy());
////            }
////            return itemstack;
////        }
////        return old;
////    }
//}
