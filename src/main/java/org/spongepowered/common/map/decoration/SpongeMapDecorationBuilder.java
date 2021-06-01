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

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientations;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.map.decoration.orientation.SpongeMapDecorationOrientation;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.MapUtil;
import org.spongepowered.math.vector.Vector2i;


public class SpongeMapDecorationBuilder implements MapDecoration.Builder {
    private @Nullable MapDecorationType type = null;
    private int x;
    private int y;
    private MapDecorationOrientation rot = MapDecorationOrientations.NORTH.get();
    private @Nullable Component customName = null;

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
        this.rot = MapDecorationOrientations.NORTH.get();
        this.customName = null;
        return this;
    }

    @Override
    public MapDecoration.Builder from(MapDecoration value) {
        Preconditions.checkNotNull(value, "MapDecoration cannot be null");
        this.type = value.type();
        this.x = value.position().x();
        this.y = value.position().y();
        this.rot = value.rotation();
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
        Preconditions.checkArgument(MapUtil.isInMapDecorationBounds(position.x()), "x not in bounds");
        Preconditions.checkArgument(MapUtil.isInMapDecorationBounds(position.y()), "y not in bounds");
        this.x = position.x();
        this.y = position.y();
        return this;
    }

    @Override
    public MapDecoration.Builder customName(Component customName) {
        this.customName = customName;
        return this;
    }

    @Override
    public MapDecoration.Builder fromContainer(final DataView container) {
        // Exclude Decoration id because the user should not care about it,
        // and if we did copy it over, it would overwrite any existing decoration
        // with that id, which is quite likely
        if (!container.contains(Constants.Map.DECORATION_TYPE, Constants.Map.DECORATION_ROTATION)) {
            return this;
        }
        this.reset();

        final byte type = getByteFromContainer(container, Constants.Map.DECORATION_TYPE);
        final byte rot = getByteFromContainer(container, Constants.Map.DECORATION_ROTATION);

        final net.minecraft.world.level.saveddata.maps.MapDecoration.Type[] decorations = net.minecraft.world.level.saveddata.maps.MapDecoration.Type.values();
        final int possibleTypes = decorations.length;
        if (type < 0 || type > possibleTypes) {
            throw new InvalidDataException(Constants.Map.DECORATION_TYPE + ", is out of bounds. Should be 0-" + (possibleTypes-1));
        }

        final MapDecorationType mapDecorationType = SpongeMapDecorationType.toSpongeType(decorations[type])
                .orElseThrow(() -> new IllegalStateException("Missing a MapDecorationType, could not find one for Minecraft's MapDecoration.Type: "
                    + decorations[type].toString()));
        this.type(mapDecorationType);

        final int intRot = MapUtil.normalizeDecorationOrientation(rot);
        this.rotation(MapUtil.getMapRotById(intRot));

        // We do not require X and Y to build, therefore we do not require them here.
        // They are mutable once it is created.
        if (container.contains(Constants.Map.DECORATION_X, Constants.Map.DECORATION_Y)) {
            final byte x = getByteFromContainer(container, Constants.Map.DECORATION_X);
            final byte y = getByteFromContainer(container, Constants.Map.DECORATION_Y);

            this.position(Vector2i.from(x, y));
        }

        if (container.contains(Constants.Map.NAME)) {
            final Component component = SpongeAdventure.GSON.deserialize(
                    container.getString(Constants.Map.NAME)
                            .orElseThrow(() -> new InvalidDataException("Invalid data type for " + Constants.Map.NAME + ". Should be String"))
            );
            this.customName(component);
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
        MapDecoration decoration = (MapDecoration) new net.minecraft.world.level.saveddata.maps.MapDecoration(
                ((SpongeMapDecorationType)type).getType(),
                (byte)this.x, (byte)this.y, (byte) ((SpongeMapDecorationOrientation)this.rot).getOrientationNumber(),
                this.customName == null ? null : SpongeAdventure.asVanilla(this.customName));
        ((MapDecorationBridge)decoration).bridge$setPersistent(true); // Anything that comes out of this builder should be persistent
        return decoration;
    }
}
