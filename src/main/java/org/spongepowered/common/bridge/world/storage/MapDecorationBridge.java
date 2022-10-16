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
package org.spongepowered.common.bridge.world.storage;

import org.spongepowered.api.map.MapInfo;

public interface MapDecorationBridge {

    /**
     * If this MapDecoration should be saved to disk,
     * not necessary if it would be recalculated.
     * @param persistent if should be saved to disk
     */
    void bridge$setPersistent(boolean persistent);

    /**
     * If this MapDecoration will be saved to disk
     * @return if this MapDecoration will be saved to disk
     */
    boolean bridge$isPersistent();

    /**
     * Sets the key used in the decoration map of a MapData
     * This can be compared using == since they are the same String
     * @param key String to set as key
     */
    void bridge$setKey(String key);

    /**
     * Gets the key used in the decoration map of a MapData
     * This can be compared using == since they are the same String
     * @return String key
     */
    String bridge$getKey();

    /**
     * Notifies this {@link org.spongepowered.api.map.decoration.MapDecoration MapDecoration}
     * that it has been added, from the given {@link MapInfo}, and therefore,
     * when the MapDecoration changes, the given MapData needs to be re-saved.
     * @param mapInfo that needs to be marked as dirty if this changes.
     *
     * @see #bridge$markAllDirty()
     */
    void notifyAddedToMap(MapInfo mapInfo);

    /**
     * Notifies this {@link org.spongepowered.api.map.decoration.MapDecoration MapDecoration}
     * that it has been removed, from the given {@link MapInfo}, and therefore,
     * when the MapDecoration changes, the given MapData does not need to be need to be re-saved.
     * @param mapInfo that no longer needs to be marked as dirty if this changes.
     *
     * @see #bridge$markAllDirty()
     */
    void notifyRemovedFromMap(MapInfo mapInfo);

    /**
     * If {@link #bridge$isPersistent()}, marks every {@link net.minecraft.world.storage.MapData MapData} that
     * this {@link org.spongepowered.api.map.decoration.MapDecoration MapDecoration}
     * is attached to, that this decoration has changed, and therefore all of those MapDatas need to be saved in order to preserve
     * the changes made to this Decoration.
     */
    void bridge$markAllDirty();
}
