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

import com.google.common.base.Preconditions;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.util.MapUtil;
import org.spongepowered.common.map.decoration.orientation.SpongeMapDecorationOrientation;
import org.spongepowered.common.registry.builtin.sponge.MapDecorationOrientationStreamGenerator;
import org.spongepowered.common.registry.builtin.sponge.MapDecorationTypeStreamGenerator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector2i;

@Mixin(MapDecoration.class)
public abstract class MapDecorationMixin_API implements org.spongepowered.api.map.decoration.MapDecoration {
    @Shadow @Final private MapDecoration.Type type;
    @Shadow private byte x;
    @Shadow private byte y;

    @Shadow private byte rot;

    @Override
    public MapDecorationType getType() {
        return MapDecorationTypeStreamGenerator.getByMcType(type)
                .orElseThrow(() -> new IllegalStateException("Tried to get MapDecoration type but it didn't exist in Sponge's registries! Have MC Decoration types been missed?"));
    }

    @Override
    public Vector2i getPosition() {
        return new Vector2i(this.x, this.y);
    }

    @Override
    public void setPosition(Vector2i position) {
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(position.getX()), "x position out of bounds");
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(position.getY()), "y position out of bounds");
        this.x = (byte) position.getX();
        this.y = (byte) position.getY();
        ((MapDecorationBridge)this).bridge$markAllDirty();
    }

    @Override
    public void setRotation(MapDecorationOrientation dir) {
        this.rot = (byte) ((SpongeMapDecorationOrientation)dir).getOrientationNumber();
        ((MapDecorationBridge)this).bridge$markAllDirty();
    }

    @Override
    public MapDecorationOrientation getRotation() {
        int rot = MapUtil.getMapDecorationOrientation(this.rot);
        return MapDecorationOrientationStreamGenerator.getByInt(rot)
                .orElseThrow(() -> new IllegalStateException("A map had a rotation that didn't exist!"));
    }

    @Override
    public boolean isPersistent() {
        return ((MapDecorationBridge)this).bridge$isPersistent();
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
                .set(Constants.Map.DECORATION_ROTATION, (byte) MapUtil.getMapDecorationOrientation(this.rot));
    }
}
