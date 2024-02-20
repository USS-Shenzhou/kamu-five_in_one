package cn.ussshenzhou.kamufive_in_one_2.network;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class PacketPacket {
    public final String type;
    public final ByteBuf content;
    public final int contentLength;

    public PacketPacket(String type, ByteBuf content) {
        this.type = type;
        this.contentLength = content.readableBytes();
        this.content = content;
    }

    @Decoder
    public PacketPacket(FriendlyByteBuf buf) {
        this.type = buf.readUtf();
        this.contentLength = buf.readInt();
        this.content = buf.readBytes(contentLength);
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(type);
        buf.writeInt(contentLength);
        buf.writeBytes(content);
    }

    @Consumer
    public void handler(Supplier<NetworkEvent.Context> context) {
        //noinspection StatementWithEmptyBody
        if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
        } else {
            clientHandler();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandler() {
        try {
            var packetClass = Class.forName(type);
            var constructor = packetClass.getConstructor(FriendlyByteBuf.class);
            constructor.setAccessible(true);
            var packet = (Packet<?>) constructor.newInstance(new FriendlyByteBuf(content));
            Minecraft.getInstance().getConnection().send(packet);
        } catch (Exception e) {
            LogUtils.getLogger().error("{}", e.getMessage());
        }
    }
}
