package cn.ussshenzhou.kamufive_in_one_2.mixin;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.InputHelper;
import cn.ussshenzhou.kamufive_in_one_2.network.KeyPressRelayPacket;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author USS_Shenzhou
 */
@Mixin(KeyboardHandler.class)
public class KeyBoardHandlerMixin {

    @Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWindow()J", ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void interceptKeyInput(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo ci) {
        if (FioManager.Client.mainPlayer != null
                && FioManager.Client.part != null
                && !InputHelper.ignore(pKey)
                && !FioManager.Client.isMainPlayer(Minecraft.getInstance().player)) {
            assert Minecraft.getInstance().player != null;
            NetworkHelper.sendToServer(new KeyPressRelayPacket(Minecraft.getInstance().player.getUUID(), pKey, pScanCode, pAction, pModifiers));
            ci.cancel();
        }
    }
}
