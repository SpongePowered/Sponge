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

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.projectile.explosive.fireball.Fireball;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Direction;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

@Plugin(id = AccelerationDataTest.ID, name = AccelerationDataTest.NAME, description = AccelerationDataTest.DESCRIPTION, version = AccelerationDataTest.VERSION)
public class AccelerationDataTest implements LoadableModule {

    public static final String ID = "accelerationdatatest";
    public static final String NAME = "Acceleration Data Test";
    public static final String DESCRIPTION = "Testing fireball acceleration.";
    public static final String VERSION = "0.0.0";

    @Nullable private Task task;
    @Nullable private Fireball fireball;

    @Override
    public void disable(final CommandSource src) {
        if (this.task != null) {
            this.task.cancel();

            // Remove the fireball
            if (this.fireball != null && !this.fireball.isRemoved()) {
                this.fireball.remove();
            }

            Sponge.getServer().getBroadcastChannel().send(Text.of(NAME, " has been cancelled."));
        }
    }

    @Override
    public void enable(final CommandSource src) {
        if (this.task != null) {
            this.task.cancel();
        }

        if (!(src instanceof Player)) {
            return;
        }

        final Player player = (Player) src;

        this.task = Task.builder()
          .name("accelerationtest")
          .interval(1, TimeUnit.SECONDS)
          .execute(() -> {
              // If the fireball is null or removed then create a new one
              if (this.fireball == null || this.fireball.isRemoved()) {
                  this.createAndSpawnFireball(player);
                  return;
              }

              // If the fireball gets out of range then remove it and create a new one
              if (this.fireball != null && player.getPosition().distance(this.fireball.getLocation().getPosition()) > 50) {
                  this.fireball.remove();
                  this.createAndSpawnFireball(player);
                  return;
              }

              this.updateFireball();
          })
          .delay(1, TimeUnit.SECONDS)
          .submit(this);
    }

    private void createAndSpawnFireball(Player player) {
        final Direction direction = player.get(Keys.DIRECTION).orElse(Direction.UP);
        final Vector3i offset = direction.asBlockOffset().mul(8);
        final Vector3i origin = player.getLocation().getBlockPosition().add(offset);

        // Spawn the fireball
        this.fireball = (Fireball) player.getWorld().createEntity(EntityTypes.FIREBALL, origin);

        // Set initial velocity
        this.fireball.offer(Keys.VELOCITY, Direction.UP.asOffset());

        // Spawn fireball
        player.getWorld().spawnEntity(fireball);
    }

    private void updateFireball() {
        if (this.fireball == null) {
            return;
        }

        // Set the acceleration to a random offset
        this.fireball.offer(Keys.ACCELERATION, getRandomOffset());
    }

    private Vector3d getRandomOffset() {
        final double x = new Random().ints(-2, 3).findFirst().orElse(0) * 0.025;
        final double y = new Random().ints(-2, 3).findFirst().orElse(0) * 0.025;
        final double z = new Random().ints(-2, 3).findFirst().orElse(0) * 0.025;
        return new Vector3d(x, y, z);
    }
}
