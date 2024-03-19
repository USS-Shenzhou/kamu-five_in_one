package cn.ussshenzhou.kamufive_in_one_2;

import cn.ussshenzhou.kamufive_in_one_2.network.ArmRotPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FioManagerClient extends FioManager {

    private static final FioManagerClient instanceClient = new FioManagerClient();

    public static FioManagerClient getInstanceClient() {
        return instanceClient;
    }

    public @Nullable Part part;

    public Optional<AbstractClientPlayer> getMainPlayerClient() {
        if (mainPlayer == null) {
            return Optional.empty();
        }
        //noinspection DataFlowIssue
        return Optional.ofNullable((AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(mainPlayer));
    }

    @Override
    public boolean isFioPlayer(Player player) {
        return FioManager.NAME.equals(player.getGameProfile().getName());
    }

    @Override
    public boolean isMainPlayer(Player player) {
        return mainPlayer != null && player.getUUID().equals(mainPlayer);
    }

    public boolean isMainPlayer() {
        return mainPlayer != null && Minecraft.getInstance().player.getUUID().equals(mainPlayer);
    }

    public boolean isArm() {
        return part == Part.LEFT_ARM || part == Part.RIGHT_ARM;
    }

    public boolean isHead() {
        return part == Part.HEAD;
    }

    public boolean isFoot() {
        return part == Part.LEFT_FOOT || part == Part.RIGHT_FOOT;
    }

    @Override
    public boolean isArm(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerParts.inverse().get(player.getUUID());
        return part == Part.LEFT_ARM || part == Part.RIGHT_ARM;
    }

    @Override
    public boolean isHead(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerParts.inverse().get(player.getUUID());
        return part == Part.HEAD;
    }

    @Override
    public boolean isFoot(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerParts.inverse().get(player.getUUID());
        return part == Part.LEFT_FOOT || part == Part.RIGHT_FOOT;
    }

    @Override
    public Optional<Part> getPart(@Nullable Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(playerParts.inverse().get(player.getUUID()));
    }

    public Optional<Part> getPart() {
        return Optional.ofNullable(part);
    }

    public Optional<Part> getPart(@Nullable UUID player) {
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(playerParts.inverse().get(player));
    }

    public Quaternionf prevQuatL = new Quaternionf().rotateLocalX(-Mth.PI / 2);
    public Quaternionf quatL = new Quaternionf().rotateLocalX(-Mth.PI / 2);
    public Quaternionf prevQuatR = new Quaternionf().rotateLocalX(-Mth.PI / 2);
    public Quaternionf quatR = new Quaternionf().rotateLocalX(-Mth.PI / 2);

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        prevQuatL.set(quatL);
        prevQuatR.set(quatR);
    }

    public Quaternionf getRotL(float partialTick) {
        return new Quaternionf(
                Mth.lerp(partialTick, prevQuatL.x, quatL.x),
                Mth.lerp(partialTick, prevQuatL.y, quatL.y),
                Mth.lerp(partialTick, prevQuatL.z, quatL.z),
                Mth.lerp(partialTick, prevQuatL.w, quatL.w)
        );

    }

    public Quaternionf getRotR(float partialTick) {
        return new Quaternionf(
                Mth.lerp(partialTick, prevQuatR.x, quatR.x),
                Mth.lerp(partialTick, prevQuatR.y, quatR.y),
                Mth.lerp(partialTick, prevQuatR.z, quatR.z),
                Mth.lerp(partialTick, prevQuatR.w, quatR.w)
        );

    }

    public void rotArmAndBroadCast(double dx, double dy) {
        float zAxis = (float) (dx * Math.PI / 180);
        float xAxis = (float) (dy * Math.PI / 180);
        if (part == Part.LEFT_ARM) {
            quatL.rotateZ(zAxis);
            quatL.rotateX(xAxis);
            NetworkHelper.sendToServer(new ArmRotPacket(Minecraft.getInstance().player.getUUID(), quatL.x, quatL.y, quatL.z, quatL.w));
        } else if (part == Part.RIGHT_ARM) {
            quatR.rotateZ(zAxis);
            quatR.rotateX(xAxis);
            NetworkHelper.sendToServer(new ArmRotPacket(Minecraft.getInstance().player.getUUID(), quatR.x, quatR.y, quatR.z, quatR.w));
        }
    }

    public void rotArmRaw(UUID from, float x, float y, float z, float w) {
        getPart(from).ifPresent(p -> {
            if (p == Part.LEFT_ARM) {
                quatL.set(x, y, z, w);
            } else if (p == Part.RIGHT_ARM) {
                quatR.set(x, y, z, w);
            }
        });
    }
}
