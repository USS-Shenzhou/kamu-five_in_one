package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.InputHelper;
import cn.ussshenzhou.kamufive_in_one_2.network.MouseMoveRelayPacket;
import cn.ussshenzhou.kamufive_in_one_2.network.MousePressRelayPacket;
import cn.ussshenzhou.kamufive_in_one_2.network.MouseScrollRelayPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.util.SmoothDouble;
import org.spongepowered.asm.mixin.Final;
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

    @Shadow
    private double lastMouseEventTime;

    @Shadow
    @Final
    private SmoothDouble smoothTurnX;

    @Shadow
    @Final
    private SmoothDouble smoothTurnY;

    @Shadow
    private double accumulatedDX;

    @Shadow
    private double accumulatedDY;

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
                        var w = Minecraft.getInstance().getWindow();
                        double scale = (double) w.getGuiScaledWidth() / (double) w.getScreenWidth();
                        NetworkHelper.sendToServer(new MouseMoveRelayPacket(Minecraft.getInstance().player.getUUID(), (pXpos - this.xpos) * scale, (pYpos - this.ypos) * scale));
                        ci.cancel();
                    }
                    break;
                }
                case LEFT_ARM, RIGHT_ARM -> {
                    //turn on local, then notify others
                    double dx = pXpos - this.xpos;
                    double dy = pYpos - this.ypos;
                    this.xpos = pXpos;
                    this.ypos = pYpos;
                    double d0 = Blaze3D.getTime();
                    var minecraft = Minecraft.getInstance();
                    double d1 = d0 - lastMouseEventTime;
                    lastMouseEventTime = d0;
                    double d4 = minecraft.options.sensitivity().get() * (double) 0.6F + (double) 0.2F;
                    double d5 = d4 * d4 * d4;
                    double d6 = d5 * 8.0D;
                    double d2;
                    double d3;
                    if (minecraft.options.smoothCamera) {
                        d2 = smoothTurnX.getNewDeltaValue(dx * d6, d1 * d6);
                        d3 = smoothTurnY.getNewDeltaValue(dy * d6, d1 * d6);
                    } else {
                        smoothTurnX.reset();
                        smoothTurnY.reset();
                        d2 = dx * d5;
                        d3 = dy * d5;
                    } //else {
                    //    smoothTurnX.reset();
                    //    smoothTurnY.reset();
                    //    d2 = dx * d6;
                    //    d3 = dy * d6;
                    //}

                    FioManager.Client.rotArmAndBroadCast(d2, d3);
                    ci.cancel();
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
