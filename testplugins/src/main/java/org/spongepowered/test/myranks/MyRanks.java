/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
