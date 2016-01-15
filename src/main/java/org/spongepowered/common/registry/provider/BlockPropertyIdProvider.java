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
package org.spongepowered.common.registry.provider;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.CaseFormat;
import net.minecraft.block.*;
import net.minecraft.block.properties.IProperty;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.common.registry.TypeProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Optional;

public class BlockPropertyIdProvider implements TypeProvider<IProperty<?>, String> {

    private final IdentityHashMap<IProperty<?>, String> propertyIdMap = new IdentityHashMap<>();
    private final HashMap<String, IProperty<?>> idPropertyMap = new HashMap<>();

    public static BlockPropertyIdProvider getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Optional<String> get(IProperty<?> key) {
        return Optional.ofNullable(this.propertyIdMap.get(checkNotNull(key, "Property cannot be null!")));
    }

    @Override
    public Optional<IProperty<?>> getKey(String value) {
        return Optional.ofNullable(this.idPropertyMap.get(checkNotNull(value, "Id cannot be null!").toLowerCase()));
    }

    private boolean isRegistered(IProperty<?> property) {
        return this.propertyIdMap.containsKey(property);
    }

    public static String getIdAndTryRegistration(IProperty<?> property, Block block, String blockId) {
        BlockPropertyIdProvider instance = getInstance();
        checkNotNull(property, "Property is null! Cannot retrieve a registration for a null property!");
        checkNotNull(block, "Block cannot be null!");
        checkNotNull(blockId, "Block id cannot be null!");
        checkArgument(!blockId.isEmpty(), "Block id cannot be empty!");
        if (instance.isRegistered(property)) {
            return instance.propertyIdMap.get(property);
        } else {
            try { // Seriously, don't look past this try state. just continue on with your day...
                  // I warned you...
                final String originalClass = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, block.getClass().getSimpleName());
                Class<?> blockClass = block.getClass();
                while (true) {
                    if (blockClass == Object.class) {
                        final String propertyId = originalClass + "_" + property.getName();
                        LogManager.getLogger("Sponge").warn("Could not find {} owning class, assigning fallback id: {}", property.getName(),
                                propertyId);
                        instance.register(property, propertyId);
                        return propertyId;
                    }
                    // Had enough?
                    for (Field field : blockClass.getDeclaredFields()) {
                        field.setAccessible(true);

                        final boolean isStatic = Modifier.isStatic(field.getModifiers());
                        final Object o = isStatic ? field.get(null) : field.get(block);

                        if (property != o) {
                            continue;
                        }
                        final String propertyName = ((IProperty<?>) o).getName();
                        final String className = field.getDeclaringClass().getSimpleName().replace("Block", "");
                        final String propertyClassName = isStatic ? CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className)
                                                                  : originalClass;
                        final String combinedId = propertyClassName + "_" + propertyName.toLowerCase();
                        if (instance.idPropertyMap.containsKey(combinedId)) {
                            throw new UnsupportedOperationException("Sorry! but someone has the SAME block class name and property name: "
                                                                    + combinedId);
                        }
                        instance.register(((IProperty<?>) o), combinedId);
                        return combinedId;
                    }
                    blockClass = blockClass.getSuperclass();
                }

            } catch (Exception e) {
                final String propertyId = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, blockId) + "_" + property.getName();
                LogManager.getLogger("Sponge").warn("An exception was thrown while trying to resolve the property "
                                                    + property.getName() +"'s owning class, assigning "
                                                    + "fallback id: " + propertyId, e);
                instance.register(property, propertyId);
                return propertyId;
            }
        }
    }

    private void register(IProperty<?> property, String id) {
        checkArgument(!this.propertyIdMap.containsKey(property), "Property is already registered! Property: " + property.getName()
                                                                 + " is registered as : " + this.propertyIdMap.get(property));
        this.propertyIdMap.put(property, id.toLowerCase());
        this.idPropertyMap.put(id.toLowerCase(), property);
    }

    private BlockPropertyIdProvider() {
        register(BlockRotatedPillar.AXIS, "pillar_axis");
        register(BlockDirectional.FACING, "directional_facing");
        register(BlockLog.LOG_AXIS, "log_axis");
        register(BlockNewLog.VARIANT, "new_log_variant");
        register(BlockOldLog.VARIANT, "log_variant");
        register(BlockFarmland.MOISTURE, "farmland_moisture");
        register(BlockPistonBase.FACING, "piston_facing");
        register(BlockPistonBase.EXTENDED, "piston_extended");
        register(BlockVine.NORTH, "vine_north");
        register(BlockVine.EAST, "vine_east");
        register(BlockVine.SOUTH, "vine_south");
        register(BlockVine.WEST, "vine_west");
        register(BlockVine.UP, "vine_up");
        register(BlockRedSandstone.TYPE, "red_sandstone_type");
        register(BlockLiquid.LEVEL, "liquid_level");
        register(BlockReed.AGE, "reed_age");
        register(BlockMycelium.SNOWY, "mycelium_snowy");
        register(BlockColored.COLOR, "dyed_color");
        register(BlockTorch.FACING, "torch_facing");
        register(BlockDirt.SNOWY, "dirt_snowy");
        register(BlockDirt.VARIANT, "dirt_variant");
        register(BlockEndPortalFrame.FACING, "end_portal_facing");
        register(BlockEndPortalFrame.EYE, "end_portal_eye");
        register(BlockCarpet.COLOR, "carpet_color");
        register(BlockStone.VARIANT, "stone_variant");
        register(BlockHugeMushroom.VARIANT, "huge_mushroom_variant");
        register(BlockSnow.LAYERS, "snow_layer");
        register(BlockWall.UP, "wall_up");
        register(BlockWall.NORTH, "wall_north");
        register(BlockWall.EAST, "wall_east");
        register(BlockWall.SOUTH, "wall_south");
        register(BlockWall.WEST, "wall_west");
        register(BlockWall.VARIANT, "wall_variant");
        register(BlockStairs.FACING, "stairs_facing");
        register(BlockStairs.HALF, "stairs_half");
        register(BlockStairs.SHAPE, "stairs_shape");
        register(BlockButton.FACING, "button_facing");
        register(BlockButton.POWERED, "button_powered");
        register(BlockCactus.AGE, "cactus_age");
        register(BlockCrops.AGE, "crops_age");
        register(BlockNetherWart.AGE, "nether_wart_age");
        register(BlockDoublePlant.VARIANT, "double_plant_variant");
        register(BlockDoublePlant.HALF, "double_plant_half");
        register(BlockStem.AGE, "stem_age");
        register(BlockStem.FACING, "stem_facing");
        register(BlockTallGrass.TYPE, "tall_grass_type");
        register(BlockSapling.TYPE, "sapling_type");
        register(BlockSapling.STAGE, "sapling_stage");
        register(BlockPrismarine.VARIANT, "prismarine_variant");
        register(BlockFence.NORTH, "fence_north");
        register(BlockFence.EAST, "fence_east");
        register(BlockFence.SOUTH, "fence_south");
        register(BlockFence.WEST, "fence_west");
        register(BlockSilverfish.VARIANT, "disguised_variant");
        register(BlockPane.NORTH, "pane_north");
        register(BlockPane.EAST, "pane_east");
        register(BlockPane.SOUTH, "pane_south");
        register(BlockPane.WEST, "pane_west");
        register(BlockStainedGlassPane.COLOR, "stained_dyed_color");
        register(BlockQuartz.VARIANT, "quartz_variant");
        register(BlockPistonExtension.FACING, "piston_extension_facing");
        register(BlockPistonExtension.TYPE, "piston_extension_type");
        register(BlockPistonExtension.SHORT, "piston_extension_short");
        register(BlockSandStone.TYPE, "sand_stone_type");
        register(BlockPlanks.VARIANT, "plank_variant");
        register(BlockPortal.AXIS, "portal_axis");
        register(BlockStainedGlass.COLOR, "stained_glass_color");
        register(BlockRail.SHAPE, "rail_shape");
        register(BlockRailPowered.POWERED, "powered_rail_powered");
        register(BlockRailPowered.SHAPE, "powered_rail_shape");
        register(BlockRailDetector.POWERED, "detector_rail_powered");
        register(BlockRailDetector.SHAPE, "detector_rail_shape");
        register(BlockLeaves.DECAYABLE, "leaves_decay");
        register(BlockLeaves.CHECK_DECAY, "leaves_check_decay");
        register(BlockOldLeaf.VARIANT, "old_leaves_variant");
        register(BlockNewLeaf.VARIANT, "new_leaves_variant");
        register(BlockGrass.SNOWY, "grass_snowy");
        register(BlockCauldron.LEVEL, "cauldron_level");
        register(BlockFurnace.FACING, "furnace_facing");
        register(BlockBanner.FACING, "banner_facing");
        register(BlockBanner.ROTATION, "banner_rotation");
        register(BlockCommandBlock.TRIGGERED, "command_block_triggered");
        register(BlockChest.FACING, "chest_facing");
        register(BlockSkull.FACING, "skull_facing");
        register(BlockSkull.NODROP, "skull_no_drop");
        register(BlockStandingSign.ROTATION, "standing_sign_rotation");
        register(BlockWallSign.FACING, "wall_sign_facing");
        register(BlockBrewingStand.HAS_BOTTLE[0], "brewing_stand_1_has_bottle");
        register(BlockBrewingStand.HAS_BOTTLE[1], "brewing_stand_2_has_bottle");
        register(BlockBrewingStand.HAS_BOTTLE[2], "brewing_stand_3_has_bottle");
        register(BlockHopper.ENABLED, "hopper_enabled");
        register(BlockHopper.FACING, "hopper_facing");
        register(BlockEnderChest.FACING, "ender_chest_facing");
        register(BlockFlowerPot.LEGACY_DATA, "flower_pot_legacy");
        register(BlockFlowerPot.CONTENTS, "flower_pot_contents");
        register(BlockDaylightDetector.POWER, "daylight_detector_power");
        register(BlockDispenser.FACING, "dispenser_facing");
        register(BlockDispenser.TRIGGERED, "dispenser_triggered");
        register(BlockJukebox.HAS_RECORD, "jukebox_has_record");
        register(BlockSand.VARIANT, "sand_variant");
        register(BlockAnvil.DAMAGE, "anvil_damage");
        register(BlockAnvil.FACING, "anvil_facing");
        register(BlockCake.BITES, "cake_bites");
        register(BlockFire.AGE, "fire_age");
        register(BlockFire.ALT, "fire_alt");
        register(BlockFire.NORTH, "fire_north");
        register(BlockFire.EAST, "fire_east");
        register(BlockFire.SOUTH, "fire_south");
        register(BlockFire.WEST, "fire_west");
        register(BlockFire.UPPER, "fire_upper");
        register(BlockFire.FLIP, "fire_flip");
        register(BlockSlab.HALF, "slab_half");
        register(BlockStoneSlabNew.SEAMLESS, "stone_slab_new_seamless");
        register(BlockStoneSlabNew.VARIANT, "stone_slab_new_variant");
        register(BlockStoneSlab.SEAMLESS, "stone_slab_seamless");
        register(BlockStoneSlab.VARIANT, "stone_slab_variant");
        register(BlockWoodSlab.VARIANT, "wood_slab_variant");
        register(BlockLadder.FACING, "ladder_facing");
        register(BlockSponge.WET, "sponge_wet");
        register(BlockTripWireHook.FACING, "trip_wire_hook_facing");
        register(BlockTripWireHook.ATTACHED, "trip_wire_hook_attached");
        register(BlockTripWireHook.POWERED, "trip_wire_hook_powered");
        register(BlockTripWireHook.SUSPENDED, "trip_wire_hook_suspended");
        register(BlockDoor.FACING, "door_facing");
        register(BlockDoor.OPEN, "door_open");
        register(BlockDoor.HINGE, "door_hinge");
        register(BlockDoor.POWERED, "door_powered");
        register(BlockDoor.HALF, "door_half");
        register(BlockStoneBrick.VARIANT, "stone_brick_variant");
        register(BlockLever.FACING, "lever_variant");
        register(BlockLever.POWERED, "lever_powered");
        register(BlockTNT.EXPLODE, "tnt_explode");
        register(BlockBed.PART, "bed_part");
        register(BlockBed.OCCUPIED, "bed_occupied");
        register(BlockRedstoneComparator.MODE, "comparator_mode");
        register(BlockRedstoneComparator.POWERED, "comparator_powered");
        register(BlockCocoa.AGE, "cocoa_facing");
        register(BlockFenceGate.IN_WALL, "fence_gate_in_wall");
        register(BlockFenceGate.OPEN, "fence_gate_open");
        register(BlockFenceGate.POWERED, "fence_gate_powered");
        register(BlockRedstoneWire.NORTH, "redstone_north");
        register(BlockRedstoneWire.EAST, "redstone_east");
        register(BlockRedstoneWire.SOUTH, "redstone_south");
        register(BlockRedstoneWire.WEST, "redstone_west");
        register(BlockRedstoneWire.POWER, "redstone_power");
        register(BlockTripWire.POWERED, "trip_wire_powered");
        register(BlockTripWire.SUSPENDED, "trip_wire_suspended");
        register(BlockTripWire.ATTACHED, "trip_wire_attached");
        register(BlockTripWire.DISARMED, "trip_wire_disarmed");
        register(BlockTripWire.NORTH, "trip_wire_north");
        register(BlockTripWire.EAST, "trip_wire_east");
        register(BlockTripWire.SOUTH, "trip_wire_south");
        register(BlockTripWire.WEST, "trip_wire_west");
        register(BlockPressurePlateWeighted.POWER, "weighted_pressure_plate_power");
        register(BlockPressurePlate.POWERED, "pressure_plate_power");
        register(BlockTrapDoor.FACING, "trap_door_facing");
        register(BlockTrapDoor.OPEN, "trap_door_open");
        register(BlockTrapDoor.HALF, "trap_door_half");
        register(BlockRedstoneRepeater.DELAY, "redstone_repeater_delay");
        register(BlockRedstoneRepeater.LOCKED, "redstone_repeater_locked");
    }

    private static final class Holder {
        static final BlockPropertyIdProvider INSTANCE = new BlockPropertyIdProvider();
    }
}
