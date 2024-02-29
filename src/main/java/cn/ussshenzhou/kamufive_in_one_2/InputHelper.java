package cn.ussshenzhou.kamufive_in_one_2;

import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;

import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author USS_Shenzhou
 */
public class InputHelper {

    private static final Set<Integer> SPECIAL_KEY = Sets.newHashSet(
            GLFW_KEY_ESCAPE,
            GLFW_KEY_F1,
            GLFW_KEY_F2,
            GLFW_KEY_F3,
            GLFW_KEY_F4,
            GLFW_KEY_F5,
            GLFW_KEY_F6,
            GLFW_KEY_F7,
            GLFW_KEY_F8,
            GLFW_KEY_F9,
            GLFW_KEY_F10,
            GLFW_KEY_F11,
            GLFW_KEY_F12,
            GLFW_KEY_SLASH
    );

    public static boolean ignore(int key) {
        return SPECIAL_KEY.contains(key);
    }

    public static boolean inGame() {
        return FioManager.Client.mainPlayer != null
                && FioManager.Client.part != null
                && Minecraft.getInstance().screen == null
                && Minecraft.getInstance().player != null;
    }

}
