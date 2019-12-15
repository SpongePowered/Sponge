package org.spongepowered.common.data.provider.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
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
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.HugeMushroomBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PressurePlateBlock;
import net.minecraft.block.RedstoneDiodeBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.RotatedPillarBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SnowyDirtBlock;
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
import net.minecraft.block.WeightedPressurePlateBlock;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.BoundedValue;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.BlockStateBooleanDataProvider;
import org.spongepowered.common.data.provider.BlockStateBoundedIntDataProvider;
import org.spongepowered.common.data.provider.BlockStateDirectionDataProvider;
import org.spongepowered.common.data.provider.DataProviderRegistry;

import java.util.Map;

public class BlockDataProviders {

    private final DataProviderRegistry registry;

    public BlockDataProviders(DataProviderRegistry registry) {
        this.registry = registry;
    }

    private void register(DataProvider<?,?> dataProvider) {
        this.registry.register(dataProvider);
    }

    private void registerBoolean(Class<? extends Block> blockType, Key<? extends Value<Boolean>> key, BooleanProperty property) {
        this.registry.register(new BlockStateBooleanDataProvider(key, blockType, property));
    }

    private void registerDirection(Class<? extends Block> blockType, DirectionProperty property) {
        this.registry.register(new BlockStateDirectionDataProvider(blockType, property));
    }

    private void registerBoundedInt(Class<? extends Block> blockType, Key<? extends BoundedValue<Integer>> key, IntegerProperty property) {
        this.registry.register(new BlockStateBoundedIntDataProvider(key, blockType, property));
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

    public void register() {
        // TODO: Auto-register based on common used properties, like facing,
        //  powered, power, etc. Less common ones will still need to be done manually.
        //  This improves interop with modded blocks.

        // AbstractBannerBlock
        register(new AbstractBannerBlockAttachedProvider());

        // AbstractSignBlock
        register(new AbstractSignBlockAttachedProvider());

        // AbstractButtonBlock
        registerBoolean(AbstractButtonBlock.class, Keys.POWERED, AbstractButtonBlock.POWERED);

        // AbstractFurnaceBlock
        registerDirection(AbstractFurnaceBlock.class, AbstractFurnaceBlock.FACING);
        registerBoolean(AbstractFurnaceBlock.class, Keys.LIT, AbstractFurnaceBlock.LIT);

        // AnvilBlock
        registerDirection(AnvilBlock.class, AbstractFurnaceBlock.FACING);

        // AttachedStemBlock
        registerDirection(AttachedStemBlock.class, AttachedStemBlock.FACING);

        // BannerBlock
        register(new BannerBlockDirectionProvider());

        // BedBlock
        registerBoolean(BedBlock.class, Keys.OCCUPIED, BedBlock.OCCUPIED);
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
        registerBoolean(DetectorRailBlock.class, Keys.POWERED, DetectorRailBlock.POWERED);
        // TODO: Shape

        // DoorBlock
        registerDirection(DoorBlock.class, DoorBlock.FACING);
        registerBoolean(DoorBlock.class, Keys.POWERED, DoorBlock.POWERED);
        registerBoolean(DoorBlock.class, Keys.OPEN, DoorBlock.OPEN);
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
        registerBoolean(FenceGateBlock.class, Keys.OPEN, FenceGateBlock.OPEN);
        registerBoolean(FenceGateBlock.class, Keys.POWERED, FenceGateBlock.POWERED);
        registerBoolean(FenceGateBlock.class, Keys.IN_WALL, FenceGateBlock.IN_WALL);

        // HorizontalBlock
        registerDirection(HorizontalBlock.class, HorizontalBlock.HORIZONTAL_FACING);

        // HorizontalFaceBlock
        // TODO: Attach Face

        // HopperBlock
        registerDirection(HopperBlock.class, HopperBlock.FACING);
        // registerBoolean(HopperBlock.class, Keys.ENABLED, HopperBlock.ENABLED); // TODO

        // NetherPortalBlock
        register(new AxisBlockAxisProvider(NetherPortalBlock.class, NetherPortalBlock.AXIS));

        // NetherWartBlock
        registerBoundedInt(NetherWartBlock.class, Keys.GROWTH_STAGE, NetherWartBlock.AGE);

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
        registerBoolean(RedstoneDiodeBlock.class, Keys.POWERED, RedstoneDiodeBlock.POWERED);

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

        // StandingSignBlock
        register(new StandingSignBlockDirectionProvider());

        // StemBlock
        registerBoundedInt(StemBlock.class, Keys.GROWTH_STAGE, StemBlock.AGE);

        // PaneBlock
        registerHorizontalConnectedSides(PaneBlock.class,
                PaneBlock.NORTH, PaneBlock.SOUTH, PaneBlock.EAST, PaneBlock.WEST);

        // PressurePlateBlock
        registerBoolean(PressurePlateBlock.class, Keys.POWERED, PressurePlateBlock.POWERED);

        // TNTBlock
        registerBoolean(TNTBlock.class, Keys.UNSTABLE, TNTBlock.UNSTABLE);

        // TrapDoorBlock
        registerBoolean(TrapDoorBlock.class, Keys.OPEN, TrapDoorBlock.OPEN);
        registerBoolean(TrapDoorBlock.class, Keys.POWERED, TrapDoorBlock.POWERED);
        // TODO: Half, Waterlogged

        // TripWireBlock
        registerBoolean(TripWireBlock.class, Keys.ATTACHED, TripWireBlock.ATTACHED);
        registerBoolean(TripWireBlock.class, Keys.DISARMED, TripWireBlock.DISARMED);
        registerBoolean(TripWireBlock.class, Keys.POWERED, TripWireBlock.POWERED);
        registerHorizontalConnectedSides(TripWireBlock.class,
                TripWireBlock.NORTH, TripWireBlock.SOUTH, TripWireBlock.EAST, TripWireBlock.WEST);

        // TripWireHookBlock
        registerDirection(TripWireHookBlock.class, TripWireHookBlock.FACING);
        registerBoolean(TripWireHookBlock.class, Keys.ATTACHED, TripWireHookBlock.ATTACHED);
        registerBoolean(TripWireHookBlock.class, Keys.POWERED, TripWireHookBlock.POWERED);

        // VineBlock
        registerHorizontalAndUpConnectedSides(VineBlock.class,
                VineBlock.NORTH, VineBlock.SOUTH, VineBlock.EAST, VineBlock.WEST, VineBlock.UP);

        // WallBannerBlock
        registerDirection(WallBannerBlock.class, WallBannerBlock.HORIZONTAL_FACING);

        // WallSignBlock
        registerDirection(WallSignBlock.class, WallSignBlock.FACING);
        // TODO: Waterlogged

        // WallBlock
        registerHorizontalAndUpConnectedSides(WallBlock.class,
                WallBlock.NORTH, WallBlock.SOUTH, WallBlock.EAST, WallBlock.WEST, WallBlock.UP);
        // TODO: Waterlogged

        // WeightedPressurePlateBlock
        registerBoundedInt(WeightedPressurePlateBlock.class, Keys.POWER, WeightedPressurePlateBlock.POWER);
    }
}
