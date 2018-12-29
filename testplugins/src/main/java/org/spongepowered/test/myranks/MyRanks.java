package org.spongepowered.test.myranks;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.test.myranks.api.Keys;
import org.spongepowered.test.myranks.api.Rank;
import org.spongepowered.test.myranks.api.Ranks;

import java.util.Collection;

@Plugin(id = "myranks", name = "MyRanks", version = "0.0.0", description = "A simple ranks plugin")
public class MyRanks {

    @Inject
    private Logger logger;

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        Sponge.getRegistry().registerModule(Rank.class, new RankRegistryModule());
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        CommandSpec myCommandSpec = CommandSpec.builder()
                .description(Text.of("Rank Command"))
                .executor((src, args) -> {
                    Collection<Rank> ranks = Sponge.getRegistry().getAllOf(Rank.class);
                    Text text = Text.builder("Ranks: ").append(Text.of(ranks)).build();
                    Sponge.getServer().getBroadcastChannel().send(text);

                    Sponge.getServer().getBroadcastChannel().send(Text.of(Ranks.STAFF.getId()));
                    return CommandResult.success();
                })
                .build();

        Sponge.getCommandManager().register(this, myCommandSpec, "ranks");
    }

    @Listener
    public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> event) {
        event.register(Keys.RANK);
    }
}
