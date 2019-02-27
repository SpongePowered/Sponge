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
package org.spongepowered.common.interfaces.entity;

import co.aikar.timings.Timing;
import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.dismount.DismountType;
import org.spongepowered.api.event.entity.IgniteEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.phase.tick.EntityTickContext;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinTrackable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public interface IMixinEntity extends org.spongepowered.api.entity.Entity, IMixinTrackable {

    boolean isInConstructPhase();

    void firePostConstructEvents();

    /**
     * Overridden method for Players to determine whether this entity is immune to fire
     * such that {@link IgniteEntityEvent}s are not needed to be thrown as they cannot
     * take fire damage, nor do they light on fire.
     *
     * @return True if this entity is immune to fire.
     */
    boolean isImmuneToFireForIgniteEvent();

    /**
     * Gets whether this entity has been added to a World's tracked entity lists
     * @return True if this entity is being tracked in a world's chunk lists.
     */
    boolean isTrackedInWorld();

    /**
     * Sets an entity to be tracked or untracked. Specifically used in
     * {@link net.minecraft.world.World#onEntityAdded(Entity)} and
     * {@link net.minecraft.world.World#onEntityRemoved(Entity)}.
     *
     * @param tracked Tracked
     */
    void setTrackedInWorld(boolean tracked);

    boolean isTeleporting();

    void setIsTeleporting(boolean teleporting);

    Entity getTeleportVehicle();

    void setTeleportVehicle(Entity entity);

    boolean dismountRidingEntity(DismountType type);

    boolean removePassengers(DismountType type);

    Optional<User> getTrackedPlayer(String nbtKey);

    Optional<User> getCreatorUser();

    Optional<User> getNotifierUser();

    void trackEntityUniqueId(String nbtKey, @Nullable UUID uuid);

    NBTTagCompound getEntityData();

    default NBTTagCompound getSpongeData() {
        final NBTTagCompound data = this.getEntityData();
        if (!data.contains(NbtDataUtil.SPONGE_DATA)) {
            data.put(NbtDataUtil.SPONGE_DATA, new NBTTagCompound());
        }
        return data.getCompound(NbtDataUtil.SPONGE_DATA);
    }

    default void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {
    }

    /**
     * Read extra data (SpongeData) from the entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    void readFromNbt(NBTTagCompound compound);

    /**
     * Write extra data (SpongeData) to the entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    void writeToNbt(NBTTagCompound compound);

    void setImplVelocity(Vector3d velocity);

    boolean isVanished();

    void setVanished(boolean vanished);

    boolean ignoresCollision();

    void setIgnoresCollision(boolean prevents);

    boolean isUntargetable();

    void setUntargetable(boolean untargetable);

    @Nullable Text getDisplayNameText();

    void setDisplayName(@Nullable Text displayName);

    void setCurrentCollidingBlock(BlockState state);

    BlockState getCurrentCollidingBlock();

    BlockPos getLastCollidedBlockPos();

    boolean isVanilla();

    void setDestructCause(Cause cause);

    void setLocationAndAngles(Location location);

    void setLocationAndAngles(Transform transform);

    void createForgeCapabilities();

    // Timings
    Timing getTimingsHandler();

    default void onJoinWorld() {

    }

    @Nullable IMixinChunk getActiveChunk();

    void setActiveChunk(IMixinChunk chunk);

    boolean shouldTick();

    default void clearWrappedCaptureList() {

    }

    /**
     * @author gabizou - July 26th, 2018
     * @reason Due to vanilla logic, a block is removed *after* the held item is set,
     * so, when the block event gets cancelled, we don't have a chance to cancel the
     * enderman pickup. Specifically applies to Enderman so far, may have other uses
     * in the future.
     *
     * @param phaseContext The context, for whatever reason in the future
     */
    default void onCancelledBlockChange(EntityTickContext phaseContext) {

    }

    void onVanish(EntityTrackerEntry entry);

    void onUnVanish();
}
