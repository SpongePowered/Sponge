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
package org.spongepowered.common.event.tracking.context;

import static com.google.common.base.Preconditions.checkNotNull;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.server.ServerWorld;
import java.util.Random;

public class ItemDropData {

    public static Builder item(ItemStack stack) {
        return new Builder(stack);
    }

    final ItemStack stack;
    final Vector3d position;
    final double pitch;
    final double yaw;
    final Vector3d motion;

    ItemDropData(Builder builder) {
        this.stack = builder.stack;
        this.position = builder.position;
        this.pitch = builder.pitch;
        this.yaw = builder.yaw;
        this.motion = builder.motion;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public Vector3d getPosition() {
        return this.position;
    }

    public double getPitch() {
        return this.pitch;
    }

    public double getYaw() {
        return this.yaw;
    }

    public Vector3d getMotion() {
        return this.motion;
    }

    public ItemEntity create(ServerWorld worldServer) {
        final ItemEntity entityItem = new ItemEntity(worldServer, this.position.getX(), this.position.getY(), this.position.getZ(), this.stack);
        if (this.motion != Vector3d.ZERO) {
            entityItem.motionX = this.motion.getX();
            entityItem.motionY = this.motion.getY();
            entityItem.motionZ = this.motion.getZ();
        }
        return entityItem;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.stack, this.position, this.pitch, this.yaw, this.motion);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ItemDropData other = (ItemDropData) obj;
        return Objects.equal(this.stack, other.stack)
               && Objects.equal(this.position, other.position)
               && Objects.equal(this.pitch, other.pitch)
               && Objects.equal(this.yaw, other.yaw)
               && Objects.equal(this.motion, other.motion);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("stack", this.stack)
                .add("position", this.position)
                .add("pitch", this.pitch)
                .add("yaw", this.yaw)
                .add("motion", this.motion)
                .toString();
    }

    public static class Builder {

        ItemStack stack;
        Vector3d position;
        double pitch;
        double yaw;
        Vector3d motion;

        Builder() {
        }

        Builder(ItemStack itemStack) {
            this.stack = itemStack;
        }

        public Builder position(Vector3d position) {
            this.position = position;
            return this;
        }

        public Builder pitch(double pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder yaw(double yaw) {
            this.yaw = yaw;
            return this;
        }

        public Builder motion(Vector3d motion) {
            this.motion = motion;
            return this;
        }

        public ItemDropData build() {
            if (this.motion == null) {
                this.motion = Vector3d.ZERO;
            }
            checkNotNull(this.stack, "ItemStack cannot be null!");
            checkNotNull(this.position, "Position cannot be null!");
            return new ItemDropData(this);
        }

    }

    public static final class Player extends ItemDropData {

        public static org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder player(PlayerEntity player) {
            return new org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder(player);
        }

        private final boolean trace;
        private final String playerName;
        private final boolean dropAround;
        private final Random random;

        Player(org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder builder) {
            super(builder);
            this.trace = builder.trace;
            this.playerName = builder.playerName;
            this.dropAround = builder.dropAround;
            this.random = builder.random;
        }

        public boolean isTrace() {
            return this.trace;
        }

        public String getPlayerName() {
            return this.playerName;
        }

        public boolean isDropAround() {
            return this.dropAround;
        }

        public Random getRandom() {
            return this.random;
        }


        @Override
        public ItemEntity create(ServerWorld worldServer) {
            final ItemEntity entityItem = super.create(worldServer);
            entityItem.setPickupDelay(40);
            if (this.trace) {
                entityItem.setThrower(this.playerName);
            }
            return entityItem;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            Player player = (Player) o;
            return this.trace == player.trace &&
                   this.dropAround == player.dropAround &&
                   Objects.equal(this.playerName, player.playerName) &&
                   Objects.equal(this.random, player.random);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(super.hashCode(), this.trace, this.playerName, this.dropAround, this.random);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("trace", this.trace)
                    .add("playerName", this.playerName)
                    .add("dropAround", this.dropAround)
                    .add("random", this.random)
                    .add("stack", this.stack)
                    .add("position", this.position)
                    .add("pitch", this.pitch)
                    .add("yaw", this.yaw)
                    .add("motion", this.motion)
                    .toString();
        }

        public static final class Builder extends ItemDropData.Builder {

            boolean trace;
            String playerName;
            boolean dropAround;
            Random random;

            Builder(PlayerEntity player) {
                this.playerName = player.getName();
                this.random = ((org.spongepowered.api.entity.Entity) player).getRandom();
            }

            public org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder stack(ItemStack stack) {
                this.stack = stack;
                return this;
            }

            public org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder trace(boolean trace) {
                this.trace = trace;
                return this;
            }

            public org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder dropAround(boolean dropAround) {
                this.dropAround = dropAround;
                return this;
            }

            @Override
            public org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder position(Vector3d position) {
                super.position(position);
                return this;
            }

            @Override
            public org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder pitch(double pitch) {
                super.pitch(pitch);
                return this;
            }

            @Override
            public org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder yaw(double yaw) {
                super.yaw(yaw);
                return this;
            }

            @Override
            public org.spongepowered.common.event.tracking.context.ItemDropData.Player.Builder motion(Vector3d motion) {
                super.motion(motion);
                return this;
            }

            @Override
            public ItemDropData.Player build() {
                return new Player(this);
            }


        }
    }
}
