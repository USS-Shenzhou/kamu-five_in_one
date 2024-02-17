package cn.ussshenzhou.kamufive_in_one_2;

import cn.ussshenzhou.kamufive_in_one_2.entity.FioPlayerClient;
import cn.ussshenzhou.kamufive_in_one_2.entity.FioPlayerServer;
import net.minecraft.client.player.AbstractClientPlayer;
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

import javax.annotation.Nullable;

/**
 * @author USS_Shenzhou
 */
public class FioManager {

    public static final String NAME = "我们一起";

    private static FioPlayerServer mainPlayer;
    private static MinecraftServer server = (MinecraftServer) LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER);

    public static FioPlayerServer getOrCreateMainPlayer(Player from) {
        if (mainPlayer == null) {
            mainPlayer = FioPlayerServer.create((ServerLevel) from.level(), from.getX(), from.getY(), from.getZ());
            from.level().addFreshEntity(mainPlayer);
        }
        return mainPlayer;
    }

    public static boolean isFioPlayer(Player player) {
        return NAME.equals(player.getGameProfile().getName());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Client {

        public static FioPlayerClient mainPlayer;

        public static @Nullable FioPlayerClient getOrCreateMainPlayer(Player from) {
            return mainPlayer;
        }

        @OnlyIn(Dist.CLIENT)
        @SubscribeEvent
        public static void replaceOnClient(EntityJoinLevelEvent event) {
            if (event.getEntity() instanceof AbstractClientPlayer player && FioManager.isFioPlayer(player)) {
                event.setCanceled(true);
                var fioPlayerClient = new FioPlayerClient(player.clientLevel, player.getGameProfile());
                fioPlayerClient.restoreFrom(player);
                player.clientLevel.addFreshEntity(fioPlayerClient);
            }
            if (event.getEntity() instanceof FioPlayerClient playerClient) {
                mainPlayer = playerClient;
            }
        }
    }

}
