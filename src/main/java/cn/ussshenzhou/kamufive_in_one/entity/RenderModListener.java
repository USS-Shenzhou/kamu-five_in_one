package cn.ussshenzhou.kamufive_in_one.entity;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderModListener {

    @SubscribeEvent
    public static void entityRendererRegistry(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityType.PLAYER, FIOPlayerRenderer::new);
    }

    @SubscribeEvent
    public static void regLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FIOPlayerModel.LAYER_LOCATION, FIOPlayerModel::createBodyLayer);
    }
}
