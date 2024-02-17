package cn.ussshenzhou.kamufive_in_one_2.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

/**
 * @author USS_Shenzhou
 */
public class FioPlayerClient extends AbstractClientPlayer {

    public FioPlayerClient(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
    }
}
