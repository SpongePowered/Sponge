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
package org.spongepowered.test.vanish;

import com.google.inject.Inject;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("vanish-test")
public class VanishTest {

    private final PluginContainer plugin;

    @Inject
    public VanishTest(PluginContainer plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        final Parameter.Value<Entity> entityKey = Parameter.entity()
            .key("entity")
            .usage(key -> "any entity")
            .optional()
            .build();
        final Parameter.Value<Boolean> collisions = Parameter.bool()
            .key("collisions")
            .optional()
            .build();
        final Parameter.Value<Boolean> targeting = Parameter.bool()
            .key("targeting")
            .optional()
            .build();
        event.register(
            this.plugin,
            Command.builder()
                .addParameter(entityKey)
                .executor(ctx -> {
                    ctx.all(entityKey).forEach(entity -> {
                        entity.offer(Keys.VANISH, true);
                        ctx.one(collisions)
                            .ifPresent(collision -> entity.offer(Keys.VANISH_IGNORES_COLLISION, collision));
                        ctx.one(targeting)
                            .ifPresent(target -> entity.offer(Keys.VANISH_PREVENTS_TARGETING, target));
                    });
                    return CommandResult.success();
                })
                .build(),
            "vanish"
        );
        event.register(
            this.plugin,
            Command.builder()
                .addParameter(entityKey)
                .executor(ctx -> {
                    ctx.all(entityKey).forEach(entity -> entity.offer(Keys.VANISH, false));
                    return CommandResult.success();
                })
                .build(),
            "unvanish"
        );
    }

}
