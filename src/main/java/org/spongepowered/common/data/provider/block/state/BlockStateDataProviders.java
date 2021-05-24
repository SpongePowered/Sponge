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
package org.spongepowered.common.data.provider.block.state;

import org.spongepowered.common.data.provider.DataProviderRegistratorBuilder;

public final class BlockStateDataProviders extends DataProviderRegistratorBuilder {

    @Override
    public void registerProviders() {
        AbstractBannerData.register(this.registrator);
        AbstractButtonData.register(this.registrator);
        AbstractFurnaceData.register(this.registrator);
        AbstractRailData.register(this.registrator);
        AbstractSignData.register(this.registrator);
        AnvilData.register(this.registrator);
        AttachedStemData.register(this.registrator);
        BambooData.register(this.registrator);
        BannerData.register(this.registrator);
        BedData.register(this.registrator);
        BlockData.register(this.registrator);
        CactusData.register(this.registrator);
        CakeData.register(this.registrator);
        CampfireData.register(this.registrator);
        ChestData.register(this.registrator);
        CocoaData.register(this.registrator);
        ComparatorData.register(this.registrator);
        CropsData.register(this.registrator);
        DaylightDetectorData.register(this.registrator);
        DetectorRailData.register(this.registrator);
        DirectionalData.register(this.registrator);
        DispenserData.register(this.registrator);
        DoorData.register(this.registrator);
        DoublePlantData.register(this.registrator);
        EnderChestData.register(this.registrator);
        EndPortalFrameData.register(this.registrator);
        FarmlandData.register(this.registrator);
        FenceData.register(this.registrator);
        FenceGateData.register(this.registrator);
        FlowingFluidData.register(this.registrator);
        HopperData.register(this.registrator);
        HorizontalData.register(this.registrator);
        HorizontalFaceData.register(this.registrator);
        HugeMushroomData.register(this.registrator);
        LadderData.register(this.registrator);
        LeavesData.register(this.registrator);
        LeverData.register(this.registrator);
        MovingPistonData.register(this.registrator);
        NetherPortalData.register(this.registrator);
        NetherWartData.register(this.registrator);
        NoteBlockData.register(this.registrator);
        PaneData.register(this.registrator);
        PistonData.register(this.registrator);
        PoweredRailData.register(this.registrator);
        PressurePlateData.register(this.registrator);
        RedstoneDiodeData.register(this.registrator);
        RedstoneTorchData.register(this.registrator);
        RedstoneWireData.register(this.registrator);
        RepeaterData.register(this.registrator);
        RotatedPillarData.register(this.registrator);
        SaplingData.register(this.registrator);
        SeaPickleData.register(this.registrator);
        SimpleWaterloggedBlockData.register(this.registrator);
        SkullData.register(this.registrator);
        SlabData.register(this.registrator);
        SnowData.register(this.registrator);
        SnowyDirtData.register(this.registrator);
        StainedGlassBlockData.register(this.registrator);
        StainedGlassPaneBlockData.register(this.registrator);
        StairsData.register(this.registrator);
        StandingSignData.register(this.registrator);
        StemData.register(this.registrator);
        SugarCaneData.register(this.registrator);
        TerracottaData.register(this.registrator);
        TNTData.register(this.registrator);
        TorchData.register(this.registrator);
        TrapDoorData.register(this.registrator);
        TripWireData.register(this.registrator);
        TripWireHookData.register(this.registrator);
        VineData.register(this.registrator);
        WallBannerData.register(this.registrator);
        WallData.register(this.registrator);
        WallSignData.register(this.registrator);
        WallSkullBlockData.register(this.registrator);
        WallTorchData.register(this.registrator);
        WeightedPressurePlateData.register(this.registrator);
        WoolData.register(this.registrator);
    }
}
