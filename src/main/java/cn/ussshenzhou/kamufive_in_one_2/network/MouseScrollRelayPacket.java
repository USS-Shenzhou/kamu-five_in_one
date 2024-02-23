package cn.ussshenzhou.kamufive_in_one_2.network;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
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
public class MouseScrollRelayPacket {

    public final UUID from;
    public final double x;
    public final double y;

    public MouseScrollRelayPacket(UUID from, double x, double y) {
        this.from = from;
        this.x = x;
        this.y = y;
    }

    @Decoder
    public MouseScrollRelayPacket(FriendlyByteBuf buf) {
        this.from = buf.readUUID();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(from);
        buf.writeDouble(x);
        buf.writeDouble(y);
    }

    @Consumer
    public void handler(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            serverHandler();
        } else {
            clientHandler();
        }
    }

    private void serverHandler() {
        FioManager.relayToMain(this);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandler() {
        var mc = Minecraft.getInstance();
        mc.mouseHandler.onScroll(mc.getWindow().getWindow(), x, y);
    }
}
