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
package org.spongepowered.common.data.persistence;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.extent.MutableBlockVolume;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer;
import org.spongepowered.common.world.schematic.BimapPalette;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongeSchematic;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SchematicTranslator implements DataTranslator<Schematic> {

    private static final SchematicTranslator INSTANCE = new SchematicTranslator();
    private static final TypeToken<Schematic> TYPE_TOKEN = TypeToken.of(Schematic.class);
    private static final int VERSION = 1;
    private static final int MAX_SIZE = 65535;

    public static SchematicTranslator get() {
        return INSTANCE;
    }

    private SchematicTranslator() {

    }

    @Override
    public String getId() {
        return "sponge:schematic";
    }

    @Override
    public String getName() {
        return "Sponge Schematic Translator";
    }

    @Override
    public TypeToken<Schematic> getToken() {
        return TYPE_TOKEN;
    }

    @Override
    public Schematic translate(DataView view) throws InvalidDataException {
        int version = view.getInt(DataQueries.Schematic.VERSION).get();
        // TODO version conversions
        if (version != VERSION) {
            throw new InvalidDataException(String.format("Unknown schematic version %d (current version is %d)", version, VERSION));
        }
        DataView metadata = view.getView(DataQueries.Schematic.METADATA).orElse(null);

        // TODO error handling for these optionals
        int width = view.getShort(DataQueries.Schematic.WIDTH).get();
        int height = view.getShort(DataQueries.Schematic.HEIGHT).get();
        int length = view.getShort(DataQueries.Schematic.LENGTH).get();
        if (width > MAX_SIZE || height > MAX_SIZE || length > MAX_SIZE) {
            throw new InvalidDataException(String.format("Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)",
                    width, height, length, MAX_SIZE));
        }

        int[] offset = (int[]) view.get(DataQueries.Schematic.OFFSET).orElse(null);
        if (offset == null) {
            offset = new int[3];
        }
        if (offset.length != 3) {
            throw new InvalidDataException("Schematic offset was not of length 3");
        }
        BlockPalette palette;
        Optional<DataView> paletteData = view.getView(DataQueries.Schematic.PALETTE);
        int palette_max = view.getInt(DataQueries.Schematic.PALETTE_MAX).orElse(0xFFFF);
        if (paletteData.isPresent()) {
            // If we had a default palette_max we don't want to allocate all
            // that space for nothing so we use a sensible default instead
            palette = new BimapPalette(palette_max != 0xFFFF ? palette_max : 64);
            DataView paletteMap = paletteData.get();
            Set<DataQuery> paletteKeys = paletteMap.getKeys(false);
            for (DataQuery key : paletteKeys) {
                BlockState state = Sponge.getRegistry().getType(BlockState.class, key.getParts().get(0)).get();
                ((BimapPalette) palette).assign(state, paletteMap.getInt(key).get());
            }
        } else {
            palette = GlobalPalette.instance;
        }

        MutableBlockVolume buffer =
                new ArrayMutableBlockBuffer(palette, new Vector3i(-offset[0], -offset[1], -offset[2]), new Vector3i(width, height, length));

        byte[] blockdata = (byte[]) view.get(DataQueries.Schematic.BLOCK_DATA).get();
        int index = 0;
        int i = 0;
        int value = 0;
        int varint_length = 0;
        while (i < blockdata.length) {
            value = 0;
            varint_length = 0;

            while (true) {
                value |= (blockdata[i] & 127) << (varint_length++ * 7);
                if (varint_length > 5) {
                    throw new RuntimeException("VarInt too big (probably corrupted data)");
                }
                if ((blockdata[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            // index = (y * length + z) * width + x
            int y = index / (width * length);
            int z = (index % (width * length)) / width;
            int x = (index % (width * length)) % width;
            BlockState state = palette.get(value).get();
            buffer.setBlock(x - offset[0], y - offset[1], z - offset[2], state, SpongeImpl.getImplementationCause());

            index++;
        }
        Map<Vector3i, TileEntityArchetype> tiles = Maps.newHashMap();
        List<DataView> tiledata = view.getViewList(DataQueries.Schematic.TILEENTITY_DATA).orElse(null);
        if (tiledata != null) {
            for (DataView tile : tiledata) {
                int[] pos = (int[]) tile.get(DataQueries.Schematic.TILEENTITY_POS).get();
                if (offset.length != 3) {
                    throw new InvalidDataException("Schematic tileentity pos was not of length 3");
                }
                TileEntityType type = TileEntityTypeRegistryModule.getInstance()
                        .getForClass(TileEntity.REGISTRY.getObject(new ResourceLocation(tile.getString(DataQuery.of("id")).get())));
                TileEntityArchetype archetype = new SpongeTileEntityArchetypeBuilder()
                        .state(buffer.getBlock(pos[0] - offset[0], pos[1] - offset[1], pos[2] - offset[2]))
                        .tileData(tile)
                        .tile(type)
                        .build();
                tiles.put(new Vector3i(pos[0] - offset[0], pos[1] - offset[1], pos[2] - offset[2]), archetype);
            }
        }

        Schematic schematic = new SpongeSchematic(buffer, tiles, metadata);
        return schematic;
    }

    @Override
    public DataContainer translate(Schematic schematic) throws InvalidDataException {
        DataContainer data = new MemoryDataContainer(DataView.SafetyMode.NO_DATA_CLONED);
        final int xMin = schematic.getBlockMin().getX();
        final int yMin = schematic.getBlockMin().getY();
        final int zMin = schematic.getBlockMin().getZ();
        final int width = schematic.getBlockSize().getX();
        final int height = schematic.getBlockSize().getY();
        final int length = schematic.getBlockSize().getZ();
        if (width > MAX_SIZE || height > MAX_SIZE || length > MAX_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)", width, height, length, MAX_SIZE));
        }
        data.set(DataQueries.Schematic.WIDTH, width);
        data.set(DataQueries.Schematic.HEIGHT, height);
        data.set(DataQueries.Schematic.LENGTH, length);

        data.set(DataQueries.Schematic.VERSION, VERSION);
        for (DataQuery metaKey : schematic.getMetadata().getKeys(false)) {
            data.set(DataQueries.Schematic.METADATA.then(metaKey), schematic.getMetadata().get(metaKey).get());
        }

        int[] offset = new int[] {-xMin, -yMin, -zMin};
        data.set(DataQueries.Schematic.OFFSET, offset);

        BlockPalette palette = schematic.getPalette();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);

        for (int y = 0; y < height; y++) {
            int y0 = yMin + y;
            for (int z = 0; z < length; z++) {
                int z0 = zMin + z;
                for (int x = 0; x < width; x++) {
                    int x0 = xMin + x;
                    BlockState state = schematic.getBlock(x0, y0, z0);
                    int id = palette.getOrAssign(state);

                    while ((id & -128) != 0) {
                        buffer.write(id & 127 | 128);
                        id >>>= 7;
                    }
                    buffer.write(id);
                }
            }
        }

        data.set(DataQueries.Schematic.BLOCK_DATA, buffer.toByteArray());

        if (palette.getType() == BlockPaletteTypes.LOCAL) {
            DataQuery paletteQuery = DataQueries.Schematic.PALETTE;
            for (BlockState state : palette.getEntries()) {
                // getOrAssign to skip the optional, it will never assign
                data.set(paletteQuery.then(state.getId()), palette.getOrAssign(state));
            }
            data.set(DataQueries.Schematic.PALETTE_MAX, palette.getHighestId());
        }
        List<DataView> tileEntities = Lists.newArrayList();
        for (Map.Entry<Vector3i, TileEntityArchetype> entry : schematic.getTileEntityArchetypes().entrySet()) {
            Vector3i pos = entry.getKey();
            DataContainer tiledata = entry.getValue().getTileData();
            int[] apos = new int[]{pos.getX() - xMin, pos.getY() - yMin, pos.getZ() - zMin};
            tiledata.set(DataQueries.Schematic.TILEENTITY_POS, apos);
            if(!tiledata.contains(DataQueries.CONTENT_VERSION)) {
                // Set a default content version of 1
                tiledata.set(DataQueries.CONTENT_VERSION, 1);
            }
            tileEntities.add(tiledata);
        }
        data.set(DataQueries.Schematic.TILEENTITY_DATA, tileEntities);

        return data;
    }

}
