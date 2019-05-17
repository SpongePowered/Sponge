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
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.tileentity.SmeltEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@Plugin(id = "furnacesmelttest", name = "Furnace Smelt Test", description = "A plugin to test the smelt events fired by a furnace.", version = "0.0.0")
public class FurnaceSmeltTest implements LoadableModule {

    @Inject private PluginContainer container;

    private final FurnaceSmeltListener listener = new FurnaceSmeltListener();

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class FurnaceSmeltListener {

        private ParticleEffect smokingEffect = ParticleEffect.builder().type(ParticleTypes.SMOKE).build();

        @Listener
        public void onSmeltStart(SmeltEvent.Start event) {
            Location<World> loc = event.getTargetTile().getLocation();
            loc.getExtent().playSound(SoundTypes.ENTITY_EXPERIENCE_ORB_PICKUP, loc.getPosition(), 1);
        }

        @Listener
        public void onSmeltConsumeFuel(SmeltEvent.ConsumeFuel event) {
            Location<World> loc = event.getTargetTile().getLocation();
            loc.getExtent().playSound(SoundTypes.ENTITY_SHULKER_TELEPORT, loc.getPosition(), 1);
        }

        @Listener
        public void onSmeltTick(SmeltEvent.Tick event) {
            Location<World> loc = event.getTargetTile().getLocation();
            MutableBoundedValue<Integer> passedBurnTime = event.getTargetTile().getFurnaceData().passedBurnTime();
            if (passedBurnTime.get() >= passedBurnTime.getMaxValue()) {
                loc.getExtent().spawnParticles(this.smokingEffect, loc.getPosition().add(0.5, 1, 0.5));
            } else {
                loc.getExtent().spawnParticles(this.smokingEffect, loc.getPosition().add(0.5, 1, 0.5));
                loc.getExtent().spawnParticles(this.smokingEffect, loc.getPosition().add(0.25, 1, 0.25));
                loc.getExtent().spawnParticles(this.smokingEffect, loc.getPosition().add(0.25, 1, 0.75));
                loc.getExtent().spawnParticles(this.smokingEffect, loc.getPosition().add(0.75, 1, 0.25));
                loc.getExtent().spawnParticles(this.smokingEffect, loc.getPosition().add(0.75, 1, 0.75));
            }
        }

        @Listener
        public void onSmeltInterupt(SmeltEvent.Interrupt event) {
            Location<World> loc = event.getTargetTile().getLocation();
            loc.getExtent().playSound(SoundTypes.BLOCK_STONE_BUTTON_CLICK_OFF, loc.getPosition(), 1);
        }

        @Listener
        public void onSmeltFinish(SmeltEvent.Finish event) {
            Location<World> loc = event.getTargetTile().getLocation();
            loc.getExtent().playSound(SoundTypes.ENTITY_PLAYER_LEVELUP, loc.getPosition(), 1);
        }
    }
}
