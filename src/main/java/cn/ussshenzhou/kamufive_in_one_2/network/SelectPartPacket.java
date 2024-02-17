package cn.ussshenzhou.kamufive_in_one_2.network;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import cn.ussshenzhou.t88.network.annotation.Consumer;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
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
public class SelectPartPacket {
    public final UUID player;
    public final Part part;

    public SelectPartPacket(UUID player, Part part) {
        this.player = player;
        this.part = part;
    }

    @Decoder
    public SelectPartPacket(FriendlyByteBuf buf) {
        player = buf.readUUID();
        part = buf.readEnum(Part.class);
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeEnum(part);
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
        Minecraft mc = Minecraft.getInstance();
        assert mc.level != null;
        FioManager.Client.playerParts.put(part, (AbstractClientPlayer) mc.level.getPlayerByUUID(player));
        assert mc.player != null;
        if (player.equals(mc.player.getUUID())) {
            FioManager.Client.part = part;
            mc.player = FioManager.Client.getMainPlayer();
            mc.setCameraEntity(mc.player);
        }
    }
}
