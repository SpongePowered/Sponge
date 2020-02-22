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
package org.spongepowered.common.data.provider.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CakeBlock;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.DaylightDetectorBlock;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.MovingPistonBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.TrapDoorBlock;
import net.minecraft.block.TripWireBlock;
import net.minecraft.block.TripWireHookBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.WeightedPressurePlateBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.BlockStateBooleanDataProvider;
import org.spongepowered.common.data.provider.BlockStateBoundedIntDataProvider;
import org.spongepowered.common.data.provider.BlockStateDirectionDataProvider;
import org.spongepowered.common.data.provider.DataProviderRegistry;
import org.spongepowered.common.data.provider.DataProviderRegistryBuilder;
import org.spongepowered.common.mixin.accessor.block.BlockAccessor;
import org.spongepowered.common.mixin.accessor.block.FireBlockAccessor;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class BlockDataProviders extends DataProviderRegistryBuilder {

    public BlockDataProviders(DataProviderRegistry registry) {
        super(registry);
    }

    private <E> void register(Supplier<? extends Key<? extends Value<E>>> key, Function<BlockState, E> getter) {
        register(BlockState.class, key.get(), getter);
    }

    private void registerBoolean(Class<? extends Block> blockType, Supplier<? extends Key<? extends Value<Boolean>>> key, BooleanProperty property) {
        registerBoolean(blockType, key.get(), property);
    }

    private void registerBoolean(Class<? extends Block> blockType, Key<? extends Value<Boolean>> key, BooleanProperty property) {
        register(new BlockStateBooleanDataProvider(key, blockType, property));
    }

    private void registerDirection(Class<? extends Block> blockType, DirectionProperty property) {
        register(new BlockStateDirectionDataProvider(blockType, property));
    }

    private void registerBoundedInt(Class<? extends Block> blockType, Supplier<? extends Key<? extends BoundedValue<Integer>>> key,
            IntegerProperty property) {
        registerBoundedInt(blockType, key.get(), property);
    }

    private void registerBoundedInt(Class<? extends Block> blockType, Key<? extends BoundedValue<Integer>> key, IntegerProperty property) {
        register(new BlockStateBoundedIntDataProvider(key, blockType, property));
    }

    private void registerHorizontalConnectedSides(Class<? extends Block> blockType,
            BooleanProperty north, BooleanProperty south, BooleanProperty east, BooleanProperty west) {
        registerBoolean(blockType, Keys.CONNECTED_EAST, east);
        registerBoolean(blockType, Keys.CONNECTED_WEST, west);
        registerBoolean(blockType, Keys.CONNECTED_SOUTH, south);
        registerBoolean(blockType, Keys.CONNECTED_NORTH, north);
        register(new BlockDirectionalSetProvider(Keys.CONNECTED_DIRECTIONS, blockType, ImmutableMap.of(
                Direction.EAST, east,
                Direction.WEST, west,
                Direction.SOUTH, south,
                Direction.NORTH, north)));
    }

    private void registerHorizontalAndUpConnectedSides(Class<? extends Block> blockType,
            BooleanProperty north, BooleanProperty south, BooleanProperty east, BooleanProperty west, BooleanProperty up) {
        registerBoolean(blockType, Keys.CONNECTED_EAST, east);
        registerBoolean(blockType, Keys.CONNECTED_WEST, west);
        registerBoolean(blockType, Keys.CONNECTED_SOUTH, south);
        registerBoolean(blockType, Keys.CONNECTED_NORTH, north);
        registerBoolean(blockType, Keys.CONNECTED_UP, up);
        register(new BlockDirectionalSetProvider(Keys.CONNECTED_DIRECTIONS, blockType, ImmutableMap.of(
                Direction.EAST, east,
                Direction.WEST, west,
                Direction.SOUTH, south,
                Direction.NORTH, north,
                Direction.UP, up)));
    }

    @Override
    public void register() {
        // TODO: Auto-register based on common used properties, like facing,
        //  powered, power, etc. Less common ones will still need to be done manually.
        //  This improves interop with modded blocks.

        register(Keys.HARDNESS, state -> (double) ((BlockAccessor) state.getBlock()).accessor$getBlockHardness());
        register(Keys.BLAST_RESISTANCE, state -> (double) ((BlockAccessor) state.getBlock()).accessor$getBlockResistance());
        register(Keys.IS_GRAVITY_AFFECTED, state -> state.getBlock() instanceof FallingBlock);
        register(Keys.REPRESENTED_INSTRUMENT, state -> (InstrumentType) (Object) NoteBlockInstrument.byState(state));
        register(Keys.IS_PASSABLE, state -> !state.getMaterial().blocksMovement());
        register(Keys.IS_UNBREAKABLE, state -> ((BlockAccessor) state.getBlock()).accessor$getBlockHardness() < 0);
        register(Keys.IS_FLAMMABLE, state -> ((FireBlockAccessor) Blocks.FIRE).accessor$func_220274_q(state) > 0);
        register(new BlockMatterProvider());

        // AbstractBannerBlock
        register(new AbstractBannerBlockAttachedProvider());

        // AbstractSignBlock
        register(new AbstractSignBlockAttachedProvider());
        // TODO: Waterlogged

        // AbstractSkullBlock
        register(new AbstractSkullBlockAttachedProvider());

        // AbstractButtonBlock
        registerBoolean(AbstractButtonBlock.class, Keys.IS_POWERED, AbstractButtonBlock.POWERED);

        // AbstractFurnaceBlock
        registerDirection(AbstractFurnaceBlock.class, AbstractFurnaceBlock.FACING);
        registerBoolean(AbstractFurnaceBlock.class, Keys.LIT, AbstractFurnaceBlock.LIT);

        // AbstractRailBlock
        register(new AbstractRailBlockRailDirectionProvider());

        // AnvilBlock
        registerDirection(AnvilBlock.class, AbstractFurnaceBlock.FACING);

        // AttachedStemBlock
        registerDirection(AttachedStemBlock.class, AttachedStemBlock.FACING);

        // BannerBlock
        register(new BannerBlockDirectionProvider());

        // BedBlock
        registerBoolean(BedBlock.class, Keys.IS_OCCUPIED, BedBlock.OCCUPIED);
        // TODO: Part

        // HugeMushroomBlock
        registerBoolean(HugeMushroomBlock.class, Keys.BIG_MUSHROOM_PORES_EAST, HugeMushroomBlock.EAST);
        registerBoolean(HugeMushroomBlock.class, Keys.BIG_MUSHROOM_PORES_WEST, HugeMushroomBlock.WEST);
        registerBoolean(HugeMushroomBlock.class, Keys.BIG_MUSHROOM_PORES_NORTH, HugeMushroomBlock.NORTH);
        registerBoolean(HugeMushroomBlock.class, Keys.BIG_MUSHROOM_PORES_SOUTH, HugeMushroomBlock.SOUTH);
        registerBoolean(HugeMushroomBlock.class, Keys.BIG_MUSHROOM_PORES_UP, HugeMushroomBlock.UP);
        registerBoolean(HugeMushroomBlock.class, Keys.BIG_MUSHROOM_PORES_DOWN, HugeMushroomBlock.DOWN);
        register(new BlockDirectionalSetProvider(Keys.BIG_MUSHROOM_PORES, HugeMushroomBlock.class,
                ImmutableMap.<Direction, BooleanProperty>builder()
                        .put(Direction.EAST, HugeMushroomBlock.EAST)
                        .put(Direction.WEST, HugeMushroomBlock.WEST)
                        .put(Direction.SOUTH, HugeMushroomBlock.SOUTH)
                        .put(Direction.NORTH, HugeMushroomBlock.NORTH)
                        .put(Direction.UP, HugeMushroomBlock.UP)
                        .put(Direction.DOWN, HugeMushroomBlock.DOWN)
                        .build()));

        // CactusBlock
        registerBoundedInt(CactusBlock.class, Keys.GROWTH_STAGE, CactusBlock.AGE);

        // CakeBlock
        registerBoundedInt(CakeBlock.class, Keys.LAYER, CakeBlock.BITES);

        // CocoaBlock
        registerBoundedInt(CocoaBlock.class, Keys.GROWTH_STAGE, CocoaBlock.AGE);

        // ComparatorBlock
        register(new ComparatorBlockTypeProvider());

        // CropsBlock
        register(new CropsBlockGrowthStageProvider());

        // ChestBlock
        registerDirection(ChestBlock.class, ChestBlock.FACING);
        // TODO: Connection Type, Waterlogged

        // DaylightDetectorBlock
        registerBoundedInt(DaylightDetectorBlock.class, Keys.POWER, DaylightDetectorBlock.POWER);
        registerBoolean(DaylightDetectorBlock.class, Keys.INVERTED, DaylightDetectorBlock.INVERTED);

        // DetectorRailBlock
        registerBoolean(DetectorRailBlock.class, Keys.IS_POWERED, DetectorRailBlock.POWERED);

        // DoorBlock
        registerDirection(DoorBlock.class, DoorBlock.FACING);
        registerBoolean(DoorBlock.class, Keys.IS_POWERED, DoorBlock.POWERED);
        registerBoolean(DoorBlock.class, Keys.IS_OPEN, DoorBlock.OPEN);
        register(new DoorBlockHingeProvider());
        register(new DoubleBlockPortionProvider(DoorBlock.class, DoorBlock.HALF));

        // DoublePlantBlock
        register(new DoubleBlockPortionProvider(DoublePlantBlock.class, DoublePlantBlock.HALF));

        // DirectionalBlock
        registerDirection(DirectionalBlock.class, DirectionalBlock.FACING);

        // DispenserBlock
        registerDirection(DispenserBlock.class, DispenserBlock.FACING);
        // registerBoolean(DispenserBlock.class, Keys.TRIGGERED, DispenserBlock.TRIGGERED); // TODO

        // EndPortalFrameBlock
        registerDirection(EndPortalFrameBlock.class, EndPortalFrameBlock.FACING);
        // registerBoolean(EndPortalFrameBlock.class, Keys.HAS_EYE, EndPortalFrameBlock.EYE); // TODO

        // EnderChestBlock
        registerDirection(EnderChestBlock.class, EnderChestBlock.FACING);
        // TODO: Waterlogged

        // FarmlandBlock
        registerBoundedInt(FarmlandBlock.class, Keys.MOISTURE, FarmlandBlock.MOISTURE);

        // FenceBlock
        registerHorizontalConnectedSides(FenceBlock.class,
                FenceBlock.NORTH, FenceBlock.SOUTH, FenceBlock.EAST, FenceBlock.WEST);

        // FenceGateBlock
        registerBoolean(FenceGateBlock.class, Keys.IS_OPEN, FenceGateBlock.OPEN);
        registerBoolean(FenceGateBlock.class, Keys.IS_POWERED, FenceGateBlock.POWERED);
        registerBoolean(FenceGateBlock.class, Keys.IN_WALL, FenceGateBlock.IN_WALL);

        // HorizontalBlock
        registerDirection(HorizontalBlock.class, HorizontalBlock.HORIZONTAL_FACING);

        // HorizontalFaceBlock
        // TODO: Attach Face

        // HopperBlock
        registerDirection(HopperBlock.class, HopperBlock.FACING);
        // registerBoolean(HopperBlock.class, Keys.ENABLED, HopperBlock.ENABLED); // TODO

        // LadderBlock
        registerDirection(LadderBlock.class, LadderBlock.FACING);
        // TODO: Waterlogged

        // LeverBlock
        registerBoolean(LeverBlock.class, Keys.IS_POWERED, LeverBlock.POWERED);

        // MovingPistonBlock
        registerDirection(MovingPistonBlock.class, MovingPistonBlock.FACING);
        // TODO: What to do with the MovingPistonBlock type... Flatten everything else but
        //   still use an enum for stick and normal moving piston blocks...

        // NoteBlock
        register(new NoteBlockInstrumentProvider());
        register(new NoteBlockNotePitchProvider());

        // PistonBlock
        registerBoolean(PistonBlock.class, Keys.IS_EXTENDED, PistonBlock.EXTENDED);

        // PistonHeadBlock
        // registerBoolean(PistonHeadBlock.class, Keys.SHORT, PistonHeadBlock.SHORT); // TODO
        // TODO: What to do with the MovingPistonBlock type... Flatten everything else but
        //   still use an enum for stick and normal moving piston blocks...

        // NetherPortalBlock
        register(new AxisBlockAxisProvider(NetherPortalBlock.class, NetherPortalBlock.AXIS));

        // NetherWartBlock
        registerBoundedInt(NetherWartBlock.class, Keys.GROWTH_STAGE, NetherWartBlock.AGE);

        // PoweredRailBlock
        registerBoolean(PoweredRailBlock.class, Keys.IS_POWERED, PoweredRailBlock.POWERED);

        // RedstoneWireBlock
        registerBoundedInt(RedstoneWireBlock.class, Keys.POWER, RedstoneWireBlock.POWER);
        final Map<Direction, EnumProperty<RedstoneSide>> redstoneSides = ImmutableMap.of(
                Direction.EAST, RedstoneWireBlock.EAST,
                Direction.WEST, RedstoneWireBlock.WEST,
                Direction.SOUTH, RedstoneWireBlock.SOUTH,
                Direction.NORTH, RedstoneWireBlock.NORTH
        );
        register(new RedstoneWireBlockWireAttachmentProvider(Keys.WIRE_ATTACHMENT_EAST, RedstoneWireBlock.EAST));
        register(new RedstoneWireBlockWireAttachmentProvider(Keys.WIRE_ATTACHMENT_WEST, RedstoneWireBlock.WEST));
        register(new RedstoneWireBlockWireAttachmentProvider(Keys.WIRE_ATTACHMENT_NORTH, RedstoneWireBlock.NORTH));
        register(new RedstoneWireBlockWireAttachmentProvider(Keys.WIRE_ATTACHMENT_SOUTH, RedstoneWireBlock.SOUTH));
        register(new RedstoneWireBlockWireAttachmentsProvider(Keys.WIRE_ATTACHMENTS, RedstoneWireBlock.class, redstoneSides));
        register(new RedstoneWireBlockConnectedProvider(Keys.CONNECTED_EAST, RedstoneWireBlock.EAST));
        register(new RedstoneWireBlockConnectedProvider(Keys.CONNECTED_WEST, RedstoneWireBlock.WEST));
        register(new RedstoneWireBlockConnectedProvider(Keys.CONNECTED_NORTH, RedstoneWireBlock.NORTH));
        register(new RedstoneWireBlockConnectedProvider(Keys.CONNECTED_SOUTH, RedstoneWireBlock.SOUTH));
        register(new RedstoneWireBlockConnectedDirectionsProvider(Keys.CONNECTED_DIRECTIONS, RedstoneWireBlock.class, redstoneSides));

        // RedstoneDiodeBlock
        registerBoolean(RedstoneDiodeBlock.class, Keys.IS_POWERED, RedstoneDiodeBlock.POWERED);

        // RedstoneTorchBlock
        registerBoolean(RedstoneTorchBlock.class, Keys.LIT, RedstoneTorchBlock.LIT);

        // RepeaterBlock
        registerBoundedInt(RepeaterBlock.class, Keys.DELAY, RepeaterBlock.DELAY);

        // RotatedPillarBlock
        register(new AxisBlockAxisProvider(RotatedPillarBlock.class, RotatedPillarBlock.AXIS));

        // SaplingBlock
        registerBoundedInt(SaplingBlock.class, Keys.GROWTH_STAGE, SaplingBlock.STAGE);

        // SugarCaneBlock
        registerBoundedInt(SugarCaneBlock.class, Keys.GROWTH_STAGE, SugarCaneBlock.AGE);

        // SnowBlock
        registerBoundedInt(SnowBlock.class, Keys.LAYER, SnowBlock.LAYERS);

        // SnowyDirtBlock
        registerBoolean(SnowyDirtBlock.class, Keys.SNOWED, SnowyDirtBlock.SNOWY);

        // SpongeBlock
        register(new SpongeBlockIsWetProvider());

        // StairsBlock
        registerDirection(StairsBlock.class, StairsBlock.FACING);
        register(new HalfBlockPortionProvider(StairsBlock.class, StairsBlock.HALF));
        register(new StairsBlockShapeProvider());
        // TODO: Waterlogged

        // StandingSignBlock
        register(new StandingSignBlockDirectionProvider());

        // StemBlock
        registerBoundedInt(StemBlock.class, Keys.GROWTH_STAGE, StemBlock.AGE);

        // SkullBlock
        register(new SkullBlockDirectionProvider());

        // PaneBlock
        registerHorizontalConnectedSides(PaneBlock.class,
                PaneBlock.NORTH, PaneBlock.SOUTH, PaneBlock.EAST, PaneBlock.WEST);

        // PressurePlateBlock
        registerBoolean(PressurePlateBlock.class, Keys.IS_POWERED, PressurePlateBlock.POWERED);

        // TorchBlock
        register(new TorchBlockAttachedProvider());

        // TNTBlock
        registerBoolean(TNTBlock.class, Keys.UNSTABLE, TNTBlock.UNSTABLE);

        // TrapDoorBlock
        registerBoolean(TrapDoorBlock.class, Keys.IS_OPEN, TrapDoorBlock.OPEN);
        registerBoolean(TrapDoorBlock.class, Keys.IS_POWERED, TrapDoorBlock.POWERED);
        register(new HalfBlockPortionProvider(TrapDoorBlock.class, TrapDoorBlock.HALF));
        // TODO: Waterlogged

        // TripWireBlock
        registerBoolean(TripWireBlock.class, Keys.IS_ATTACHED, TripWireBlock.ATTACHED);
        registerBoolean(TripWireBlock.class, Keys.IS_DISARMED, TripWireBlock.DISARMED);
        registerBoolean(TripWireBlock.class, Keys.IS_POWERED, TripWireBlock.POWERED);
        registerHorizontalConnectedSides(TripWireBlock.class,
                TripWireBlock.NORTH, TripWireBlock.SOUTH, TripWireBlock.EAST, TripWireBlock.WEST);

        // TripWireHookBlock
        registerDirection(TripWireHookBlock.class, TripWireHookBlock.FACING);
        registerBoolean(TripWireHookBlock.class, Keys.IS_ATTACHED, TripWireHookBlock.ATTACHED);
        registerBoolean(TripWireHookBlock.class, Keys.IS_POWERED, TripWireHookBlock.POWERED);

        // VineBlock
        registerHorizontalAndUpConnectedSides(VineBlock.class,
                VineBlock.NORTH, VineBlock.SOUTH, VineBlock.EAST, VineBlock.WEST, VineBlock.UP);

        // WallBannerBlock
        registerDirection(WallBannerBlock.class, WallBannerBlock.HORIZONTAL_FACING);

        // WallSignBlock
        registerDirection(WallSignBlock.class, WallSignBlock.FACING);
        // TODO: Waterlogged

        // WallTorchBlock
        registerDirection(WallTorchBlock.class, WallTorchBlock.HORIZONTAL_FACING);

        // WallBlock
        registerHorizontalAndUpConnectedSides(WallBlock.class,
                WallBlock.NORTH, WallBlock.SOUTH, WallBlock.EAST, WallBlock.WEST, WallBlock.UP);
        // TODO: Waterlogged

        // WeightedPressurePlateBlock
        registerBoundedInt(WeightedPressurePlateBlock.class, Keys.POWER, WeightedPressurePlateBlock.POWER);
    }
}
