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
    public final UUID selectorUUID;
    public final UUID mainPlayerUUID;
    public final Part part;

    public SelectPartPacket(UUID selectorUUID, UUID mainPlayerUUID, Part part) {
        this.selectorUUID = selectorUUID;
        this.mainPlayerUUID = mainPlayerUUID;
        this.part = part;
    }

    @Decoder
    public SelectPartPacket(FriendlyByteBuf buf) {
        selectorUUID = buf.readUUID();
        mainPlayerUUID = buf.readUUID();
        part = buf.readEnum(Part.class);
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(selectorUUID);
        buf.writeUUID(mainPlayerUUID);
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
        Minecraft localMc = Minecraft.getInstance();
        assert localMc.level != null;
        var selector = (AbstractClientPlayer) localMc.level.getPlayerByUUID(selectorUUID);
        var mainPlayer = (AbstractClientPlayer) localMc.level.getPlayerByUUID(mainPlayerUUID);
        var map = FioManager.Client.playerParts;
        map.remove(map.inverse().get(selectorUUID));
        map.put(part, selectorUUID);
        assert localMc.player != null;
        FioManager.Client.mainPlayer = mainPlayerUUID;
        if (selectorUUID.equals(localMc.player.getUUID())) {
            FioManager.Client.part = part;
            //noinspection DataFlowIssue
            //localMc.setCameraEntity(mainPlayer);
        }
    }
}
