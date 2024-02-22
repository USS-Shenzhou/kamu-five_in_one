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
public class MousePressRelayPacket {

    public final UUID from;
    public final int button;
    public final int action;
    public final int modifiers;

    public MousePressRelayPacket(UUID from, int button, int action, int modifiers) {
        this.from = from;
        this.button = button;
        this.action = action;
        this.modifiers = modifiers;
    }

    @Decoder
    public MousePressRelayPacket(FriendlyByteBuf buf) {
        this.from = buf.readUUID();
        this.button = buf.readInt();
        this.action = buf.readInt();
        this.modifiers = buf.readInt();
    }

    @Encoder
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(from);
        buf.writeInt(button);
        buf.writeInt(action);
        buf.writeInt(modifiers);
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
        mc.mouseHandler.onPress(mc.getWindow().getWindow(), button, action, modifiers);
    }
}
