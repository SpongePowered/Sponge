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
package org.spongepowered.common.mixin.core.world;

import static org.spongepowered.common.data.DataTransactionBuilder.builder;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.data.Component;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.gen.BiomeBuffer;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.biome.BiomeType;
import org.spongepowered.api.world.gen.GeneratorPopulator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.data.SpongeBlockProcessor;
import org.spongepowered.common.data.SpongeManipulatorRegistry;
import org.spongepowered.common.interfaces.IMixinWorld;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.util.gen.FastChunkBuffer;
import org.spongepowered.common.util.gen.ObjectArrayMutableBiomeBuffer;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.Collection;
import java.util.List;

@NonnullByDefault
@Mixin(net.minecraft.world.chunk.Chunk.class)
public abstract class MixinChunk implements Chunk {

    private static final Vector2i BIOME_SIZE = SpongeChunkLayout.CHUNK_SIZE.toVector2(true);
    private Vector3i chunkPos;
    private Vector3i blockMin;
    private Vector3i blockMax;
    private Vector2i biomeMin;
    private Vector2i biomeMax;
    private ChunkCoordIntPair chunkCoordIntPair;

    @Shadow private net.minecraft.world.World worldObj;
    @Shadow public int xPosition;
    @Shadow public int zPosition;
    @Shadow private boolean isChunkLoaded;
    @Shadow private boolean isTerrainPopulated;

    @Shadow
    public abstract IBlockState getBlockState(BlockPos pos);

    @Shadow(prefix = "shadow$")
    public abstract Block shadow$getBlock(int x, int y, int z);

    @Shadow
    public abstract BiomeGenBase getBiome(BlockPos pos, WorldChunkManager chunkManager);

    @Shadow
    public abstract byte[] getBiomeArray();

    @Shadow
    public abstract void setBiomeArray(byte[] biomeArray);

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V", at = @At("RETURN"), remap = false)
    public void onConstructed(World world, int x, int z, CallbackInfo ci) {
        this.chunkPos = new Vector3i(x, 0, z);
        this.blockMin = SpongeChunkLayout.instance.toWorld(this.chunkPos).get();
        this.blockMax = this.blockMin.add(SpongeChunkLayout.CHUNK_SIZE).sub(1, 1, 1);
        this.biomeMin = this.blockMin.toVector2(true);
        this.biomeMax = this.blockMax.toVector2(true);
        this.chunkCoordIntPair = new ChunkCoordIntPair(x, z);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/world/chunk/ChunkPrimer;II)V", at = @At("RETURN"), remap = false)
    public void onNewlyGenerated(World world, ChunkPrimer primer, int chunkX, int chunkZ, CallbackInfo ci) {
        // The constructor with the ChunkPrimer in it is only used for newly
        // generated chunks, so we can call the generator populators here

        // Calling the generator populators here has the benefit that the chunk
        // can be modified before light is calculated and that implementations
        // of IChunkProvider provided by mods will very likely still work well

        List<GeneratorPopulator> populators = ((IMixinWorld) world).getGeneratorPopulators();
        if (!populators.isEmpty()) {
            FastChunkBuffer buffer = new FastChunkBuffer((net.minecraft.world.chunk.Chunk) (Object) this);
            BiomeGenBase[] biomeArray = world.getWorldChunkManager().getBiomeGenAt(null, chunkX * 16, chunkZ * 16, 16, 16, true);
            BiomeBuffer biomes = new ObjectArrayMutableBiomeBuffer(biomeArray, new Vector2i(chunkX * 16, chunkZ * 16), new Vector2i(16, 16));

            for (GeneratorPopulator populator : populators) {
                populator.populate((org.spongepowered.api.world.World) world, buffer, biomes);
            }
        }
    }

    @SideOnly(Side.SERVER)
    @Inject(method = "onChunkLoad()V", at = @At("RETURN"))
    public void onChunkLoadInject(CallbackInfo ci) {
        SpongeHooks.logChunkLoad(this.worldObj, this.chunkPos);
    }

    @SideOnly(Side.SERVER)
    @Inject(method = "onChunkUnload()V", at = @At("RETURN"))
    public void onChunkUnloadInject(CallbackInfo ci) {
        SpongeHooks.logChunkUnload(this.worldObj, this.chunkPos);
    }

    @Override
    public Vector3i getPosition() {
        return this.chunkPos;
    }

    @Override
    public boolean isLoaded() {
        return this.isChunkLoaded;
    }

    @Override
    public boolean isPopulated() {
        return this.isTerrainPopulated;
    }

    @Override
    public boolean loadChunk(boolean generate) {
        WorldServer worldserver = (WorldServer) this.worldObj;
        net.minecraft.world.chunk.Chunk chunk = null;
        if (worldserver.theChunkProviderServer.chunkExists(this.xPosition, this.zPosition) || generate) {
            chunk = worldserver.theChunkProviderServer.loadChunk(this.xPosition, this.zPosition);
        }

        return chunk != null;
    }

    @Override
    public org.spongepowered.api.world.World getWorld() {
        return (org.spongepowered.api.world.World) this.worldObj;
    }

    @Override
    public <T extends Component<T>> Optional<T> getData(int x, int y, int z, Class<T> dataClass) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil(dataClass);
        if (blockUtilOptional.isPresent()) {
            return blockUtilOptional.get().fromBlockPos(this.worldObj, new BlockPos(x, y, z));
        }
        return Optional.absent();
    }


    @Override
    public <T extends Component<T>> Optional<T> getOrCreate(int x, int y, int z, Class<T> manipulatorClass) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil(manipulatorClass);
        if (blockUtilOptional.isPresent()) {
            return blockUtilOptional.get().fromBlockPos(this.worldObj, new BlockPos(x, y, z));
        }
        return Optional.absent();
    }

    @Override
    public <T extends Component<T>> boolean remove(int x, int y, int z, Class<T> manipulatorClass) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil(manipulatorClass);
        if (blockUtilOptional.isPresent()) {
            return blockUtilOptional.get().remove(this.worldObj, new BlockPos(x, y, z));
        }
        return false;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends Component<T>> DataTransactionResult offer(int x, int y, int z, T manipulatorData, DataPriority priority) {
        Optional<SpongeBlockProcessor<T>> blockUtilOptional = SpongeManipulatorRegistry.getInstance().getBlockUtil((Class<T>) (Class) manipulatorData
                .getClass());
        if (blockUtilOptional.isPresent()) {
            return blockUtilOptional.get().setData(this.worldObj, new BlockPos(x, y, z), manipulatorData, priority);
        }
        return builder().result(DataTransactionResult.Type.FAILURE).build();
    }

    @Override
    public Collection<Component<?>> getComponents(int x, int y, int z) {
        final BlockPos blockPos = new BlockPos(x, y, z);
        return ((IMixinBlock) getBlock(x, y, z)).getManipulators(this.worldObj, blockPos);
    }

    @Override
    public BiomeType getBiome(int x, int z) {
        checkBiomeBounds(x, z);
        return (BiomeType) getBiome(new BlockPos(x, 0, z), this.worldObj.getWorldChunkManager());
    }

    @Override
    public void setBiome(int x, int z, BiomeType biome) {
        checkBiomeBounds(x, z);
        // Taken from Chunk#getBiome
        byte[] biomeArray = getBiomeArray();
        int i = x & 15;
        int j = z & 15;
        biomeArray[j << 4 | i] = (byte) (((BiomeGenBase) biome).biomeID & 255);
        setBiomeArray(biomeArray);
    }

    @Override
    public BlockState getBlock(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockState) getBlockState(new BlockPos(x, y, z));
    }

    @Override
    public void setBlock(int x, int y, int z, BlockState block) {
        checkBlockBounds(x, y, z);
        SpongeHooks.setBlockState((net.minecraft.world.chunk.Chunk) (Object) this, x, y, z, block);
    }

    @Override
    public BlockType getBlockType(int x, int y, int z) {
        checkBlockBounds(x, y, z);
        return (BlockType) shadow$getBlock(x, y, z);
    }

    @Override
    public Vector2i getBiomeMin() {
        return this.biomeMin;
    }

    @Override
    public Vector2i getBiomeMax() {
        return this.biomeMax;
    }

    @Override
    public Vector2i getBiomeSize() {
        return BIOME_SIZE;
    }

    @Override
    public Vector3i getBlockMin() {
        return this.blockMin;
    }

    @Override
    public Vector3i getBlockMax() {
        return this.blockMax;
    }

    @Override
    public Vector3i getBlockSize() {
        return SpongeChunkLayout.CHUNK_SIZE;
    }

    @Override
    public boolean containsBiome(int x, int z) {
        return VecHelper.inBounds(x, z, this.biomeMin, this.biomeMax);
    }

    @Override
    public boolean containsBlock(int x, int y, int z) {
        return VecHelper.inBounds(x, y, z, this.blockMin, this.blockMax);
    }

    private void checkBiomeBounds(int x, int z) {
        if (!containsBiome(x, z)) {
            throw new PositionOutOfBoundsException(new Vector2i(x, z), this.biomeMin, this.biomeMax);
        }
    }

    private void checkBlockBounds(int x, int y, int z) {
        if (!containsBlock(x, y, z)) {
            throw new PositionOutOfBoundsException(new Vector3i(x, y, z), this.blockMin, this.blockMax);
        }
    }
}
