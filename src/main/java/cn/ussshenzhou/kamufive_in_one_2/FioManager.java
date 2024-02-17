package cn.ussshenzhou.kamufive_in_one_2;

import cn.ussshenzhou.kamufive_in_one_2.entity.FioPlayerClient;
import cn.ussshenzhou.kamufive_in_one_2.entity.FioPlayerServer;
import cn.ussshenzhou.kamufive_in_one_2.network.SelectPartPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author USS_Shenzhou
 */
public class FioManager {

    public static final String NAME = "我们一起";

    private static @Nullable FioPlayerServer mainPlayer;
    private static MinecraftServer server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);

    public static void selectPart(CommandSourceStack source, Part part) {
        var sourcePlayer = source.getPlayer();
        assert sourcePlayer != null;
        if (part == Part.HEAD && mainPlayer == null) {
            mainPlayer = FioManager.getOrCreateMainPlayer(sourcePlayer);
            NetworkHelper.sendTo(PacketDistributor.ALL.noArg(), new SelectPartPacket(sourcePlayer.getUUID(), part));

            return;
        }
        switch (part) {
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

    public static @Nonnull FioPlayerServer getOrCreateMainPlayer(ServerPlayer from) {
        if (mainPlayer == null) {
            mainPlayer = FioPlayerServer.create(from, (ServerLevel) from.level(), from.getX(), from.getY(), from.getZ());
            from.level().addFreshEntity(mainPlayer);
        }
        return mainPlayer;
    }

    public static boolean isFioPlayer(Player player) {
        return NAME.equals(player.getGameProfile().getName());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    @OnlyIn(Dist.CLIENT)
    public static class Client {

        public static @Nullable FioPlayerClient mainPlayer;
        public static Map<Part, AbstractClientPlayer> playerParts = new HashMap<>();
        public static @Nullable Part part;

        public static @Nullable FioPlayerClient getOrCreateMainPlayer(Player from) {
            return mainPlayer;
        }

        public static @Nonnull FioPlayerClient getMainPlayer() {
            if (mainPlayer == null) {
                throw new IllegalStateException();
            }
            return mainPlayer;
        }

        @SubscribeEvent
        public static void replaceOnClient(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof AbstractClientPlayer player && FioManager.isFioPlayer(player)) {
                event.setCanceled(true);
                var fioPlayerClient = FioPlayerClient.create();
                fioPlayerClient.restoreFrom(player);
                player.clientLevel.addFreshEntity(fioPlayerClient);
            }
            if (event.getEntity() instanceof FioPlayerClient playerClient) {
                mainPlayer = playerClient;
            }
        }
    }

}
