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
import net.minecraft.block.BlockJukebox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.DataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
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
public abstract class AbstractTileBuilder<T extends org.spongepowered.api.block.tileentity.TileEntity> extends AbstractDataBuilder<T> implements DataBuilder<T> {

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
            .map(TileEntityType::getTileEntityType)
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
        TileEntity tileEntity = ((net.minecraft.world.World) worldOptional.get()).func_175625_s(pos);
        if (tileEntity == null) {
            return Optional.empty(); // TODO throw exception maybe?
        }
        // We really need to validate only after the implementing class deems it ready...
        tileEntity.func_145843_s();
        return Optional.of((T) tileEntity);
    }

    // We need these mappings for rebuilding a tile entity at the proper location.
    static {
        // These are our known block types. We need to find a way to support the mod ones
        addBlockMapping(TileEntityDropper.class, BlockTypes.DROPPER);
        addBlockMapping(TileEntityChest.class, BlockTypes.CHEST);
        addBlockMapping(TileEntityEnderChest.class, BlockTypes.ENDER_CHEST);
        addBlockMapping(BlockJukebox.TileEntityJukebox.class, BlockTypes.JUKEBOX);
        addBlockMapping(TileEntityDispenser.class, BlockTypes.DISPENSER);
        addBlockMapping(TileEntityDropper.class, BlockTypes.DROPPER);
        addBlockMapping(TileEntitySign.class, BlockTypes.STANDING_SIGN);
        addBlockMapping(TileEntityMobSpawner.class, BlockTypes.MOB_SPAWNER);
        addBlockMapping(TileEntityNote.class, BlockTypes.NOTEBLOCK);
        addBlockMapping(TileEntityPiston.class, BlockTypes.PISTON);
        addBlockMapping(TileEntityFurnace.class, BlockTypes.FURNACE);
        addBlockMapping(TileEntityBrewingStand.class, BlockTypes.BREWING_STAND);
        addBlockMapping(TileEntityEnchantmentTable.class, BlockTypes.ENCHANTING_TABLE);
        addBlockMapping(TileEntityEndPortal.class, BlockTypes.END_PORTAL);
        addBlockMapping(TileEntityCommandBlock.class, BlockTypes.COMMAND_BLOCK);
        addBlockMapping(TileEntityBeacon.class, BlockTypes.BEACON);
        addBlockMapping(TileEntitySkull.class, BlockTypes.SKULL);
        addBlockMapping(TileEntityDaylightDetector.class, BlockTypes.DAYLIGHT_DETECTOR);
        addBlockMapping(TileEntityHopper.class, BlockTypes.HOPPER);
        addBlockMapping(TileEntityComparator.class, BlockTypes.UNPOWERED_COMPARATOR);
        addBlockMapping(TileEntityFlowerPot.class, BlockTypes.FLOWER_POT);
        addBlockMapping(TileEntityBanner.class, BlockTypes.STANDING_BANNER);
    }

    private static void addBlockMapping(Class<? extends TileEntity> tileClass, BlockType blocktype) {
        classToTypeMap.put(tileClass, blocktype);
    }

}
