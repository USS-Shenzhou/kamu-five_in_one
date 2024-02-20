package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.network.PacketHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author USS_Shenzhou
 */
@Mixin(Connection.class)
public abstract class ConnectionMixin {

    @Shadow
    private static <T extends PacketListener> void genericsFtw(Packet<T> pPacket, PacketListener pListener) {
    }

    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V"))
    private void rerouteToMain(Packet<? extends PacketListener> packet, PacketListener thisListener) {
        if (thisListener instanceof ServerGamePacketListenerImpl serverGamePacketListener) {
            var originalPlayer = serverGamePacketListener.player;
            var main = FioManager.getMainPlayer();
            if (FioManager.playerParts.inverse().containsKey(serverGamePacketListener.player.getUUID())
                    && main.isPresent()
                    && originalPlayer != main.get()
                    && !PacketHelper.ignore(packet)
            ) {
                PacketHelper.reroute(packet);
                //genericsFtw(packet, main.get().connection);
                return;
            }
        }
        genericsFtw(packet, thisListener);
    }
}
