package cn.ussshenzhou.kamufive_in_one_2.network;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.Sets;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundKeepAlivePacket;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;

/**
 * @author USS_Shenzhou
 */
public class PacketHelper {

    private static HashSet<Class<?>> exceptPackets = Sets.newHashSet(
            ServerboundKeepAlivePacket.class
    );

    public static boolean ignore(Object packet) {
        return exceptPackets.contains(packet.getClass());
    }

    public static void reroute(Packet<? extends PacketListener> packet) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.write(buf);
        FioManager.getMainPlayer().ifPresent(serverPlayer -> NetworkHelper.sendToPlayer(serverPlayer, new PacketPacket(packet.getClass().getName(), buf)));
    }
}
