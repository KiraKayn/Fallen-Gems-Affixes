//package net.kayn.fallen_gems_affixes.mixin;
//
//
//import net.kayn.fallen_gems_affixes.util.ProtectedMobEffectMap;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.world.entity.EntityType;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.Level;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//
//import static net.kayn.fallen_gems_affixes.event.PermanentEffectHandler.checkGemBonus;
//
//@Mixin(value = Player.class)
//public abstract class PlayerMixin extends LivingEntity {
//    private static final Logger LOGGER = LogManager.getLogger();
//
//    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
//        super(pEntityType, pLevel);
//    }
//
//    @Inject(method = "setItemSlot", at = @At("TAIL"))
//    private void onSetItemSlot(EquipmentSlot pSlot, ItemStack pStack, CallbackInfo ci) {
//        LOGGER.info("into onSetItemSlot");
//        if (!((Object)this instanceof LocalPlayer player)) return;
//        if (player.getActiveEffectsMap() instanceof ProtectedMobEffectMap map) {
//            checkGemBonus(pStack, (bonus, rarity) -> {
//                map.addPermanentEffect(bonus.getEffect());
//            });
//            LOGGER.info("effect map {}", map);
//        }
//    }
//}
