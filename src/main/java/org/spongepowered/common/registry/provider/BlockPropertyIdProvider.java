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
import org.spongepowered.api.CatalogKey;
import org.spongepowered.common.registry.TypeProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Optional;

public class BlockPropertyIdProvider implements TypeProvider<IProperty<?>, CatalogKey> {

    private final IdentityHashMap<IProperty<?>, CatalogKey> propertyIdMap = new IdentityHashMap<>();
    private final HashMap<CatalogKey, IProperty<?>> idPropertyMap = new HashMap<>();

    public static BlockPropertyIdProvider getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public Optional<CatalogKey> get(IProperty<?> key) {
        return Optional.ofNullable(this.propertyIdMap.get(checkNotNull(key, "Property cannot be null!")));
    }

    @Override
    public Optional<IProperty<?>> getKey(CatalogKey value) {
        return Optional.ofNullable(this.idPropertyMap.get(checkNotNull(value, "Id cannot be null!")));
    }

    private boolean isRegistered(IProperty<?> property) {
        return this.propertyIdMap.containsKey(property);
    }

    public static CatalogKey getIdAndTryRegistration(IProperty<?> property, Block block, String blockId) {
        BlockPropertyIdProvider instance = getInstance();
        checkNotNull(property, "Property is null! Cannot retrieve a registration for a null property!");
        checkNotNull(block, "Block cannot be null!");
        checkNotNull(blockId, "Block id cannot be null!");
        checkArgument(!blockId.isEmpty(), "Block id cannot be empty!");
        if (instance.isRegistered(property)) {
            return instance.propertyIdMap.get(property);
        }
        final String lowerCasedBlockId = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, blockId);
        final String modId = lowerCasedBlockId.split(":")[0];
        final String propertyName = property.getName();
        final CatalogKey lastAttemptId = CatalogKey.of(modId, lowerCasedBlockId + "_" + property.getName());
        try { // Seriously, don't look past this try state. just continue on with your day...
              // I warned you...
            final String originalClass = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, block.getClass().getSimpleName());
            Class<?> blockClass = block.getClass();
            while (true) {
                if (blockClass == Object.class) {
                    final CatalogKey propertyId = CatalogKey.of(modId, originalClass + "_" + property.getName());
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
                    final String className = field.getDeclaringClass().getSimpleName().replace("Block", "").replace("block", "");
                    final String classNameId = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
                    final String propertyClassName = isStatic ? classNameId : originalClass;
                    final CatalogKey combinedId = CatalogKey.of(modId, propertyClassName + "_" + propertyName.toLowerCase(Locale.ENGLISH));
                    if (instance.idPropertyMap.containsKey(combinedId)) {
                        // in this case, we really do have to fall back on the full block id...
                        if (instance.idPropertyMap.containsKey(lastAttemptId)) {
                            // we really are screwed...
                            throw new IllegalArgumentException("Sorry! Someone is trying to re-register a block with the same property instances of"
                                                               + "block: " + blockId + " , with property: " + propertyName);
                        }
                        instance.register((IProperty<?>) o, lastAttemptId);
                        return lastAttemptId;
                    }
                    instance.register(((IProperty<?>) o), combinedId);
                    return combinedId;
                }
                blockClass = blockClass.getSuperclass();
            }

        } catch (Exception e) {
            LogManager.getLogger("Sponge").warn("An exception was thrown while trying to resolve the property "
                                                + property.getName() +"'s owning class, assigning "
                                                + "fallback id: " + lastAttemptId, e);
            instance.register(property, lastAttemptId);
            return lastAttemptId;
        }
    }

    private void register(IProperty<?> property, CatalogKey id) {
        checkArgument(!this.propertyIdMap.containsKey(property), "Property is already registered! Property: " + property.getName()
                                                                 + " is registered as : " + this.propertyIdMap.get(property));
        this.propertyIdMap.put(property, id);
        this.idPropertyMap.put(id, property);
    }

    BlockPropertyIdProvider() {
        register(BlockHorizontal.FACING, CatalogKey.minecraft("horizontal_facing"));
        register(BlockRotatedPillar.AXIS, CatalogKey.minecraft("pillar_axis"));
        register(BlockDirectional.FACING, CatalogKey.minecraft("directional_facing"));
        register(BlockLog.LOG_AXIS, CatalogKey.minecraft("log_axis"));
        register(BlockNewLog.VARIANT, CatalogKey.minecraft("new_log_variant"));
        register(BlockOldLog.VARIANT, CatalogKey.minecraft("log_variant"));
        register(BlockFarmland.MOISTURE, CatalogKey.minecraft("farmland_moisture"));
        register(BlockPistonBase.EXTENDED, CatalogKey.minecraft("piston_extended"));
        register(BlockVine.NORTH, CatalogKey.minecraft("vine_north"));
        register(BlockVine.EAST, CatalogKey.minecraft("vine_east"));
        register(BlockVine.SOUTH, CatalogKey.minecraft("vine_south"));
        register(BlockVine.WEST, CatalogKey.minecraft("vine_west"));
        register(BlockVine.UP, CatalogKey.minecraft("vine_up"));
        register(BlockRedSandstone.TYPE, CatalogKey.minecraft("red_sandstone_type"));
        register(BlockLiquid.LEVEL, CatalogKey.minecraft("liquid_level"));
        register(BlockReed.AGE, CatalogKey.minecraft("reed_age"));
        register(BlockMycelium.SNOWY, CatalogKey.minecraft("mycelium_snowy"));
        register(BlockColored.COLOR, CatalogKey.minecraft("dyed_color"));
        register(BlockTorch.FACING, CatalogKey.minecraft("torch_facing"));
        register(BlockDirt.SNOWY, CatalogKey.minecraft("dirt_snowy"));
        register(BlockDirt.VARIANT, CatalogKey.minecraft("dirt_variant"));
        register(BlockEndPortalFrame.EYE, CatalogKey.minecraft("end_portal_eye"));
        register(BlockCarpet.COLOR, CatalogKey.minecraft("carpet_color"));
        register(BlockStone.VARIANT, CatalogKey.minecraft("stone_variant"));
        register(BlockHugeMushroom.VARIANT, CatalogKey.minecraft("huge_mushroom_variant"));
        register(BlockSnow.LAYERS, CatalogKey.minecraft("snow_layer"));
        register(BlockWall.UP, CatalogKey.minecraft("wall_up"));
        register(BlockWall.NORTH, CatalogKey.minecraft("wall_north"));
        register(BlockWall.EAST, CatalogKey.minecraft("wall_east"));
        register(BlockWall.SOUTH, CatalogKey.minecraft("wall_south"));
        register(BlockWall.WEST, CatalogKey.minecraft("wall_west"));
        register(BlockWall.VARIANT, CatalogKey.minecraft("wall_variant"));
        register(BlockStairs.HALF, CatalogKey.minecraft("stairs_half"));
        register(BlockStairs.SHAPE, CatalogKey.minecraft("stairs_shape"));
        register(BlockButton.POWERED, CatalogKey.minecraft("button_powered"));
        register(BlockCactus.AGE, CatalogKey.minecraft("cactus_age"));
        register(BlockCrops.AGE, CatalogKey.minecraft("crops_age"));
        register(BlockNetherWart.AGE, CatalogKey.minecraft("nether_wart_age"));
        register(BlockDoublePlant.VARIANT, CatalogKey.minecraft("double_plant_variant"));
        register(BlockDoublePlant.HALF, CatalogKey.minecraft("double_plant_half"));
        register(BlockStem.AGE, CatalogKey.minecraft("stem_age"));
        register(BlockTallGrass.TYPE, CatalogKey.minecraft("tall_grass_type"));
        register(BlockSapling.TYPE, CatalogKey.minecraft("sapling_type"));
        register(BlockSapling.STAGE, CatalogKey.minecraft("sapling_stage"));
        register(BlockPrismarine.VARIANT, CatalogKey.minecraft("prismarine_variant"));
        register(BlockFence.NORTH, CatalogKey.minecraft("fence_north"));
        register(BlockFence.EAST, CatalogKey.minecraft("fence_east"));
        register(BlockFence.SOUTH, CatalogKey.minecraft("fence_south"));
        register(BlockFence.WEST, CatalogKey.minecraft("fence_west"));
        register(BlockSilverfish.VARIANT, CatalogKey.minecraft("disguised_variant"));
        register(BlockPane.NORTH, CatalogKey.minecraft("pane_north"));
        register(BlockPane.EAST, CatalogKey.minecraft("pane_east"));
        register(BlockPane.SOUTH, CatalogKey.minecraft("pane_south"));
        register(BlockPane.WEST, CatalogKey.minecraft("pane_west"));
        register(BlockStainedGlassPane.COLOR, CatalogKey.minecraft("stained_dyed_color"));
        register(BlockQuartz.VARIANT, CatalogKey.minecraft("quartz_variant"));
        register(BlockPistonExtension.TYPE, CatalogKey.minecraft("piston_extension_type"));
        register(BlockPistonExtension.SHORT, CatalogKey.minecraft("piston_extension_short"));
        register(BlockSandStone.TYPE, CatalogKey.minecraft("sand_stone_type"));
        register(BlockPlanks.VARIANT, CatalogKey.minecraft("plank_variant"));
        register(BlockPortal.AXIS, CatalogKey.minecraft("portal_axis"));
        register(BlockStainedGlass.COLOR, CatalogKey.minecraft("stained_glass_color"));
        register(BlockRail.SHAPE, CatalogKey.minecraft("rail_shape"));
        register(BlockRailPowered.POWERED, CatalogKey.minecraft("powered_rail_powered"));
        register(BlockRailPowered.SHAPE, CatalogKey.minecraft("powered_rail_shape"));
        register(BlockRailDetector.POWERED, CatalogKey.minecraft("detector_rail_powered"));
        register(BlockRailDetector.SHAPE, CatalogKey.minecraft("detector_rail_shape"));
        register(BlockLeaves.DECAYABLE, CatalogKey.minecraft("leaves_decay"));
        register(BlockLeaves.CHECK_DECAY, CatalogKey.minecraft("leaves_check_decay"));
        register(BlockOldLeaf.VARIANT, CatalogKey.minecraft("old_leaves_variant"));
        register(BlockNewLeaf.VARIANT, CatalogKey.minecraft("new_leaves_variant"));
        register(BlockGrass.SNOWY, CatalogKey.minecraft("grass_snowy"));
        register(BlockCauldron.LEVEL, CatalogKey.minecraft("cauldron_level"));
        register(BlockBanner.ROTATION, CatalogKey.minecraft("banner_rotation"));
        register(BlockSkull.NODROP, CatalogKey.minecraft("skull_no_drop"));
        register(BlockStandingSign.ROTATION, CatalogKey.minecraft("standing_sign_rotation"));
        register(BlockBrewingStand.HAS_BOTTLE[0], CatalogKey.minecraft("brewing_stand_1_has_bottle"));
        register(BlockBrewingStand.HAS_BOTTLE[1], CatalogKey.minecraft("brewing_stand_2_has_bottle"));
        register(BlockBrewingStand.HAS_BOTTLE[2], CatalogKey.minecraft("brewing_stand_3_has_bottle"));
        register(BlockHopper.ENABLED, CatalogKey.minecraft("hopper_enabled"));
        register(BlockHopper.FACING, CatalogKey.minecraft("hopper_facing"));
        register(BlockFlowerPot.LEGACY_DATA, CatalogKey.minecraft("flower_pot_legacy"));
        register(BlockFlowerPot.CONTENTS, CatalogKey.minecraft("flower_pot_contents"));
        register(BlockDaylightDetector.POWER, CatalogKey.minecraft("daylight_detector_power"));
        register(BlockDispenser.TRIGGERED, CatalogKey.minecraft("dispenser_triggered"));
        register(BlockJukebox.HAS_RECORD, CatalogKey.minecraft("jukebox_has_record"));
        register(BlockSand.VARIANT, CatalogKey.minecraft("sand_variant"));
        register(BlockAnvil.DAMAGE, CatalogKey.minecraft("anvil_damage"));
        register(BlockCake.BITES, CatalogKey.minecraft("cake_bites"));
        register(BlockFire.AGE, CatalogKey.minecraft("fire_age"));
        register(BlockFire.NORTH, CatalogKey.minecraft("fire_north"));
        register(BlockFire.EAST, CatalogKey.minecraft("fire_east"));
        register(BlockFire.SOUTH, CatalogKey.minecraft("fire_south"));
        register(BlockFire.WEST, CatalogKey.minecraft("fire_west"));
        register(BlockFire.UPPER, CatalogKey.minecraft("fire_upper"));
        register(BlockSlab.HALF, CatalogKey.minecraft("slab_half"));
        register(BlockStoneSlabNew.SEAMLESS, CatalogKey.minecraft("stone_slab_new_seamless"));
        register(BlockStoneSlabNew.VARIANT, CatalogKey.minecraft("stone_slab_new_variant"));
        register(BlockStoneSlab.SEAMLESS, CatalogKey.minecraft("stone_slab_seamless"));
        register(BlockStoneSlab.VARIANT, CatalogKey.minecraft("stone_slab_variant"));
        register(BlockWoodSlab.VARIANT, CatalogKey.minecraft("wood_slab_variant"));
        register(BlockSponge.WET, CatalogKey.minecraft("sponge_wet"));
        register(BlockTripWireHook.ATTACHED, CatalogKey.minecraft("trip_wire_hook_attached"));
        register(BlockTripWireHook.POWERED, CatalogKey.minecraft("trip_wire_hook_powered"));
        register(BlockDoor.OPEN, CatalogKey.minecraft("door_open"));
        register(BlockDoor.HINGE, CatalogKey.minecraft("door_hinge"));
        register(BlockDoor.POWERED, CatalogKey.minecraft("door_powered"));
        register(BlockDoor.HALF, CatalogKey.minecraft("door_half"));
        register(BlockStoneBrick.VARIANT, CatalogKey.minecraft("stone_brick_variant"));
        register(BlockLever.FACING, CatalogKey.minecraft("lever_variant"));
        register(BlockLever.POWERED, CatalogKey.minecraft("lever_powered"));
        register(BlockTNT.EXPLODE, CatalogKey.minecraft("tnt_explode"));
        register(BlockBed.PART, CatalogKey.minecraft("bed_part"));
        register(BlockBed.OCCUPIED, CatalogKey.minecraft("bed_occupied"));
        register(BlockRedstoneComparator.MODE, CatalogKey.minecraft("comparator_mode"));
        register(BlockRedstoneComparator.POWERED, CatalogKey.minecraft("comparator_powered"));
        register(BlockCocoa.AGE, CatalogKey.minecraft("cocoa_age"));
        register(BlockFenceGate.IN_WALL, CatalogKey.minecraft("fence_gate_in_wall"));
        register(BlockFenceGate.OPEN, CatalogKey.minecraft("fence_gate_open"));
        register(BlockFenceGate.POWERED, CatalogKey.minecraft("fence_gate_powered"));
        register(BlockRedstoneWire.NORTH, CatalogKey.minecraft("redstone_north"));
        register(BlockRedstoneWire.EAST, CatalogKey.minecraft("redstone_east"));
        register(BlockRedstoneWire.SOUTH, CatalogKey.minecraft("redstone_south"));
        register(BlockRedstoneWire.WEST, CatalogKey.minecraft("redstone_west"));
        register(BlockRedstoneWire.POWER, CatalogKey.minecraft("redstone_power"));
        register(BlockTripWire.POWERED, CatalogKey.minecraft("trip_wire_powered"));
        register(BlockTripWire.ATTACHED, CatalogKey.minecraft("trip_wire_attached"));
        register(BlockTripWire.DISARMED, CatalogKey.minecraft("trip_wire_disarmed"));
        register(BlockTripWire.NORTH, CatalogKey.minecraft("trip_wire_north"));
        register(BlockTripWire.EAST, CatalogKey.minecraft("trip_wire_east"));
        register(BlockTripWire.SOUTH, CatalogKey.minecraft("trip_wire_south"));
        register(BlockTripWire.WEST, CatalogKey.minecraft("trip_wire_west"));
        register(BlockPressurePlateWeighted.POWER, CatalogKey.minecraft("weighted_pressure_plate_power"));
        register(BlockPressurePlate.POWERED, CatalogKey.minecraft("pressure_plate_power"));
        register(BlockTrapDoor.OPEN, CatalogKey.minecraft("trap_door_open"));
        register(BlockTrapDoor.HALF, CatalogKey.minecraft("trap_door_half"));
        register(BlockRedstoneRepeater.DELAY, CatalogKey.minecraft("redstone_repeater_delay"));
        register(BlockRedstoneRepeater.LOCKED, CatalogKey.minecraft("redstone_repeater_locked"));
        register(BlockConcretePowder.COLOR, CatalogKey.minecraft("concrete_powder_color"));
    }

    private static final class Holder {
        static final BlockPropertyIdProvider INSTANCE = new BlockPropertyIdProvider();
    }
}
