package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author USS_Shenzhou
 */
@Mixin(Player.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        if (this.level().isClientSide) {
            return this.getUUID().equals(FioManager.Client.mainPlayer) ? super.getDimensions(pPose) : EntityDimensions.fixed(0, 0);
        } else {
            return this.getUUID().equals(FioManager.mainPlayer) ? super.getDimensions(pPose) : EntityDimensions.fixed(0, 0);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
