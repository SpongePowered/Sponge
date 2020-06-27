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
import org.spongepowered.api.data.type.ParrotVariant;
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
public class ParrotDataTest implements LoadableModule {

    @Inject private PluginContainer container;

    private final ParrotDataEventListener listener = new ParrotDataEventListener();

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .arguments(GenericArguments.onlyOne(GenericArguments.catalogedElement(Text.of("parrot variant"), ParrotVariant.class)))
                        .executor((src, args) -> {
                            this.listener.setParrotVariant(args.<ParrotVariant>getOne("parrot variant").get());

                            src.sendMessage(Text.of(TextColors.DARK_GREEN, "Click a parrot to change their variant to: ",
                                    TextColors.GRAY, this.listener.getParrotVariant().getName()));

                            return CommandResult.success();
                        })
                        .build(),
                "parrottest");
    }

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class ParrotDataEventListener {

        private ParrotVariant parrotVariant;

        @Listener
        public void onEntityInteract(InteractEntityEvent event, @Root Player player) {
            final Entity entity = event.getTargetEntity();
            if (entity.getType().equals(EntityTypes.PARROT) && this.parrotVariant != null) {
                entity.offer(Keys.PARROT_VARIANT, this.parrotVariant);
                player.sendMessage(Text.of(TextColors.GOLD, "The selected parrot has been turned to the variant: ",
                        TextColors.GRAY, this.parrotVariant.getName()));
                this.parrotVariant = null;
            }
        }

        public ParrotVariant getParrotVariant() {
            return this.parrotVariant;
        }

        public void setParrotVariant(ParrotVariant parrotVariant) {
            this.parrotVariant = parrotVariant;
        }
    }

}
