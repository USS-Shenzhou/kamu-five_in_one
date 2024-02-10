package cn.ussshenzhou.kamufive_in_one_2.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 */
public class FIOPlayerRenderer extends LivingEntityRenderer<Player, FIOPlayerModel<Player>> {
    public FIOPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new FIOPlayerModel<>(context.bakeLayer(FIOPlayerModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Player pEntity) {
        return DefaultPlayerSkin.getDefaultSkin();
    }
}
