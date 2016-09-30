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
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.trait.BlockTrait;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseData;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.event.tracking.ItemDropData;
import org.spongepowered.common.interfaces.block.IMixinBlock;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.BlockTypeRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;
import org.spongepowered.common.util.VecHelper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@NonnullByDefault
@Mixin(value = Block.class, priority = 999)
@Implements(@Interface(iface = BlockType.class, prefix = "block$"))
public abstract class MixinBlock implements BlockType, IMixinBlock {

    private final boolean isVanilla = getClass().getName().startsWith("net.minecraft.");
    private boolean hasCollideLogic;
    private boolean hasCollideWithStateLogic;
    private boolean requiresLocationCheckForLight;
    private boolean requiresLocationCheckForOpacity;
    private Timing timing;

    @Shadow private boolean needsRandomTick;

    @Shadow public abstract String getUnlocalizedName();
    @Shadow public abstract Material getMaterial(IBlockState state);
    @Shadow public abstract IBlockState shadow$getDefaultState();
    @Shadow public abstract void dropBlockAsItem(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, int fortune);

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
        // Determine which blocks can avoid executing un-needed event logic
        // This will allow us to avoid running event logic for blocks that do nothing such as grass collisions
        // -- blood

        this.hasCollideLogic = true;
        this.hasCollideWithStateLogic = true;
        this.requiresLocationCheckForLight = true;
        this.requiresLocationCheckForOpacity = true;

        // onEntityCollidedWithBlock
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityCollidedWithBlock" : "func_176199_a";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, Entity.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.hasCollideLogic = false;
            }
        } catch (Throwable ex) {
            // ignore
        }

        // onEntityCollidedWithBlock (IBlockState)
        try {
            String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "onEntityCollidedWithBlock" : "func_180634_a";
            Class<?>[] argTypes = { net.minecraft.world.World.class, BlockPos.class, IBlockState.class, Entity.class };
            Class<?> clazz = this.getClass().getMethod(mapping, argTypes).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.hasCollideWithStateLogic = false;
            }
        } catch (Throwable ex) {
            // ignore
        }

        // requiresLocationCheckForLightValue // Forge added method getLightValue that takes a state, world, and position
        try {
            Class<?>[] args = {IBlockState.class, IBlockAccess.class, BlockPos.class};
            Class<?> clazz = this.getClass().getMethod("getLightValue", args).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.requiresLocationCheckForLight = false;
            }
        } catch (Throwable throwable) {
            // Ignore
        }

        // requiresLocationCheckForOpacity // Forge added method getLightValue that takes a state, world, and position
        try {
            Class<?>[] args = {IBlockState.class, IBlockAccess.class, BlockPos.class};
            Class<?> clazz = this.getClass().getMethod("getLightOpacity", args).getDeclaringClass();
            if (clazz.equals(Block.class)) {
                this.requiresLocationCheckForOpacity = false;
            }
        } catch (Throwable throwable) {
            // Ignore
        }
    }

    @Inject(method = "registerBlock", at = @At("RETURN"))
    private static void onRegisterBlock(int id, ResourceLocation location, Block block, CallbackInfo ci) {
        BlockTypeRegistryModule.getInstance().registerFromGameData(location.toString(), (BlockType) block);
    }

    @Override
    public String getId() {
        return Block.REGISTRY.getNameForObject((Block) (Object) this).toString();
    }

    @Override
    public String getName() {
        return Block.REGISTRY.getNameForObject((Block) (Object) this).toString();
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

    @Intrinsic
    public boolean block$getTickRandomly() {
        return this.getTickRandomly();
    }

    @Override
    public void setTickRandomly(boolean tickRandomly) {
        this.needsRandomTick = tickRandomly;
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

    @Redirect(method = "dropBlockAsItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropBlockAsItemWithChance(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;FI)V"))
    private void onDropItemWithFortuneChances(Block block, net.minecraft.world.World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (world instanceof IMixinWorldServer && CauseTracker.ENABLED) {
            TrackingUtil.performBlockDrop(block, (IMixinWorldServer) world, pos, state, chance, fortune);
            return;
        }
        block.dropBlockAsItemWithChance(world, pos, state, chance, fortune);

    }

    @Inject(method = "spawnAsEntity", at = @At(value = "NEW", args = {"class=net/minecraft/entity/item/EntityItem"}), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private static void checkSpawnAsEntity(net.minecraft.world.World worldIn, BlockPos pos, ItemStack stack, CallbackInfo callbackInfo, float chance, double x, double y, double z) {
        Transform<World> position = new Transform<>((World) worldIn, new Vector3d(x, y, z));
        final ConstructEntityEvent.Pre
                eventPre =
                SpongeEventFactory.createConstructEntityEventPre(Cause.source(worldIn.getBlockState(pos)).build(), EntityTypes.ITEM, position);
        SpongeImpl.postEvent(eventPre);
        if (eventPre.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = "dropBlockAsItemWithChance", at = @At(value = "HEAD"), cancellable = true)
    public void onDropBlockAsItemWithChance(net.minecraft.world.World worldIn, BlockPos pos, IBlockState state, float chance, int fortune, CallbackInfo ci) {
        if (!worldIn.isRemote && ((IMixinWorldServer) worldIn).getCauseTracker().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS) {
            ci.cancel();
        }
    }

    @Inject(method = "spawnAsEntity", at = @At(value = "HEAD"), cancellable = true)
    private static void onSpawnAsEntity(net.minecraft.world.World worldIn, BlockPos pos, net.minecraft.item.ItemStack stack, CallbackInfo ci) {
        if (!worldIn.isRemote && ((IMixinWorldServer) worldIn).getCauseTracker().getCurrentState() == BlockPhase.State.RESTORING_BLOCKS) {
            ci.cancel();
        }
    }

    @Redirect(method = "spawnAsEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules;getBoolean(Ljava/lang/String;)Z"))
    private static boolean redirectGameRulesToCaptureItemDrops(GameRules gameRules, String argument, net.minecraft.world.World worldIn, BlockPos pos, ItemStack stack) {
        final boolean allowTileDrops = gameRules.getBoolean(argument);
        if (allowTileDrops && worldIn instanceof IMixinWorldServer) {
            final IMixinWorldServer mixin = (IMixinWorldServer) worldIn;
            final PhaseData currentPhase = mixin.getCauseTracker().getCurrentPhaseData();
            final IPhaseState currentState = currentPhase.state;
            if (currentState.tracksBlockSpecificDrops()) {
                final PhaseContext context = currentPhase.context;
                final Multimap<BlockPos, ItemDropData> multimap = context.getCapturedBlockDrops();
                final Collection<ItemDropData> itemStacks = multimap.get(pos);
                SpongeImplHooks.addItemStackToListForSpawning(itemStacks, ItemDropData.item(stack).position(VecHelper.toVector3d(pos)).build());
                return false;
            }
        }
        return allowTileDrops;
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
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getBlockTiming((net.minecraft.block.Block)(Object) this);
        }
        return this.timing;
    }

    @Override
    public boolean requiresLocationCheckForLightValue() {
        return this.requiresLocationCheckForLight;
    }

    @Override
    public boolean requiresLocationCheckForOpacity() {
        return this.requiresLocationCheckForOpacity;
    }
}
