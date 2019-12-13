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
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.PressurePlateBlock;
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
import net.minecraft.state.IntegerProperty;
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
        register(new BannerBlockDirectionStateProvider());

        // BedBlock
        registerBoolean(BedBlock.class, Keys.OCCUPIED, BedBlock.OCCUPIED);
        // TODO: Part

        // CactusBlock
        registerBoundedInt(CactusBlock.class, Keys.GROWTH_STAGE, CactusBlock.AGE);

        // CakeBlock
        registerBoundedInt(CakeBlock.class, Keys.LAYER, CakeBlock.BITES);

        // CocoaBlock
        registerBoundedInt(CocoaBlock.class, Keys.GROWTH_STAGE, CocoaBlock.AGE);

        // CropsBlock
        register(new CropsBlockGrowthStageStateProvider());

        // ChestBlock
        registerDirection(ChestBlock.class, ChestBlock.FACING);
        // TODO: Connection Type, Waterlogged

        // DaylightDetectorBlock
        registerBoundedInt(DaylightDetectorBlock.class, Keys.POWER, DaylightDetectorBlock.POWER);
        registerBoolean(DaylightDetectorBlock.class, Keys.INVERTED, DaylightDetectorBlock.INVERTED);

        // DoorBlock
        registerDirection(DoorBlock.class, DoorBlock.FACING);
        registerBoolean(DoorBlock.class, Keys.POWERED, DoorBlock.POWERED);
        registerBoolean(DoorBlock.class, Keys.OPEN, DoorBlock.OPEN);
        register(new DoorBlockHingeStateProvider());
        register(new DoubleBlockPortionStateProvider(DoorBlock.class, DoorBlock.HALF));

        // DoublePlantBlock
        register(new DoubleBlockPortionStateProvider(DoublePlantBlock.class, DoublePlantBlock.HALF));

        // DirectionalBlock
        registerDirection(DirectionalBlock.class, DirectionalBlock.FACING);

        // DispenserBlock
        registerDirection(DispenserBlock.class, DispenserBlock.FACING);
        // registerBoolean(DispenserBlock.class, Keys.TRIGGERED, DispenserBlock.TRIGGERED); // TODO

        // FarmlandBlock
        registerBoundedInt(FarmlandBlock.class, Keys.MOISTURE, FarmlandBlock.MOISTURE);

        // HorizontalBlock
        registerDirection(HorizontalBlock.class, HorizontalBlock.HORIZONTAL_FACING);

        // HorizontalFaceBlock
        // TODO: Attach Face

        // NetherPortalBlock
        register(new AxisBlockStateProvider(NetherPortalBlock.class, NetherPortalBlock.AXIS));

        // RotatedPillarBlock
        register(new AxisBlockStateProvider(RotatedPillarBlock.class, RotatedPillarBlock.AXIS));

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
        register(new StandingSignBlockDirectionStateProvider());

        // StemBlock
        registerBoundedInt(StemBlock.class, Keys.GROWTH_STAGE, StemBlock.AGE);

        // PaneBlock
        registerBoolean(PaneBlock.class, Keys.CONNECTED_EAST, PaneBlock.EAST);
        registerBoolean(PaneBlock.class, Keys.CONNECTED_WEST, PaneBlock.WEST);
        registerBoolean(PaneBlock.class, Keys.CONNECTED_SOUTH, PaneBlock.SOUTH);
        registerBoolean(PaneBlock.class, Keys.CONNECTED_NORTH, PaneBlock.NORTH);
        register(new ConnectedDirectionsBlockStateProvider(PaneBlock.class, ImmutableMap.of(
                Direction.EAST, PaneBlock.EAST,
                Direction.WEST, PaneBlock.WEST,
                Direction.SOUTH, PaneBlock.SOUTH,
                Direction.NORTH, PaneBlock.NORTH)));

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
        registerBoolean(TripWireBlock.class, Keys.CONNECTED_EAST, TripWireBlock.EAST);
        registerBoolean(TripWireBlock.class, Keys.CONNECTED_WEST, TripWireBlock.WEST);
        registerBoolean(TripWireBlock.class, Keys.CONNECTED_SOUTH, TripWireBlock.SOUTH);
        registerBoolean(TripWireBlock.class, Keys.CONNECTED_NORTH, TripWireBlock.NORTH);
        register(new ConnectedDirectionsBlockStateProvider(TripWireBlock.class, ImmutableMap.of(
                Direction.EAST, TripWireBlock.EAST,
                Direction.WEST, TripWireBlock.WEST,
                Direction.SOUTH, TripWireBlock.SOUTH,
                Direction.NORTH, TripWireBlock.NORTH)));

        // TripWireHookBlock
        registerDirection(TripWireHookBlock.class, TripWireHookBlock.FACING);
        registerBoolean(TripWireHookBlock.class, Keys.ATTACHED, TripWireHookBlock.ATTACHED);
        registerBoolean(TripWireHookBlock.class, Keys.POWERED, TripWireHookBlock.POWERED);

        // VineBlock
        registerBoolean(VineBlock.class, Keys.CONNECTED_EAST, VineBlock.EAST);
        registerBoolean(VineBlock.class, Keys.CONNECTED_WEST, VineBlock.WEST);
        registerBoolean(VineBlock.class, Keys.CONNECTED_SOUTH, VineBlock.SOUTH);
        registerBoolean(VineBlock.class, Keys.CONNECTED_NORTH, VineBlock.NORTH);
        registerBoolean(VineBlock.class, Keys.CONNECTED_UP, VineBlock.UP);
        register(new ConnectedDirectionsBlockStateProvider(VineBlock.class, ImmutableMap.of(
                Direction.EAST, VineBlock.EAST,
                Direction.WEST, VineBlock.WEST,
                Direction.SOUTH, VineBlock.SOUTH,
                Direction.NORTH, VineBlock.NORTH,
                Direction.UP, VineBlock.UP)));

        // WallBannerBlock
        registerDirection(WallBannerBlock.class, WallBannerBlock.HORIZONTAL_FACING);

        // WallSignBlock
        registerDirection(WallSignBlock.class, WallSignBlock.FACING);
        // TODO: Waterlogged

        // WallBlock
        registerBoolean(WallBlock.class, Keys.CONNECTED_EAST, WallBlock.EAST);
        registerBoolean(WallBlock.class, Keys.CONNECTED_WEST, WallBlock.WEST);
        registerBoolean(WallBlock.class, Keys.CONNECTED_SOUTH, WallBlock.SOUTH);
        registerBoolean(WallBlock.class, Keys.CONNECTED_NORTH, WallBlock.NORTH);
        registerBoolean(WallBlock.class, Keys.CONNECTED_UP, WallBlock.UP);
        register(new ConnectedDirectionsBlockStateProvider(WallBlock.class, ImmutableMap.of(
                Direction.EAST, WallBlock.EAST,
                Direction.WEST, WallBlock.WEST,
                Direction.SOUTH, WallBlock.SOUTH,
                Direction.NORTH, WallBlock.NORTH,
                Direction.UP, WallBlock.UP)));
        // TODO: Waterlogged

        // WeightedPressurePlateBlock
        registerBoundedInt(WeightedPressurePlateBlock.class, Keys.POWER, WeightedPressurePlateBlock.POWER);
    }
}
