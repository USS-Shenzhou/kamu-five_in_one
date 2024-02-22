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
public class MouseMoveRelayPacket {

    public final UUID from;
    public final double dx;
    public final double dy;

    public MouseMoveRelayPacket(UUID from, double dx, double dy) {
        this.from = from;
        this.dx = dx;
        this.dy = dy;
    }

    @Decoder
    public MouseMoveRelayPacket(FriendlyByteBuf buf) {
        this.from = buf.readUUID();
        this.dx = buf.readDouble();
        this.dy = buf.readDouble();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(from);
        buf.writeDouble(dx);
        buf.writeDouble(dy);
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
        FioManager.relay(this);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandler() {
        var mc = Minecraft.getInstance();
        mc.mouseHandler.onMove(mc.getWindow().getWindow(), dx + mc.mouseHandler.xpos(), dy + mc.mouseHandler.ypos());
    }
}
