package cn.ussshenzhou.kamufive_in_one_2;

import cn.ussshenzhou.kamufive_in_one_2.network.SelectPartPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FioManager {

    public static final String NAME = "我们一起";

    private static @Nullable UUID mainPlayer;
    private static MinecraftServer server;
    public static BiMap<Part, UUID> playerParts = HashBiMap.create();

    /*@SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (server == null) {
            server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        }
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        getMainPlayer().ifPresent(main -> {
            playerParts.values().stream()
                    .map(uuid -> server.getPlayerList().getPlayer(uuid))
                    .filter(serverPlayer -> serverPlayer != main)
                    .forEach(serverPlayer -> {
                        if (serverPlayer == null) {
                            return;
                        }
                        serverPlayer.setDeltaMovement(main.getDeltaMovement());
                        serverPlayer.moveTo(main.position());
                    });
        });
    }*/

    public static void selectPart(CommandSourceStack source, Part part) {
        var sourcePlayer = source.getPlayer();
        assert sourcePlayer != null;
        if (mainPlayer == null) {
            FioManager.getOrCreateMainPlayer(sourcePlayer);
        }
        playerParts.remove(playerParts.inverse().get(sourcePlayer.getUUID()));
        playerParts.put(part, sourcePlayer.getUUID());
        NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), new SelectPartPacket(sourcePlayer.getUUID(), mainPlayer, part));
        //sourcePlayer.connection = ServerGamePacketListenerImplModified.from(sourcePlayer.connection, mainPlayer);


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
            mainPlayer = from.getUUID();
        }
        //noinspection DataFlowIssue
        return from.server.getPlayerList().getPlayer(mainPlayer);
    }

    public static Optional<ServerPlayer> getMainPlayer() {
        return mainPlayer == null ? Optional.empty() : Optional.ofNullable(server.getPlayerList().getPlayer(mainPlayer));
    }

    public static boolean isFioPlayer(Player player) {
        return NAME.equals(player.getGameProfile().getName());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    @OnlyIn(Dist.CLIENT)
    public static class Client {

        public static @Nullable UUID mainPlayer;
        public static BiMap<Part, UUID> playerParts = HashBiMap.create();
        public static @Nullable Part part;

        public static Optional<AbstractClientPlayer> getMainPlayer() {
            if (mainPlayer == null) {
                return Optional.empty();
            }
            //noinspection DataFlowIssue
            return Optional.ofNullable((AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(mainPlayer));
        }
    }

}
