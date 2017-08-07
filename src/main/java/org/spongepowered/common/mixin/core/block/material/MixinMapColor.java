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
package org.spongepowered.common.mixin.core.block.material;

import com.google.common.base.MoreObjects;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.map.color.MapColor;
import org.spongepowered.api.map.color.MapShade;
import org.spongepowered.api.util.Color;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.interfaces.block.material.IMixinMapColor;
import org.spongepowered.common.map.SpongeMapColor;

import java.util.HashMap;
import java.util.Map;

@NonnullByDefault
@Mixin(net.minecraft.block.material.MapColor.class)
public class MixinMapColor implements MapColor.Base, IMixinMapColor {

    @Shadow @Final public int colorValue;
    @Shadow @Final public int colorIndex;

    private Map<MapShade, MapColor> cachedShadings = new HashMap<>();
    private String id;
    private Color color;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruct(CallbackInfo callbackInfo) {
        color = Color.ofRgb(colorValue);
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public MapColor shade(MapShade newShading) {
        MapColor shaded = cachedShadings.get(newShading);
        if (shaded == null) {
            shaded = new SpongeMapColor(this, newShading);
            cachedShadings.put(newShading, shaded);
        }
        return shaded;
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew();
        container.set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.MAP_COLOR_INDEX, this.colorIndex);
        return container;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof MapColor) {
            return ((MapColor) obj).getColor().getRgb() == this.colorValue;
        }
        return false;
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected MoreObjects.ToStringHelper toStringHelper() {
        return MoreObjects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("colorValue", this.colorValue)
                .add("colorIndex", this.colorIndex);
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
