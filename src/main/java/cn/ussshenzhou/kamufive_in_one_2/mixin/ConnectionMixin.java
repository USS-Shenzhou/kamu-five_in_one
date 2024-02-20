package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.network.BackSyncMovePacket;
import cn.ussshenzhou.kamufive_in_one_2.network.PacketHelper;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.network.PacketDistributor;
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
            var original = serverGamePacketListener.player;
            var main = FioManager.getMainPlayer();
            if (FioManager.playerParts.inverse().containsKey(serverGamePacketListener.player.getUUID())
                    && main.isPresent()
                    && !PacketHelper.ignore(packet)
            ) {
                if (original != main.get()) {
                    if (packet instanceof ServerboundMovePlayerPacket movePlayerPacket) {
                        NetworkHelper.sendToPlayer(main.get(), new BackSyncMovePacket(
                                movePlayerPacket.getX(Double.MAX_VALUE),
                                movePlayerPacket.getY(Double.MAX_VALUE),
                                movePlayerPacket.getZ(Double.MAX_VALUE),
                                movePlayerPacket.getYRot(Float.MAX_VALUE),
                                movePlayerPacket.getXRot(Float.MAX_VALUE),
                                movePlayerPacket.isOnGround()
                        ));
                    }
                    genericsFtw(packet, main.get().connection);
                    return;
                } else {
                    /*if (packet instanceof ServerboundMovePlayerPacket movePlayerPacket) {
                        FioManager.playerParts.forEach((part, uuid) -> {
                            var p = (ServerPlayer) main.get().serverLevel().getPlayerByUUID(uuid);
                            if (p != main.get()) {
                                NetworkHelper.sendToPlayer(p, new BackSyncMovePacket(
                                        movePlayerPacket.getX(Double.MAX_VALUE),
                                        movePlayerPacket.getY(Double.MAX_VALUE),
                                        movePlayerPacket.getZ(Double.MAX_VALUE),
                                        movePlayerPacket.getYRot(Float.MAX_VALUE),
                                        movePlayerPacket.getXRot(Float.MAX_VALUE),
                                        movePlayerPacket.isOnGround()
                                ));
                            }
                        });
                    }*/
                    genericsFtw(packet, thisListener);
                    return;
                }
            }
        }
        genericsFtw(packet, thisListener);
    }
}
