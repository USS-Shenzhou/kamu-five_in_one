package cn.ussshenzhou.kamufive_in_one_2.mixin;


import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author USS_Shenzhou
 */
@Mixin(RenderType.class)
public class RenderTypeMixin {

    @Shadow @Final private static BiFunction<ResourceLocation, Boolean, RenderType> ENTITY_TRANSLUCENT;

    @Shadow @Final private static Function<ResourceLocation, RenderType> ENTITY_SOLID;

    @Inject(method = "entityCutout",at = @At("RETURN"),cancellable = true)
    private static void makeItTranslucent(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir){
        cir.setReturnValue(ENTITY_TRANSLUCENT.apply(pLocation,false));
    }

    @Inject(method = "entitySolid",at = @At("RETURN"),cancellable = true)
    private static void makeItTranslucent2(ResourceLocation pLocation, CallbackInfoReturnable<RenderType> cir){
        cir.setReturnValue(ENTITY_SOLID.apply(pLocation));
    }
}
