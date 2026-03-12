package net.kayn.fallen_gems_affixes.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.UUID;

@Mixin(value = AttributeAffix.ModifierInst.class, remap = false)
public abstract class AttributeAffixMixin {

    @Final
    @Shadow
    private Operation op;

    @ModifyArg(
            method = "build",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/attributes/AttributeModifier;<init>(Ljava/util/UUID;Ljava/lang/String;DLnet/minecraft/world/entity/ai/attributes/AttributeModifier$Operation;)V"
            ),
            index = 0
    )
    private UUID fixOperationUUID(UUID original) {
        return new UUID(
                original.getMostSignificantBits(),
                original.getLeastSignificantBits() ^ ((long) this.op.ordinal() + 1)
        );
    }
}