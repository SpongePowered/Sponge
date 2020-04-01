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
import net.minecraft.state.IProperty;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Optional;

public class BlockPropertyIdProvider  {

    private final IdentityHashMap<IProperty<?>, String> propertyIdMap = new IdentityHashMap<>();
    private final HashMap<String, IProperty<?>> idPropertyMap = new HashMap<>();

    public static BlockPropertyIdProvider getInstance() {
        return Holder.INSTANCE;
    }

    public static String getIdFor(final IProperty<?> iProperty) {
        return getInstance().propertyIdMap.get(iProperty);
    }

    public Optional<String> get(final IProperty<?> key) {
        return Optional.ofNullable(this.propertyIdMap.get(checkNotNull(key, "Property cannot be null!")));
    }

    public Optional<IProperty<?>> getKey(final String value) {
        return Optional.ofNullable(this.idPropertyMap.get(checkNotNull(value, "Id cannot be null!").toLowerCase(Locale.ENGLISH)));
    }

    private boolean isRegistered(final IProperty<?> property) {
        return this.propertyIdMap.containsKey(property);
    }

    public static String getIdAndTryRegistration(final IProperty<?> property, final Block block, final String blockId) {
        final BlockPropertyIdProvider instance = getInstance();
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
        final String lastAttemptId = lowerCasedBlockId + "_" + property.getName();
        try { // Seriously, don't look past this try state. just continue on with your day...
              // I warned you...
            final String originalClass = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, block.getClass().getSimpleName());
            Class<?> blockClass = block.getClass();
            while (true) {
                if (blockClass == Object.class) {
                    final String propertyId = modId + ":" + originalClass + "_" + property.getName();
                    LogManager.getLogger("Sponge").warn("Could not find {} owning class, assigning fallback id: {}", property.getName(),
                            propertyId);
                    instance.register(property, propertyId);
                    return propertyId;
                }
                // Had enough?
                for (final Field field : blockClass.getDeclaredFields()) {
                    field.setAccessible(true);

                    final boolean isStatic = Modifier.isStatic(field.getModifiers());
                    final Object o = isStatic ? field.get(null) : field.get(block);

                    if (property != o) {
                        continue;
                    }
                    final String className = field.getDeclaringClass().getSimpleName().replace("Block", "").replace("block", "");
                    final String classNameId = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, className);
                    final String propertyClassName = isStatic ? classNameId : originalClass;
                    final String combinedId = modId + ":" + propertyClassName + "_" + propertyName.toLowerCase(Locale.ENGLISH);
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

        } catch (final Exception e) {
            LogManager.getLogger("Sponge").warn("An exception was thrown while trying to resolve the property "
                                                + property.getName() +"'s owning class, assigning "
                                                + "fallback id: " + lastAttemptId, e);
            instance.register(property, lastAttemptId);
            return lastAttemptId;
        }
    }

    private void register(final IProperty<?> property, final String id) {
        checkArgument(!this.propertyIdMap.containsKey(property), "Property is already registered! Property: " + property.getName()
                                                                 + " is registered as : " + this.propertyIdMap.get(property));
        this.propertyIdMap.put(property, id.toLowerCase(Locale.ENGLISH));
        this.idPropertyMap.put(id.toLowerCase(Locale.ENGLISH), property);
    }

    BlockPropertyIdProvider() {
        this.register(HorizontalBlock.HORIZONTAL_FACING, "minecraft:horizontal_facing");
        this.register(RotatedPillarBlock.AXIS, "minecraft:pillar_axis");
        this.register(DirectionalBlock.FACING, "minecraft:directional_facing");
//        this.register(LogBlock.LOG_AXIS, "minecraft:log_axis");
//        this.register(BlockNewLog.VARIANT, "minecraft:new_log_variant");
//        this.register(BlockOldLog.VARIANT, "minecraft:log_variant");
        this.register(FarmlandBlock.MOISTURE, "minecraft:farmland_moisture");
        this.register(PistonBlock.EXTENDED, "minecraft:piston_extended");
        this.register(VineBlock.NORTH, "minecraft:vine_north");
        this.register(VineBlock.EAST, "minecraft:vine_east");
        this.register(VineBlock.SOUTH, "minecraft:vine_south");
        this.register(VineBlock.WEST, "minecraft:vine_west");
        this.register(VineBlock.UP, "minecraft:vine_up");
//        this.register(BlockRedSandstone.TYPE, "minecraft:red_sandstone_type");
//        this.register(BlockLiquid.LEVEL, "minecraft:liquid_level");
        this.register(SugarCaneBlock.AGE, "minecraft:reed_age");
        this.register(MyceliumBlock.SNOWY, "minecraft:mycelium_snowy");
//        this.register(BlockColored.COLOR, "minecraft:dyed_color");
//        this.register(TorchBlock.FACING, "minecraft:torch_facing");
//        this.register(BlockDirt.SNOWY, "minecraft:dirt_snowy");
//        this.register(BlockDirt.VARIANT, "minecraft:dirt_variant");
        this.register(EndPortalFrameBlock.EYE, "minecraft:end_portal_eye");
//        this.register(CarpetBlock.COLOR, "minecraft:carpet_color");
//        this.register(BlockStone.VARIANT, "minecraft:stone_variant");
//        this.register(HugeMushroomBlock.VARIANT, "minecraft:huge_mushroom_variant");
        this.register(SnowBlock.LAYERS, "minecraft:snow_layer");
        this.register(WallBlock.UP, "minecraft:wall_up");
        this.register(WallBlock.NORTH, "minecraft:wall_north");
        this.register(WallBlock.EAST, "minecraft:wall_east");
        this.register(WallBlock.SOUTH, "minecraft:wall_south");
        this.register(WallBlock.WEST, "minecraft:wall_west");
//        this.register(WallBlock.VARIANT, "minecraft:wall_variant");
        this.register(StairsBlock.HALF, "minecraft:stairs_half");
        this.register(StairsBlock.SHAPE, "minecraft:stairs_shape");
        this.register(AbstractButtonBlock.POWERED, "minecraft:button_powered");
        this.register(CactusBlock.AGE, "minecraft:cactus_age");
        this.register(CropsBlock.AGE, "minecraft:crops_age");
        this.register(NetherWartBlock.AGE, "minecraft:nether_wart_age");
//        this.register(DoublePlantBlock.VARIANT, "minecraft:double_plant_variant");
        this.register(DoublePlantBlock.HALF, "minecraft:double_plant_half");
        this.register(StemBlock.AGE, "minecraft:stem_age");
//        this.register(TallGrassBlock.TYPE, "minecraft:tall_grass_type");
//        this.register(SaplingBlock.TYPE, "minecraft:sapling_type");
        this.register(SaplingBlock.STAGE, "minecraft:sapling_stage");
//        this.register(BlockPrismarine.VARIANT, "minecraft:prismarine_variant");
        this.register(FenceBlock.NORTH, "minecraft:fence_north");
        this.register(FenceBlock.EAST, "minecraft:fence_east");
        this.register(FenceBlock.SOUTH, "minecraft:fence_south");
        this.register(FenceBlock.WEST, "minecraft:fence_west");
//        this.register(SilverfishBlock.VARIANT, "minecraft:disguised_variant");
        this.register(PaneBlock.NORTH, "minecraft:pane_north");
        this.register(PaneBlock.EAST, "minecraft:pane_east");
        this.register(PaneBlock.SOUTH, "minecraft:pane_south");
        this.register(PaneBlock.WEST, "minecraft:pane_west");
//        this.register(StainedGlassPaneBlock.COLOR, "minecraft:stained_dyed_color");
//        this.register(BlockQuartz.VARIANT, "minecraft:quartz_variant");
        this.register(PistonHeadBlock.TYPE, "minecraft:piston_extension_type");
        this.register(PistonHeadBlock.SHORT, "minecraft:piston_extension_short");
//        this.register(BlockSandStone.TYPE, "minecraft:sand_stone_type");
//        this.register(BlockPlanks.VARIANT, "minecraft:plank_variant");
        this.register(NetherPortalBlock.AXIS, "minecraft:portal_axis");
//        this.register(StainedGlassBlock.COLOR, "minecraft:stained_glass_color");
        this.register(RailBlock.SHAPE, "minecraft:rail_shape");
        this.register(PoweredRailBlock.POWERED, "minecraft:powered_rail_powered");
        this.register(PoweredRailBlock.SHAPE, "minecraft:powered_rail_shape");
        this.register(DetectorRailBlock.POWERED, "minecraft:detector_rail_powered");
        this.register(DetectorRailBlock.SHAPE, "minecraft:detector_rail_shape");
//        this.register(LeavesBlock.DECAYABLE, "minecraft:leaves_decay");
//        this.register(LeavesBlock.CHECK_DECAY, "minecraft:leaves_check_decay");
//        this.register(BlockOldLeaf.VARIANT, "minecraft:old_leaves_variant");
//        this.register(BlockNewLeaf.VARIANT, "minecraft:new_leaves_variant");
        this.register(GrassBlock.SNOWY, "minecraft:grass_snowy");
        this.register(CauldronBlock.LEVEL, "minecraft:cauldron_level");
        this.register(BannerBlock.ROTATION, "minecraft:banner_rotation");
//        this.register(SkullBlock.NODROP, "minecraft:skull_no_drop");
        this.register(StandingSignBlock.ROTATION, "minecraft:standing_sign_rotation");
        this.register(BrewingStandBlock.HAS_BOTTLE[0], "minecraft:brewing_stand_1_has_bottle");
        this.register(BrewingStandBlock.HAS_BOTTLE[1], "minecraft:brewing_stand_2_has_bottle");
        this.register(BrewingStandBlock.HAS_BOTTLE[2], "minecraft:brewing_stand_3_has_bottle");
        this.register(HopperBlock.ENABLED, "minecraft:hopper_enabled");
        this.register(HopperBlock.FACING, "minecraft:hopper_facing");
//        this.register(FlowerPotBlock.LEGACY_DATA, "minecraft:flower_pot_legacy");
//        this.register(FlowerPotBlock.CONTENTS, "minecraft:flower_pot_contents");
        this.register(DaylightDetectorBlock.POWER, "minecraft:daylight_detector_power");
        this.register(DispenserBlock.TRIGGERED, "minecraft:dispenser_triggered");
        this.register(JukeboxBlock.HAS_RECORD, "minecraft:jukebox_has_record");
//        this.register(SandBlock.VARIANT, "minecraft:sand_variant");
//        this.register(AnvilBlock.DAMAGE, "minecraft:anvil_damage");
        this.register(CakeBlock.BITES, "minecraft:cake_bites");
        this.register(FireBlock.AGE, "minecraft:fire_age");
        this.register(FireBlock.NORTH, "minecraft:fire_north");
        this.register(FireBlock.EAST, "minecraft:fire_east");
        this.register(FireBlock.SOUTH, "minecraft:fire_south");
        this.register(FireBlock.WEST, "minecraft:fire_west");
        this.register(FireBlock.UP, "minecraft:fire_upper");
//        this.register(SlabBlock.HALF, "minecraft:slab_half");
//        this.register(BlockStoneSlabNew.SEAMLESS, "minecraft:stone_slab_new_seamless");
//        this.register(BlockStoneSlabNew.VARIANT, "minecraft:stone_slab_new_variant");
//        this.register(BlockStoneSlab.SEAMLESS, "minecraft:stone_slab_seamless");
//        this.register(BlockStoneSlab.VARIANT, "minecraft:stone_slab_variant");
//        this.register(BlockWoodSlab.VARIANT, "minecraft:wood_slab_variant");
//        this.register(SpongeBlock.WET, "minecraft:sponge_wet");
        this.register(TripWireHookBlock.ATTACHED, "minecraft:trip_wire_hook_attached");
        this.register(TripWireHookBlock.POWERED, "minecraft:trip_wire_hook_powered");
        this.register(DoorBlock.OPEN, "minecraft:door_open");
        this.register(DoorBlock.HINGE, "minecraft:door_hinge");
        this.register(DoorBlock.POWERED, "minecraft:door_powered");
        this.register(DoorBlock.HALF, "minecraft:door_half");
//        this.register(BlockStoneBrick.VARIANT, "minecraft:stone_brick_variant");
//        this.register(LeverBlock.FACING, "minecraft:lever_variant");
        this.register(LeverBlock.POWERED, "minecraft:lever_powered");
//        this.register(TNTBlock.EXPLODE, "minecraft:tnt_explode");
        this.register(BedBlock.PART, "minecraft:bed_part");
        this.register(BedBlock.OCCUPIED, "minecraft:bed_occupied");
        this.register(ComparatorBlock.MODE, "minecraft:comparator_mode");
        this.register(ComparatorBlock.POWERED, "minecraft:comparator_powered");
        this.register(CocoaBlock.AGE, "minecraft:cocoa_age");
        this.register(FenceGateBlock.IN_WALL, "minecraft:fence_gate_in_wall");
        this.register(FenceGateBlock.OPEN, "minecraft:fence_gate_open");
        this.register(FenceGateBlock.POWERED, "minecraft:fence_gate_powered");
        this.register(RedstoneWireBlock.NORTH, "minecraft:redstone_north");
        this.register(RedstoneWireBlock.EAST, "minecraft:redstone_east");
        this.register(RedstoneWireBlock.SOUTH, "minecraft:redstone_south");
        this.register(RedstoneWireBlock.WEST, "minecraft:redstone_west");
        this.register(RedstoneWireBlock.POWER, "minecraft:redstone_power");
        this.register(TripWireBlock.POWERED, "minecraft:trip_wire_powered");
        this.register(TripWireBlock.ATTACHED, "minecraft:trip_wire_attached");
        this.register(TripWireBlock.DISARMED, "minecraft:trip_wire_disarmed");
        this.register(TripWireBlock.NORTH, "minecraft:trip_wire_north");
        this.register(TripWireBlock.EAST, "minecraft:trip_wire_east");
        this.register(TripWireBlock.SOUTH, "minecraft:trip_wire_south");
        this.register(TripWireBlock.WEST, "minecraft:trip_wire_west");
        this.register(WeightedPressurePlateBlock.POWER, "minecraft:weighted_pressure_plate_power");
        this.register(PressurePlateBlock.POWERED, "minecraft:pressure_plate_power");
        this.register(TrapDoorBlock.OPEN, "minecraft:trap_door_open");
        this.register(TrapDoorBlock.HALF, "minecraft:trap_door_half");
        this.register(RepeaterBlock.DELAY, "minecraft:redstone_repeater_delay");
        this.register(RepeaterBlock.LOCKED, "minecraft:redstone_repeater_locked");
//        this.register(ConcretePowderBlock.COLOR, "minecraft:concrete_powder_color");
    }

    private static final class Holder {
        static final BlockPropertyIdProvider INSTANCE = new BlockPropertyIdProvider();
    }
}
