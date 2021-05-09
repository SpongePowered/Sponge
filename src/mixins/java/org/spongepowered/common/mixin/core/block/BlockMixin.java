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

import co.aikar.timings.Timing;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.TimingBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.block.BlockBridge;
import org.spongepowered.common.bridge.block.DyeColorBlockBridge;
import co.aikar.timings.sponge.SpongeTimings;

import javax.annotation.Nullable;
import net.minecraft.world.level.block.Block;
import org.spongepowered.common.util.ReflectionUtil;

@Mixin(value = Block.class)
public abstract class BlockMixin implements BlockBridge, TrackableBridge, TimingBridge {

    private final boolean impl$isVanilla = this.getClass().getName().startsWith("net.minecraft.");
    private boolean impl$hasCollideLogic = ReflectionUtil.isStepOnDeclared(this.getClass());
    private boolean impl$hasCollideWithStateLogic = ReflectionUtil.isEntityInsideDeclared(this.getClass());
    // Used to determine if this block needs to be handled in WorldServer#addBlockEvent
    private boolean impl$shouldFireBlockEvents = true;
    @Nullable private Timing impl$timing;
    // Used by tracker config
    private boolean impl$allowsBlockBulkCapture = true;
    private boolean impl$allowsEntityBulkCapture = true;
    private boolean impl$allowsBlockEventCreation = true;
    private boolean impl$allowsEntityEventCreation = true;

    /**
     * We captured the dye color when creating the Block.Properties.
     * As the Properties objects are discarded we transfer it over to the Block itself now.
     */

    @Inject(method = "<init>", at = @At("RETURN"))
    private void impl$setUpSpongeFields(final Block.Properties properties, final CallbackInfo ci) {
        ((DyeColorBlockBridge) this).bridge$setDyeColor(((DyeColorBlockBridge)properties).bridge$getDyeColor().orElse(null));
    }

/*
    @Inject(method = "spawnAsEntity",
            at = @At(value = "NEW", target = "net/minecraft/entity/item/ItemEntity"),
            cancellable = true,
            locals = LocalCapture.CAPTURE_FAILSOFT,
            require = 0,
            expect = 0
    )
    private static void impl$throwConstructPreEvent(
            final net.minecraft.world.World worldIn, final BlockPos pos, final ItemStack stack, final CallbackInfo ci,
            final float unused, final double xOffset, final double yOffset, final double zOffset) {
        if (!ShouldFire.CONSTRUCT_ENTITY_EVENT_PRE) {
            return;
        }
        final double xPos = (double) pos.getX() + xOffset;
        final double yPos = (double) pos.getY() + yOffset;
        final double zPos = (double) pos.getZ() + zOffset;
        // Go ahead and throw the construction event
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(worldIn.getBlockState(pos));
            final ConstructEntityEvent.Pre eventPre = SpongeEventFactory.createConstructEntityEventPre(frame.getCurrentCause(), ServerLocation.of((ServerWorld) worldIn, xPos, yPos, zPos), new Vector3d(0, 0, 0), EntityTypes.ITEM.get());
            SpongeCommon.postEvent(eventPre);
            if (eventPre.isCancelled()) {
                ci.cancel();
            }
        }
    }
*/

    @Override
    public boolean bridge$isVanilla() {
        return this.impl$isVanilla;
    }

    @Override
    public boolean bridge$hasCollideLogic() {
        return this.impl$hasCollideLogic;
    }

    @Override
    public boolean bridge$hasCollideWithStateLogic() {
        return this.impl$hasCollideWithStateLogic;
    }

    @Override
    public Timing bridge$getTimingsHandler() {
        if (this.impl$timing == null) {
            this.impl$timing = SpongeTimings.getBlockTiming((BlockType) this);
        }
        return this.impl$timing;
    }

    @Override
    public boolean bridge$allowsBlockBulkCaptures() {
        return this.impl$allowsBlockBulkCapture;
    }

    @Override
    public boolean bridge$allowsEntityBulkCaptures() {
        return this.impl$allowsEntityBulkCapture;
    }

    @Override
    public boolean bridge$allowsBlockEventCreation() {
        return this.impl$allowsBlockEventCreation;
    }

    @Override
    public boolean bridge$allowsEntityEventCreation() {
        return this.impl$allowsEntityEventCreation;
    }

    @Override
    public void bridge$refreshTrackerStates() {
        // not needed
    }

    /**
     * Used to determine if this block should fire
     * sponge events during WorldServer#addBlockEvent.
     */
    @Override
    public boolean bridge$shouldFireBlockEvents() {
        return this.impl$shouldFireBlockEvents;
    }

    /*
    @SuppressWarnings("ConstantConditions")
    @Override
    public void bridge$initializeTrackerState() {
        final ConfigHandle<TrackerConfig> trackerConfigAdapter = SpongeConfigs.getTracker();
        final BlockTrackerCategory blockTrackerCat = trackerConfigAdapter.get().getBlockTracker();
        final ResourceLocation key = Registry.BLOCK.getKey((Block) (Object) this);
        final String modId = key.getNamespace();
        final String name = key.getPath();

        BlockTrackerModCategory blockTrackerModCat = blockTrackerCat.getModMappings().get(modId);

        if (blockTrackerModCat == null) {
            blockTrackerModCat = new BlockTrackerModCategory();
            blockTrackerCat.getModMappings().put(modId, blockTrackerModCat);
        }

        // Determine if this block needs to be handled during WorldServer#addBlockEvent
        if ((Block) (Object) this instanceof SpawnerBlock || (Block) (Object) this instanceof EnderChestBlock
            || (Block) (Object) this instanceof ChestBlock || (Block) (Object) this instanceof ShulkerBoxBlock
            || (Block) (Object) this instanceof EndGatewayBlock || (Block) (Object) this instanceof BeaconBlock) {
            this.impl$shouldFireBlockEvents = false;
        }
        // Determine which blocks can avoid executing un-needed event logic
        // This will allow us to avoid running event logic for blocks that do nothing such as grass collisions
        // -- blood
        // @author gabizou - October 9th, 2018
        // @reason Due to early class initialization and object instantiation, a lot of the reflection access
        // logic can be delayed until actual type registration with sponge. This will at the very least allow
        // mod type registrations to go through without getting the overall cost of reflection during object construction.

        if (!blockTrackerModCat.isEnabled()) {
            this.impl$allowsBlockBulkCapture = false;
            this.impl$allowsEntityBulkCapture = false;
            this.impl$allowsBlockEventCreation = false;
            this.impl$allowsEntityEventCreation = false;
            blockTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsBlockBulkCapture);
            blockTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsEntityBulkCapture);
            blockTrackerModCat.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsBlockEventCreation);
            blockTrackerModCat.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> this.impl$allowsEntityEventCreation);
        } else {
            this.impl$allowsBlockBulkCapture = blockTrackerModCat.getBlockBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
            this.impl$allowsEntityBulkCapture = blockTrackerModCat.getEntityBulkCaptureMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
            this.impl$allowsBlockEventCreation = blockTrackerModCat.getBlockEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
            this.impl$allowsEntityEventCreation = blockTrackerModCat.getEntityEventCreationMap().computeIfAbsent(name.toLowerCase(Locale.ENGLISH), k -> true);
        }

        if (blockTrackerCat.autoPopulateData()) {
            trackerConfigAdapter.save();
        }
    }
     */
}
