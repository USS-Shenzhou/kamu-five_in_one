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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

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
                switch (part) {
                    case HEAD -> {
                        this.setRotation(entity.getViewYRot(partialTick), entity.getViewXRot(partialTick));
                        this.setPosition(
                                Mth.lerp(partialTick, entity.xo, entity.getX()),
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + Mth.lerp(partialTick, this.eyeHeightOld, this.eyeHeight),
                                Mth.lerp(partialTick, entity.zo, entity.getZ())
                        );
                    }
                    case RIGHT_FOOT -> {
                        var y = Mth.lerp(partialTick, entity.yRotO, yRot);
                        this.setRotation(y, 0);
                        this.setPosition(
                                Mth.lerp(partialTick, entity.xo, entity.getX()) + Mth.cos(y * Mth.PI / 180) * 2 / 16,
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getType().getHeight() / 10 / 16f,
                                Mth.lerp(partialTick, entity.zo, entity.getZ()) + Mth.sin(y * Mth.PI / 180) * 2 / 16
                        );
                    }
                    case LEFT_FOOT -> {
                        var y = Mth.lerp(partialTick, entity.yRotO, yRot);
                        this.setRotation(y, 0);
                        this.setPosition(
                                Mth.lerp(partialTick, entity.xo, entity.getX()) + Mth.cos(y * Mth.PI / 180) * -2 / 16,
                                Mth.lerp(partialTick, entity.yo, entity.getY()) + entity.getType().getHeight() / 16f,
                                Mth.lerp(partialTick, entity.zo, entity.getZ()) + Mth.sin(y * Mth.PI / 180) * -2 / 16
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
}
