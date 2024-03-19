package cn.ussshenzhou.kamufive_in_one_2.network;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.FioManagerClient;
import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
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
    public final float x, y, z, w;

    public ArmRotPacket(UUID from, float x, float y, float z, float w) {
        this.from = from;
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    @Decoder
    public ArmRotPacket(FriendlyByteBuf buf) {
        this.from = buf.readUUID();
        this.x = buf.readFloat();
        this.y = buf.readFloat();
        this.z = buf.readFloat();
        this.w = buf.readFloat();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(from);
        buf.writeFloat(x);
        buf.writeFloat(y);
        buf.writeFloat(z);
        buf.writeFloat(w);
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
        FioManager.getInstanceServer().broadCastExcept(this, context.getSender().getUUID());
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandler(NetworkEvent.Context context) {
        FioManagerClient.getInstanceClient().rotArmRaw(from, x, y, z, w);
    }
}
