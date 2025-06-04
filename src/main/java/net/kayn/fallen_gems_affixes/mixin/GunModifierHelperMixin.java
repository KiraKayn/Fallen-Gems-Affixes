package net.kayn.fallen_gems_affixes.mixin;

import net.kayn.fallen_gems_affixes.attributes.AAAttributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.registries.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import top.ribs.scguns.util.GunModifierHelper;

@Mixin(value = GunModifierHelper.class, remap = false)
public class GunModifierHelperMixin {

    @Unique
    private static double fallenGemsAffixes$getAttributeValue(ItemStack weapon, RegistryObject<Attribute> attributeRO) {
        return weapon.getAttributeModifiers(EquipmentSlot.MAINHAND).get(attributeRO.get()).stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    @ModifyReturnValue(method = "getModifiedProjectileSpeed", at = @At("RETURN"))
    private static double onGetModifiedProjectileSpeed(double originalSpeed, ItemStack weapon, double speed) {
        double modifier = fallenGemsAffixes$getAttributeValue(weapon, AAAttributes.PROJECTILE_SPEED);
        return originalSpeed * (1 + modifier);
    }

    @ModifyReturnValue(method = "getKickReduction", at = @At("RETURN"))
    private static float onGetKickReduction(float originalReduction, ItemStack weapon) {
        double modifier = fallenGemsAffixes$getAttributeValue(weapon, AAAttributes.KICK_REDUCTION);
        return originalReduction + (float) (modifier * (1 - originalReduction));
    }

    @ModifyReturnValue(method = "getModifiedRate", at = @At("RETURN"))
    private static int onGetModifiedRate(int originalRate, ItemStack weapon, int rate) {
        double modifier = fallenGemsAffixes$getAttributeValue(weapon, AAAttributes.FIRE_RATE);
        return (int) (originalRate / (1 + modifier));
    }

    @ModifyReturnValue(method = "getModifiedAmmoCapacity", at = @At("RETURN"))
    private static int onGetModifiedAmmoCapacity(int originalCapacity, ItemStack weapon, top.ribs.scguns.common.Gun modifiedGun) {
        double additionalAmmo = fallenGemsAffixes$getAttributeValue(weapon, AAAttributes.ADDITIONAL_AMMO);
        return originalCapacity + (int) additionalAmmo;
    }

    @ModifyReturnValue(method = "getModifiedSpread", at = @At("RETURN"))
    private static float onGetModifiedSpread(float originalSpread, ItemStack weapon, float spread) {
        double modifier = fallenGemsAffixes$getAttributeValue(weapon, AAAttributes.SPREAD_REDUCTION);
        return originalSpread * (float) (1 - modifier);
    }


    @ModifyReturnValue(method = "getModifiedReloadSpeed", at = @At("RETURN"))
    private static double onGetModifiedReloadSpeed(double originalReloadSpeed, ItemStack weapon, double reloadSpeed) {
        double modifier = fallenGemsAffixes$getAttributeValue(weapon, AAAttributes.RELOAD_SPEED);
        return originalReloadSpeed / (1 + modifier);
    }


    @ModifyReturnValue(method = "getModifiedProjectileDamage", at = @At("RETURN"))
    private static float onGetModifiedProjectileDamage(float originalDamage, ItemStack weapon, float damage) {
        double modifier = fallenGemsAffixes$getAttributeValue(weapon, AAAttributes.BULLET_DAMAGE);
        return originalDamage * (float) (1 + modifier);
    }

}