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
package org.spongepowered.common.mixin.core.block;

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
public abstract class MixinBlock implements BlockType, IMixinBlock {

    private final boolean isVanilla = getClass().getName().startsWith("net.minecraft.");
    private boolean hasCollideLogic = false;
    private boolean hasCollideWithStateLogic = false;
    private boolean hasNotifyNeighborLogic = false;
    private boolean hasOnBlockAddedLogic = true;
    private Timing timing;

    @Shadow private boolean needsRandomTick;

    @Shadow public abstract boolean isBlockNormalCube();
    @Shadow public abstract boolean getEnableStats();
    @Shadow public abstract int getLightValue();
    @Shadow public abstract String getUnlocalizedName();
    @Shadow public abstract IBlockState getStateFromMeta(int meta);
    @Shadow public abstract Material getMaterial();
    @Shadow(prefix = "shadow$")
    public abstract IBlockState shadow$getDefaultState();

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
        // Determine which blocks can avoid executing un-needed event logic
        // This will allow us to avoid running event logic for blocks that do nothing such as grass collisions
        // -- blood

        // onEntityCollidedWithBlock
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityCollidedWithBlock" : "func_176199_a";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, Entity.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (!clazz.equals(Block.class)) {
                this.hasCollideLogic = true;
            }
        } catch (Throwable ex) {
            // ignore
        }

        // onEntityCollidedWithBlock (IBlockState)
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityCollidedWithBlock" : "func_180634_a";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, IBlockState.class, Entity.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (!clazz.equals(Block.class)) {
                this.hasCollideWithStateLogic = true;
            }
        } catch (Throwable ex) {
            // ignore
        }

        // onNeighborBlockChange
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onNeighborBlockChange" : "func_176204_a";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, IBlockState.class, Block.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (!clazz.equals(Block.class)) {
                this.hasNotifyNeighborLogic = true;
            }
        } catch (Throwable ex) {
            // ignore
        }

        // onBlockAdded - disable until it can be tested further
        /*try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onBlockAdded" : "func_176213_c";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, IBlockState.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (!clazz.equals(Block.class)) {
                this.hasOnBlockAddedLogic = true;
            }
        } catch (Throwable ex) {
            // ignore
        }*/
    }

    @Inject(method = "registerBlock", at = @At("RETURN"))
    private static void onRegisterBlock(int id, ResourceLocation location, Block block, CallbackInfo ci) {
        BlockTypeRegistryModule.getInstance().registerFromGameData(location.toString(), (BlockType) block);
    }

    @Override
    public String getId() {
        return Block.blockRegistry.getNameForObject((Block) (Object) this).toString();
    }

    @Override
    public String getName() {
        return Block.blockRegistry.getNameForObject((Block) (Object) this).toString();
    }

    @Override
    public BlockState getDefaultState() {
        return (BlockState) shadow$getDefaultState();
    }

    @Override
    public Optional<ItemType> getItem() {
        return Optional.ofNullable((ItemType) Item.getItemFromBlock((Block) (Object) this));
    }

    @Override
    public Translation getTranslation() {
        return new SpongeTranslation(getUnlocalizedName() + ".name");
    }

    /**
     * @author - unknown - Before April 2015
     * @reason Use our API defined value for whether this block
     * is to be ticked randomly.
     *
     * @return True if this block is to be ticked randomly
     */
    @Override
    @Overwrite
    public boolean getTickRandomly() {
        return this.needsRandomTick;
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
    }

    @Inject(method = "randomTick", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILEXCEPTION, cancellable = true)
    public void callRandomTickEvent(net.minecraft.world.World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (world.isRemote) {
            return;
        }

        BlockSnapshot blockSnapshot = ((World) world).createSnapshot(VecHelper.toVector(pos));
        final TickBlockEvent event = SpongeEventFactory.createTickBlockEvent(Cause.of(NamedCause.source(world)), blockSnapshot);
        SpongeImpl.postEvent(event);
        if(event.isCancelled()) {
            ci.cancel();
        }
    }

    @Override
    public boolean supports(Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return false;
    }

    @Override
    public Optional<BlockState> getStateWithData(IBlockState blockState, ImmutableDataManipulator<?, ?> manipulator) {
        return Optional.empty();
    }

    @Override
    public <E> Optional<BlockState> getStateWithValue(IBlockState blockState, Key<? extends BaseValue<E>> key, E value) {
        return Optional.empty(); // By default, all blocks just have a single state unless otherwise dictated.
    }

    @Override
    public List<ImmutableDataManipulator<?, ?>> getManipulators(IBlockState blockState) {
        return ImmutableList.of();
    }

    @Override
    public BlockState getDefaultBlockState() {
        return getDefaultState();
    }

    @Override
    public Collection<BlockTrait<?>> getTraits() {
        return getDefaultBlockState().getTraits();
    }

    @Override
    public Optional<BlockTrait<?>> getTrait(String blockTrait) {
        return getDefaultBlockState().getTrait(blockTrait);
    }

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "HEAD"), cancellable = true)
    public void onDropBlockAsItemWithChance(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune, CallbackInfo ci) {
        if (!worldIn.isRemote && ((IMixinWorld) worldIn).getCauseTracker().isRestoringBlocks()) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnAsEntity", at = @At(value = "HEAD"), cancellable = true)
    private static void onSpawnAsEntityHead(net.minecraft.world.World worldIn, BlockPos pos, net.minecraft.item.ItemStack stack, CallbackInfo ci) {
        if (!worldIn.isRemote && ((IMixinWorld) worldIn).getCauseTracker().isRestoringBlocks()) {
            ci.cancel();
        }
    }

    @Redirect(method = "spawnAsEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    private static boolean onSpawnAsEntity(net.minecraft.world.World worldIn, Entity entityitem) {
        final CauseTracker causeTracker = ((IMixinWorld) worldIn).getCauseTracker();
        if (!causeTracker.isCapturingBlocks()) {
            return worldIn.spawnEntityInWorld(entityitem);
        }

        // Grab the last block captured
        BlockSnapshot blockSnapshot = null;
        if (causeTracker.getCapturedSpongeBlockSnapshots().size() > 0) {
            blockSnapshot = causeTracker.getCapturedSpongeBlockSnapshots().get(causeTracker.getCapturedSpongeBlockSnapshots().size() - 1);
        }
        if (blockSnapshot == null) {
            blockSnapshot = BlockSnapshot.builder().from(new Location<>((World) worldIn, VecHelper.toVector(entityitem.getPosition()))).build();
        }
        BlockSpawnCause spawnCause = BlockSpawnCause.builder()
                .block(blockSnapshot)
                .type(SpawnTypes.DROPPED_ITEM)
                .build();
        IMixinEntity spongeEntity = (IMixinEntity) entityitem;
        spongeEntity.setSpawnCause(spawnCause);
        spongeEntity.setSpawnedFromBlockBreak(true);
        boolean preCaptureEntities = causeTracker.isCapturingSpawnedEntities();
        causeTracker.setCaptureSpawnedEntities(true);
        boolean result = worldIn.spawnEntityInWorld(entityitem);
        causeTracker.setCaptureSpawnedEntities(preCaptureEntities);
        return result;
    }

    /**
     * Redirects for block breaks to spawn entities with a custom spawn cause,
     * this will redirect to use the SpongeAPI world spawn entity method instead.
     *
     * @param world
     * @param entity
     * @return
     */
    @Redirect(method = "dropXpOnBlockBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntityInWorld(Lnet/minecraft/entity/Entity;)Z"))
    private boolean onSpawnEntityFromBlockBreak(net.minecraft.world.World world, Entity entity, net.minecraft.world.World worldIn, BlockPos posIn, int amount) {
        BlockSpawnCause spawnCause = BlockSpawnCause.builder()
                .block(BlockSnapshot.builder().from(new Location<>((World) world, VecHelper.toVector(posIn))).build())
                .type(SpawnTypes.EXPERIENCE)
                .build();
        IMixinEntity spongeEntity = (IMixinEntity) entity;
        spongeEntity.setSpawnCause(spawnCause);
        spongeEntity.setSpawnedFromBlockBreak(true);
        final CauseTracker causeTracker = ((IMixinWorld) world).getCauseTracker();
        boolean preCaptureEntities = causeTracker.isCapturingSpawnedEntities();
        causeTracker.setCaptureSpawnedEntities(true);
        boolean result = world.spawnEntityInWorld(entity);
        causeTracker.setCaptureSpawnedEntities(preCaptureEntities);
        return result;
    }

    @Override
    public boolean isVanilla() {
        return this.isVanilla;
    }

    @Override
    public boolean hasCollideLogic() {
        return this.hasCollideLogic;
    }

    @Override
    public boolean hasCollideWithStateLogic() {
        return this.hasCollideWithStateLogic;
    }

    @Override
    public boolean hasNotifyNeighborLogic() {
        return this.hasNotifyNeighborLogic;
    }

    @Override
    public boolean hasOnBlockAddedLogic() {
        return this.hasOnBlockAddedLogic;
    }

    @Override
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getBlockTiming((net.minecraft.block.Block)(Object) this);
        }
        return this.timing;
    }
}
