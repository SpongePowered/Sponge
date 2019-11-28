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
package org.spongepowered.common.mixin.entitycollisions;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.CollisionModCategory;
import org.spongepowered.common.config.category.EntityCollisionCategory;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.CollisionsCapability;

@Mixin(Block.class)
public abstract class BlockMixin_Collisions implements CollisionsCapability {

    private int collision$maxCollisions = 8;
    private String collision$modId;
    @SuppressWarnings("unused")
    private String collision$modBlockName;
    private boolean collision$refreshCache = true;

    @Override
    public int collision$getMaxCollisions() {
        return this.collision$maxCollisions;
    }

    @Override
    public void collision$setMaxCollisions(int max) {
        this.collision$maxCollisions = max;
    }

    @Override
    public void collision$setModDataName(String name) {
        this.collision$modBlockName = name;
    }

    @Override
    public String collision$getModDataId() {
        return this.collision$modId;
    }

    @Override
    public void collision$setModDataId(String id) {
        this.collision$modId = id;
    }

    @Override
    public void collision$requiresCollisionsCacheRefresh(boolean flag) {
        this.collision$refreshCache = flag;
    }

    @Override
    public boolean collision$requiresCollisionsCacheRefresh() {
        return this.collision$refreshCache;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public void collision$initializeCollisionState(World world) {
        final SpongeConfig<WorldConfig> worldConfigAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        final SpongeConfig<GlobalConfig> globalConfigAdapter = SpongeImpl.getGlobalConfigAdapter();
        final EntityCollisionCategory worldCollCat = worldConfigAdapter.getConfig().getEntityCollisionCategory();
        final EntityCollisionCategory globalCollCat = globalConfigAdapter.getConfig().getEntityCollisionCategory();

        this.collision$setMaxCollisions(worldCollCat.getMaxEntitiesWithinAABB());
        
        boolean requiresSave = false;
        String[] ids = ((BlockType) this).getId().split(":");
        String modId = ids[0];
        String name = ids[1];

        CollisionModCategory worldCollMod = worldCollCat.getModList().get(modId);
        CollisionModCategory globalCollMod = globalCollCat.getModList().get(modId);
        if (worldCollMod == null && worldCollCat.autoPopulateData()) {
            globalCollMod = new CollisionModCategory(modId);
            globalCollCat.getModList().put(modId, globalCollMod);
            globalCollMod.getBlockList().put(name, this.collision$getMaxCollisions());
            globalConfigAdapter.save();
            return;
        } else if (worldCollMod != null) {
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
                globalCollMod.getBlockList().put(name, this.collision$getMaxCollisions());
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
