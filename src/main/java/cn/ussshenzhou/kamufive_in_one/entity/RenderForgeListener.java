package cn.ussshenzhou.kamufive_in_one.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderForgeListener {

    private static FIOPlayerRenderer fioPlayerRenderer = null;

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (fioPlayerRenderer == null) {
            fioPlayerRenderer = (FIOPlayerRenderer) Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(event.getEntity().getType());
        }
        event.setCanceled(true);
        var player = event.getEntity();
        fioPlayerRenderer.render(player,
                Mth.lerp(event.getPartialTick(), player.yRotO, player.getYRot()),
                event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight()
        );
    }
}
