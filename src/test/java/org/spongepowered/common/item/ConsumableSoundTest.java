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
package org.spongepowered.common.item;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.VanishState;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.DefaultWorldKeys;

import java.util.ArrayList;
import java.util.List;

public final class ConsumableSoundTest {

    private static final Consumable EATING = Consumable.builder().hasConsumeParticles(false).build();

    private final List<Holder<SoundEvent>> played = new ArrayList<>();

    private Pig consumer() {
        final Level level = (Level) Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get();
        return new Pig(EntityTypes.PIG, level) {
            @Override
            public void playSound(final Holder<SoundEvent> sound, final float volume, final float pitch) {
                ConsumableSoundTest.this.played.add(sound);
            }
        };
    }

    private void consume(final Consumable consumable, final Pig consumer) {
        consumable.emitParticlesAndSounds(consumer.getRandom(), consumer, new ItemStack(Items.APPLE), 0);
    }

    @Test
    public void visibleConsumerPlaysTheSound() {
        this.consume(ConsumableSoundTest.EATING, this.consumer());
        Assertions.assertEquals(List.of(SoundEvents.GENERIC_EAT), this.played);
    }

    @Test
    public void silentConsumablePlaysNothing() {
        this.consume(Consumable.builder().hasConsumeParticles(false).noSound().build(), this.consumer());
        Assertions.assertEquals(List.of(), this.played);
    }

    @Test
    public void vanishedConsumerPlaysNothing() {
        final Pig consumer = this.consumer();
        ((Entity) consumer).offer(Keys.VANISH_STATE, VanishState.vanished());
        this.consume(ConsumableSoundTest.EATING, consumer);
        Assertions.assertEquals(List.of(), this.played);
    }

    @Test
    public void vanishedConsumerThatCreatesSoundsPlaysTheSound() {
        final Pig consumer = this.consumer();
        ((Entity) consumer).offer(Keys.VANISH_STATE, VanishState.vanished().createSounds(true));
        this.consume(ConsumableSoundTest.EATING, consumer);
        Assertions.assertEquals(List.of(SoundEvents.GENERIC_EAT), this.played);
    }
}
