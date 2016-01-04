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

import com.flowpowered.math.vector.Vector3d;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.living.player.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMixinEntity {

    boolean isTeleporting();

    void setIsTeleporting(boolean teleporting);

    Entity getTeleportVehicle();

    void setTeleportVehicle(Entity entity);

    byte getActivationType();

    long getActivatedTick();

    boolean getDefaultActivationState();

    Optional<User> getTrackedPlayer(String nbtKey);

    void trackEntityUniqueId(String nbtKey, UUID uuid);

    void setActivatedTick(long tick);

    void inactiveTick();

    NBTTagCompound getSpongeData();

    default void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) { }

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
    
    Vector3d getVelocity();
    
    void setVelocity(Vector3d velocity);

    boolean isReallyREALLYInvisible();

    void setReallyInvisible(boolean invisible);

    boolean ignoresCollision();

    void setIgnoresCollision(boolean prevents);

    boolean isUntargetable();

    void setUntargetable(boolean untargetable);

}
