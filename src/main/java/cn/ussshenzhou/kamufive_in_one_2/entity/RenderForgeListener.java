package cn.ussshenzhou.kamufive_in_one_2.entity;

import cn.ussshenzhou.kamufive_in_one_2.FioManagerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderForgeListener {

    private static FioPlayerRenderer fioPlayerRenderer = null;

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (fioPlayerRenderer == null) {
            fioPlayerRenderer = (FioPlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(event.getEntity().getType());
        }
        event.setCanceled(true);
        var player = event.getEntity();
        if (FioManagerClient.getInstanceClient().mainPlayer != null && FioManagerClient.getInstanceClient().mainPlayer.equals(player.getUUID())) {
            fioPlayerRenderer.render(player,
                    Mth.lerp(event.getPartialTick(), player.yRotO, player.getYRot()),
                    event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()
            );
        }
    }
}
