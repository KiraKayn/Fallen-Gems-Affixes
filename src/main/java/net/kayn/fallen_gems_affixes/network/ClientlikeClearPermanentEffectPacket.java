package net.kayn.fallen_gems_affixes.network;

import net.kayn.fallen_gems_affixes.Fallen;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ClientlikeClearPermanentEffectPacket implements CustomPacketPayload{
    public static final String version = "1.0";
    public static final CustomPacketPayload.Type<ClientlikeClearPermanentEffectPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FallenGemsAffixes.MOD_ID, "ccl_pe"));

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientlikeClearPermanentEffectPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(@NotNull RegistryFriendlyByteBuf buf, @NotNull ClientlikeClearPermanentEffectPacket value) {}

        @Override
        public @NotNull ClientlikeClearPermanentEffectPacket decode(@NotNull RegistryFriendlyByteBuf buf) {
            return new ClientlikeClearPermanentEffectPacket();
        }
    };

    public static void handleClient(ClientlikeClearPermanentEffectPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            var cap = player.getCapability(Fallen.Capabilities.PE_CAP);
            if (cap != null) {
                for (Iterator<Map.Entry<Holder<MobEffect>, List<Integer>>> it = cap.getContainer().getIterator(); it.hasNext(); ) {
                    Map.Entry<Holder<MobEffect>, List<Integer>> entry = it.next();
                    if (player.hasEffect(entry.getKey())) {
                        cap.getEffectHandler().removeEffectRet(entry.getKey(), entry.getValue().getLast());
                        it.remove();
                    }
                }
            }
        });
    }
}
