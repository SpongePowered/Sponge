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
package org.spongepowered.common.mixin.core.world.storage;

import net.minecraft.world.storage.MapData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mixin(net.minecraft.world.storage.MapData.class)
public abstract class MapDataBridgeMixin implements MapDataBridge {
    @Shadow public byte dimension;
    private static Field mapDataBridgeMixin$dimensionId;
    private static Method getTypeMethod;

    @Shadow public abstract void updateMapData(int x, int y);

    static {
        try {
            if (SpongeImplHooks.isDeobfuscatedEnvironment()) {
                mapDataBridgeMixin$dimensionId = MapData.class.getDeclaredField("dimension");
            } else {
                mapDataBridgeMixin$dimensionId = MapData.class.getDeclaredField("field_76200_c");
            }
            Class<?> clazz = mapDataBridgeMixin$dimensionId.getType();
            if (clazz.isInstance(Number.class)) {
                SpongeImpl.getLogger().fatal("Field dimensionId in MapData was not an instance of Number. SpongeForge patches to int and normally it is a byte. Actual type was " + clazz.getTypeName());
            }
            // See java's Number class. all primitive number types are able to
            // be converted to from any number via .xxxValue()
            // its not the best solution, but due to primitive types you cant use
            // Class.cast() since it auto-boxes into the wrapper class, which can't be cast.
            String methodName = clazz.getName() + "Value";
            // Integer is what we currently use until we meet with the field
            getTypeMethod = Integer.class.getMethod(methodName);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int bridge$getDimensionId() {
        try {
            return ((Number)mapDataBridgeMixin$dimensionId.get(this)).intValue();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return 0;
        }

    }

    @Override
    public void bridge$setDimensionId(int dimensionId) {
        try {
            mapDataBridgeMixin$dimensionId.set(this, getTypeMethod.invoke(dimensionId));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateMapArea(int x, int y) {
        updateMapData(x, y);
    }
}
