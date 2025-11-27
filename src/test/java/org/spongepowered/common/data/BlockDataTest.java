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
package org.spongepowered.common.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.AttachmentSurfaces;
import org.spongepowered.api.data.type.ComparatorModes;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.InstrumentTypes;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.data.type.PistonTypes;
import org.spongepowered.api.data.type.PortionTypes;
import org.spongepowered.api.data.type.RailDirections;
import org.spongepowered.api.data.type.SlabPortions;
import org.spongepowered.api.data.type.StairShapes;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.type.WireAttachmentTypes;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.api.world.DefaultWorldKeys;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class BlockDataTest {
    private ServerLocation location;

    @BeforeEach
    public void prepare() {
        this.location = Sponge.server().worldManager().world(DefaultWorldKeys.DEFAULT).get().location(Vector3d.ZERO);
    }

    @Test
    public void testLever() {
        final BlockState lever = BlockTypes.LEVER.get().defaultState();
        DataTest.checkWithData(lever, Keys.ATTACHMENT_SURFACE, AttachmentSurfaces.WALL.get());
        DataTest.checkWithData(lever, Keys.ATTACHMENT_SURFACE, AttachmentSurfaces.FLOOR.get());
    }

    @Test
    public void testLog() {
        final BlockState log = BlockTypes.OAK_LOG.get().defaultState();
        DataTest.checkWithData(log, Keys.AXIS, Axis.Y);
        DataTest.checkWithData(log, Keys.AXIS, Axis.X);
    }

    @Test
    public void testBlastResistance() {
        DataTest.checkGetData(BlockTypes.OBSIDIAN.get().defaultState(), Keys.BLAST_RESISTANCE, 1200.0);
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.BLAST_RESISTANCE, 0.5);
        DataTest.checkGetData(BlockTypes.BRICKS.get().defaultState(), Keys.BLAST_RESISTANCE, 6.0);
    }

    @Test
    public void testDestroySpeed() {
        DataTest.checkGetData(BlockTypes.OBSIDIAN.get().defaultState(), Keys.DESTROY_SPEED, 50.0);
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.DESTROY_SPEED, 0.5);
        DataTest.checkGetData(BlockTypes.BRICKS.get().defaultState(), Keys.DESTROY_SPEED, 2.0);
    }

    @Test
    public void testComparator() {
        final BlockState comparator = BlockTypes.COMPARATOR.get().defaultState();
        DataTest.checkGetData(comparator, Keys.COMPARATOR_MODE, ComparatorModes.COMPARE.get());
        DataTest.checkWithData(comparator, Keys.COMPARATOR_MODE, ComparatorModes.SUBTRACT.get());
    }

    @Test
    public void testHopper() {
        this.location.setBlock(BlockTypes.HOPPER.get().defaultState());
        final BlockEntity hopper = this.location.blockEntity().get();
        DataTest.checkGetData(hopper, Keys.COOLDOWN, Ticks.zero());
        DataTest.checkOfferData(hopper, Keys.COOLDOWN, Ticks.of(10));
    }

    @Test
    public void testChest() {
        this.location.setBlock(BlockTypes.CHEST.get().defaultState());
        DataTest.checkGetData(this.location, Keys.CUSTOM_NAME, null);
        DataTest.checkGetData(this.location, Keys.DISPLAY_NAME, Component.translatable("container.chest"));
        DataTest.checkOfferData(this.location, Keys.CUSTOM_NAME, Component.text("Just a Chest"));
        DataTest.checkGetData(this.location, Keys.DISPLAY_NAME, Component.text("Just a Chest"));
    }

    @Test
    public void testEndGateway() {
        this.location.setBlock(BlockTypes.END_GATEWAY.get().defaultState());
        final BlockEntity endGateway = this.location.blockEntity().get();

        DataTest.checkGetData(endGateway, Keys.COOLDOWN, Ticks.zero());
        DataTest.checkOfferData(endGateway, Keys.COOLDOWN, Ticks.of(10));

        DataTest.checkGetData(endGateway, Keys.END_GATEWAY_AGE, Ticks.of(0L));
        DataTest.checkOfferData(endGateway, Keys.END_GATEWAY_AGE, Ticks.of(100L));
    }

    @Test
    public void testLeaves() {
        final BlockState leaves = BlockTypes.ACACIA_LEAVES.get().defaultState();
        DataTest.checkGetData(leaves, Keys.DECAY_DISTANCE, 7);
        DataTest.checkWithData(leaves, Keys.DECAY_DISTANCE, 2);
    }

    @Test
    public void testStairs() {
        final BlockState stairs = BlockTypes.ACACIA_STAIRS.get().defaultState();
        DataTest.checkGetData(stairs, Keys.DIRECTION, Direction.NORTH);
        DataTest.checkWithData(stairs, Keys.DIRECTION, Direction.WEST);

        DataTest.checkWithData(stairs, Keys.IS_WATERLOGGED, true);

        DataTest.checkWithData(stairs, Keys.STAIR_SHAPE, StairShapes.INNER_LEFT.get());
        DataTest.checkWithData(stairs, Keys.STAIR_SHAPE, StairShapes.OUTER_LEFT.get());
        DataTest.checkWithData(stairs, Keys.STAIR_SHAPE, StairShapes.STRAIGHT.get());
    }

    @Test
    public void testDyeColor() {
        DataTest.checkGetData(BlockTypes.RED_BED.get().defaultState(), Keys.DYE_COLOR, DyeColors.RED.get());
        DataTest.checkGetData(BlockTypes.BLUE_CONCRETE.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
        DataTest.checkGetData(BlockTypes.BLUE_CONCRETE_POWDER.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
        DataTest.checkGetData(BlockTypes.BLUE_TERRACOTTA.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
        DataTest.checkGetData(BlockTypes.BLUE_GLAZED_TERRACOTTA.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
        DataTest.checkGetData(BlockTypes.BLUE_STAINED_GLASS.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
        DataTest.checkGetData(BlockTypes.BLUE_STAINED_GLASS_PANE.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
        DataTest.checkGetData(BlockTypes.BLUE_BANNER.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
        DataTest.checkGetData(BlockTypes.BLUE_WALL_BANNER.get().defaultState(), Keys.DYE_COLOR, DyeColors.BLUE.get());
    }

    @Disabled
    @Test
    public void testWater() {
        final BlockState water = BlockTypes.WATER.get().defaultState();
        DataTest.checkGetData(water, Keys.FLUID_LEVEL, 8);
    }

    @Test
    public void testGrowthStage() {
        final BlockState melonStem = BlockTypes.MELON_STEM.get().defaultState();
        final BlockState cactus = BlockTypes.CACTUS.get().defaultState();
        DataTest.checkGetData(melonStem, Keys.GROWTH_STAGE, 0);
        DataTest.checkWithData(melonStem, Keys.GROWTH_STAGE, 4);
        DataTest.checkGetData(cactus, Keys.GROWTH_STAGE, 0);
        DataTest.checkWithData(cactus, Keys.GROWTH_STAGE, 4);
    }

    @Test
    public void testMushroomBlock() {
        final BlockState mushroomBlock = BlockTypes.BROWN_MUSHROOM_BLOCK.get().defaultState();
        DataTest.checkGetData(mushroomBlock, Keys.HAS_PORES_DOWN, true);
        DataTest.checkGetData(mushroomBlock, Keys.HAS_PORES_EAST, true);
        DataTest.checkGetData(mushroomBlock, Keys.HAS_PORES_NORTH, true);
        DataTest.checkGetData(mushroomBlock, Keys.HAS_PORES_SOUTH, true);
        DataTest.checkGetData(mushroomBlock, Keys.HAS_PORES_UP, true);
        DataTest.checkGetData(mushroomBlock, Keys.HAS_PORES_WEST, true);
        DataTest.checkWithData(mushroomBlock, Keys.PORES, Set.of(Direction.DOWN, Direction.NORTH));
    }

    @Test
    public void testHeldItem() {
        DataTest.checkGetData(BlockTypes.OBSIDIAN.get().defaultState(), Keys.HELD_ITEM, ItemTypes.OBSIDIAN.get());
        DataTest.checkGetData(BlockTypes.WATER.get().defaultState(), Keys.HELD_ITEM, null);
    }

    @Test
    public void testNoteBlock() {
        final BlockState noteBlock = BlockTypes.NOTE_BLOCK.get().defaultState();
        DataTest.checkGetData(noteBlock, Keys.INSTRUMENT_TYPE, InstrumentTypes.HARP.get());
        DataTest.checkWithData(noteBlock, Keys.INSTRUMENT_TYPE, InstrumentTypes.COW_BELL.get());
    }

    @Test
    public void testDaylightDetector() {
        final BlockState daylightDetector = BlockTypes.DAYLIGHT_DETECTOR.get().defaultState();
        DataTest.checkGetData(daylightDetector, Keys.INVERTED, false);
        DataTest.checkWithData(daylightDetector, Keys.INVERTED, true);
    }

    @Test
    public void testFenceGate() {
        final BlockState fenceGate = BlockTypes.ACACIA_FENCE_GATE.get().defaultState();
        DataTest.checkGetData(fenceGate, Keys.IN_WALL, false);
        DataTest.checkWithData(fenceGate, Keys.IN_WALL, true);

        DataTest.checkWithData(fenceGate, Keys.IS_OPEN, true);
        DataTest.checkWithData(fenceGate, Keys.IS_OPEN, false);
    }

    @Test
    public void testAttached() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.IS_ATTACHED, null);
        DataTest.checkGetData(BlockTypes.TORCH.get().defaultState(), Keys.IS_ATTACHED, false);
    }

    @Test
    public void testConnected() {
        final BlockState fence = BlockTypes.ACACIA_FENCE.get().defaultState();
        DataTest.checkWithData(fence, Keys.IS_CONNECTED_EAST, true);
        DataTest.checkWithData(fence, Keys.IS_CONNECTED_NORTH, false);
        DataTest.checkWithData(fence, Keys.IS_CONNECTED_SOUTH, true);
        DataTest.checkWithData(fence, Keys.IS_CONNECTED_WEST, false);

        final BlockState wall = BlockTypes.ANDESITE_WALL.get().defaultState();
        DataTest.checkWithData(wall, Keys.IS_CONNECTED_EAST, true);
        DataTest.checkWithData(wall, Keys.IS_CONNECTED_NORTH, false);
        DataTest.checkWithData(wall, Keys.IS_CONNECTED_SOUTH, true);
        DataTest.checkWithData(wall, Keys.IS_CONNECTED_WEST, false);
        DataTest.checkWithData(wall, Keys.IS_CONNECTED_UP, true);

        final BlockState vine = BlockTypes.VINE.get().defaultState();
        DataTest.checkWithData(vine, Keys.IS_CONNECTED_UP, false);
        DataTest.checkWithData(vine, Keys.IS_CONNECTED_UP, true);
    }

    @Test
    public void testTripwire() {
        final BlockState tripWire = BlockTypes.TRIPWIRE.get().defaultState();
        DataTest.checkWithData(tripWire, Keys.IS_DISARMED, true);
    }

    @Test
    public void testPiston() {
        final BlockState piston = BlockTypes.PISTON.get().defaultState();
        DataTest.checkWithData(piston, Keys.IS_EXTENDED, true);
        DataTest.checkWithData(piston, Keys.PISTON_TYPE, PistonTypes.NORMAL.get());
        DataTest.checkWithData(piston, Keys.PISTON_TYPE, PistonTypes.STICKY.get());
    }

    @Test
    public void testEndPortalFrame() {
        final BlockState portalFrame = BlockTypes.END_PORTAL_FRAME.get().defaultState();
        DataTest.checkWithData(portalFrame, Keys.IS_FILLED, true);
        DataTest.checkWithData(portalFrame, Keys.IS_FILLED, false);
    }

    @Test
    public void testFlammable() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.IS_FLAMMABLE, false);
        DataTest.checkGetData(BlockTypes.BRICKS.get().defaultState(), Keys.IS_FLAMMABLE, false);
        DataTest.checkGetData(BlockTypes.ACACIA_LEAVES.get().defaultState(), Keys.IS_FLAMMABLE, true);
        DataTest.checkGetData(BlockTypes.BAMBOO_FENCE.get().defaultState(), Keys.IS_FLAMMABLE, true);
    }

    @Test
    public void testGravityAffected() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.IS_GRAVITY_AFFECTED, false);
        DataTest.checkGetData(BlockTypes.SAND.get().defaultState(), Keys.IS_GRAVITY_AFFECTED, true);
    }

    @Test
    public void testLit() {
        final BlockState furnace = BlockTypes.FURNACE.get().defaultState();
        final BlockState campfire = BlockTypes.CAMPFIRE.get().defaultState();
        final BlockState redstoneTorch = BlockTypes.REDSTONE_TORCH.get().defaultState();
        DataTest.checkWithData(furnace, Keys.IS_LIT, false);
        DataTest.checkWithData(furnace, Keys.IS_LIT, true);
        DataTest.checkWithData(campfire, Keys.IS_LIT, false);
        DataTest.checkWithData(campfire, Keys.IS_LIT, true);
        DataTest.checkWithData(redstoneTorch, Keys.IS_LIT, false);
        DataTest.checkWithData(redstoneTorch, Keys.IS_LIT, true);
    }

    @Test
    public void testBed() {
        final BlockState bed = BlockTypes.BLACK_BED.get().defaultState();
        DataTest.checkWithData(bed, Keys.IS_OCCUPIED, true);
        DataTest.checkWithData(bed, Keys.IS_OCCUPIED, false);
        DataTest.checkWithData(bed, Keys.PORTION_TYPE, PortionTypes.BOTTOM.get());
        DataTest.checkWithData(bed, Keys.PORTION_TYPE, PortionTypes.TOP.get());
    }

    @Test
    public void testPassable() {
        DataTest.checkGetData(BlockTypes.WATER.get().defaultState(), Keys.IS_PASSABLE, true);
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.IS_PASSABLE, false);
    }

    @Test
    public void testPowered() {
        final BlockState lever = BlockTypes.LEVER.get().defaultState();
        DataTest.checkWithData(lever, Keys.IS_POWERED, true);
        DataTest.checkWithData(lever, Keys.IS_POWERED, false);
    }

    @Test
    public void testReplaceable() {
        DataTest.checkGetData(BlockTypes.WATER.get().defaultState(), Keys.IS_REPLACEABLE, true);
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.IS_REPLACEABLE, false);
    }

    @Test
    public void testSolid() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.IS_SOLID, true);
        DataTest.checkGetData(BlockTypes.OBSIDIAN.get().defaultState(), Keys.IS_SOLID, true);
        DataTest.checkGetData(BlockTypes.WATER.get().defaultState(), Keys.IS_SOLID, false);
    }

    @Test
    public void testSurrogate() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.IS_SURROGATE_BLOCK, false);
    }

    @Test
    public void testUnbreakable() {
        DataTest.checkGetData(BlockTypes.OBSIDIAN.get().defaultState(), Keys.IS_UNBREAKABLE, false);
        DataTest.checkGetData(BlockTypes.BEDROCK.get().defaultState(), Keys.IS_UNBREAKABLE, true);
    }

    @Test
    public void testLayer() {
        DataTest.checkWithData(BlockTypes.SNOW.get().defaultState(), Keys.LAYER, 4);
        DataTest.checkWithData(BlockTypes.CAKE.get().defaultState(), Keys.LAYER, 4);
    }

    @Test
    public void testLightEmission() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.LIGHT_EMISSION, 0);
        DataTest.checkGetData(BlockTypes.GLOWSTONE.get().defaultState(), Keys.LIGHT_EMISSION, 15);
    }

    @Test
    public void testMatterType() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.MATTER_TYPE, MatterTypes.SOLID.get());
        DataTest.checkGetData(BlockTypes.WATER.get().defaultState(), Keys.MATTER_TYPE, MatterTypes.LIQUID.get());
        DataTest.checkGetData(BlockTypes.AIR.get().defaultState(), Keys.MATTER_TYPE, MatterTypes.GAS.get());
    }

    @Test
    public void testFarmland() {
        DataTest.checkWithData(BlockTypes.FARMLAND.get().defaultState(), Keys.MOISTURE, 1);
    }

    @Test
    public void testDoor() {
        final BlockState door = BlockTypes.ACACIA_DOOR.get().defaultState();
        DataTest.checkWithData(door, Keys.PORTION_TYPE, PortionTypes.TOP.get());
    }

    @Test
    public void testRedstoneWire() {
        final BlockState redstoneWire = BlockTypes.REDSTONE_WIRE.get().defaultState();
        DataTest.checkWithData(redstoneWire, Keys.POWER, 10);

        DataTest.checkWithData(redstoneWire, Keys.WIRE_ATTACHMENT_EAST, WireAttachmentTypes.NONE.get());
        DataTest.checkWithData(redstoneWire, Keys.WIRE_ATTACHMENT_NORTH, WireAttachmentTypes.UP.get());
        DataTest.checkWithData(redstoneWire, Keys.WIRE_ATTACHMENT_SOUTH, WireAttachmentTypes.SIDE.get());
        DataTest.checkWithData(redstoneWire, Keys.WIRE_ATTACHMENT_WEST, WireAttachmentTypes.UP.get());

        final Map<Direction, WireAttachmentType> map = new HashMap<>();
        map.put(Direction.NORTH, WireAttachmentTypes.NONE.get());
        map.put(Direction.EAST, WireAttachmentTypes.NONE.get());
        map.put(Direction.SOUTH, WireAttachmentTypes.NONE.get());
        map.put(Direction.WEST, WireAttachmentTypes.UP.get());
        DataTest.checkGetData(redstoneWire.with(Keys.WIRE_ATTACHMENT_WEST, WireAttachmentTypes.UP.get()).get(), Keys.WIRE_ATTACHMENTS, map);
    }

    @Test
    public void testRail() {
        final BlockState rail = BlockTypes.RAIL.get().defaultState();
        DataTest.checkWithData(rail, Keys.RAIL_DIRECTION, RailDirections.ASCENDING_EAST.get());
    }

    @Test
    public void testRepeater() {
        final BlockState repeater = BlockTypes.REPEATER.get().defaultState();
        DataTest.checkWithData(repeater, Keys.REDSTONE_DELAY, 2);
    }

    @Test
    public void testRepresentedInstrument() {
        DataTest.checkGetData(BlockTypes.DIRT.get().defaultState(), Keys.REPRESENTED_INSTRUMENT, InstrumentTypes.HARP.get());
        DataTest.checkGetData(BlockTypes.ACACIA_WOOD.get().defaultState(), Keys.REPRESENTED_INSTRUMENT, InstrumentTypes.BASS.get());
    }



    @Test
    public void testSign() {
        this.location.setBlock(BlockTypes.SPRUCE_SIGN.get().defaultState());
        final Component emptyText = Component.empty().style(Style.empty());
        DataTest.checkGetData(location, Keys.SIGN_LINES, Arrays.asList(emptyText, emptyText, emptyText, emptyText));
        final Component text = Component.text("Test").style(Style.style(NamedTextColor.RED));
        DataTest.checkOfferData(location, Keys.SIGN_LINES, Arrays.asList(text, text, text, text));
    }

    @Test
    public void testSlab() {
        final BlockState slab = BlockTypes.BIRCH_SLAB.get().defaultState();
        DataTest.checkWithData(slab, Keys.SLAB_PORTION, SlabPortions.BOTTOM.get());
        DataTest.checkWithData(slab, Keys.SLAB_PORTION, SlabPortions.DOUBLE.get());
        DataTest.checkWithData(slab, Keys.SLAB_PORTION, SlabPortions.TOP.get());
    }

    @Test
    public void testTNT() {
        final BlockState tnt = BlockTypes.TNT.get().defaultState();
        DataTest.checkWithData(tnt, Keys.UNSTABLE, true);
    }

    @Test
    public void testDelegation() {
        // ServerWorld -> ServerLocation -> BlockState -> BlockType
        // BlockSnapshot -> BlockState -> BlockType
        final BlockState dioriteState = BlockTypes.DIORITE.get().defaultState();
        this.location.setBlock(dioriteState);
        final Double destroySpeed = this.location.world().get(this.location.blockPosition(), Keys.DESTROY_SPEED).get();
        DataTest.checkGetData(this.location, Keys.DESTROY_SPEED, destroySpeed);
        DataTest.checkGetData(dioriteState, Keys.DESTROY_SPEED, destroySpeed);
        final BlockSnapshot dioriteSnapshot = dioriteState.snapshotFor(this.location);
        DataTest.checkGetData(dioriteSnapshot, Keys.DESTROY_SPEED, destroySpeed);
    }
}
