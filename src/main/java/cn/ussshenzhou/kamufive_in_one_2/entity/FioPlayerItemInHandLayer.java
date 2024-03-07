package cn.ussshenzhou.kamufive_in_one_2.entity;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author USS_Shenzhou
 */
@OnlyIn(Dist.CLIENT)
public class FioPlayerItemInHandLayer<T extends Player, M extends EntityModel<T> & ArmedModel & HeadedModel> extends ItemInHandLayer<T, M> {
    private final ItemInHandRenderer itemInHandRenderer;
    private static final float X_ROT_MIN = (-(float) Math.PI / 6F);
    private static final float X_ROT_MAX = ((float) Math.PI / 2F);

    public FioPlayerItemInHandLayer(RenderLayerParent<T, M> pRenderer, ItemInHandRenderer pItemInHandRenderer) {
        super(pRenderer, pItemInHandRenderer);
        this.itemInHandRenderer = pItemInHandRenderer;
    }


    @Override
    public void render(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, T pLivingEntity, float pLimbSwing, float pLimbSwingAmount, float pPartialTicks, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        boolean flag = pLivingEntity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack itemstack = flag ? pLivingEntity.getOffhandItem() : pLivingEntity.getMainHandItem();
        ItemStack itemstack1 = flag ? pLivingEntity.getMainHandItem() : pLivingEntity.getOffhandItem();
        if (!itemstack.isEmpty() || !itemstack1.isEmpty()) {
            pPoseStack.pushPose();
            if (this.getParentModel().young) {
                float f = 0.5F;
                pPoseStack.translate(0.0F, 0.75F, 0.0F);
                pPoseStack.scale(0.5F, 0.5F, 0.5F);
            }

            this.renderArmWithItem(pLivingEntity, itemstack1, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, pPoseStack, pBuffer, pPackedLight, pPartialTicks);
            this.renderArmWithItem(pLivingEntity, itemstack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, pPoseStack, pBuffer, pPackedLight, pPartialTicks);
            pPoseStack.popPose();
        }
    }


    protected void renderArmWithItem(LivingEntity pLivingEntity, ItemStack pItemStack, ItemDisplayContext pDisplayContext, HumanoidArm pArm, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, float partialTick) {
        if (pItemStack.is(Items.SPYGLASS) && pLivingEntity.getUseItem() == pItemStack && pLivingEntity.swingTime == 0) {
            this.renderArmWithSpyglass(pLivingEntity, pItemStack, pArm, poseStack, pBuffer, pPackedLight);
        } else {
            if (!pItemStack.isEmpty()) {
                poseStack.pushPose();
                //this.getParentModel().translateToHand(pArm, poseStack);
                //poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                //poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                if (pArm == HumanoidArm.LEFT) {
                    poseStack.translate(0.5, 0, 0);
                    poseStack.rotateAround(FioManager.Client.getRotL(partialTick), 0, 0, 0);
                } else if (pArm == HumanoidArm.RIGHT) {
                    poseStack.translate(-0.5, 0, 0);
                    poseStack.rotateAround(FioManager.Client.getRotR(partialTick), 0, 0, 0);
                }
                boolean flag = pArm == HumanoidArm.LEFT;
                poseStack.translate((float) (flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
                this.itemInHandRenderer.renderItem(pLivingEntity, pItemStack, pDisplayContext, flag, poseStack, pBuffer, pPackedLight);
                poseStack.popPose();
            }
        }

    }

    private void renderArmWithSpyglass(LivingEntity pEntity, ItemStack pStack, HumanoidArm pArm, PoseStack pPoseStack, MultiBufferSource pBuffer, int pCombinedLight) {
        pPoseStack.pushPose();
        ModelPart modelpart = this.getParentModel().getHead();
        float f = modelpart.xRot;
        modelpart.xRot = Mth.clamp(modelpart.xRot, (-(float) Math.PI / 6F), ((float) Math.PI / 2F));
        modelpart.translateAndRotate(pPoseStack);
        modelpart.xRot = f;
        CustomHeadLayer.translateToHead(pPoseStack, false);
        boolean flag = pArm == HumanoidArm.LEFT;
        pPoseStack.translate((flag ? -2.5F : 2.5F) / 16.0F, -0.0625F, 0.0F);
        this.itemInHandRenderer.renderItem(pEntity, pStack, ItemDisplayContext.HEAD, false, pPoseStack, pBuffer, pCombinedLight);
        pPoseStack.popPose();
    }


}