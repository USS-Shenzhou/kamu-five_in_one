package cn.ussshenzhou.kamufive_in_one_2.entity;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.FiveInOne;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * @author USS_Shenzhou
 */
public class FioPlayerModel<T extends Player> extends HumanoidModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(FiveInOne.MOD_ID, "fio_player_model"), "main");

    private final ModelPart headF;
    private final ModelPart rightArmBodyF;
    private final ModelPart rightArmF;
    private final ModelPart leftArmF;
    private final ModelPart rightLegF;
    private final ModelPart leftLegF;
    private final ModelPart leftArmBodyF;
    private final ModelPart leftFootBodyF;
    private final ModelPart rightFootBodyF;

    public FioPlayerModel(ModelPart root) {
        super(root);
        this.headF = root.getChild("HeadF");
        this.rightArmBodyF = root.getChild("RightArmBodyF");
        this.rightArmF = root.getChild("RightArmF");
        this.leftArmF = root.getChild("LeftArmF");
        this.rightLegF = root.getChild("RightLegF");
        this.leftLegF = root.getChild("LeftLegF");
        this.leftArmBodyF = root.getChild("LeftArmBodyF");
        this.leftFootBodyF = root.getChild("LeftFootBodyF");
        this.rightFootBodyF = root.getChild("RightFootBodyF");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Head = partdefinition.addOrReplaceChild("HeadF", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArmF", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArmF", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLegF", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLegF", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition RightArmBody = partdefinition.addOrReplaceChild("RightArmBodyF", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 32).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LeftArmBody = partdefinition.addOrReplaceChild("LeftArmBodyF", CubeListBuilder.create().texOffs(20, 16).addBox(0.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 32).addBox(0.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition RightFootBody = partdefinition.addOrReplaceChild("RightFootBodyF", CubeListBuilder.create().texOffs(16, 22).addBox(-4.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(24, 38).addBox(-4.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition LeftFootBody = partdefinition.addOrReplaceChild("LeftFootBodyF", CubeListBuilder.create().texOffs(20, 22).addBox(0.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(18, 38).addBox(0.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));


        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        headF.copyFrom(head);
        rightArmBodyF.copyFrom(body);
        leftArmBodyF.copyFrom(body);
        rightFootBodyF.copyFrom(body);
        leftFootBodyF.copyFrom(body);
        rightArmF.copyFrom(rightArm);
        rightLegF.copyFrom(rightLeg);
        leftArmF.copyFrom(leftArm);
        leftLegF.copyFrom(leftLeg);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.HEAD, headF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.RIGHT_ARM, rightArmBodyF, rightArmF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.LEFT_ARM, leftArmBodyF, leftArmF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.RIGHT_FOOT, rightFootBodyF, rightLegF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, Part.LEFT_FOOT, leftFootBodyF, leftLegF);
    }

    private void renderPart(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay, Part part, ModelPart... modelParts) {
        var uuid = FioManager.Client.playerParts.get(part);
        if (uuid == null) {
            return;
        }
        var player = ((AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(uuid));
        if (player == null) {
            return;
        }
        var location = player.getSkinTextureLocation();
        var vertexConsumer = multiBufferSource.getBuffer(this.renderType(location));
        float alpha = part == FioManager.Client.part ? 0.15f : 1;
        for (ModelPart modelPart : modelParts) {
            poseStack.pushPose();
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, alpha);
            poseStack.popPose();
        }
    }
}
