package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }

    @Inject(method = "sendPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z", shift = At.Shift.BEFORE))
    private void setOthersPos(CallbackInfo ci, @Local(ordinal = 1) boolean flag1) {
        /*if (flag1) {
            FioManager.Client.playerParts.forEach((part, uuid) -> {
                if (!this.getUUID().equals(uuid)) {
                    var player = Minecraft.getInstance().level.getPlayerByUUID(uuid);
                    if (player != null) {
                        player.setPos(this.getX(), this.getY(), this.getZ());
                    }
                }
            });
        }*/
    }
}
