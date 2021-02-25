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
package org.spongepowered.test.scaledhealth;

import com.google.inject.Inject;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.Optional;

@Plugin("scaledhealthtest")
public class ScaledHealthTest {

    private final PluginContainer pluginContainer;

    @Inject
    public ScaledHealthTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Listener
    public void registerCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(this.pluginContainer,
                Command.builder()
                        .child(this.getShowHealth(), "show")
                        .child(this.getSetHealthScale(), "scale", "setScale")
                        .child(this.getReSetHealthScale(), "resetScale")
                        .child(this.setHealthCommand(), "setHealth", "set")
                        .child(this.getSetMaxHealth(), "setMax", "max")
                        .setExecutor(this::showHealthInfo)
                        .build(),
                "health");
    }

    private Command.Parameterized getSetMaxHealth() {
        final Parameter.Value<Double> health = Parameter.doubleNumber().setKey("health").build();
        return Command.builder()
                .parameter(health)
                .setShortDescription(Component.text("Sets your maximum health", NamedTextColor.AQUA))
                .setExecutionRequirements(c -> c.getSubject() instanceof ServerPlayer)
                .setExecutor(ctx -> {
                    final Double newHealth = ctx.requireOne(health);
                    ((ServerPlayer) ctx.getSubject()).offer(Keys.MAX_HEALTH, newHealth);
                    ctx.sendMessage(Identity.nil(), Component.text("Max Health set to: ", NamedTextColor.DARK_AQUA).append(Component.text(newHealth, NamedTextColor.RED)));
                    return CommandResult.success();
                })
                .build();

    }

    private Command.Parameterized setHealthCommand() {
        final Parameter.Value<Double> health = Parameter.doubleNumber().setKey("health").build();
        return Command.builder()
                .parameter(health)
                .setShortDescription(Component.text("Sets your health", NamedTextColor.AQUA))
                .setExecutionRequirements(c -> c.getSubject() instanceof ServerPlayer)
                .setExecutor(ctx -> {
                    final Double newHealth = ctx.requireOne(health);
                    ((ServerPlayer) ctx.getSubject()).offer(Keys.HEALTH, newHealth);
                    ctx.sendMessage(Identity.nil(), Component.text("Health set to: ", NamedTextColor.DARK_AQUA).append(Component.text(newHealth, NamedTextColor.RED)));
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized getSetHealthScale() {
        final Parameter.Value<Double> health = Parameter.doubleNumber().setKey("health").build();
        return Command.builder()
                .parameter(health)
                .setShortDescription(Component.text("Sets your health scale", NamedTextColor.AQUA))
                .setExecutionRequirements(c -> c.getSubject() instanceof ServerPlayer)
                .setExecutor(ctx -> {
                    final Double newHealth = ctx.requireOne(health);
                    if (((ServerPlayer) ctx.getSubject()).offer(Keys.HEALTH_SCALE, newHealth).isSuccessful()) {
                        ctx.sendMessage(Identity.nil(), Component.text("Health scaled to: ", NamedTextColor.DARK_AQUA).append(Component.text(newHealth, NamedTextColor.RED)));
                    }
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized getReSetHealthScale() {
        return Command.builder()
                .setShortDescription(Component.text("Sets your health scale", NamedTextColor.AQUA))
                .setExecutionRequirements(c -> c.getSubject() instanceof ServerPlayer)
                .setExecutor(ctx -> {
                    ((ServerPlayer) ctx.getSubject()).remove(Keys.HEALTH_SCALE);
                    ctx.sendMessage(Identity.nil(), Component.text("Health not scaled", NamedTextColor.DARK_AQUA));
                    return CommandResult.success();
                })
                .build();
    }

    private Command.Parameterized getShowHealth() {
        return Command.builder()
                .setShortDescription(Component.text("Shows your health", NamedTextColor.AQUA))
                .setExecutionRequirements(c -> c.getSubject() instanceof ServerPlayer)
                .setExecutor(this::showHealthInfo)
                .build();
    }

    private CommandResult showHealthInfo(org.spongepowered.api.command.parameter.CommandContext ctx) {
        final double health = ((ServerPlayer) ctx.getSubject()).get(Keys.HEALTH).orElse(0D);
        final double maxHealth = ((ServerPlayer) ctx.getSubject()).get(Keys.MAX_HEALTH).orElse(0D);
        final Optional<Double> scaling = ((ServerPlayer) ctx.getSubject()).get(Keys.HEALTH_SCALE);
        ctx.sendMessage(Identity.nil(), Component.text("Health: ", NamedTextColor.DARK_AQUA)
                            .append(Component.text(health, NamedTextColor.RED))
                            .append(Component.text("/"))
                            .append(Component.text(maxHealth, NamedTextColor.RED)));
        if (scaling.isPresent()) {
            ctx.sendMessage(Identity.nil(), Component.text("Scaling to: ", NamedTextColor.DARK_AQUA)
                    .append(Component.text(scaling.get(), NamedTextColor.RED)));
        } else {
            ctx.sendMessage(Identity.nil(), Component.text("Scaling to max health", NamedTextColor.DARK_AQUA));
        }
        return CommandResult.success();
    }


}
