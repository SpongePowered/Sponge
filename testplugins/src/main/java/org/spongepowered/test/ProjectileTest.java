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
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.item.inventory.UseItemStackEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Test Bow causes and events.
 */
@Plugin(id = "projectiletest", name = "Projectile Test", version = "0.0.0", description = "A plugin to test projectiles")
public class ProjectileTest implements LoadableModule {

    private static final Text UNKNOWN = Text.of("Unknown");

    @Inject private PluginContainer container;

    private final ProjListener listener = new ProjListener();

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class ProjListener {

        @Exclude(UseItemStackEvent.Tick.class)
        @Listener
        public void onUseItemStack(UseItemStackEvent event) {
            title(event.getClass().getSimpleName());
            broadcast("Cause", event.getCause());
            broadcast("Stack", event.getItemStackInUse());
        }

        @Listener
        public void onLaunchProjectile(LaunchProjectileEvent event) {
            title("LaunchProjectile");
            broadcast("     Cause", event.getCause());
            broadcast("Projectile", event.getTargetEntity());
        }

        @Listener
        public void onEntityCreate(ConstructEntityEvent.Post event) {
            if (!(event.getTargetEntity() instanceof Projectile)) {
                return;
            }
            Projectile proj = (Projectile) event.getTargetEntity();
            Text creatorText = proj.getCreator().map(creatorUUID ->
                    Sponge.getServer().getWorlds().stream()
                            .map(w -> w.getEntity(creatorUUID))
                            .flatMap(this::streamOpt)
                            .findFirst()
                            .<Text>map(Text::of)
                            .orElse(UNKNOWN)
            ).orElse(UNKNOWN);
            title("ConstructEntity");
            broadcast("Shooter", Text.of(proj.getShooter()));
            broadcast("Creator", creatorText);
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

        /**
         * allow flat mapping over optionals, to single stream, or empty.
         */
        private <T> Stream<T> streamOpt(Optional<T> opt) {
            return opt.map(Stream::of).orElse(Stream.empty());
        }
    }
}
