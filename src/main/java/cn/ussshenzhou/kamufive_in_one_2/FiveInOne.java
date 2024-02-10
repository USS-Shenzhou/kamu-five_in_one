package cn.ussshenzhou.kamufive_in_one_2;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * @author USS_Shenzhou
 */
// The value here should match an entry in the META-INF/mods.toml file
@Mod(FiveInOne.MOD_ID)
public class FiveInOne {

    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "fio";

    public FiveInOne() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    }
}
