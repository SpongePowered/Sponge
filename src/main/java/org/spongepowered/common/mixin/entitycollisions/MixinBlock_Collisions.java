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
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.CollisionModCategory;
import org.spongepowered.common.config.category.EntityCollisionCategory;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.IModData_Collisions;

@Mixin(Block.class)
public abstract class MixinBlock_Collisions implements IModData_Collisions {

    private int maxCollisions = 8;
    private String modId;
    @SuppressWarnings("unused")
    private String modBlockName;
    private boolean refreshCache = true;

    @Override
    public int getMaxCollisions() {
        return this.maxCollisions;
    }

    @Override
    public void setMaxCollisions(int max) {
        this.maxCollisions = max;
    }

    @Override
    public void setModDataName(String name) {
        this.modBlockName = name;
    }

    @Override
    public String getModDataId() {
        return this.modId;
    }

    @Override
    public void setModDataId(String id) {
        this.modId = id;
    }

    @Override
    public void requiresCollisionsCacheRefresh(boolean flag) {
        this.refreshCache = flag;
    }

    @Override
    public boolean requiresCollisionsCacheRefresh() {
        return this.refreshCache;
    }

    @Override
    public void initializeCollisionState(World worldIn) {
        SpongeConfig<? extends GeneralConfigBase> activeConfig = ((IMixinWorldServer) worldIn).getActiveConfig();
        EntityCollisionCategory collisionCat = activeConfig.getConfig().getEntityCollisionCategory();
        this.setMaxCollisions(collisionCat.getMaxEntitiesWithinAABB());
        String[] ids = ((BlockType) this).getId().split(":");
        String modId = ids[0];
        String name = ids[1];
        CollisionModCategory collisionMod = collisionCat.getModList().get(modId);
        if (collisionMod == null && activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
            collisionMod = new CollisionModCategory(modId);
            collisionCat.getModList().put(modId, collisionMod);
            collisionMod.getBlockList().put(name, this.getMaxCollisions());
            if (activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
                activeConfig.save();
            }

            return;
        } else if (collisionMod != null) {
            if (!collisionMod.isEnabled()) {
                this.setMaxCollisions(-1);
                return;
            }
            // check mod overrides
            Integer modCollisionMax = collisionMod.getDefaultMaxCollisions().get("blocks");
            if (modCollisionMax != null) {
                this.setMaxCollisions(modCollisionMax);
            }

            Integer blockMaxCollision = collisionMod.getBlockList().get(name);
            // entity overrides
            if (blockMaxCollision == null && activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
                collisionMod.getBlockList().put(name, this.getMaxCollisions());
            } else if (blockMaxCollision != null) {
                this.setMaxCollisions(blockMaxCollision);
            }
        }

        if (this.getMaxCollisions() <= 0) {
            return;
        }

        if (activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
            activeConfig.save();
        }
    }
}
