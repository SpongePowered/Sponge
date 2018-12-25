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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ChangeEntityEquipmentEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;

@Plugin(id = "changeentityequipmenttest", name = "Entity Equipment Change Test", description = ChangeEntityEquipmentTest.DESCRIPTION, version = "0.0.0")
public class ChangeEntityEquipmentTest {
    public static final String DESCRIPTION = "A plugin for testing ChangeEntityEquipmentEvents.";

    @Nullable
    private TestListener listener = null;

    @Listener
    public void onGameInitialization(GameInitializationEvent event) {
        CommandSpec command = CommandSpec.builder().executor(this::onCommand).build();
        Sponge.getCommandManager().register(this, command, "togglechangeentityequipmenttest");
    }

    private CommandResult onCommand(CommandSource source, CommandContext context) {
        if (this.listener != null) {
            Sponge.getEventManager().unregisterListeners(this.listener);
            this.listener = null;
        } else {
            this.listener = new TestListener();
            Sponge.getEventManager().registerListeners(this, this.listener);
        }
        return CommandResult.success();
    }

    public class TestListener {
        private TestListener() {}

        @Listener
        public void onChangeEntityEquipment(ChangeEntityEquipmentEvent event) {
            Entity entity = event.getTargetEntity();
            String name = entity instanceof Humanoid ? ((Humanoid) entity).getName() : entity.getUniqueId().toString();
            Text text = Text.builder()
                    .append(Text.of("[", name, "]", " ChangeEntityEquipmentEvent is fired")).append(Text.NEW_LINE)
                    .append(Text.of("[", name, "]", " ", event.getCause().toString())).append(Text.NEW_LINE)
                    .append(Text.of("[", name, "]", " ", event.getTransaction().toString())).build();
            Sponge.getServer().getConsole().sendMessage(text);
        }
    }
}
