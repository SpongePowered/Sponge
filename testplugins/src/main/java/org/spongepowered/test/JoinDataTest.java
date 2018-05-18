package org.spongepowered.test;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

@Plugin(id = "join_data_test", name = "Join Data Test", description = "Run '/getjoindata [player]' to display the JoinData for a player, either online or offline.")
public class JoinDataTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder()
                .description(Text.of("Gets the JoinData for the specified player (allowed to be offline)"))
                .arguments(GenericArguments.userOrSource(Text.of("user")))
                .executor((src, args) -> {
                    User user = args.<User>getOne(Text.of("user")).get();

                    JoinData data = user.get(JoinData.class).get();
                    src.sendMessage(Text.of(String.format("Player '%s' first played on '%s', and last played on '%s'", user.getName(), data.firstPlayed().get(), data.lastPlayed().get())));
                    return CommandResult.success();
                })
                .build(),
                "getjoindata");
    }

}
