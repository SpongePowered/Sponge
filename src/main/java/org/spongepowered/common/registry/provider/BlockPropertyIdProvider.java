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
import java.util.Locale;
import java.util.Optional;

public class BlockPropertyIdProvider implements TypeProvider<IProperty<?>, String> {

    private final IdentityHashMap<IProperty<?>, String> propertyIdMap = new IdentityHashMap<>();
    private final HashMap<String, IProperty<?>> idPropertyMap = new HashMap<>();

    public static BlockPropertyIdProvider getInstance() {
        return Holder.INSTANCE;
    }

    public static String getIdFor(IProperty<?> iProperty) {
        return getInstance().propertyIdMap.get(iProperty);
    }

    @Override
    public Optional<String> get(IProperty<?> key) {
        return Optional.ofNullable(this.propertyIdMap.get(checkNotNull(key, "Property cannot be null!")));
    }

    @Override
    public Optional<IProperty<?>> getKey(String value) {
        return Optional.ofNullable(this.idPropertyMap.get(checkNotNull(value, "Id cannot be null!").toLowerCase(Locale.ENGLISH)));
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

        } catch (Exception e) {
            LogManager.getLogger("Sponge").warn("An exception was thrown while trying to resolve the property "
                                                + property.getName() +"'s owning class, assigning "
                                                + "fallback id: " + lastAttemptId, e);
            instance.register(property, lastAttemptId);
            return lastAttemptId;
        }
    }

    private void register(IProperty<?> property, String id) {
        checkArgument(!this.propertyIdMap.containsKey(property), "Property is already registered! Property: " + property.getName()
                                                                 + " is registered as : " + this.propertyIdMap.get(property));
        this.propertyIdMap.put(property, id.toLowerCase(Locale.ENGLISH));
        this.idPropertyMap.put(id.toLowerCase(Locale.ENGLISH), property);
    }

    BlockPropertyIdProvider() {
        register(HorizontalBlock.HORIZONTAL_FACING, "minecraft:horizontal_facing");
        register(RotatedPillarBlock.AXIS, "minecraft:pillar_axis");
        register(DirectionalBlock.FACING, "minecraft:directional_facing");
        register(LogBlock.LOG_AXIS, "minecraft:log_axis");
        register(BlockNewLog.VARIANT, "minecraft:new_log_variant");
        register(BlockOldLog.VARIANT, "minecraft:log_variant");
        register(FarmlandBlock.MOISTURE, "minecraft:farmland_moisture");
        register(PistonBlock.EXTENDED, "minecraft:piston_extended");
        register(VineBlock.NORTH, "minecraft:vine_north");
        register(VineBlock.EAST, "minecraft:vine_east");
        register(VineBlock.SOUTH, "minecraft:vine_south");
        register(VineBlock.WEST, "minecraft:vine_west");
        register(VineBlock.UP, "minecraft:vine_up");
        register(BlockRedSandstone.TYPE, "minecraft:red_sandstone_type");
        register(BlockLiquid.LEVEL, "minecraft:liquid_level");
        register(SugarCaneBlock.AGE, "minecraft:reed_age");
        register(MyceliumBlock.SNOWY, "minecraft:mycelium_snowy");
        register(BlockColored.COLOR, "minecraft:dyed_color");
        register(TorchBlock.FACING, "minecraft:torch_facing");
        register(BlockDirt.SNOWY, "minecraft:dirt_snowy");
        register(BlockDirt.VARIANT, "minecraft:dirt_variant");
        register(EndPortalFrameBlock.EYE, "minecraft:end_portal_eye");
        register(CarpetBlock.COLOR, "minecraft:carpet_color");
        register(BlockStone.VARIANT, "minecraft:stone_variant");
        register(HugeMushroomBlock.VARIANT, "minecraft:huge_mushroom_variant");
        register(SnowBlock.LAYERS, "minecraft:snow_layer");
        register(WallBlock.UP, "minecraft:wall_up");
        register(WallBlock.NORTH, "minecraft:wall_north");
        register(WallBlock.EAST, "minecraft:wall_east");
        register(WallBlock.SOUTH, "minecraft:wall_south");
        register(WallBlock.WEST, "minecraft:wall_west");
        register(WallBlock.VARIANT, "minecraft:wall_variant");
        register(StairsBlock.HALF, "minecraft:stairs_half");
        register(StairsBlock.SHAPE, "minecraft:stairs_shape");
        register(AbstractButtonBlock.POWERED, "minecraft:button_powered");
        register(CactusBlock.AGE, "minecraft:cactus_age");
        register(CropsBlock.AGE, "minecraft:crops_age");
        register(NetherWartBlock.AGE, "minecraft:nether_wart_age");
        register(DoublePlantBlock.VARIANT, "minecraft:double_plant_variant");
        register(DoublePlantBlock.HALF, "minecraft:double_plant_half");
        register(StemBlock.AGE, "minecraft:stem_age");
        register(TallGrassBlock.TYPE, "minecraft:tall_grass_type");
        register(SaplingBlock.TYPE, "minecraft:sapling_type");
        register(SaplingBlock.STAGE, "minecraft:sapling_stage");
        register(BlockPrismarine.VARIANT, "minecraft:prismarine_variant");
        register(FenceBlock.NORTH, "minecraft:fence_north");
        register(FenceBlock.EAST, "minecraft:fence_east");
        register(FenceBlock.SOUTH, "minecraft:fence_south");
        register(FenceBlock.WEST, "minecraft:fence_west");
        register(SilverfishBlock.VARIANT, "minecraft:disguised_variant");
        register(PaneBlock.NORTH, "minecraft:pane_north");
        register(PaneBlock.EAST, "minecraft:pane_east");
        register(PaneBlock.SOUTH, "minecraft:pane_south");
        register(PaneBlock.WEST, "minecraft:pane_west");
        register(StainedGlassPaneBlock.COLOR, "minecraft:stained_dyed_color");
        register(BlockQuartz.VARIANT, "minecraft:quartz_variant");
        register(PistonHeadBlock.TYPE, "minecraft:piston_extension_type");
        register(PistonHeadBlock.SHORT, "minecraft:piston_extension_short");
        register(BlockSandStone.TYPE, "minecraft:sand_stone_type");
        register(BlockPlanks.VARIANT, "minecraft:plank_variant");
        register(NetherPortalBlock.AXIS, "minecraft:portal_axis");
        register(StainedGlassBlock.COLOR, "minecraft:stained_glass_color");
        register(RailBlock.SHAPE, "minecraft:rail_shape");
        register(PoweredRailBlock.POWERED, "minecraft:powered_rail_powered");
        register(PoweredRailBlock.SHAPE, "minecraft:powered_rail_shape");
        register(DetectorRailBlock.POWERED, "minecraft:detector_rail_powered");
        register(DetectorRailBlock.SHAPE, "minecraft:detector_rail_shape");
        register(LeavesBlock.DECAYABLE, "minecraft:leaves_decay");
        register(LeavesBlock.CHECK_DECAY, "minecraft:leaves_check_decay");
        register(BlockOldLeaf.VARIANT, "minecraft:old_leaves_variant");
        register(BlockNewLeaf.VARIANT, "minecraft:new_leaves_variant");
        register(GrassBlock.SNOWY, "minecraft:grass_snowy");
        register(CauldronBlock.LEVEL, "minecraft:cauldron_level");
        register(BannerBlock.ROTATION, "minecraft:banner_rotation");
        register(SkullBlock.NODROP, "minecraft:skull_no_drop");
        register(StandingSignBlock.ROTATION, "minecraft:standing_sign_rotation");
        register(BrewingStandBlock.HAS_BOTTLE[0], "minecraft:brewing_stand_1_has_bottle");
        register(BrewingStandBlock.HAS_BOTTLE[1], "minecraft:brewing_stand_2_has_bottle");
        register(BrewingStandBlock.HAS_BOTTLE[2], "minecraft:brewing_stand_3_has_bottle");
        register(HopperBlock.ENABLED, "minecraft:hopper_enabled");
        register(HopperBlock.FACING, "minecraft:hopper_facing");
        register(FlowerPotBlock.LEGACY_DATA, "minecraft:flower_pot_legacy");
        register(FlowerPotBlock.CONTENTS, "minecraft:flower_pot_contents");
        register(DaylightDetectorBlock.POWER, "minecraft:daylight_detector_power");
        register(DispenserBlock.TRIGGERED, "minecraft:dispenser_triggered");
        register(JukeboxBlock.HAS_RECORD, "minecraft:jukebox_has_record");
        register(SandBlock.VARIANT, "minecraft:sand_variant");
        register(AnvilBlock.DAMAGE, "minecraft:anvil_damage");
        register(CakeBlock.BITES, "minecraft:cake_bites");
        register(FireBlock.AGE, "minecraft:fire_age");
        register(FireBlock.NORTH, "minecraft:fire_north");
        register(FireBlock.EAST, "minecraft:fire_east");
        register(FireBlock.SOUTH, "minecraft:fire_south");
        register(FireBlock.WEST, "minecraft:fire_west");
        register(FireBlock.UP, "minecraft:fire_upper");
        register(SlabBlock.HALF, "minecraft:slab_half");
        register(BlockStoneSlabNew.SEAMLESS, "minecraft:stone_slab_new_seamless");
        register(BlockStoneSlabNew.VARIANT, "minecraft:stone_slab_new_variant");
        register(BlockStoneSlab.SEAMLESS, "minecraft:stone_slab_seamless");
        register(BlockStoneSlab.VARIANT, "minecraft:stone_slab_variant");
        register(BlockWoodSlab.VARIANT, "minecraft:wood_slab_variant");
        register(SpongeBlock.WET, "minecraft:sponge_wet");
        register(TripWireHookBlock.ATTACHED, "minecraft:trip_wire_hook_attached");
        register(TripWireHookBlock.POWERED, "minecraft:trip_wire_hook_powered");
        register(DoorBlock.OPEN, "minecraft:door_open");
        register(DoorBlock.HINGE, "minecraft:door_hinge");
        register(DoorBlock.POWERED, "minecraft:door_powered");
        register(DoorBlock.HALF, "minecraft:door_half");
        register(BlockStoneBrick.VARIANT, "minecraft:stone_brick_variant");
        register(LeverBlock.FACING, "minecraft:lever_variant");
        register(LeverBlock.POWERED, "minecraft:lever_powered");
        register(TNTBlock.EXPLODE, "minecraft:tnt_explode");
        register(BedBlock.PART, "minecraft:bed_part");
        register(BedBlock.OCCUPIED, "minecraft:bed_occupied");
        register(ComparatorBlock.MODE, "minecraft:comparator_mode");
        register(ComparatorBlock.POWERED, "minecraft:comparator_powered");
        register(CocoaBlock.AGE, "minecraft:cocoa_age");
        register(FenceGateBlock.IN_WALL, "minecraft:fence_gate_in_wall");
        register(FenceGateBlock.OPEN, "minecraft:fence_gate_open");
        register(FenceGateBlock.POWERED, "minecraft:fence_gate_powered");
        register(RedstoneWireBlock.NORTH, "minecraft:redstone_north");
        register(RedstoneWireBlock.EAST, "minecraft:redstone_east");
        register(RedstoneWireBlock.SOUTH, "minecraft:redstone_south");
        register(RedstoneWireBlock.WEST, "minecraft:redstone_west");
        register(RedstoneWireBlock.POWER, "minecraft:redstone_power");
        register(TripWireBlock.POWERED, "minecraft:trip_wire_powered");
        register(TripWireBlock.ATTACHED, "minecraft:trip_wire_attached");
        register(TripWireBlock.DISARMED, "minecraft:trip_wire_disarmed");
        register(TripWireBlock.NORTH, "minecraft:trip_wire_north");
        register(TripWireBlock.EAST, "minecraft:trip_wire_east");
        register(TripWireBlock.SOUTH, "minecraft:trip_wire_south");
        register(TripWireBlock.WEST, "minecraft:trip_wire_west");
        register(WeightedPressurePlateBlock.POWER, "minecraft:weighted_pressure_plate_power");
        register(PressurePlateBlock.POWERED, "minecraft:pressure_plate_power");
        register(TrapDoorBlock.OPEN, "minecraft:trap_door_open");
        register(TrapDoorBlock.HALF, "minecraft:trap_door_half");
        register(RepeaterBlock.DELAY, "minecraft:redstone_repeater_delay");
        register(RepeaterBlock.LOCKED, "minecraft:redstone_repeater_locked");
        register(ConcretePowderBlock.COLOR, "minecraft:concrete_powder_color");
    }

    private static final class Holder {
        static final BlockPropertyIdProvider INSTANCE = new BlockPropertyIdProvider();
    }
}
