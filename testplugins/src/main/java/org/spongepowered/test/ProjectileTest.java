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
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Test Bow causes and events.
 */
@Plugin(id = "projectiletest", name = "Projectile Test", description = "A plugin to test projectiles")
public class ProjectileTest {

    private static final Text ENABLE = Text.of("enable");
    private static final Text UNKNOWN = Text.of("Unknown");

    private boolean disableLogging = true;

    private static <T> Stream<T> streamOpt(Optional<T> opt) {
        return opt.map(Stream::of).orElse(Stream.empty());
    }

    @Listener
    public void onGameStartingServer(GameStartingServerEvent event) {
        Sponge.getCommandManager().register(
                this,
                CommandSpec.builder()
                        .arguments(GenericArguments.bool(ENABLE))
                        .description(Text.of("Enable logging of projectiles"))
                        .executor((src, args) -> {
                            this.disableLogging = !args.<Boolean>getOne(ENABLE).get();
                            return CommandResult.success();
                        })
                        .build(),
                "logProjectiles"
        );
    }

    @Listener
    public void onUseItemStack(UseItemStackEvent event) {
        if (this.disableLogging) {
            return;
        }
        this.title("UseItemStack");
        this.broadcast("Cause", event.getCause());
        this.broadcast("Stack", event.getItemStackInUse());
    }

    @Listener
    public void onLaunchProjectile(LaunchProjectileEvent event) {
        if (this.disableLogging) {
            return;
        }
        this.title("LaunchProjectile");
        this.broadcast("     Cause", event.getCause());
        this.broadcast("Projectile", event.getTargetEntity());
    }

    @Listener
    public void onEntityCreate(ConstructEntityEvent.Post event) {
        if (this.disableLogging) {
            return;
        }
        if (!(event.getTargetEntity() instanceof Projectile)) {
            return;
        }
        Projectile proj = (Projectile) event.getTargetEntity();
        Text creatorText = proj.getCreator().map(creatorUUID ->
                Sponge.getServer().getWorlds().stream()
                        .map(w -> w.getEntity(creatorUUID))
                        .flatMap(ProjectileTest::streamOpt)
                        .findFirst()
                        .<Text>map(Text::of)
                        .orElse(UNKNOWN)
        ).orElse(UNKNOWN);
        this.title("ConstructEntity");
        this.broadcast("Shooter", Text.of(proj.getShooter()));
        this.broadcast("Creator", creatorText);
    }

    private void title(String title) {
        Sponge.getServer().getBroadcastChannel().send(
                Text.of(TextColors.RED, title)
        );
    }

    private void broadcast(String label, Object logged) {
        Sponge.getServer().getBroadcastChannel().send(
                Text.of(TextColors.GOLD, label + ": ", TextColors.AQUA, logged)
        );
    }
}
