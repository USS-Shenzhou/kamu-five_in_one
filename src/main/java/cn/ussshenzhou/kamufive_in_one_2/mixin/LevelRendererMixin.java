package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManagerClient;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author USS_Shenzhou
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {


    @Redirect(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSleeping()Z"))
    private boolean alwaysRenderBody(LivingEntity instance) {
        //noinspection SimplifiableConditionalExpression
        return FioManagerClient.getInstanceClient().part == Part.HEAD ? instance.isSleeping() : true;
    }
}
