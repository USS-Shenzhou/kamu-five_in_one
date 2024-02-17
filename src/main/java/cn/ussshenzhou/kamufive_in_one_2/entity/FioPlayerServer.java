package cn.ussshenzhou.kamufive_in_one_2.entity;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
public class FioPlayerServer extends ServerPlayer {

    public FioPlayerServer(MinecraftServer pServer, ServerLevel pLevel, GameProfile pGameProfile) {
        super(pServer, pLevel, pGameProfile);
    }

    public static FioPlayerServer create(ServerPlayer player, ServerLevel level, double x, double y, double z) {
        MinecraftServer server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        UUID uuid = UUID.randomUUID();
        var profile = new CustomGameProfile(uuid, FioManager.NAME);
        FioPlayerServer falsePlayer = new FioPlayerServer(server, level, profile);
        //falsePlayer.connection = new ServerGamePacketListenerImpl(server, new FakeConnection(), falsePlayer, CommonListenerCookie.createInitial(profile));
        falsePlayer.connection = player.connection;
        falsePlayer.setPos(x, y, z);
        return falsePlayer;
    }

    public static class CustomGameProfile extends GameProfile {

        public CustomGameProfile(UUID uuid, String name) {
            super(uuid, name);
        }
    }
}
