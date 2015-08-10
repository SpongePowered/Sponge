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
package org.spongepowered.common.data.processor.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.builder;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;
import static org.spongepowered.common.data.DataTransactionBuilder.successReplaceData;
import static org.spongepowered.common.data.util.DataUtil.checkDataExists;

import com.google.common.base.Optional;
import net.minecraft.entity.item.EntityPainting;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.entity.ArtData;
import org.spongepowered.api.data.type.Art;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.entity.SpongeArtData;

public class SpongeArtDataProcessor implements SpongeDataProcessor<ArtData>  {

    @Override
    public Optional<ArtData> getFrom(DataHolder dataHolder) {
        if (!(checkNotNull(dataHolder) instanceof EntityPainting)) {
            return Optional.absent();
        }
        return Optional.of(create().setArt((Art) (Object) ((EntityPainting) dataHolder).art));
    }

    @Override
    public Optional<ArtData> fillData(DataHolder dataHolder, ArtData manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof EntityPainting)) {
            return Optional.absent();
        }
        switch (checkNotNull(priority)) {
            case DATA_HOLDER:
            case PRE_MERGE:
                return Optional.of(checkNotNull(manipulator).setArt(((Art) (Object) ((EntityPainting) dataHolder).art)));
            default:
                return Optional.of(manipulator);
        }
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, ArtData manipulator, DataPriority priority) {
        if (!(checkNotNull(dataHolder) instanceof EntityPainting)) {
            return fail(manipulator);
        }
        switch (checkNotNull(priority)) {
            case DATA_MANIPULATOR:
            case POST_MERGE:
                final ArtData oldData = getFrom(dataHolder).get();
                ((EntityPainting) dataHolder).art = ((EntityPainting.EnumArt) (Object) manipulator.getArt());
                return successReplaceData(oldData);
            default:
                return successNoData();
        }
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<ArtData> build(DataView container) throws InvalidDataException {
        checkDataExists(container, SpongeArtData.ART);
        final String artId = container.getString(SpongeArtData.ART).get();
        Optional<Art> artOptional = Sponge.getGame().getRegistry().getType(Art.class, artId);
        if (!artOptional.isPresent()) {
            return Optional.absent();
        }
        return Optional.of(create().setArt(artOptional.get()));
    }

    @Override
    public ArtData create() {
        return new SpongeArtData();
    }

    @Override
    public Optional<ArtData> createFrom(DataHolder dataHolder) {
        if (!(checkNotNull(dataHolder) instanceof EntityPainting)) {
            return Optional.absent();
        }
        return Optional.of(create().setArt((Art) (Object) ((EntityPainting) dataHolder).art));
    }
}
