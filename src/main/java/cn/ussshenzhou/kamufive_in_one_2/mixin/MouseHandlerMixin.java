package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.InputHelper;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import cn.ussshenzhou.kamufive_in_one_2.network.MouseMoveRelayPacket;
import cn.ussshenzhou.kamufive_in_one_2.network.MousePressRelayPacket;
import cn.ussshenzhou.kamufive_in_one_2.network.MouseScrollRelayPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@SuppressWarnings("DuplicatedCode")
@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Shadow
    private double xpos;

    @Shadow
    private double ypos;

    @Inject(method = "onPress", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWindow()J", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void interceptMousePress(long pWindowPointer, int pButton, int pAction, int pModifiers, CallbackInfo ci) {
        if (FioManager.Client.mainPlayer != null
                && FioManager.Client.part != null
                && Minecraft.getInstance().screen == null
                //needCheck
                && !FioManager.Client.isMainPlayer(Minecraft.getInstance().player)) {
            if (Minecraft.getInstance().player != null) {
                NetworkHelper.sendToServer(new MousePressRelayPacket(Minecraft.getInstance().player.getUUID(), pButton, pAction, pModifiers));
                ci.cancel();
            }
        }
    }

    @SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWindow()J", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void interceptMouseScroll(long pWindowPointer, double pXOffset, double pYOffset, CallbackInfo ci) {
        if (FioManager.Client.mainPlayer != null
                && FioManager.Client.part != null
                && Minecraft.getInstance().screen == null
                //needCheck
                && !FioManager.Client.isMainPlayer(Minecraft.getInstance().player)) {
            if (Minecraft.getInstance().player != null) {
                NetworkHelper.sendToServer(new MouseScrollRelayPacket(Minecraft.getInstance().player.getUUID(), pXOffset, pYOffset));
                ci.cancel();
            }
        }
    }

    @Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWindow()J", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void interceptMouseMove(long pWindowPointer, double pXpos, double pYpos, CallbackInfo ci) {
        if (InputHelper.inGame()) {
            //noinspection DataFlowIssue
            switch (FioManager.Client.part) {
                case HEAD -> {
                    if (FioManager.Client.isMainPlayer()) {
                        //turn to vanilla local turnPlayer.
                    } else {
                        //send to main player to execute turning
                        NetworkHelper.sendToServer(new MouseMoveRelayPacket(Minecraft.getInstance().player.getUUID(), pXpos - this.xpos, pYpos - this.ypos));
                        ci.cancel();
                    }
                    break;
                }
                case LEFT_ARM, RIGHT_ARM -> {
                    //TODO turn on local, then notify others
                    break;
                }
                case LEFT_FOOT, RIGHT_FOOT -> {
                    //foot can't turn
                    ci.cancel();
                    break;
                }
            }
        }
    }
}
