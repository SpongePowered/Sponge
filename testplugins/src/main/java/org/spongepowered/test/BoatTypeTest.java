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
package org.spongepowered.test;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.TreeType;
import org.spongepowered.api.data.type.TreeTypes;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

@Plugin(id = "boat_type_test", name = "Boat Type Test", description = BoatTypeTest.DESCRIPTION, version = "0.0.0")
public class BoatTypeTest implements LoadableModule {

    public static final String DESCRIPTION = "Right click a boat to get the TreeType, run /makeboat <treetype> to make a boat.";

    private final BoatTypeListener listener = new BoatTypeListener();

    @Inject private PluginContainer container;

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .description(Text.of("Gives you a boat of a specific TreeType"))
                        .arguments(GenericArguments.catalogedElement(Text.of("tree"), TreeType.class))
                        .executor((src, args) -> {
                            if (!(src instanceof Player)) {
                                src.sendMessage(Text.of("Only players can run this command"));
                                return CommandResult.empty();
                            }
                            Player player = (Player) src;
                            Boat boat = (Boat) player.getLocation().getExtent().createEntity(EntityTypes.BOAT, player.getLocation().getPosition());
                            boat.offer(Keys.TREE_TYPE, args.<TreeType>getOne("tree").orElse(TreeTypes.OAK));
                            player.getWorld().spawnEntity(boat);
                            return CommandResult.success();
                        })
                        .build(),
                "makeboat");
    }

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class BoatTypeListener {

        @Listener
        public void onInteractEntity(InteractEntityEvent.Secondary.MainHand event, @Getter("getTargetEntity") Boat boat, @First Player player) {
            player.sendMessage(Text.of("This boat is of type: " + boat.get(Keys.TREE_TYPE).orElse(TreeTypes.OAK).getName()));
        }
    }
}
