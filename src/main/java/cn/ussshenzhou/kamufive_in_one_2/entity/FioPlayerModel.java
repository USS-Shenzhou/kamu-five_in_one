package cn.ussshenzhou.kamufive_in_one_2.entity;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.FiveInOne;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 */
public class FioPlayerModel<T extends Player> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(FiveInOne.MOD_ID, "fio_player_model"), "main");

    private final ModelPart head;
    private final ModelPart rightArmBody;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart leftArmBody;
    private final ModelPart leftFootBody;
    private final ModelPart rightFootBody;

    public FioPlayerModel(ModelPart root) {
        this.head = root.getChild("Head");
        this.rightArmBody = root.getChild("RightArmBody");
        this.rightArm = root.getChild("RightArm");
        this.leftArm = root.getChild("LeftArm");
        this.rightLeg = root.getChild("RightLeg");
        this.leftLeg = root.getChild("LeftLeg");
        this.leftArmBody = root.getChild("LeftArmBody");
        this.leftFootBody = root.getChild("LeftFootBody");
        this.rightFootBody = root.getChild("RightFootBody");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition RightArmBody = partdefinition.addOrReplaceChild("RightArmBody", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 32).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LeftArmBody = partdefinition.addOrReplaceChild("LeftArmBody", CubeListBuilder.create().texOffs(20, 16).addBox(0.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 32).addBox(0.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightFootBody = partdefinition.addOrReplaceChild("RightFootBody", CubeListBuilder.create().texOffs(16, 22).addBox(-4.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 38).addBox(-4.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LeftFootBody = partdefinition.addOrReplaceChild("LeftFootBody", CubeListBuilder.create().texOffs(20, 22).addBox(0.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 38).addBox(0.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));


        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.HEAD, head);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.RIGHT_ARM, rightArmBody, rightArm);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.LEFT_ARM, leftArmBody, leftArm);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.RIGHT_FOOT, rightFootBody, rightLeg);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.LEFT_FOOT, leftFootBody, leftLeg);
    }

    private void renderPart(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay, Part part, ModelPart... modelParts) {
        var abstractClientPlayer = FioManager.Client.playerParts.get(part);
        if (abstractClientPlayer == null) {
            return;
        }
        var location = abstractClientPlayer.getSkinTextureLocation();
        var vertexConsumer = multiBufferSource.getBuffer(this.renderType(location));
        float alpha = part == FioManager.Client.part ? 0.15f : 1;
        for (ModelPart modelPart : modelParts) {
            poseStack.pushPose();
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, alpha);
            poseStack.popPose();
        }
    }
}
