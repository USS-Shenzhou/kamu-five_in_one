package cn.ussshenzhou.kamufive_in_one_2.command;

import cn.ussshenzhou.kamufive_in_one_2.FioManager;
import cn.ussshenzhou.kamufive_in_one_2.Part;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * @author USS_Shenzhou
 */
public class SelectPartCommand {

    public static void selectPart(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fio")
                .then(Commands
                        .literal("head")
                        .executes(commandContext -> setPart(commandContext, Part.HEAD))
                )
                .then(Commands
                        .literal("l_arm")
                        .executes(commandContext -> setPart(commandContext, Part.LEFT_ARM))
                )
                .then(Commands
                        .literal("r_arm")
                        .executes(commandContext -> setPart(commandContext, Part.RIGHT_ARM))
                )
                .then(Commands
                        .literal("l_foot")
                        .executes(commandContext -> setPart(commandContext, Part.LEFT_FOOT))
                )
                .then(Commands
                        .literal("r_foot")
                        .executes(commandContext -> setPart(commandContext, Part.RIGHT_FOOT))
                )
        );
    }

    private static int setPart(CommandContext<CommandSourceStack> commandContext, Part part) {
        FioManager.getInstanceServer().selectPart(commandContext.getSource(), part);
        return Command.SINGLE_SUCCESS;
    }
}
