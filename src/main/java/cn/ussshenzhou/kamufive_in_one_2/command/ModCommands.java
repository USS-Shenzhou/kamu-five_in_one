package cn.ussshenzhou.kamufive_in_one_2.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author USS_Shenzhou
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommands {

    @SubscribeEvent
    public static void regCommand(RegisterCommandsEvent event) {
        SelectPartCommand.selectPart(event.getDispatcher());
    }
}
