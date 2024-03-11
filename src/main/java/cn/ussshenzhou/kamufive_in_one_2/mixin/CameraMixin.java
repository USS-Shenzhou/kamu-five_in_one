package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.*;

/**
 * @author USS_Shenzhou
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private boolean initialized;

    @Shadow
    private BlockGetter level;

    @Shadow
    private Entity entity;

    @Shadow
    private boolean detached;

    @Shadow
    protected abstract void setRotation(float pYRot, float pXRot);

    @Shadow
    protected abstract void setPosition(Vec3 pPos);

    @Shadow
    protected abstract void setPosition(double pX, double pY, double pZ);

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Shadow
    protected abstract void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset);

    @Shadow
    protected abstract double getMaxZoom(double pStartingDistance);

    @Shadow
    private float eyeHeightOld;

    @Shadow
    private float eyeHeight;

    @Shadow
    @Final
    private Quaternionf rotation;

    @Shadow
    @Final
    private Vector3f forwards;

    @Shadow
    @Final
    private Vector3f up;

    @Shadow
    @Final
    private Vector3f left;


    /**
     * @author
     * @reason
     */
    @Overwrite
    public void setup(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick) {
        this.initialized = true;
        this.level = level;
        this.entity = entity;
        this.detached = detached;
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            FioManager.Client.getPart().ifPresentOrElse(part -> {
                float footOffset = 4 / 16f;
                var bodyY = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
                switch (part) {
                    case HEAD -> {
                        //this.setRotation(entity.getViewYRot(partialTick), entity.getViewXRot(partialTick));
                        this.setPosition(
                                Mth.lerp(partialTick, entity.xo, entity.getX()),
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight),
                                Mth.lerp(partialTick, entity.zo, entity.getZ())
                        );
                    }
                    case LEFT_FOOT -> {
                        //this.setRotation(bodyY, 0);
                        this.setPosition(
                                Mth.lerp(partialTick, entity.xo, entity.getX()) + Mth.cos(bodyY * Mth.PI / 180) * footOffset,
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getType().getHeight() / 16f,
                                Mth.lerp(partialTick, entity.zo, entity.getZ()) + Mth.sin(bodyY * Mth.PI / 180) * footOffset
                        );
                    }
                    case RIGHT_FOOT -> {
                        //this.setRotation(bodyY, 0);
                        this.setPosition(
                                Mth.lerp(partialTick, entity.xo, entity.getX()) + Mth.cos(bodyY * Mth.PI / 180) * -footOffset,
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getType().getHeight() / 16f,
                                Mth.lerp(partialTick, entity.zo, entity.getZ()) + Mth.sin(bodyY * Mth.PI / 180) * -footOffset
                        );
                    }
                    case LEFT_ARM -> {
                        //var rot = FioManager.Client.getRotL(partialTick)
                        //        .rotateY(bodyY * Mth.PI / 180)
                        //        .rotateX(90 * Mth.PI / 180);
                        var original = new Vector3f(5.5f, 26, 0).mul(1 / 16f).rotate(Axis.YP.rotationDegrees(bodyY));
                        //var offset = new Vector3f(-1, -7, 2).mul(1 / 16f).rotate(rot);
                        //var offset = new Vector3f(0, 0, 0).mul(1 / 16f).rotate(rot);
                        //-5 22 0
                        //-1 7 0
                        this.setPosition(
                                //Mth.lerp(partialTick, entity.xo, entity.getX()) + original.x + offset.x,
                                //Mth.lerp(partialTick, entity.yo, entity.getY()) + original.y + offset.y,
                                //Mth.lerp(partialTick, entity.zo, entity.getZ()) + original.z + offset.z
                                Mth.lerp(partialTick, entity.xo, entity.getX()) + original.x,
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + original.y,
                                Mth.lerp(partialTick, entity.zo, entity.getZ()) + original.z
                        );
                    }
                    case RIGHT_ARM -> {
                        var original = new Vector3f(-5.5f, 26, 0).mul(1 / 16f).rotate(Axis.YP.rotationDegrees(bodyY));
                        this.setPosition(
                                Mth.lerp(partialTick, entity.xo, entity.getX()) + original.x,
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + original.y,
                                Mth.lerp(partialTick, entity.zo, entity.getZ()) + original.z
                        );
                    }
                }

            }, () -> {
                this.setRotation(entity.getViewYRot(partialTick), entity.getViewXRot(partialTick));
                this.setPosition(
                        Mth.lerp(partialTick, entity.xo, entity.getX()),
                        Mth.lerp(partialTick, entity.yo, entity.getY()) + Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight),
                        Mth.lerp(partialTick, entity.zo, entity.getZ())
                );
            });
        } else {
            this.setRotation(entity.getViewYRot(partialTick), entity.getViewXRot(partialTick));
            this.setPosition(
                    Mth.lerp(partialTick, entity.xo, entity.getX()),
                    Mth.lerp(partialTick, entity.yo, entity.getY()) + Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight),
                    Mth.lerp(partialTick, entity.zo, entity.getZ())
            );
        }
        if (detached) {
            if (thirdPersonReverse) {
                this.setRotation(this.yRot + 180.0F, -this.xRot);
            }
            this.move(-this.getMaxZoom(4.0D), 0.0D, 0.0D);
        } else if (entity instanceof LivingEntity && ((LivingEntity) entity).isSleeping()) {
            Direction direction = ((LivingEntity) entity).getBedOrientation();
            this.setRotation(direction != null ? direction.toYRot() - 180.0F : 0.0F, 0.0F);
            this.move(0.0D, 0.3D, 0.0D);
        }

    }

    @Unique
    protected void setRotation(Quaternionf rot) {
        var euler = rot.getEulerAnglesYXZ(new Vector3f());
        this.xRot = euler.x / Mth.PI * 180;
        this.yRot = euler.y / Mth.PI * 180;
        //this.rotation.rotationYXZ(-pYRot * ((float) Math.PI / 180F), pXRot * ((float) Math.PI / 180F), z * ((float) Math.PI / 180F));
        this.rotation.set(rot);
        this.forwards.set(0.0F, 0.0F, 1.0F).rotate(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F).rotate(this.rotation);
        this.left.set(1.0F, 0.0F, 0.0F).rotate(this.rotation);
    }
}
