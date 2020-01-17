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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.ParrotType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "parrotdatatest", name = "Parrot Data Test", description = "A plugin to test parrot data.", version = "0.0.0")
public class ParrotDataTest {

    private ParrotType parrotVariant;

    @Inject private PluginContainer container;

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Parameter.Value<ParrotType> paramParrot = Parameter.catalogedElement(ParrotType.class).setKey("parrot variant").build();
        Sponge.getCommandManager().register(this.container,
                Command.builder()
                        .parameters(paramParrot)
                        .setExecutor((ctx) -> {
                            this.parrotVariant = ctx.getOne(paramParrot).get();

                            ctx.getMessageReceiver().sendMessage(Text.of(TextColors.DARK_GREEN, "Click a parrot to change their variant to: ",
                                    TextColors.GRAY, this.parrotVariant.getKey()));

                            return CommandResult.success();
                        })
                        .build(),
                "parrottest");
    }

    @Listener
    public void onEntityInteract(InteractEntityEvent event, @Root Player player) {
        final Entity entity = event.getEntity();
        if (entity.getType() == EntityTypes.PARROT.get() && this.parrotVariant != null) {
            entity.offer(Keys.PARROT_TYPE, this.parrotVariant);
            player.sendMessage(Text.of(TextColors.GOLD, "The selected parrot has been turned to the variant: ",
                    TextColors.GRAY, this.parrotVariant.getKey()));
            this.parrotVariant = null;
        }
    }

}
