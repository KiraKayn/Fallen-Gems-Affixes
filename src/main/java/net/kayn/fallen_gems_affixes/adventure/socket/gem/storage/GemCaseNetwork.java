package net.kayn.fallen_gems_affixes.adventure.socket.gem.storage;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.kayn.fallen_gems_affixes.FallenGemsAffixes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class GemCaseNetwork {

    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(FallenGemsAffixes.MOD_ID, "gem_case"), () -> VERSION, VERSION::equals, VERSION::equals);

    private GemCaseNetwork() {
    }

    public static void init() {
        int id = 0;
        CHANNEL.registerMessage(id++, GemCaseSelectMessage.class, GemCaseSelectMessage::encode, GemCaseSelectMessage::decode, GemCaseSelectMessage::handle);
    }

    public static final class GemCaseSelectMessage {

        private final ResourceLocation gemId;

        public GemCaseSelectMessage(ResourceLocation gemId) {
            this.gemId = gemId;
        }

        public void encode(FriendlyByteBuf buf) {
            buf.writeResourceLocation(gemId);
        }

        public static GemCaseSelectMessage decode(FriendlyByteBuf buf) {
            return new GemCaseSelectMessage(buf.readResourceLocation());
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                var sender = ctx.get().getSender();
                if (sender != null && sender.containerMenu instanceof GemCaseMenu menu) {
                    DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(gemId);
                    menu.setSelectedGem(holder);
                }
            });
            ctx.get().setPacketHandled(true);
        }
    }
}

