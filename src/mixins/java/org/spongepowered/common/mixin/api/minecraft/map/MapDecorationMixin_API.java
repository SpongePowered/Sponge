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
package org.spongepowered.common.mixin.api.minecraft.map;

import com.google.common.base.Preconditions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.map.decoration.MapDecorationType;
import org.spongepowered.api.map.decoration.orientation.MapDecorationOrientation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.map.decoration.SpongeMapDecorationType;
import org.spongepowered.common.util.MapUtil;
import org.spongepowered.common.map.decoration.orientation.SpongeMapDecorationOrientation;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector2i;

import javax.annotation.Nullable;

@Mixin(MapDecoration.class)
public abstract class MapDecorationMixin_API implements org.spongepowered.api.map.decoration.MapDecoration {

    // @formatter:off
    @Shadow @Final private MapDecoration.Type type;
    @Shadow private byte x;
    @Shadow private byte y;
    @Shadow private byte rot;
    @Shadow @Nullable public abstract Component shadow$getName();
    // @formatter:on

    @Override
    public MapDecorationType type() {
        return SpongeMapDecorationType.toSpongeType(this.type)
                .orElseThrow(() -> new IllegalStateException("Tried to get MapDecoration type but it didn't exist in Sponge's registries! Have MC Decoration types been missed?"));
    }

    @Override
    public Vector2i position() {
        return new Vector2i(this.x, this.y);
    }

    @Override
    public void setPosition(Vector2i position) {
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(position.x()), "x position out of bounds");
        Preconditions.checkState(MapUtil.isInMapDecorationBounds(position.y()), "y position out of bounds");
        this.x = (byte) position.x();
        this.y = (byte) position.y();
        ((MapDecorationBridge)this).bridge$markAllDirty();
    }

    @Override
    public void setRotation(MapDecorationOrientation dir) {
        this.rot = (byte) ((SpongeMapDecorationOrientation)dir).getOrientationNumber();
        ((MapDecorationBridge)this).bridge$markAllDirty();
    }

    @Override
    public MapDecorationOrientation rotation() {
        int rot = MapUtil.normalizeDecorationOrientation(this.rot);
        return MapUtil.getMapRotById(rot);
    }

    @Override
    public boolean isPersistent() {
        return ((MapDecorationBridge)this).bridge$isPersistent();
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer data = DataContainer.createNew()
                .set(Constants.Map.DECORATION_TYPE, this.type.getIcon())
                .set(Constants.Map.DECORATION_ID, ((MapDecorationBridge) this).bridge$getKey())
                .set(Constants.Map.DECORATION_X, this.x)
                .set(Constants.Map.DECORATION_Y, this.y)
                .set(Constants.Map.DECORATION_ROTATION, (byte) MapUtil.normalizeDecorationOrientation(this.rot));
        if (this.shadow$getName() != null) {
            data.set(Constants.Map.NAME, TextComponent.Serializer.toJson(this.shadow$getName()));
        }
        return data;
    }
}
