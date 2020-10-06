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
package org.spongepowered.common.map.decoration;

import com.flowpowered.math.vector.Vector2i;
import com.google.common.base.Preconditions;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.map.decoration.orientation.SpongeMapDecorationOrientation;
import org.spongepowered.common.registry.type.map.MapDecorationOrientationRegistryModule;
import org.spongepowered.common.registry.type.map.MapDecorationRegistryModule;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;

public class SpongeMapDecorationBuilder implements MapDecoration.Builder {
    @Nullable
    private MapDecorationType type = null;
    private int x;
    private int y;
    @Nullable private MapDecorationOrientation rot = null;

    @Override
    public MapDecoration.Builder type(MapDecorationType type) {
        this.type = type;
        return this;
    }

    @Override
    public MapDecoration.Builder reset() {
        this.type = null;
        this.x = 0;
        this.y = 0;
        this.rot = null;
        return this;
    }

    @Override
    public MapDecoration.Builder from(MapDecoration value) {
        Preconditions.checkNotNull(value, "MapDecoration cannot be null");
        this.type = value.getType();
        this.x = value.getPosition().getX();
        this.y = value.getPosition().getY();
        this.rot = value.getRotation();
        return this;
    }

    @Override
    public MapDecoration.Builder rotation(MapDecorationOrientation rot) throws IllegalArgumentException {
        Preconditions.checkNotNull(rot, "Orientation cannot be null");
        this.rot = rot;
        return this;
    }

    @Override
    public MapDecoration.Builder position(Vector2i position) throws IllegalArgumentException {
        Preconditions.checkNotNull(position, "position cannot be null");
        Preconditions.checkArgument(MapUtil.isInMapDecorationBounds(position.getX()), "x not in bounds");
        Preconditions.checkArgument(MapUtil.isInMapDecorationBounds(position.getY()), "y not in bounds");
        this.x = position.getX();
        this.y = position.getY();
        return this;
    }

    @Override
    public MapDecoration.Builder fromContainer(final DataView container) {
        // Exclude Decoration id because the user should not care about it,
        // and if we did copy it over, it would overwrite any existing decoration
        // with that id, which is quite likely
        if (!container.contains(
                Constants.Map.DECORATION_TYPE,
                Constants.Map.DECORATION_ROTATION
        )) {
            return this;
        }
        reset();

        final byte type = getByteFromContainer(container, Constants.Map.DECORATION_TYPE);
        final byte rot = getByteFromContainer(container, Constants.Map.DECORATION_ROTATION);

        final int possibleTypes = net.minecraft.world.storage.MapDecoration.Type.values().length;
        if (type < 0 || type > possibleTypes) {
            throw new InvalidDataException(Constants.Map.DECORATION_TYPE + ", is out of bounds. Should be 0-" + (possibleTypes-1));
        }
        final MapDecorationType mapDecorationType = MapDecorationRegistryModule.getByMcType(net.minecraft.world.storage.MapDecoration.Type.values()[type])
                .orElseThrow(() -> new IllegalStateException("Missing a MapDecorationType, could not find one for Minecraft's MapDecoration.Type: "
                        + net.minecraft.world.storage.MapDecoration.Type.values()[type].toString()));

        this.type(mapDecorationType);

        final int intRot = MapUtil.getMapDecorationOrientation(rot);
        MapDecorationOrientation orientation = MapDecorationOrientationRegistryModule.getInstance().getByOrientationId(intRot)
                .orElseThrow(() -> new InvalidDataException(intRot + " is not a valid number for a MapDecorationOrientation"));

        this.rotation(orientation);

        // We do not require X and Y to build, therefore we do not require them here.
        // They are mutable once it is created.
        if (container.contains(
                Constants.Map.DECORATION_X,
                Constants.Map.DECORATION_Y)) {
            final byte x = getByteFromContainer(container, Constants.Map.DECORATION_X);
            final byte y = getByteFromContainer(container, Constants.Map.DECORATION_Y);

            this.position(Vector2i.from(x, y));
        }

        return this;
    }

    private byte getByteFromContainer(final DataView container, final DataQuery query) throws InvalidDataException {
        return container.getByte(query)
                .orElseThrow(() -> new InvalidDataException("Invalid data type for " + query + ". Should be byte"));
    }

    @Override
    public MapDecoration build() throws IllegalStateException {
        Preconditions.checkNotNull(this.type, "Type has not been set");
        Preconditions.checkNotNull(this.rot, "Rotation has not been set");
        return (MapDecoration) new net.minecraft.world.storage.MapDecoration(
                ((SpongeMapDecorationType)type).getType(),
                (byte)this.x, (byte)this.y, (byte) ((SpongeMapDecorationOrientation)this.rot).getOrientationNumber());
    }
}
