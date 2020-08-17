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
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.map.decoration.MapDecoration;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class SpongeMapDecorationBuilder implements MapDecoration.Builder {
    @Nullable
    private MapDecorationType type = null;
    private int x;
    private int y;
    @Nullable private Direction dir = null;

    @Override
    public MapDecoration.Builder type(MapDecorationType type) {
        this.type = type;
        return this;
    }

    @Override
    public DataBuilder<MapDecoration> reset() {
        this.type = null;
        this.x = 0;
        this.y = 0;
        this.dir = null;
        return this;
    }

    @Override
    public DataBuilder<MapDecoration> from(MapDecoration value) {
        this.type = value.getType();
        this.x = value.getX();
        this.y = value.getY();
        this.dir = value.getRotation();
        return this;
    }

    @Override
    public MapDecoration.Builder x(int x) throws IllegalStateException {
        checkState(MapUtil.isInMapDecorationBounds(x), "x not in bounds");
        this.x = x;
        return this;
    }

    @Override
    public MapDecoration.Builder y(int y) throws IllegalStateException {
        checkState(MapUtil.isInMapDecorationBounds(y), "y not in bounds");
        this.y = y;
        return this;
    }

    @Override
    public MapDecoration.Builder rotation(Direction direction) {
        checkState(direction.isCardinal()
                || direction.isOrdinal()
                || direction.isSecondaryOrdinal(),
                "Direction given in MapDecorationBuilder.rotation was not a cardinal, ordinal or secondary ordinal");
        this.dir = direction;
        return this;
    }

    @Override
    public MapDecoration.Builder position(Vector2i position) throws IllegalStateException {
        checkState(MapUtil.isInMapDecorationBounds(position.getX()), "x not in bounds");
        checkState(MapUtil.isInMapDecorationBounds(position.getY()), "y not in bounds");
        this.x = position.getX();
        this.y = position.getY();
        return this;
    }

    @Override
    public MapDecoration build() throws IllegalStateException {
        checkNotNull(type, "Type has not been set");
        checkNotNull(this.dir, "Direction has not been set");
        return (SpongeMapDecoration)new net.minecraft.world.storage.MapDecoration(
                ((SpongeMapDecorationType)type).getType(),
                (byte)this.x, (byte)this.y, Constants.Map.DIRECTION_CONVERSION_MAP.get(this.dir));
    }

    @Override
    public Optional<MapDecoration> build(DataView container) throws InvalidDataException {
        return SpongeMapDecoration.fromContainer(container);
    }
}
