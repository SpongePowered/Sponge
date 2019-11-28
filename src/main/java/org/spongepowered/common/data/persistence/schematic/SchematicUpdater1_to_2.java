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
package org.spongepowered.common.data.persistence.schematic;

import org.spongepowered.api.data.persistence.DataContentUpdater;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.common.util.Constants;

// TODO - Migrate this to DataFixer DSL in 1.14.
public class SchematicUpdater1_to_2 implements DataContentUpdater {

    @Override
    public int getInputVersion() {
        return 1;
    }

    @Override
    public int getOutputVersion() {
        return 2;
    }

    @Override
    public DataView update(final DataView content) {
        content.set(Constants.Sponge.Schematic.VERSION, 2);
        content.set(Constants.Sponge.Schematic.DATA_VERSION, Constants.MINECRAFT_DATA_VERSION);
        content.getViewList(Constants.Sponge.Schematic.Versions.V1_TILE_ENTITY_DATA).ifPresent(tiles -> {
            tiles.forEach(tile -> {
                // Remove unnecessary version information.
                tile.getString(Constants.Sponge.Schematic.Versions.V1_TILE_ENTITY_ID).ifPresent(id -> {
                    // This is a fix for v1 created schematics by SpongeCommon implementation that was
                    // unfortunately breaking the rules on the schematic format. The format called for
                    // using "Id", but the implementation was saving "id", and it was reading "id".
                    tile.remove(Constants.Sponge.Schematic.Versions.V1_TILE_ENTITY_ID);
                    tile.set(Constants.Sponge.Schematic.BLOCKENTITY_ID, id);
                });
                tile.remove(Queries.CONTENT_VERSION);
            });
            content.remove(Constants.Sponge.Schematic.Versions.V1_TILE_ENTITY_DATA);
            content.set(Constants.Sponge.Schematic.BLOCKENTITY_DATA, tiles);
        });
        return content;
    }
}
