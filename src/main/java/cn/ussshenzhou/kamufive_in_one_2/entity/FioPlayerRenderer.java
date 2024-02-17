package cn.ussshenzhou.kamufive_in_one_2.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 */
public class FioPlayerRenderer extends LivingEntityRenderer<Player, FioPlayerModel<Player>> {
    public FioPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new FioPlayerModel<>(context.bakeLayer(FioPlayerModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Player pEntity) {
        return DefaultPlayerSkin.getDefaultSkin();
    }
}
