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
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.DataTranslator;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.world.extent.worker.procedure.BlockVolumeVisitor;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.entity.SpongeEntityArchetypeBuilder;
import org.spongepowered.common.mixin.core.server.MinecraftServerAccessor;
import org.spongepowered.common.mixin.core.tileentity.TileEntityAccessor;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.gen.ArrayMutableBlockBuffer;
import org.spongepowered.common.world.schematic.GlobalPalette;
import org.spongepowered.common.world.schematic.SpongeSchematicBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LegacySchematicTranslator implements DataTranslator<Schematic> {

    private static final LegacySchematicTranslator INSTANCE = new LegacySchematicTranslator();
    private static final TypeToken<Schematic> TYPE_TOKEN = TypeToken.of(Schematic.class);
    private static final int MAX_SIZE = 65535;
    private static final DataQuery TILE_ID = DataQuery.of("id");
    private static DataFixer VANILLA_FIXER;

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
        if (VANILLA_FIXER == null) {
            VANILLA_FIXER = ((MinecraftServerAccessor) SpongeImpl.getServer()).accessor$getDataFixer();
        }
        // We default to sponge as the assumption should be that if this tag
        // (which is not in the sponge schematic specification) is not present
        // then it is more likely that its a sponge schematic than a legacy
        // schematic
        String materials = view.getString(Constants.Sponge.Schematic.Legacy.MATERIALS).orElse("Sponge");
        if ("Sponge".equalsIgnoreCase(materials)) {
            // not a legacy schematic use the new loader instead.
            return DataTranslators.SCHEMATIC.translate(view);
        } else if (!"Alpha".equalsIgnoreCase(materials)) {
            throw new InvalidDataException(String.format("Schematic specifies unknown materials %s", materials));
        }
        int width = view.getShort(Constants.Sponge.Schematic.WIDTH).get();
        int height = view.getShort(Constants.Sponge.Schematic.HEIGHT).get();
        int length = view.getShort(Constants.Sponge.Schematic.LENGTH).get();
        if (width > MAX_SIZE || height > MAX_SIZE || length > MAX_SIZE) {
            throw new InvalidDataException(String.format(
                    "Schematic is larger than maximum allowable size (found: (%d, %d, %d) max: (%d, %<d, %<d)", width, height, length, MAX_SIZE));
        }
        int offsetX = view.getInt(Constants.Sponge.Schematic.Legacy.WE_OFFSET_X).orElse(0);
        int offsetY = view.getInt(Constants.Sponge.Schematic.Legacy.WE_OFFSET_Y).orElse(0);
        int offsetZ = view.getInt(Constants.Sponge.Schematic.Legacy.WE_OFFSET_Z).orElse(0);
        Palette<BlockState> palette = GlobalPalette.getBlockPalette();
        final SpongeSchematicBuilder builder = new SpongeSchematicBuilder();
        ArrayMutableBlockBuffer buffer = new ArrayMutableBlockBuffer(new Vector3i(-offsetX, -offsetY, -offsetZ),
                new Vector3i(width, height, length));
        byte[] block_ids = (byte[]) view.get(Constants.Sponge.Schematic.Legacy.BLOCKS).get();
        byte[] block_data = (byte[]) view.get(Constants.Sponge.Schematic.Legacy.BLOCK_DATA).get();
        byte[] add_block = (byte[]) view.get(Constants.Sponge.Schematic.Legacy.ADD_BLOCKS).orElse(null);
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
                        blockState = Optional.of(((BlockType) Block.field_149771_c.func_148754_a(default_state_id)).getDefaultState());
                    }
                    BlockState block = blockState.orElseGet(BlockTypes.COBBLESTONE::getDefaultState);
                    buffer.setBlock(x - offsetX, y - offsetY, z - offsetZ, block);
                }
            }
        }
        Map<Vector3i, TileEntityArchetype> tiles = Maps.newHashMap();
        List<DataView> tiledata = view.getViewList(Constants.Sponge.Schematic.Legacy.TILE_ENTITIES).orElse(null);
        if (tiledata != null) {
            for (DataView tile : tiledata) {
                int x = tile.getInt(Constants.Sponge.Schematic.Legacy.X_POS).get();
                int y = tile.getInt(Constants.Sponge.Schematic.Legacy.Y_POS).get();
                int z = tile.getInt(Constants.Sponge.Schematic.Legacy.Z_POS).get();
                final String tileType = tile.getString(TILE_ID).get();
                final ResourceLocation name = new ResourceLocation(tileType);
                TileEntityType type = TileEntityTypeRegistryModule.getInstance()
                        .getForClass(TileEntityAccessor.accessor$getRegistry().func_82594_a(name));
                final BlockState state = buffer.getBlock(x - offsetX, y - offsetY, z - offsetZ);
                // Somehow we need to get some DataFixers in here, because some data may be legacy from older versions before data
                // fixers.
                final DataView upgraded;

                CompoundNBT tileNbt = NbtTranslator.getInstance().translate(tile);
                tileNbt = VANILLA_FIXER.func_188251_a(FixTypes.BLOCK_ENTITY, tileNbt, 0);
                upgraded = NbtTranslator.getInstance().translate(tileNbt);

                if (type!= null && SpongeImplHooks.hasBlockTileEntity(((Block) state.getType()), (net.minecraft.block.BlockState) state)) {
                    TileEntityArchetype archetype = new SpongeTileEntityArchetypeBuilder()
                        .state(state)
                        .tileData(upgraded)
                        .tile(type)
                        .build();
                    tiles.put(new Vector3i(x - offsetX, y - offsetY, z - offsetZ), archetype);
                }
            }
        }
        final List<EntityArchetype> entities = new ArrayList<>();
        view.getViewList(Constants.Sponge.Schematic.Legacy.ENTITIES).ifPresent(entityViews -> {
            for (DataView entity : entityViews) {
                int x = entity.getInt(Constants.DataSerializers.X_POS).get();
                int y = entity.getInt(Constants.DataSerializers.Y_POS).get();
                int z = entity.getInt(Constants.DataSerializers.Z_POS).get();
                final String entityType = entity.getString(Constants.Sponge.Schematic.Legacy.ENTITY_ID).get();
                final ResourceLocation name = new ResourceLocation(entityType);
                EntityType type = EntityTypeRegistryModule.getInstance().getById(entityType).orElse(null);
                if (type != null) {
                    final DataView upgraded;

                    CompoundNBT entityNbt = NbtTranslator.getInstance().translate(entity);
                    entityNbt = VANILLA_FIXER.func_188251_a(FixTypes.ENTITY, entityNbt, 0);
                    upgraded = NbtTranslator.getInstance().translate(entityNbt);
                    upgraded.set(Queries.POSITION, new Vector3i(x - offsetX, y - offsetY, z - offsetZ));
                    final EntityArchetype build = new SpongeEntityArchetypeBuilder().type(type).entityData(upgraded).build();
                    entities.add(build);


                }
            }
        });
        if (!entities.isEmpty()) {
            builder.entities(entities);
        }
        builder.blockPalette(GlobalPalette.getBlockPalette());
        builder.blocks(buffer)
            .tiles(tiles);
        return builder.build();
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
        data.set(Constants.Sponge.Schematic.WIDTH, width);
        data.set(Constants.Sponge.Schematic.HEIGHT, height);
        data.set(Constants.Sponge.Schematic.LENGTH, length);
        data.set(Constants.Sponge.Schematic.Legacy.MATERIALS, "Alpha");
        // These are added for better interop with WorldEdit
        data.set(Constants.Sponge.Schematic.Legacy.WE_OFFSET_X, -xMin);
        data.set(Constants.Sponge.Schematic.Legacy.WE_OFFSET_Y, -yMin);
        data.set(Constants.Sponge.Schematic.Legacy.WE_OFFSET_Z, -zMin);
        SaveIterator itr = new SaveIterator(width, height, length);
        schematic.getBlockWorker().iterate(itr);
        byte[] blockids = itr.blockids;
        byte[] extraids = itr.extraids;
        byte[] blockdata = itr.blockdata;
        data.set(Constants.Sponge.Schematic.Legacy.BLOCKS, blockids);
        data.set(Constants.Sponge.Schematic.Legacy.BLOCK_DATA, blockdata);
        if (extraids != null) {
            data.set(Constants.Sponge.Schematic.Legacy.ADD_BLOCKS, extraids);
        }
        List<DataView> tileEntities = Lists.newArrayList();
        for (Map.Entry<Vector3i, TileEntityArchetype> entry : schematic.getTileEntityArchetypes().entrySet()) {
            Vector3i pos = entry.getKey();
            DataContainer tiledata = entry.getValue().getTileData();
            tiledata.set(Constants.DataSerializers.X_POS, pos.getX() - xMin);
            tiledata.set(Constants.DataSerializers.Y_POS, pos.getY() - yMin);
            tiledata.set(Constants.DataSerializers.Z_POS, pos.getZ() - zMin);
            tileEntities.add(tiledata);
        }
        data.set(Constants.Sponge.Schematic.Legacy.TILE_ENTITIES, tileEntities);
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
            int id = GlobalPalette.getBlockPalette().get(volume.getBlock(x, y, z)).get();
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
