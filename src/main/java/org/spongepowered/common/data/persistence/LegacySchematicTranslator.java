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
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeVisitor;
import org.spongepowered.api.world.schematic.BlockPalette;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongeSchematic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LegacySchematicTranslator implements DataTranslator<Schematic> {

    private static final LegacySchematicTranslator INSTANCE = new LegacySchematicTranslator();
    private static final TypeToken<Schematic> TYPE_TOKEN = TypeToken.of(Schematic.class);
    private static final int MAX_SIZE = 65535;
    private static final DataQuery TILE_ID = DataQuery.of("id");

    public static LegacySchematicTranslator get() {
        return INSTANCE;
    }

    private LegacySchematicTranslator() {

    }

    @Override
    public String getId() {
        return "sponge:legacy_schematic";
    }

    @Override
    public String getName() {
        return "Legacy Schematic translator";
    }

    @Override
    public TypeToken<Schematic> getToken() {
        return TYPE_TOKEN;
    }

    @Override
    public Schematic translate(DataView view) throws InvalidDataException {
        // We default to sponge as the assumption should be that if this tag
        // (which is not in the sponge schematic specification) is not present
        // then it is more likely that its a sponge schematic than a legacy
        // schematic
        String materials = view.getString(DataQueries.Schematic.LEGACY_MATERIALS).orElse("Sponge");
        if ("Sponge".equalsIgnoreCase(materials)) {
            // not a legacy schematic use the new loader instead.
            return DataTranslators.SCHEMATIC.translate(view);
        } else if (!"Alpha".equalsIgnoreCase(materials)) {
            throw new InvalidDataException(String.format("Schematic specifies unknown materials %s", materials));
        }
        int width = view.getShort(DataQueries.Schematic.WIDTH).get();
        int height = view.getShort(DataQueries.Schematic.HEIGHT).get();
        int length = view.getShort(DataQueries.Schematic.LENGTH).get();
        if (width > MAX_SIZE || height > MAX_SIZE || length > MAX_SIZE) {
            throw new InvalidDataException(String.format(
                    "Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)", width, height, length, MAX_SIZE));
        }
        int offsetX = view.getInt(DataQueries.Schematic.LEGACY_OFFSET_X).orElse(0);
        int offsetY = view.getInt(DataQueries.Schematic.LEGACY_OFFSET_Y).orElse(0);
        int offsetZ = view.getInt(DataQueries.Schematic.LEGACY_OFFSET_Z).orElse(0);
        BlockPalette palette = GlobalPalette.instance;
        ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(new Vector3i(-offsetX, -offsetY, -offsetZ),
                new Vector3i(width, height, length));
        byte[] block_ids = (byte[]) view.get(DataQueries.Schematic.LEGACY_BLOCKS).get();
        byte[] block_data = (byte[]) view.get(DataQueries.Schematic.LEGACY_BLOCK_DATA).get();
        byte[] add_block = (byte[]) view.get(DataQueries.Schematic.LEGACY_ADD_BLOCKS).orElse(null);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = (y * length + z) * width + x;
                    final int default_state_id = block_ids[index];
                    final int blockData = block_data[index] & 0xF;
                    int palette_id = default_state_id << 4 | blockData;
                    if (add_block != null) {
                        palette_id |= add_block[index] << 12;
                    }
                    Optional<BlockState> blockState = palette.get(palette_id);
                    if (!blockState.isPresent()) {
                        // At the very least get the default state id
                        blockState = Optional.of((BlockState) Block.REGISTRY.getObjectById(default_state_id));
                    }
                    BlockState block = blockState.orElseGet(BlockTypes.COBBLESTONE::getDefaultState);
                    buffer.setBlock(x - offsetX, y - offsetY, z - offsetZ, block);
                }
            }
        }
        Map<Vector3i, TileEntityArchetype> tiles = Maps.newHashMap();
        List<DataView> tiledata = view.getViewList(DataQueries.Schematic.LEGACY_TILEDATA).orElse(null);
        if (tiledata != null) {
            for (DataView tile : tiledata) {
                int x = tile.getInt(DataQueries.X_POS).get();
                int y = tile.getInt(DataQueries.Y_POS).get();
                int z = tile.getInt(DataQueries.Z_POS).get();
                final String tileType = tile.getString(TILE_ID).get();
                final ResourceLocation name = new ResourceLocation(tileType);
                TileEntityType type = TileEntityTypeRegistryModule.getInstance()
                        .getForClass(TileEntity.REGISTRY.getObject(name));
                final BlockState state = buffer.getBlock(x - offsetX, y - offsetY, z - offsetZ);
                // Somehow we need to get some DataFixers in here, because some data may be legacy from older versions before data
                // fixers.
                if (type!= null && SpongeImplHooks.hasBlockTileEntity(((Block) state.getType()), BlockUtil.toNative(state))) {
                    TileEntityArchetype archetype = new SpongeTileEntityArchetypeBuilder()
                        .state(state)
                        .tileData(tile)
                        .tile(type)
                        .build();
                    tiles.put(new Vector3i(x - offsetX, y - offsetY, z - offsetZ), archetype);
                }
            }
        }
        SpongeSchematic schematic = new SpongeSchematic(buffer, tiles);
        return schematic;
    }

    @Override
    public DataContainer translate(Schematic schematic) throws InvalidDataException {
        DataContainer data = DataContainer.createNew(DataView.SafetyMode.NO_DATA_CLONED);
        addTo(schematic, data);
        return data;
    }

    @Override
    public DataView addTo(Schematic schematic, DataView data) {
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
        data.set(DataQueries.Schematic.LEGACY_MATERIALS, "Alpha");
        // These are added for better interop with WorldEdit
        data.set(DataQueries.Schematic.LEGACY_OFFSET_X, -xMin);
        data.set(DataQueries.Schematic.LEGACY_OFFSET_Y, -yMin);
        data.set(DataQueries.Schematic.LEGACY_OFFSET_Z, -zMin);
        SaveIterator itr = new SaveIterator(width, height, length);
        schematic.getBlockWorker().iterate(itr);
        byte[] blockids = itr.blockids;
        byte[] extraids = itr.extraids;
        byte[] blockdata = itr.blockdata;
        data.set(DataQueries.Schematic.LEGACY_BLOCKS, blockids);
        data.set(DataQueries.Schematic.LEGACY_BLOCK_DATA, blockdata);
        if (extraids != null) {
            data.set(DataQueries.Schematic.LEGACY_ADD_BLOCKS, extraids);
        }
        List<DataView> tileEntities = Lists.newArrayList();
        for (Map.Entry<Vector3i, TileEntityArchetype> entry : schematic.getTileEntityArchetypes().entrySet()) {
            Vector3i pos = entry.getKey();
            DataContainer tiledata = entry.getValue().getTileData();
            tiledata.set(DataQueries.X_POS, pos.getX() - xMin);
            tiledata.set(DataQueries.Y_POS, pos.getY() - yMin);
            tiledata.set(DataQueries.Z_POS, pos.getZ() - zMin);
            tileEntities.add(tiledata);
        }
        data.set(DataQueries.Schematic.LEGACY_TILEDATA, tileEntities);
        return data;
    }

    private static class SaveIterator implements BlockVolumeVisitor<Schematic> {

        private final int width;
        private final int length;

        public byte[] blockids;
        public byte[] extraids;
        public byte[] blockdata;

        public SaveIterator(int width, int height, int length) {
            this.width = width;
            this.length = length;

            this.blockids = new byte[width * height * length];
            this.extraids = null;
            this.blockdata = new byte[width * height * length];
        }

        @Override
        public void visit(Schematic volume, int x, int y, int z) {
            int x0 = x - volume.getBlockMin().getX();
            int y0 = y - volume.getBlockMin().getY();
            int z0 = z - volume.getBlockMin().getZ();
            int id = GlobalPalette.instance.get(volume.getBlock(x, y, z)).get();
            int blockid = id >> 4;
            int dataid = id & 0xF;
            int index = (y0 * this.length + z0) * this.width + x0;
            this.blockids[index] = (byte) (blockid & 0xFF);
            if (blockid > 0xFF) {
                if (this.extraids == null) {
                    this.extraids = new byte[(this.blockdata.length >> 2) + 1];
                }
                this.extraids[index >> 1] = (byte) (((index & 1) == 0) ? this.extraids[index >> 1] & 0xF0 | (blockid >> 8) & 0xF
                        : this.extraids[index >> 1] & 0xF | ((blockid >> 8) & 0xF) << 4);
            }
            this.blockdata[index] = (byte) dataid;
        }

    }

}
