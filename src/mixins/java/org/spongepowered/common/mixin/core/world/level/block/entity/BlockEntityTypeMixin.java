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
package org.spongepowered.common.mixin.core.world.level.block.entity;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.ResourceKeyBridge;
import org.spongepowered.common.bridge.TrackableBridge;
import org.spongepowered.common.bridge.world.level.block.entity.BlockEntityTypeBridge;
import org.spongepowered.common.config.SpongeGameConfigs;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.config.tracker.BlockEntityTrackerCategory;
import org.spongepowered.common.config.tracker.TrackerConfig;
import org.spongepowered.plugin.PluginContainer;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeMixin implements ResourceKeyBridge, BlockEntityTypeBridge {

    private ResourceKey impl$key;
    private boolean impl$canTick;

    @Redirect(method = "register",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/core/Registry;register(Lnet/minecraft/core/Registry;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private static Object impl$setKeyAndInitializeTrackerState(final Registry<Object> registry, final String key, final Object tileEntityType) {
        final PluginContainer plugin = SpongeCommon.activePlugin();

        Registry.register(registry, key, tileEntityType);

        final ResourceKeyBridge resourceKeyBridge = (ResourceKeyBridge) tileEntityType;
        resourceKeyBridge.bridge$setKey(ResourceKey.of(plugin, key));

        final TrackableBridge trackableBridge = (TrackableBridge) tileEntityType;

        final ConfigHandle<TrackerConfig> trackerConfigAdapter = SpongeGameConfigs.getTracker();
        final BlockEntityTrackerCategory blockEntityTracker = trackerConfigAdapter.get().blockEntity;

        BlockEntityTrackerCategory.ModSubCategory modCapturing = blockEntityTracker.mods.get(plugin.metadata().id());

        if (modCapturing == null) {
            modCapturing = new BlockEntityTrackerCategory.ModSubCategory();
            blockEntityTracker.mods.put(plugin.metadata().id(), modCapturing);
        }

        if (!modCapturing.enabled) {
            trackableBridge.bridge$setAllowsBlockBulkCaptures(false);
            trackableBridge.bridge$setAllowsBlockEventCreation(false);
            trackableBridge.bridge$setAllowsEntityBulkCaptures(false);
            trackableBridge.bridge$setAllowsEntityEventCreation(false);
            modCapturing.blockBulkCapture.computeIfAbsent(key, k -> false);
            modCapturing.entityBulkCapture.computeIfAbsent(key, k -> false);
            modCapturing.blockEventCreation.computeIfAbsent(key, k -> false);
            modCapturing.entityEventCreation.computeIfAbsent(key, k -> false);
        } else {
            trackableBridge.bridge$setAllowsBlockBulkCaptures(modCapturing.blockBulkCapture.computeIfAbsent(key, k -> true));
            trackableBridge.bridge$setAllowsBlockEventCreation(modCapturing.blockEventCreation.computeIfAbsent(key, k -> true));
            trackableBridge.bridge$setAllowsEntityBulkCaptures(modCapturing.entityBulkCapture.computeIfAbsent(key, k -> true));
            trackableBridge.bridge$setAllowsEntityEventCreation(modCapturing.entityEventCreation.computeIfAbsent(key, k -> true));
        }

        if (blockEntityTracker.autoPopulate) {
            trackerConfigAdapter.save();
        }

        return tileEntityType;
    }

    @Override
    public ResourceKey bridge$getKey() {
        return this.impl$key;
    }

    @Override
    public void bridge$setKey(final ResourceKey key) {
        this.impl$key = key;
    }

    @Override
    public boolean bridge$canTick() {
        return this.impl$canTick;
    }

    @Override
    public boolean bridge$setCanTick(final boolean canTick) {
        return this.impl$canTick = canTick;
    }
}
