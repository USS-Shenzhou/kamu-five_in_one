package cn.ussshenzhou.kamufive_in_one_2;

import cn.ussshenzhou.kamufive_in_one_2.network.SelectPartPacket;
import cn.ussshenzhou.kamufive_in_one_2.network.ServerGamePacketListenerImplModified;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
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

    public static @Nullable UUID mainPlayer;
    private static MinecraftServer server;
    public static BiMap<Part, UUID> playerPartsServer = HashBiMap.create();
    public static AABB ZERO = new AABB(0, 0, 0, 0, 0, 0);

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (server == null) {
            server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        }
        /*if (event.phase == TickEvent.Phase.END) {
            return;
        }*/
    }

    public static void selectPart(CommandSourceStack source, Part part) {
        var sourcePlayer = source.getPlayer();
        assert sourcePlayer != null;
        if (mainPlayer == null) {
            FioManager.getOrCreateMainPlayer(sourcePlayer);
        }
        playerPartsServer.remove(playerPartsServer.inverse().get(sourcePlayer.getUUID()));
        playerPartsServer.put(part, sourcePlayer.getUUID());
        NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), new SelectPartPacket(sourcePlayer.getUUID(), mainPlayer, part));
        sourcePlayer.server.getAllLevels()
                .forEach(level -> level.getChunkSource().chunkMap.entityMap
                        .forEach((i, trackedEntity) -> trackedEntity.seenBy
                                .removeIf(serverPlayerConnection -> serverPlayerConnection.getPlayer().getUUID().equals(sourcePlayer.getUUID()))
                        )
                );
        sourcePlayer.connection = ServerGamePacketListenerImplModified.from(sourcePlayer.connection);
        if (!sourcePlayer.getUUID().equals(mainPlayer)) {
            getMainPlayer().ifPresent(sourcePlayer::setCamera);
            sourcePlayer.setBoundingBox(ZERO);
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

    public static boolean isFioPlayer(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        return NAME.equals(player.getGameProfile().getName());
    }

    public static boolean isMainPlayer(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        return mainPlayer != null && player.getUUID().equals(mainPlayer);
    }

    public static boolean isArm(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerPartsServer.inverse().get(player.getUUID());
        return part == Part.LEFT_ARM || part == Part.RIGHT_ARM;
    }

    public static boolean isHead(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerPartsServer.inverse().get(player.getUUID());
        return part == Part.HEAD;
    }

    public static boolean isFoot(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerPartsServer.inverse().get(player.getUUID());
        return part == Part.LEFT_FOOT || part == Part.RIGHT_FOOT;
    }

    public static <MSG> void relayToMain(MSG packet) {
        getMainPlayer().ifPresent(main -> NetworkHelper.sendToPlayer(main, packet));
    }

    public static <MSG> void broadCast(MSG packet) {
        playerPartsServer.forEach((part, uuid) -> {
            NetworkHelper.sendToPlayer(server.getPlayerList().getPlayer(uuid), packet);
        });
    }

    public static Optional<Part> getPart(@Nullable Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(playerPartsServer.inverse().get(player.getUUID()));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class Client {

        public static @Nullable UUID mainPlayer;
        public static BiMap<Part, UUID> playerPartsClient = HashBiMap.create();
        public static @Nullable Part part;

        public static Optional<AbstractClientPlayer> getMainPlayer() {
            if (mainPlayer == null) {
                return Optional.empty();
            }
            //noinspection DataFlowIssue
            return Optional.ofNullable((AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(mainPlayer));
        }

        public static boolean isFioPlayer(Player player) {
            return NAME.equals(player.getGameProfile().getName());
        }

        public static boolean isMainPlayer(Player player) {
            return mainPlayer != null && player.getUUID().equals(mainPlayer);
        }

        public static boolean isMainPlayer() {
            return mainPlayer != null && Minecraft.getInstance().player.getUUID().equals(mainPlayer);
        }

        public static boolean isArm() {
            return part == Part.LEFT_ARM || part == Part.RIGHT_ARM;
        }

        public static boolean isHead() {
            return part == Part.HEAD;
        }

        public static boolean isFoot() {
            return part == Part.LEFT_FOOT || part == Part.RIGHT_FOOT;
        }

        public static boolean isArm(@Nullable Player player) {
            if (player == null) {
                return false;
            }
            var part = playerPartsClient.inverse().get(player.getUUID());
            return part == Part.LEFT_ARM || part == Part.RIGHT_ARM;
        }

        public static boolean isHead(@Nullable Player player) {
            if (player == null) {
                return false;
            }
            var part = playerPartsClient.inverse().get(player.getUUID());
            return part == Part.HEAD;
        }

        public static boolean isFoot(@Nullable Player player) {
            if (player == null) {
                return false;
            }
            var part = playerPartsClient.inverse().get(player.getUUID());
            return part == Part.LEFT_FOOT || part == Part.RIGHT_FOOT;
        }

        public static Optional<Part> getPart(@Nullable Player player) {
            if (player == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(playerPartsClient.inverse().get(player.getUUID()));
        }

        public static float xRotOldL, xRotL, yRotOldL, yRotL, xRotOldR, xRotR, yRotOldR, yRotR;

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                return;
            }
            xRotOldL = xRotL;
            yRotOldL = yRotL;
            xRotOldR = xRotR;
            yRotOldR = yRotR;
        }

        public static float getXRotL(float partialTick) {
            return Mth.lerp(partialTick, xRotOldL, xRotL);
        }

        public static float getYRotL(float partialTick) {
            return Mth.lerp(partialTick, yRotOldL, yRotL);
        }

        public static float getXRotR(float partialTick) {
            return Mth.lerp(partialTick, xRotOldR, xRotR);
        }

        public static float getYRotR(float partialTick) {
            return Mth.lerp(partialTick, yRotOldR, yRotR);
        }
    }

}
