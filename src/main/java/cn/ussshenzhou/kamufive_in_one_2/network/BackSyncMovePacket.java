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

import java.util.function.Supplier;

/**
 * @author USS_Shenzhou
 */
@NetPacket
public class BackSyncMovePacket {

    public final double x;
    public final double y;
    public final double z;
    public final float yRot;
    public final float xRot;
    public final boolean onGround;

    public BackSyncMovePacket(double x, double y, double z, float yRot, float xRot, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
        this.onGround = onGround;
    }

    @Decoder
    public BackSyncMovePacket(FriendlyByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        yRot = buf.readFloat();
        xRot = buf.readFloat();
        onGround = buf.readUnsignedByte() != 0;
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeFloat(this.yRot);
        buf.writeFloat(this.xRot);
        buf.writeByte(this.onGround ? 1 : 0);
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
        Minecraft localMc = Minecraft.getInstance();
        assert localMc.player != null;
        FioManager.Client.getMainPlayer().ifPresent(main -> {
            main.setPos(
                    x == Double.MAX_VALUE ? main.getX() : x,
                    y == Double.MAX_VALUE ? main.getY() : y,
                    z == Double.MAX_VALUE ? main.getZ() : z);
            main.setYRot(yRot == Float.MAX_VALUE ? main.getYRot() : yRot);
            main.setXRot(xRot == Float.MAX_VALUE ? main.getXRot() : xRot);
            main.setOnGround(onGround);
        });
        if (!FioManager.Client.isMainPlayer(localMc.player)) {
            localMc.player.setPos(
                    x == Double.MAX_VALUE ? localMc.player.getX() : x,
                    y == Double.MAX_VALUE ? localMc.player.getY() : y,
                    z == Double.MAX_VALUE ? localMc.player.getZ() : z);
            localMc.player.setYRot(yRot == Float.MAX_VALUE ? localMc.player.getYRot() : yRot);
            localMc.player.setXRot(xRot == Float.MAX_VALUE ? localMc.player.getXRot() : xRot);
            localMc.player.setOnGround(onGround);
        }
    }
}
