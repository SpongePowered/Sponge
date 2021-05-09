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
package org.spongepowered.test.collide;

import com.google.inject.Inject;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.CollideBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

@Plugin("collidetest")
public class CollideTest implements LoadableModule {

    private final PluginContainer pluginContainer;

    @Inject
    public CollideTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
        Sponge.eventManager().registerListeners(this.pluginContainer, new CollideListener());
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.pluginContainer, new CollideListener());
    }

    public static final class CollideListener {

        @Listener
        public void onMoveCollide(CollideBlockEvent.Move event) {
            // TODO cancel is not working for players & projectiles yet - and maybe more?
            if (event.targetBlock().type().isAnyOf(BlockTypes.EMERALD_BLOCK)) {
                 event.setCancelled(true);
            }
        }

        @Listener
        public void onFallOnBlock(CollideBlockEvent.Fall event, @First ServerPlayer player) {
            if (event.targetBlock().type().isAnyOf(BlockTypes.DIAMOND_BLOCK)) {
                player.transform(Keys.POTION_EFFECTS, e -> {
                    e.add(PotionEffect.of(PotionEffectTypes.JUMP_BOOST, 4, 20));
                    player.sendActionBar(Component.text("JUMP!"));
                    return e;
                });
            } else if (event.targetBlock().type().isAnyOf(BlockTypes.FARMLAND)) {
                player.sendActionBar(Component.text("Farmland saved!"));
                event.setCancelled(true);
            }
        }

        @Listener
        public void onStepOnBlock(CollideBlockEvent.StepOn event, @First ServerPlayer player) {
            if (event.targetBlock().type().isAnyOf(BlockTypes.REDSTONE_BLOCK)) {
                player.transform(Keys.POTION_EFFECTS, e -> {
                    e.add(PotionEffect.of(PotionEffectTypes.SPEED, 4, 20));
                    player.sendActionBar(Component.text("RUN!"));
                    return e;
                });
            } else if (event.targetBlock().type().isAnyOf(BlockTypes.TURTLE_EGG)) {
                player.sendActionBar(Component.text("Eggwalker!"));
                event.setCancelled(true);
            }

        }

        @Listener
        public void onInsideBlock(CollideBlockEvent.Inside event, @First ServerPlayer player) {
            if (event.targetBlock().type().isAnyOf(BlockTypes.TALL_GRASS)) {
                player.transform(Keys.POTION_EFFECTS, e -> {
                    player.sendActionBar(Component.text("Invisibility!"));
                    e.add(PotionEffect.of(PotionEffectTypes.INVISIBILITY, 1, 20));
                    return e;
                });
            } else if (event.targetBlock().type().isAnyOf(BlockTypes.FIRE)) {
                player.sendActionBar(Component.text("Cool!"));
                event.setCancelled(true);
            }
        }


        @Listener
        public void onImpact(CollideBlockEvent.Impact event) {
            if (event.targetBlock().type().isAnyOf(BlockTypes.EMERALD_BLOCK)) {
                event.setCancelled(true);
            }
        }


    }

}
