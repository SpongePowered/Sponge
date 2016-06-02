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
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.util.RelativePositions;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.EnumSet;

/**
 * THIS IS EXPLICITLY PROVIDED ONLY FOR A BACKWARDS COMPATIBILTY SUPPORT FOR
 * THE REMAINDER OF API 4.X, SINCE A SMALL CHANGE IN THE METHOD SIGNATURES
 * WERE A BREAKING CHANGE NECESSARY FOR A PORTAL/TELEPORTATION API IMPLEMENTATION,
 * THESE METHODS ARE PREFIX IMPLEMENTED ONLY FOR OLDER PLUGINS. THESE METHODS
 * AND INTERFACE SHOULD NEVER BE USED IN THE IMPLEMENTATION OTHERWISE.
 */
public interface IMixinEntityBackwardsCompatible {

    void setLocation(Location<World> location);

    void setLocationAndRotation(Location<World> location, Vector3d rotation);

    void setLocationAndRotation(Location<World> location, Vector3d rotation, EnumSet<RelativePositions> relativePositions);

    void setTransform(Transform<World> transform);

}
