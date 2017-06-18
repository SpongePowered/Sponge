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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

@Plugin(id = "damageabledatatest", name = "Damageable Data Test", description = "A plugin to test damageable data.")
public class DamageableDataTest {

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                Command.builder()
                        .parameter(Parameter.entityOrSource().onlyOne().setKey("target").build())
                        .setExecutor((cause, src, args) -> {
                            final Entity entity = args.<Entity>getOne("target").get();

                            final Optional<EntitySnapshot> optionalAttacker = entity.get(Keys.LAST_ATTACKER).get();
                            final Optional<Double> lastDamage = entity.get(Keys.LAST_DAMAGE).get();
                            if (optionalAttacker.isPresent() && lastDamage.isPresent()) {
                                final EntitySnapshot attacker = optionalAttacker.get();
                                src.sendMessage(Text.of(attacker.get(Keys.DISPLAY_NAME).orElse(Text.of(attacker.getUniqueId())) + " dealt "
                                        + lastDamage.get() + " damage to " + entity.get(Keys.DISPLAY_NAME).orElse(Text.of(entity.getUniqueId()))));
                            } else {
                                src.sendMessage(Text.of(TextColors.RED, "This target does not have a last attacker."));
                            }

                            return CommandResult.success();
                        })
                        .build(),
                "lastattackertest");
    }

}
