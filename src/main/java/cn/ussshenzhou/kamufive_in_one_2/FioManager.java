package cn.ussshenzhou.kamufive_in_one_2;

import cn.ussshenzhou.kamufive_in_one_2.network.SelectPartPacket;
import cn.ussshenzhou.kamufive_in_one_2.network.ServerGamePacketListenerImplModified;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.Collections2;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author USS_Shenzhou
 */
public class FioManager {

    public static final String NAME = "我们一起";

    private static @Nullable ServerPlayer mainPlayer;
    private static MinecraftServer server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);

    public static void selectPart(CommandSourceStack source, Part part) {
        var sourcePlayer = source.getPlayer();
        assert sourcePlayer != null;
        if (mainPlayer == null) {
            mainPlayer = FioManager.getOrCreateMainPlayer(sourcePlayer);
        }
        NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), new SelectPartPacket(sourcePlayer.getUUID(), mainPlayer.getUUID(), part));
        sourcePlayer.connection = ServerGamePacketListenerImplModified.from(sourcePlayer.connection, mainPlayer);


        switch (part) {
            case HEAD -> {

            }
            case LEFT_ARM -> {

            }
            case RIGHT_ARM -> {

            }
            case LEFT_FOOT -> {

            }
            case RIGHT_FOOT -> {

            }
        }
    }

    public static @Nonnull ServerPlayer getOrCreateMainPlayer(ServerPlayer from) {
        if (mainPlayer == null) {
            mainPlayer = from;
        }
        return mainPlayer;
    }

    public static boolean isFioPlayer(Player player) {
        return NAME.equals(player.getGameProfile().getName());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    @OnlyIn(Dist.CLIENT)
    public static class Client {

        public static @Nullable AbstractClientPlayer mainPlayer;
        public static BiMap<Part, AbstractClientPlayer> playerParts = HashBiMap.create();
        public static @Nullable Part part;

        public static Optional<AbstractClientPlayer> getMainPlayer() {
            return Optional.ofNullable(mainPlayer);
        }
    }

}
