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
public class ArmRotPacket {

    public final UUID from;
    public final float xRotOld, xRot, yRotOld, yRot;

    public ArmRotPacket(UUID from, float xRotOld, float xRot, float yRotOld, float yRot) {
        this.from = from;
        this.xRotOld = xRotOld;
        this.xRot = xRot;
        this.yRotOld = yRotOld;
        this.yRot = yRot;
    }

    @Decoder
    public ArmRotPacket(FriendlyByteBuf buf) {
        this.from = buf.readUUID();
        this.xRotOld = buf.readFloat();
        this.xRot = buf.readFloat();
        this.yRotOld = buf.readFloat();
        this.yRot = buf.readFloat();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(from);
        buf.writeFloat(xRotOld);
        buf.writeFloat(xRot);
        buf.writeFloat(yRotOld);
        buf.writeFloat(yRot);
    }

    @Consumer
    public void handler(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
            serverHandler(context.get());
        } else {
            clientHandler(context.get());
        }
    }

    private void serverHandler(NetworkEvent.Context context) {
        FioManager.broadCast(this);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandler(NetworkEvent.Context context) {
        var mc = Minecraft.getInstance();
        //TODO
    }
}
