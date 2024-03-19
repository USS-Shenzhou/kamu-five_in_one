package cn.ussshenzhou.kamufive_in_one_2;

import cn.ussshenzhou.kamufive_in_one_2.network.SelectPartPacket;
import cn.ussshenzhou.kamufive_in_one_2.network.ServerGamePacketListenerImplModified;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
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
    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    private static final FioManager instanceServer = new FioManager();

    public static FioManager getInstanceServer() {
        return instanceServer;
    }

    public static final String NAME = "我们一起";
    public static final AABB ZERO = new AABB(0, 0, 0, 0, 0, 0);

    public @Nullable UUID mainPlayer;
    public BiMap<Part, UUID> playerParts = HashBiMap.create();

    private @Nullable MinecraftServer server;


    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event) {
        if (instanceServer.server == null) {
            instanceServer.server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);
        }
        /*if (event.phase == TickEvent.Phase.END) {
            return;
        }*/
    }

    public void selectPart(CommandSourceStack source, Part part) {
        var sourcePlayer = source.getPlayer();
        assert sourcePlayer != null;
        if (mainPlayer == null) {
            FioManager.getInstanceServer().getOrCreateMainPlayer(sourcePlayer);
        }
        playerParts.remove(playerParts.inverse().get(sourcePlayer.getUUID()));
        playerParts.put(part, sourcePlayer.getUUID());
        NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), new SelectPartPacket(sourcePlayer.getUUID(), mainPlayer, part));
        sourcePlayer.server.getAllLevels()
                .forEach(level -> level.getChunkSource().chunkMap.entityMap
                        .forEach((i, trackedEntity) -> trackedEntity.seenBy
                                .removeIf(serverPlayerConnection -> serverPlayerConnection.getPlayer().getUUID().equals(sourcePlayer.getUUID()))
                        )
                );
        sourcePlayer.connection = ServerGamePacketListenerImplModified.from(sourcePlayer.connection);
        if (!sourcePlayer.getUUID().equals(mainPlayer)) {
            getMainPlayerServer().ifPresent(sourcePlayer::setCamera);
            sourcePlayer.setBoundingBox(ZERO);
        }
    }

    public @Nonnull ServerPlayer getOrCreateMainPlayer(ServerPlayer from) {
        if (mainPlayer == null) {
            mainPlayer = from.getUUID();
        }
        //noinspection DataFlowIssue
        return from.server.getPlayerList().getPlayer(mainPlayer);
    }

    public Optional<ServerPlayer> getMainPlayerServer() {
        return mainPlayer == null ? Optional.empty() : Optional.ofNullable(server.getPlayerList().getPlayer(mainPlayer));
    }

    public boolean isFioPlayer(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        return NAME.equals(player.getGameProfile().getName());
    }

    public boolean isMainPlayer(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        return mainPlayer != null && player.getUUID().equals(mainPlayer);
    }

    public boolean isArm(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerParts.inverse().get(player.getUUID());
        return part == Part.LEFT_ARM || part == Part.RIGHT_ARM;
    }

    public boolean isHead(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerParts.inverse().get(player.getUUID());
        return part == Part.HEAD;
    }

    public boolean isFoot(@Nullable Player player) {
        if (player == null) {
            return false;
        }
        var part = playerParts.inverse().get(player.getUUID());
        return part == Part.LEFT_FOOT || part == Part.RIGHT_FOOT;
    }

    public <MSG> void relayToMain(MSG packet) {
        getMainPlayerServer().ifPresent(main -> NetworkHelper.sendToPlayer(main, packet));
    }

    public <MSG> void broadCastExcept(MSG packet, UUID except) {
        playerParts.forEach((part, uuid) -> {
            if (uuid.equals(except)) {
                return;
            }
            NetworkHelper.sendToPlayer(server.getPlayerList().getPlayer(uuid), packet);
        });
    }

    public Optional<Part> getPart(@Nullable Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(playerParts.inverse().get(player.getUUID()));
    }
}
