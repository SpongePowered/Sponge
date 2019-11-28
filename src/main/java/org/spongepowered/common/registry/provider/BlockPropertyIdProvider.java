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
        final String propertyName = property.func_177701_a();
        final String lastAttemptId = lowerCasedBlockId + "_" + property.func_177701_a();
        try { // Seriously, don't look past this try state. just continue on with your day...
              // I warned you...
            final String originalClass = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, block.getClass().getSimpleName());
            Class<?> blockClass = block.getClass();
            while (true) {
                if (blockClass == Object.class) {
                    final String propertyId = modId + ":" + originalClass + "_" + property.func_177701_a();
                    LogManager.getLogger("Sponge").warn("Could not find {} owning class, assigning fallback id: {}", property.func_177701_a(),
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
                                                + property.func_177701_a() +"'s owning class, assigning "
                                                + "fallback id: " + lastAttemptId, e);
            instance.register(property, lastAttemptId);
            return lastAttemptId;
        }
    }

    private void register(IProperty<?> property, String id) {
        checkArgument(!this.propertyIdMap.containsKey(property), "Property is already registered! Property: " + property.func_177701_a()
                                                                 + " is registered as : " + this.propertyIdMap.get(property));
        this.propertyIdMap.put(property, id.toLowerCase(Locale.ENGLISH));
        this.idPropertyMap.put(id.toLowerCase(Locale.ENGLISH), property);
    }

    BlockPropertyIdProvider() {
        register(BlockHorizontal.field_185512_D, "minecraft:horizontal_facing");
        register(BlockRotatedPillar.field_176298_M, "minecraft:pillar_axis");
        register(BlockDirectional.field_176387_N, "minecraft:directional_facing");
        register(BlockLog.field_176299_a, "minecraft:log_axis");
        register(BlockNewLog.field_176300_b, "minecraft:new_log_variant");
        register(BlockOldLog.field_176301_b, "minecraft:log_variant");
        register(BlockFarmland.field_176531_a, "minecraft:farmland_moisture");
        register(BlockPistonBase.field_176320_b, "minecraft:piston_extended");
        register(BlockVine.field_176273_b, "minecraft:vine_north");
        register(BlockVine.field_176278_M, "minecraft:vine_east");
        register(BlockVine.field_176279_N, "minecraft:vine_south");
        register(BlockVine.field_176280_O, "minecraft:vine_west");
        register(BlockVine.field_176277_a, "minecraft:vine_up");
        register(BlockRedSandstone.field_176336_a, "minecraft:red_sandstone_type");
        register(BlockLiquid.field_176367_b, "minecraft:liquid_level");
        register(BlockReed.field_176355_a, "minecraft:reed_age");
        register(BlockMycelium.field_176384_a, "minecraft:mycelium_snowy");
        register(BlockColored.field_176581_a, "minecraft:dyed_color");
        register(BlockTorch.field_176596_a, "minecraft:torch_facing");
        register(BlockDirt.field_176385_b, "minecraft:dirt_snowy");
        register(BlockDirt.field_176386_a, "minecraft:dirt_variant");
        register(BlockEndPortalFrame.field_176507_b, "minecraft:end_portal_eye");
        register(BlockCarpet.field_176330_a, "minecraft:carpet_color");
        register(BlockStone.field_176247_a, "minecraft:stone_variant");
        register(BlockHugeMushroom.field_176380_a, "minecraft:huge_mushroom_variant");
        register(BlockSnow.field_176315_a, "minecraft:snow_layer");
        register(BlockWall.field_176256_a, "minecraft:wall_up");
        register(BlockWall.field_176254_b, "minecraft:wall_north");
        register(BlockWall.field_176257_M, "minecraft:wall_east");
        register(BlockWall.field_176258_N, "minecraft:wall_south");
        register(BlockWall.field_176259_O, "minecraft:wall_west");
        register(BlockWall.field_176255_P, "minecraft:wall_variant");
        register(BlockStairs.field_176308_b, "minecraft:stairs_half");
        register(BlockStairs.field_176310_M, "minecraft:stairs_shape");
        register(BlockButton.field_176584_b, "minecraft:button_powered");
        register(BlockCactus.field_176587_a, "minecraft:cactus_age");
        register(BlockCrops.field_176488_a, "minecraft:crops_age");
        register(BlockNetherWart.field_176486_a, "minecraft:nether_wart_age");
        register(BlockDoublePlant.field_176493_a, "minecraft:double_plant_variant");
        register(BlockDoublePlant.field_176492_b, "minecraft:double_plant_half");
        register(BlockStem.field_176484_a, "minecraft:stem_age");
        register(BlockTallGrass.field_176497_a, "minecraft:tall_grass_type");
        register(BlockSapling.field_176480_a, "minecraft:sapling_type");
        register(BlockSapling.field_176479_b, "minecraft:sapling_stage");
        register(BlockPrismarine.field_176332_a, "minecraft:prismarine_variant");
        register(BlockFence.field_176526_a, "minecraft:fence_north");
        register(BlockFence.field_176525_b, "minecraft:fence_east");
        register(BlockFence.field_176527_M, "minecraft:fence_south");
        register(BlockFence.field_176528_N, "minecraft:fence_west");
        register(BlockSilverfish.field_176378_a, "minecraft:disguised_variant");
        register(BlockPane.field_176241_b, "minecraft:pane_north");
        register(BlockPane.field_176242_M, "minecraft:pane_east");
        register(BlockPane.field_176243_N, "minecraft:pane_south");
        register(BlockPane.field_176244_O, "minecraft:pane_west");
        register(BlockStainedGlassPane.field_176245_a, "minecraft:stained_dyed_color");
        register(BlockQuartz.field_176335_a, "minecraft:quartz_variant");
        register(BlockPistonExtension.field_176325_b, "minecraft:piston_extension_type");
        register(BlockPistonExtension.field_176327_M, "minecraft:piston_extension_short");
        register(BlockSandStone.field_176297_a, "minecraft:sand_stone_type");
        register(BlockPlanks.field_176383_a, "minecraft:plank_variant");
        register(BlockPortal.field_176550_a, "minecraft:portal_axis");
        register(BlockStainedGlass.field_176547_a, "minecraft:stained_glass_color");
        register(BlockRail.field_176565_b, "minecraft:rail_shape");
        register(BlockRailPowered.field_176569_M, "minecraft:powered_rail_powered");
        register(BlockRailPowered.field_176568_b, "minecraft:powered_rail_shape");
        register(BlockRailDetector.field_176574_M, "minecraft:detector_rail_powered");
        register(BlockRailDetector.field_176573_b, "minecraft:detector_rail_shape");
        register(BlockLeaves.field_176237_a, "minecraft:leaves_decay");
        register(BlockLeaves.field_176236_b, "minecraft:leaves_check_decay");
        register(BlockOldLeaf.field_176239_P, "minecraft:old_leaves_variant");
        register(BlockNewLeaf.field_176240_P, "minecraft:new_leaves_variant");
        register(BlockGrass.field_176498_a, "minecraft:grass_snowy");
        register(BlockCauldron.field_176591_a, "minecraft:cauldron_level");
        register(BlockBanner.field_176448_b, "minecraft:banner_rotation");
        register(BlockSkull.field_176417_b, "minecraft:skull_no_drop");
        register(BlockStandingSign.field_176413_a, "minecraft:standing_sign_rotation");
        register(BlockBrewingStand.field_176451_a[0], "minecraft:brewing_stand_1_has_bottle");
        register(BlockBrewingStand.field_176451_a[1], "minecraft:brewing_stand_2_has_bottle");
        register(BlockBrewingStand.field_176451_a[2], "minecraft:brewing_stand_3_has_bottle");
        register(BlockHopper.field_176429_b, "minecraft:hopper_enabled");
        register(BlockHopper.field_176430_a, "minecraft:hopper_facing");
        register(BlockFlowerPot.field_176444_a, "minecraft:flower_pot_legacy");
        register(BlockFlowerPot.field_176443_b, "minecraft:flower_pot_contents");
        register(BlockDaylightDetector.field_176436_a, "minecraft:daylight_detector_power");
        register(BlockDispenser.field_176440_b, "minecraft:dispenser_triggered");
        register(BlockJukebox.field_176432_a, "minecraft:jukebox_has_record");
        register(BlockSand.field_176504_a, "minecraft:sand_variant");
        register(BlockAnvil.field_176505_b, "minecraft:anvil_damage");
        register(BlockCake.field_176589_a, "minecraft:cake_bites");
        register(BlockFire.field_176543_a, "minecraft:fire_age");
        register(BlockFire.field_176545_N, "minecraft:fire_north");
        register(BlockFire.field_176546_O, "minecraft:fire_east");
        register(BlockFire.field_176541_P, "minecraft:fire_south");
        register(BlockFire.field_176539_Q, "minecraft:fire_west");
        register(BlockFire.field_176542_R, "minecraft:fire_upper");
        register(BlockSlab.field_176554_a, "minecraft:slab_half");
        register(BlockStoneSlabNew.field_176558_b, "minecraft:stone_slab_new_seamless");
        register(BlockStoneSlabNew.field_176559_M, "minecraft:stone_slab_new_variant");
        register(BlockStoneSlab.field_176555_b, "minecraft:stone_slab_seamless");
        register(BlockStoneSlab.field_176556_M, "minecraft:stone_slab_variant");
        register(BlockWoodSlab.field_176557_b, "minecraft:wood_slab_variant");
        register(BlockSponge.field_176313_a, "minecraft:sponge_wet");
        register(BlockTripWireHook.field_176265_M, "minecraft:trip_wire_hook_attached");
        register(BlockTripWireHook.field_176263_b, "minecraft:trip_wire_hook_powered");
        register(BlockDoor.field_176519_b, "minecraft:door_open");
        register(BlockDoor.field_176521_M, "minecraft:door_hinge");
        register(BlockDoor.field_176522_N, "minecraft:door_powered");
        register(BlockDoor.field_176523_O, "minecraft:door_half");
        register(BlockStoneBrick.field_176249_a, "minecraft:stone_brick_variant");
        register(BlockLever.field_176360_a, "minecraft:lever_variant");
        register(BlockLever.field_176359_b, "minecraft:lever_powered");
        register(BlockTNT.field_176246_a, "minecraft:tnt_explode");
        register(BlockBed.field_176472_a, "minecraft:bed_part");
        register(BlockBed.field_176471_b, "minecraft:bed_occupied");
        register(BlockRedstoneComparator.field_176463_b, "minecraft:comparator_mode");
        register(BlockRedstoneComparator.field_176464_a, "minecraft:comparator_powered");
        register(BlockCocoa.field_176501_a, "minecraft:cocoa_age");
        register(BlockFenceGate.field_176467_M, "minecraft:fence_gate_in_wall");
        register(BlockFenceGate.field_176466_a, "minecraft:fence_gate_open");
        register(BlockFenceGate.field_176465_b, "minecraft:fence_gate_powered");
        register(BlockRedstoneWire.field_176348_a, "minecraft:redstone_north");
        register(BlockRedstoneWire.field_176347_b, "minecraft:redstone_east");
        register(BlockRedstoneWire.field_176349_M, "minecraft:redstone_south");
        register(BlockRedstoneWire.field_176350_N, "minecraft:redstone_west");
        register(BlockRedstoneWire.field_176351_O, "minecraft:redstone_power");
        register(BlockTripWire.field_176293_a, "minecraft:trip_wire_powered");
        register(BlockTripWire.field_176294_M, "minecraft:trip_wire_attached");
        register(BlockTripWire.field_176295_N, "minecraft:trip_wire_disarmed");
        register(BlockTripWire.field_176296_O, "minecraft:trip_wire_north");
        register(BlockTripWire.field_176291_P, "minecraft:trip_wire_east");
        register(BlockTripWire.field_176289_Q, "minecraft:trip_wire_south");
        register(BlockTripWire.field_176292_R, "minecraft:trip_wire_west");
        register(BlockPressurePlateWeighted.field_176579_a, "minecraft:weighted_pressure_plate_power");
        register(BlockPressurePlate.field_176580_a, "minecraft:pressure_plate_power");
        register(BlockTrapDoor.field_176283_b, "minecraft:trap_door_open");
        register(BlockTrapDoor.field_176285_M, "minecraft:trap_door_half");
        register(BlockRedstoneRepeater.field_176410_b, "minecraft:redstone_repeater_delay");
        register(BlockRedstoneRepeater.field_176411_a, "minecraft:redstone_repeater_locked");
        register(BlockConcretePowder.field_192426_a, "minecraft:concrete_powder_color");
    }

    private static final class Holder {
        static final BlockPropertyIdProvider INSTANCE = new BlockPropertyIdProvider();
    }
}
