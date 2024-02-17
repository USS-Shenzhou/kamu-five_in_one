package cn.ussshenzhou.kamufive_in_one_2.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;

/**
 * @author USS_Shenzhou
 */
public class FioPlayerClient extends LocalPlayer {

    public FioPlayerClient(Minecraft pMinecraft, ClientLevel pClientLevel, ClientPacketListener pConnection, StatsCounter pStats, ClientRecipeBook pRecipeBook, boolean pWasShiftKeyDown, boolean pWasSprinting) {
        super(pMinecraft, pClientLevel, pConnection, pStats, pRecipeBook, pWasShiftKeyDown, pWasSprinting);
    }

    public static FioPlayerClient create() {
        var local = Minecraft.getInstance().player;
        if (local==null){
            throw new IllegalStateException();
        }
        var fioPlayerC = new FioPlayerClient(Minecraft.getInstance(), local.clientLevel, local.connection, local.getStats(), local.getRecipeBook(), false, false);
        fioPlayerC.restoreFrom(local);
        fioPlayerC.updateSyncFields(local);
        return fioPlayerC;
    }
}
