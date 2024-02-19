package cn.ussshenzhou.kamufive_in_one_2.network;

import com.google.common.collect.Sets;
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

    public static boolean ignore(Object packet){
        return exceptPackets.contains(packet.getClass());
    }
}
