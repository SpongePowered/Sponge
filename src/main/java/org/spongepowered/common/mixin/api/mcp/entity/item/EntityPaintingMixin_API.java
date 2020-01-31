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
package org.spongepowered.common.mixin.api.mcp.entity.item;

import net.minecraft.entity.item.EntityPainting;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.ArtData;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeArtData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.api.mcp.entity.EntityHangingMixin_API;
import org.spongepowered.common.util.Constants;

import java.util.Collection;

@Mixin(EntityPainting.class)
public abstract class EntityPaintingMixin_API extends EntityHangingMixin_API implements Painting {

    @Shadow public EntityPainting.EnumArt art;

    @Override
    public ArtData getArtData() {
        return new SpongeArtData((Art) (Object) this.art);
    }

    @Override
    public Value<Art> art() {
        return new SpongeValue<>(Keys.ART, Constants.Catalog.DEFAULT_ART, (Art) (Object) this.art);
    }

    @Override
    protected void spongeApi$supplyVanillaManipulators(final Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);
        manipulators.add(this.getArtData());
    }
}
