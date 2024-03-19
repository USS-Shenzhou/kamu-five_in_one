package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.FioManagerClient;
import cn.ussshenzhou.kamufive_in_one_2.mixinproxy.PlayerMixinProxy;
import com.mojang.authlib.GameProfile;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

/**
 * @author USS_Shenzhou
 */
@Mixin(Player.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("unused")
public abstract class PlayerMixin extends LivingEntity implements PlayerMixinProxy {

    @Shadow
    public long timeEntitySatOnShoulder;

    @Shadow
    @Final
    public Inventory inventory;

    @Shadow
    public PlayerEnderChestContainer enderChestInventory;

    @Shadow
    @Final
    public InventoryMenu inventoryMenu;

    @Shadow
    public AbstractContainerMenu containerMenu;

    @Shadow
    public FoodData foodData;

    @Shadow
    public int jumpTriggerTime;

    @Shadow
    public float oBob;

    @Shadow
    public float bob;

    @Shadow
    public int takeXpDelay;

    @Shadow
    public double xCloakO;

    @Shadow
    public double yCloakO;

    @Shadow
    public double zCloakO;

    @Shadow
    public double xCloak;

    @Shadow
    public double yCloak;

    @Shadow
    public double zCloak;

    @Shadow
    public int sleepCounter;

    @Shadow
    public boolean wasUnderwater;

    @Shadow
    @Final
    public Abilities abilities;

    @Shadow
    public int experienceLevel;

    @Shadow
    public int totalExperience;

    @Shadow
    public float experienceProgress;

    @Shadow
    public int enchantmentSeed;

    @Shadow
    @Final
    public float defaultFlySpeed;

    @Shadow
    public int lastLevelUpTime;

    @Shadow
    @Final
    public GameProfile gameProfile;

    @Shadow
    public boolean reducedDebugInfo;

    @Shadow
    public ItemStack lastItemInMainHand;

    @Shadow
    @Final
    public ItemCooldowns cooldowns;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Shadow
    public Optional<GlobalPos> lastDeathLocation;

    @Shadow
    @Nullable
    public FishingHook fishing;

    @Shadow
    public float hurtDir;

    @Shadow
    @Final
    public java.util.Collection<MutableComponent> prefixes;

    @Shadow
    @Final
    public java.util.Collection<MutableComponent> suffixes;

    @Shadow
    @Nullable
    public Pose forcedPose;

    protected PlayerMixin(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }


    //hit result------------
    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        if (this.level().isClientSide) {
            return this.getUUID().equals(FioManagerClient.getInstanceClient().mainPlayer) ? super.getDimensions(pPose) : EntityDimensions.fixed(0, 0);
        } else {
            return this.getUUID().equals(FioManager.getInstanceServer().mainPlayer) ? super.getDimensions(pPose) : EntityDimensions.fixed(0, 0);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
