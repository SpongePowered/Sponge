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
package org.spongepowered.common.mixin.api.mcp.map;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.registry.type.map.MapDecorationRegistryModule;
import org.spongepowered.common.util.Constants;

@Mixin(MapDecoration.class)
public class MapDecorationMixin_API implements org.spongepowered.api.map.decoration.MapDecoration {
    @Shadow @Final private MapDecoration.Type type;
    @Shadow private byte x;
    @Shadow private byte y;

    @Shadow private byte rotation;

    @Override
    public MapDecorationType getType() {
        return MapDecorationRegistryModule.getByMcType(type)
                .orElseThrow(() -> new IllegalStateException("Tried to get MapDecoration type but it didn't exist in Sponge's registries! Have MC Decoration types been missed?"));
    }

    @Override
    public Vector2i getPosition() {
        return new Vector2i(this.x, this.y);
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setPosition(Vector2i position) {
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(position.getX()), "x position out of bounds");
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(position.getY()), "y position out of bounds");
        this.x = (byte) position.getX();
        this.y = (byte) position.getY();
    }

    @Override
    public void setX(int x) {
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(x), "x out of bounds");
        this.x = (byte) x;
    }

    @Override
    public void setY(int y) {
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(y), "y out of bounds");
        this.y = (byte)y;
    }

    @Override
    public void setRotation(Direction dir) {
        if (!(dir.isCardinal()
                || dir.isOrdinal()
                || dir.isSecondaryOrdinal())) {
            throw new IllegalStateException("Invalid direction. Not a valid direction, must be cardinal, ordinal or secondary ordinal.");
        }
        this.rotation = Constants.Map.DIRECTION_CONVERSION_MAP.get(dir);
    }

    @Override
    public Direction getRotation() {
        return Constants.Map.DIRECTION_CONVERSION_MAP.inverse().get(this.rotation);
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Constants.Map.DECORATION_TYPE, this.type.getIcon())
                .set(Constants.Map.DECORATION_ID, ((MapDecorationBridge) this).bridge$getKey())
                .set(Constants.Map.DECORATION_X, this.x)
                .set(Constants.Map.DECORATION_Y, this.y)
                .set(Constants.Map.DECORATION_ROTATION, this.rotation);
    }
}
