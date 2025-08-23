package net.kayn.fallen_gems_affixes.mixin.accessor;

import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DynamicRegistry.class, remap = false)
public interface DynamicRegistryAccessor {

    @Accessor("codecs")
    CodecMap<?> getCodecs();
}
