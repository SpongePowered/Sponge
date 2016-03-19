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
import org.spongepowered.common.event.tracking.phase.BlockPhase;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
public abstract class MixinBlock implements BlockType, IMixinBlock {

    @Shadow private boolean needsRandomTick;

    @Shadow public abstract boolean isBlockNormalCube();
    @Shadow public abstract boolean getEnableStats();
    @Shadow public abstract int getLightValue();
    @Shadow public abstract String getUnlocalizedName();
    @Shadow public abstract IBlockState getStateFromMeta(int meta);
    @Shadow public abstract Material getMaterial();
    @Shadow(prefix = "shadow$")
    public abstract IBlockState shadow$getDefaultState();

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
        if (((IMixinWorldServer) worldIn).getCauseTracker().getPhases().peekState() == BlockPhase.State.RESTORING_BLOCKS) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnAsEntity", at = @At(value = "HEAD"), cancellable = true)
    private static void onSpawnAsEntity(net.minecraft.world.World worldIn, BlockPos pos, net.minecraft.item.ItemStack stack, CallbackInfo ci) {
        if (((IMixinWorldServer) worldIn).getCauseTracker().getPhases().peekState() == BlockPhase.State.RESTORING_BLOCKS) {
            ci.cancel();
        }
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
                .type(InternalSpawnTypes.EXPERIENCE)
                .build();
        return ((World) world).spawnEntity(((org.spongepowered.api.entity.Entity) entity), Cause.of(NamedCause.source(spawnCause)));
    }
}
