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
package org.spongepowered.common.data.builder.block.tileentity;

import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.Banner;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.meta.BannerPatternLayer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.common.util.Constants;

import java.util.List;
import java.util.Optional;

public class SpongeBannerBuilder extends AbstractTileBuilder<Banner> {

    public SpongeBannerBuilder() {
        super(Banner.class, 1);
    }

    @Override
    protected Optional<Banner> buildContent(DataView container) throws InvalidDataException {
        return super.buildContent(container).flatMap(banner -> {
            if (!container.contains(Constants.TileEntity.Banner.BASE) || !container.contains(Constants.TileEntity.Banner.PATTERNS)) {
                return Optional.empty();
            }

            final String dyeColorId = container.getString(Constants.TileEntity.Banner.BASE).get();
            final Optional<DyeColor> colorOptional = Sponge.getRegistry().getCatalogRegistry().get(DyeColor.class, CatalogKey.resolve(dyeColorId));
            if (!colorOptional.isPresent()) {
                throw new InvalidDataException("The provided container has an invalid dye color entry!");
            }
            banner.offer(Keys.DYE_COLOR, colorOptional.get());

            // Now we have to get the patterns list
            final List<BannerPatternLayer> patternsList = container.getSerializableList(Constants.TileEntity.Banner.PATTERNS, BannerPatternLayer.class).get();
            banner.offer(Keys.BANNER_PATTERN_LAYERS, patternsList);
            ((BannerTileEntity) banner).validate();
            return Optional.of(banner);
        });
    }
}
