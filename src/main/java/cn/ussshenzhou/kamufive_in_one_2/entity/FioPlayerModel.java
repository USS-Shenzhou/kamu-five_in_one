package cn.ussshenzhou.kamufive_in_one_2.entity;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.FiveInOne;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.MatrixUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;

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
        super(root, RenderType::entityTranslucent);
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
        leftArmBodyF.copyFrom(body);
        rightArmBodyF.copyFrom(body);
        leftFootBodyF.copyFrom(body);
        rightFootBodyF.copyFrom(body);


        leftArmF.copyFrom(leftArm);
        leftArmF.xRot = 0;
        leftArmF.yRot = 0;
        leftArmF.zRot = 0;

        leftLegF.copyFrom(leftLeg);

        rightArmF.copyFrom(rightArm);
        rightArmF.xRot = 0;
        rightArmF.yRot = 0;
        rightArmF.zRot = 0;

        rightLegF.copyFrom(rightLeg);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
    }

    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay, float partialTick) {
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, partialTick, Part.HEAD, headF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, partialTick, Part.RIGHT_ARM, rightArmBodyF, rightArmF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, partialTick, Part.LEFT_ARM, leftArmBodyF, leftArmF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, partialTick, Part.RIGHT_FOOT, rightFootBodyF, rightLegF);
        renderPart(poseStack, multiBufferSource, packedLight, packedOverlay, partialTick, Part.LEFT_FOOT, leftFootBodyF, leftLegF);
    }

    private void renderPart(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay, float partialTick, Part part, ModelPart... modelParts) {
        var uuid = FioManager.Client.playerPartsClient.get(part);
        if (uuid == null) {
            return;
        }
        var player = ((AbstractClientPlayer) Minecraft.getInstance().level.getPlayerByUUID(uuid));
        if (player == null) {
            return;
        }
        var location = player.getSkinTextureLocation();
        var vertexConsumer = multiBufferSource.getBuffer(this.renderType(location));
        float alpha = part == FioManager.Client.part ? 0.2f : 1;
        for (ModelPart modelPart : modelParts) {
            poseStack.pushPose();
            if (modelPart == leftArmF) {
                var r = Mth.lerp(partialTick, player.yRotO, player.getYRot());
                var bodyR = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
                poseStack.mulPose(Axis.YN.rotationDegrees(bodyR - r));
                poseStack.rotateAround(FioManager.Client.getRotL(partialTick), 5 / 16f, 0, 0);
            } else if (modelPart == rightArmF) {
                var r = Mth.lerp(partialTick, player.yRotO, player.getYRot());
                var bodyR = Mth.lerp(partialTick, player.yBodyRotO, player.yBodyRot);
                poseStack.mulPose(Axis.YN.rotationDegrees(bodyR - r));
                poseStack.rotateAround(FioManager.Client.getRotR(partialTick), -5 / 16f, 0, 0);
            }
            modelPart.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, alpha);
            if (modelPart == leftArmF || modelPart == rightArmF) {
                renderItem(player, poseStack, packedLight, packedOverlay, partialTick, modelPart, multiBufferSource, vertexConsumer, alpha);
            }
            poseStack.popPose();
        }
    }

    private void renderItem(Player player, PoseStack poseStack, int packedLight, int packedOverlay, float partialTick, ModelPart modelPart, MultiBufferSource multiBufferSource, VertexConsumer vertexConsumer, float alpha) {
        boolean flag = player.getMainArm() == HumanoidArm.RIGHT;
        ItemStack itemstack = flag ? player.getOffhandItem() : player.getMainHandItem();
        ItemStack itemstack1 = flag ? player.getMainHandItem() : player.getOffhandItem();
        poseStack.pushPose();
        if (modelPart == leftArmF) {
            prepareItem(poseStack, modelPart);
            renderItem(player, itemstack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, poseStack, multiBufferSource, packedLight, packedOverlay, alpha, modelPart);
        } else if (modelPart == rightArmF) {
            prepareItem(poseStack, modelPart);
            renderItem(player, itemstack1, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, multiBufferSource, packedLight, packedOverlay, alpha, modelPart);
        }
        poseStack.popPose();
    }

    private void prepareItem(PoseStack poseStack, ModelPart modelPart) {
        poseStack.translate(modelPart.x / 16.0F, modelPart.y / 16.0F, modelPart.z / 16.0F);
        poseStack.translate((modelPart == leftArmF ? 1 : -1) / 16f, 10 / 16F, -2 / 16f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
    }

    /**
     * chaos!
     */
    private void renderItem(Player player, ItemStack item, ItemDisplayContext itemDisplayContext, boolean leftHand, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay, float alpha, ModelPart modelPart) {
        if (!item.isEmpty()) {
            var itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel bakedmodel = itemRenderer.getModel(item, player.level(), player, 42);
            poseStack.pushPose();
            boolean flag = itemDisplayContext == ItemDisplayContext.GUI || itemDisplayContext == ItemDisplayContext.GROUND || itemDisplayContext == ItemDisplayContext.FIXED;
            if (flag) {
                if (item.is(Items.TRIDENT)) {
                    bakedmodel = itemRenderer.itemModelShaper.getModelManager().getModel(ItemRenderer.TRIDENT_MODEL);
                } else if (item.is(Items.SPYGLASS)) {
                    bakedmodel = itemRenderer.itemModelShaper.getModelManager().getModel(ItemRenderer.SPYGLASS_MODEL);
                }
            }

            bakedmodel = ForgeHooksClient.handleCameraTransforms(poseStack, bakedmodel, itemDisplayContext, leftHand);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            if (!bakedmodel.isCustomRenderer() && (!item.is(Items.TRIDENT) || flag)) {
                boolean flag1;
                if (itemDisplayContext != ItemDisplayContext.GUI && !itemDisplayContext.firstPerson() && item.getItem() instanceof BlockItem) {
                    Block block = ((BlockItem) item.getItem()).getBlock();
                    flag1 = !(block instanceof HalfTransparentBlock) && !(block instanceof StainedGlassPaneBlock);
                } else {
                    flag1 = true;
                }
                for (var model : bakedmodel.getRenderPasses(item, flag1)) {
                    for (var rendertype : model.getRenderTypes(item, flag1)) {
                        if (rendertype == Sheets.cutoutBlockSheet() || rendertype == Sheets.solidBlockSheet()) {
                            rendertype = Sheets.translucentCullBlockSheet();
                        }
                        VertexConsumer vertexconsumer;
                        if (hasAnimatedTexture(item) && item.hasFoil()) {
                            poseStack.pushPose();
                            PoseStack.Pose posestack$pose = poseStack.last();
                            if (itemDisplayContext == ItemDisplayContext.GUI) {
                                MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.5F);
                            } else if (itemDisplayContext.firstPerson()) {
                                MatrixUtil.mulComponentWise(posestack$pose.pose(), 0.75F);
                            }

                            if (flag1) {
                                vertexconsumer = ItemRenderer.getCompassFoilBufferDirect(multiBufferSource, rendertype, posestack$pose);
                            } else {
                                vertexconsumer = ItemRenderer.getCompassFoilBuffer(multiBufferSource, rendertype, posestack$pose);
                            }

                            poseStack.popPose();
                        } else if (flag1) {
                            vertexconsumer = ItemRenderer.getFoilBufferDirect(multiBufferSource, rendertype, true, item.hasFoil());
                        } else {
                            vertexconsumer = ItemRenderer.getFoilBuffer(multiBufferSource, rendertype, true, item.hasFoil());
                        }

                        RandomSource randomsource = RandomSource.create();
                        long i = 42L;

                        for (Direction direction : Direction.values()) {
                            randomsource.setSeed(42L);
                            this.renderQuadList(poseStack, vertexconsumer, model.getQuads(null, direction, randomsource), item, packedLight, packedOverlay, alpha, itemRenderer);
                        }

                        randomsource.setSeed(42L);
                        this.renderQuadList(poseStack, vertexconsumer, model.getQuads(null, null, randomsource), item, packedLight, packedOverlay, alpha, itemRenderer);
                    }
                }
            } else {
                IClientItemExtensions.of(item).getCustomRenderer().renderByItem(item, itemDisplayContext, poseStack, multiBufferSource, packedLight, packedOverlay);
            }

            poseStack.popPose();
        }
    }

    public void renderQuadList(PoseStack pPoseStack, VertexConsumer pBuffer, List<BakedQuad> pQuads, ItemStack pItemStack, int pCombinedLight, int pCombinedOverlay, float alpha, ItemRenderer itemRenderer) {
        boolean flag = !pItemStack.isEmpty();
        PoseStack.Pose posestack$pose = pPoseStack.last();

        for (BakedQuad bakedquad : pQuads) {
            int i = -1;
            if (flag && bakedquad.isTinted()) {
                i = itemRenderer.itemColors.getColor(pItemStack, bakedquad.getTintIndex());
            }

            float f = (float) (i >> 16 & 255) / 255.0F;
            float f1 = (float) (i >> 8 & 255) / 255.0F;
            float f2 = (float) (i & 255) / 255.0F;
            pBuffer.putBulkData(posestack$pose, bakedquad, f, f1, f2, alpha, pCombinedLight, pCombinedOverlay, true);
        }

    }

    private static boolean hasAnimatedTexture(ItemStack pStack) {
        return pStack.is(ItemTags.COMPASSES) || pStack.is(Items.CLOCK);
    }
}
