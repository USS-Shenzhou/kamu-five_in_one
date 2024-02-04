package cn.ussshenzhou.kamufive_in_one.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * @author USS_Shenzhou
 */
public class FIOPlayer extends AbstractClientPlayer {

    public FIOPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }
}
