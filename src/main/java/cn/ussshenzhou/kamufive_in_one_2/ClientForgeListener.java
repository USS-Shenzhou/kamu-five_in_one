package cn.ussshenzhou.kamufive_in_one_2;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeListener {

    @SubscribeEvent
    public static void cameraRot(ViewportEvent.ComputeCameraAngles event) {
        var entity = event.getCamera().getEntity();
        var partialTick = (float) event.getPartialTick();
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            FioManager.Client.getPart().ifPresentOrElse(part -> {
                var bodyY = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
                switch (part) {
                    case HEAD -> {
                        event.setPitch(entity.getViewXRot(partialTick));
                        event.setYaw(entity.getViewYRot(partialTick));
                        event.setRoll(0);
                    }
                    case LEFT_FOOT, RIGHT_FOOT -> {
                        event.setPitch(entity.getViewXRot(partialTick));
                        event.setYaw(bodyY);
                        event.setRoll(0);
                    }
                    case LEFT_ARM, RIGHT_ARM -> {
                        //FIXME not entirely right
                        var rot = FioManager.Client.getRotL(partialTick)
                                .rotateY(bodyY * Mth.PI / 180)
                                .rotateX(90 * Mth.PI / 180)
                                //.getEulerAnglesXYZ(new Vector3f())
                                //.getEulerAnglesZXY(new Vector3f())
                                .getEulerAnglesYXZ(new Vector3f())
                                .mul(180 / Mth.PI);
                        //.rotateY(bodyY * Mth.PI / 180);
                        //.rotateYXZ(90 * Mth.PI / 180, 180 * Mth.PI / 180, 0)
                        //.rotateYXZ(bodyY * Mth.PI / 180, 0, 0);
                        event.setPitch(rot.x);
                        event.setYaw(rot.y);
                        event.setRoll(rot.z);
                    }
                }

            }, () -> {
                event.setPitch(entity.getViewXRot(partialTick));
                event.setYaw(entity.getViewYRot(partialTick));
                event.setRoll(0);
            });
        }
    }
}
