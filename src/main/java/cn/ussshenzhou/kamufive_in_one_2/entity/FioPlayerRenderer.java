package cn.ussshenzhou.kamufive_in_one_2.entity;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.t88.T88;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.Iterator;

/**
 * @author USS_Shenzhou
 */
public class FioPlayerRenderer extends LivingEntityRenderer<Player, FioPlayerModel<Player>> {
    public FioPlayerRenderer(EntityRendererProvider.Context context) {
        super(context, new FioPlayerModel<>(context.bakeLayer(FioPlayerModel.LAYER_LOCATION)), 0);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)), new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(Player pEntity) {
        return DefaultPlayerSkin.getDefaultSkin();
    }

    @Override
    public void render(Player player, float pEntityYaw, float pPartialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (FioManager.Client.mainPlayer == null || !FioManager.Client.mainPlayer.equals(player.getUUID())) {
            return;
        }

        if (!MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(player, this, pPartialTicks, poseStack, multiBufferSource, packedLight))) {
            poseStack.pushPose();
            this.model.attackTime = this.getAttackAnim(player, pPartialTicks);
            boolean shouldSit = player.isPassenger() && player.getVehicle() != null && player.getVehicle().shouldRiderSit();
            this.model.riding = shouldSit;
            this.model.young = player.isBaby();
            float f = Mth.rotLerp(pPartialTicks, player.yBodyRotO, player.yBodyRot);
            float f1 = Mth.rotLerp(pPartialTicks, player.yHeadRotO, player.yHeadRot);
            float f2 = f1 - f;
            float f7;
            if (shouldSit && player.getVehicle() instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) player.getVehicle();
                f = Mth.rotLerp(pPartialTicks, livingentity.yBodyRotO, livingentity.yBodyRot);
                f2 = f1 - f;
                f7 = Mth.wrapDegrees(f2);
                if (f7 < -85.0F) {
                    f7 = -85.0F;
                }

                if (f7 >= 85.0F) {
                    f7 = 85.0F;
                }

                f = f1 - f7;
                if (f7 * f7 > 2500.0F) {
                    f += f7 * 0.2F;
                }

                f2 = f1 - f;
            }

            float f6 = Mth.lerp(pPartialTicks, player.xRotO, player.getXRot());
            if (isEntityUpsideDown(player)) {
                f6 *= -1.0F;
                f2 *= -1.0F;
            }

            float f8;
            if (player.hasPose(Pose.SLEEPING)) {
                Direction direction = player.getBedOrientation();
                if (direction != null) {
                    f8 = player.getEyeHeight(Pose.STANDING) - 0.1F;
                    poseStack.translate((float) (-direction.getStepX()) * f8, 0.0F, (float) (-direction.getStepZ()) * f8);
                }
            }

            f7 = this.getBob(player, pPartialTicks);
            this.setupRotations(player, poseStack, f7, f, pPartialTicks);
            poseStack.scale(-1.0F, -1.0F, 1.0F);
            this.scale(player, poseStack, pPartialTicks);
            poseStack.translate(0.0F, -1.501F, 0.0F);
            f8 = 0.0F;
            float f5 = 0.0F;
            if (!shouldSit && player.isAlive()) {
                f8 = player.walkAnimation.speed(pPartialTicks);
                f5 = player.walkAnimation.position(pPartialTicks);
                if (player.isBaby()) {
                    f5 *= 3.0F;
                }

                if (f8 > 1.0F) {
                    f8 = 1.0F;
                }
            }

            this.model.prepareMobModel(player, f5, f8, pPartialTicks);
            this.model.setupAnim(player, f5, f8, f7, f2, f6);
            Minecraft minecraft = Minecraft.getInstance();
            boolean flag = this.isBodyVisible(player);
            boolean flag1 = !flag && !player.isInvisibleTo(minecraft.player);
            boolean flag2 = minecraft.shouldEntityAppearGlowing(player);
            RenderType rendertype = this.getRenderType(player, flag, flag1, flag2);
            if (rendertype != null) {
                //VertexConsumer vertexconsumer = multiBufferSource.getBuffer(rendertype);
                //int i = getOverlayCoords(player, this.getWhiteOverlayProgress(player, pPartialTicks));
                //this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
                model.render(poseStack, multiBufferSource, packedLight, getOverlayCoords(player, this.getWhiteOverlayProgress(player, pPartialTicks)), pPartialTicks);
            }

            /*if (!player.isSpectator()) {
                Iterator var24 = this.layers.iterator();

                while(var24.hasNext()) {
                    RenderLayer<T, M> renderlayer = (RenderLayer)var24.next();
                    renderlayer.render(poseStack, multiBufferSource, packedLight, player, f5, f8, pPartialTicks, f7, f2, f6);
                }
            }*/

            poseStack.popPose();
            super.render(player, pEntityYaw, pPartialTicks, poseStack, multiBufferSource, packedLight);
            MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(player, this, pPartialTicks, poseStack, multiBufferSource, packedLight));
        }
    }

    @Override
    protected void renderNameTag(Player player, Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        if (FioManager.Client.mainPlayer == null || !FioManager.Client.mainPlayer.equals(player.getUUID())) {
            return;
        }

        super.renderNameTag(player, pDisplayName, pPoseStack, pBuffer, pPackedLight);
    }
}
