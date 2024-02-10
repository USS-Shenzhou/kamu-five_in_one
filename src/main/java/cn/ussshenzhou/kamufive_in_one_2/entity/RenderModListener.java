package cn.ussshenzhou.kamufive_in_one_2.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
