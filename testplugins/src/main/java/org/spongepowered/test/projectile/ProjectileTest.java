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
package org.spongepowered.test.projectile;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.Dispenser;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Plugin("projectiletest")
public final class ProjectileTest implements LoadableModule {

    @Inject private PluginContainer plugin;
    private ProjectileTestListener listeners;

    @Override
    public void enable(CommandContext ctx) {
        this.listeners = new ProjectileTestListener();
        Sponge.getEventManager().registerListeners(this.plugin, this.listeners);
    }


    public static class ProjectileTestListener {

        private Queue<EntityType<? extends Projectile>> projectileTypes = new LinkedList<>();

        public ProjectileTestListener() {
            this.projectileTypes.add(EntityTypes.SPECTRAL_ARROW.get());
            this.projectileTypes.add(EntityTypes.ARROW.get());
            this.projectileTypes.add(EntityTypes.EGG.get());
            this.projectileTypes.add(EntityTypes.SMALL_FIREBALL.get());
            this.projectileTypes.add(EntityTypes.FIREWORK_ROCKET.get());
            this.projectileTypes.add(EntityTypes.SNOWBALL.get());
            this.projectileTypes.add(EntityTypes.EXPERIENCE_BOTTLE.get());
            this.projectileTypes.add(EntityTypes.ENDER_PEARL.get());
            this.projectileTypes.add(EntityTypes.FIREBALL.get());
            this.projectileTypes.add(EntityTypes.WITHER_SKULL.get());
            this.projectileTypes.add(EntityTypes.EYE_OF_ENDER.get());
            //             this.projectileTypes.add(EntityTypes.FISHING_BOBBER.get());
            this.projectileTypes.add(EntityTypes.POTION.get());
            this.projectileTypes.add(EntityTypes.LLAMA_SPIT.get());
            this.projectileTypes.add(EntityTypes.DRAGON_FIREBALL.get());
            this.projectileTypes.add(EntityTypes.SHULKER_BULLET.get());
        }

        @Listener
        public void onClickBlock(InteractBlockEvent.Secondary event, @First ServerPlayer player) {
            if (event.getInteractionPoint().isPresent()) {
                final Vector3d interactionPoint = event.getInteractionPoint().get();
                final ServerWorld world = player.getWorld();
                final EntityType<? extends Projectile> nextType = this.projectileTypes.poll();
                this.projectileTypes.offer(nextType);
                final Optional<? extends BlockEntity> blockEntity = world.getBlockEntity(interactionPoint.toInt());
                if (blockEntity.isPresent() && blockEntity.get() instanceof Dispenser) {
                    ((Dispenser) blockEntity.get()).launchProjectile(nextType);
                    event.setCancelled(true);
                } else {
                    player.launchProjectile(nextType);
                }
                player.sendMessage(Component.text(nextType.key().toString()));
            }

        }

    }


}
