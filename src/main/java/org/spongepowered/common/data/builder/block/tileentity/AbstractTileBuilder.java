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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Maps;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.BrewingStandTileEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.ComparatorTileEntity;
import net.minecraft.tileentity.DaylightDetectorTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.tileentity.DropperTileEntity;
import net.minecraft.tileentity.EnchantingTableTileEntity;
import net.minecraft.tileentity.EndPortalTileEntity;
import net.minecraft.tileentity.EnderChestTileEntity;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.tileentity.HopperTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.PistonTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.World;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.util.Constants;

import java.util.Map;
import java.util.Optional;

/**
 * This is the base abstract {@link DataBuilder} for all vanilla
 * {@link TileEntity}(ies).
 *
 * @param <T> The type of sponge tile entity
 */
public abstract class AbstractTileBuilder<T extends org.spongepowered.api.block.entity.BlockEntity> extends AbstractDataBuilder<T> implements DataBuilder<T> {

    private static final Map<Class<? extends TileEntity>, BlockType> classToTypeMap = Maps.newHashMap();

    protected AbstractTileBuilder(Class<T> clazz, int version) {
        super(clazz, version);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<T> buildContent(DataView container) throws InvalidDataException {
        checkNotNull(container);
        if (!container.contains(Constants.TileEntity.TILE_TYPE, Constants.TileEntity.WORLD, Queries.POSITION_X, Queries.POSITION_Y, Queries.POSITION_Z)) {
            return Optional.empty();
        }
        String worldName = container.getString(Constants.TileEntity.WORLD).get();
        Optional<World> worldOptional = Sponge.getGame().getServer().getWorld(worldName);
        if (!worldOptional.isPresent()) {
            throw new InvalidDataException("The provided container references a world that does not exist!");
        }

        final String tile = container.getString(Constants.TileEntity.TILE_TYPE)
            .orElseThrow(() -> new InvalidDataException("Could not find TileEntityType"));
        final Class<? extends TileEntity> clazz = (Class<? extends TileEntity>) TileEntityTypeRegistryModule.getInstance().getById(tile)
            .map(BlockEntityType::getTileEntityType)
            .orElse(null);
        if (clazz == null) {
            // TODO do we want to throw an InvalidDataException since the class is not registered?
            return Optional.empty(); // basically we didn't manage to find the class and the class isn't even registered with MC
        }

        BlockType type = classToTypeMap.get(clazz);
        if (type == null) {
            return Optional.empty(); // TODO throw exception maybe?
        }
        // Now we should be ready to actually translate the TileEntity with the right block.

        final int x = container.getInt(Constants.DataSerializers.X_POS).get();
        final int y = container.getInt(Constants.DataSerializers.Y_POS).get();
        final int z = container.getInt(Constants.DataSerializers.Z_POS).get();

        worldOptional.get().getLocation(x, y, z).setBlockType(type);
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tileEntity = ((net.minecraft.world.World) worldOptional.get()).getTileEntity(pos);
        if (tileEntity == null) {
            return Optional.empty(); // TODO throw exception maybe?
        }
        // We really need to validate only after the implementing class deems it ready...
        tileEntity.remove();
        return Optional.of((T) tileEntity);
    }

    // We need these mappings for rebuilding a tile entity at the proper location.
    static {
        // These are our known block types. We need to find a way to support the mod ones
        addBlockMapping(DropperTileEntity.class, BlockTypes.DROPPER);
        addBlockMapping(ChestTileEntity.class, BlockTypes.CHEST);
        addBlockMapping(EnderChestTileEntity.class, BlockTypes.ENDER_CHEST);
        addBlockMapping(JukeboxBlock.TileEntityJukebox.class, BlockTypes.JUKEBOX);
        addBlockMapping(DispenserTileEntity.class, BlockTypes.DISPENSER);
        addBlockMapping(DropperTileEntity.class, BlockTypes.DROPPER);
        addBlockMapping(SignTileEntity.class, BlockTypes.STANDING_SIGN);
        addBlockMapping(MobSpawnerTileEntity.class, BlockTypes.MOB_SPAWNER);
        addBlockMapping(TileEntityNote.class, BlockTypes.NOTEBLOCK);
        addBlockMapping(PistonTileEntity.class, BlockTypes.PISTON);
        addBlockMapping(FurnaceTileEntity.class, BlockTypes.FURNACE);
        addBlockMapping(BrewingStandTileEntity.class, BlockTypes.BREWING_STAND);
        addBlockMapping(EnchantingTableTileEntity.class, BlockTypes.ENCHANTING_TABLE);
        addBlockMapping(EndPortalTileEntity.class, BlockTypes.END_PORTAL);
        addBlockMapping(CommandBlockTileEntity.class, BlockTypes.COMMAND_BLOCK);
        addBlockMapping(BeaconTileEntity.class, BlockTypes.BEACON);
        addBlockMapping(SkullTileEntity.class, BlockTypes.SKULL);
        addBlockMapping(DaylightDetectorTileEntity.class, BlockTypes.DAYLIGHT_DETECTOR);
        addBlockMapping(HopperTileEntity.class, BlockTypes.HOPPER);
        addBlockMapping(ComparatorTileEntity.class, BlockTypes.UNPOWERED_COMPARATOR);
        addBlockMapping(TileEntityFlowerPot.class, BlockTypes.FLOWER_POT);
        addBlockMapping(BannerTileEntity.class, BlockTypes.STANDING_BANNER);
    }

    private static void addBlockMapping(Class<? extends TileEntity> tileClass, BlockType blocktype) {
        classToTypeMap.put(tileClass, blocktype);
    }

}
