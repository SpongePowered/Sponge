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
package org.spongepowered.common.mixin.entitycollision.block;

import net.minecraft.block.Block;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.storage.IServerWorldInfoBridge;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.applaunch.config.common.CommonConfig;
import org.spongepowered.common.config.inheritable.CollisionModCategory;
import org.spongepowered.common.config.inheritable.EntityCollisionCategory;
import org.spongepowered.common.config.inheritable.WorldConfig;
import org.spongepowered.common.bridge.entitycollision.CollisionCapabilityBridge;

@Mixin(Block.class)
public abstract class BlockMixin_EntityCollision implements CollisionCapabilityBridge {

    private int entityCollision$maxCollisions = 8;
    private boolean entityCollision$refreshCache = true;

    @Override
    public ResourceKey collision$getKey() {
        return ((BlockType) this).getKey();
    }

    @Override
    public int collision$getMaxCollisions() {
        return this.entityCollision$maxCollisions;
    }

    @Override
    public void collision$setMaxCollisions(final int max) {
        this.entityCollision$maxCollisions = max;
    }

    @Override
    public void collision$requiresCollisionsCacheRefresh(final boolean flag) {
        this.entityCollision$refreshCache = flag;
    }

    @Override
    public boolean collision$requiresCollisionsCacheRefresh() {
        return this.entityCollision$refreshCache;
    }

    @Override
    public void collision$initializeCollisionState(final net.minecraft.world.World world) {
        final InheritableConfigHandle<WorldConfig> worldConfigAdapter = ((IServerWorldInfoBridge) world.getLevelData()).bridge$getConfigAdapter();
        final ConfigHandle<CommonConfig> globalConfigAdapter = SpongeConfigs.getCommon();
        final EntityCollisionCategory worldCollCat = worldConfigAdapter.get().getEntityCollisionCategory();

        this.collision$setMaxCollisions(worldCollCat.getMaxEntitiesWithinAABB());

        boolean requiresSave = false;
        String[] ids = ((BlockType) this).getKey().toString().split(":");
        String modId = ids[0];
        String name = ids[1];

        final CollisionModCategory worldCollMod = worldConfigAdapter.getOrCreateValue(s -> s.getEntityCollisionCategory().getModList().get(modId),
                c -> {
                // TODO: finish after populating?
                    final CollisionModCategory globalCollision = new CollisionModCategory(modId);
                    c.getEntityCollisionCategory().getModList().put(modId, globalCollision);
                    globalCollision.getBlockList().put(name, this.collision$getMaxCollisions());
                }, worldCollCat.autoPopulateData());
        if (worldCollMod != null) {
            if (!worldCollMod.isEnabled()) {
                this.collision$setMaxCollisions(-1);
                return;
            }
            // check mod overrides
            Integer modCollisionMax = worldCollMod.getDefaultMaxCollisions().get("blocks");
            if (modCollisionMax != null) {
                this.collision$setMaxCollisions(modCollisionMax);
            }

            // entity overrides
            Integer blockMaxCollision = worldCollMod.getBlockList().get(name);
            if (blockMaxCollision == null && worldCollCat.autoPopulateData()) {
                worldCollMod.getBlockList().put(name, this.collision$getMaxCollisions());
                requiresSave = true;
            } else if (blockMaxCollision != null) {
                this.collision$setMaxCollisions(blockMaxCollision);
            }
        }

        // don't bother saving for negative values
        if (this.collision$getMaxCollisions() <= 0) {
            return;
        }

        if (requiresSave) {
            globalConfigAdapter.save();
        }
    }
}
