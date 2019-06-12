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

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.CollisionModCategory;
import org.spongepowered.common.config.category.EntityCollisionCategory;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.IModData_Collisions;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1002)
public class MixinEntity_Collisions implements IModData_Collisions {

    private int maxCollisions = 8;
    private boolean refreshCache = false;
    private SpongeEntityType spongeEntityType;
    private String entityName = "unknown";
    private String entityModId = "unknown";
    @Shadow public World world;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onEntityConstruction(World world, CallbackInfo ci) {
        if (world != null && !((WorldBridge) world).isFake() && ((IMixinWorldInfo) world.getWorldInfo()).isValid()) {
            EntityType entityType = ((Entity) this).getType();
            if (entityType == EntityTypes.UNKNOWN || !(entityType instanceof SpongeEntityType)) {
                return;
            }
            this.spongeEntityType = (SpongeEntityType) entityType;

            if ((Object) this instanceof EntityItem) {
                EntityItem item = (EntityItem) (Object) this;
                ItemStack itemstack = item.getItem();
                if (!itemstack.isEmpty()) {
                    this.entityName = itemstack.getTranslationKey().replace("item.", "");
                }
            } else {
                this.entityName = this.spongeEntityType.getName();
            }

            this.entityModId = this.spongeEntityType.getModId();
            if (!this.world.isRemote) {
                initializeCollisionState(this.world);
            }
        }
    }

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
        this.entityName = name;
    }

    @Override
    public String getModDataName() {
        return this.entityName;
    }

    @Override
    public String getModDataId() {
        return this.entityModId;
    }

    @Override
    public void setModDataId(String id) {
        this.entityModId = id;
    }

    @Override
    public void initializeCollisionState(World world) {
        final SpongeConfig<WorldConfig> worldConfigAdapter = ((IMixinWorldInfo) world.getWorldInfo()).getConfigAdapter();
        final SpongeConfig<GlobalConfig> globalConfigAdapter = SpongeImpl.getGlobalConfigAdapter();
        final EntityCollisionCategory worldCollCat = worldConfigAdapter.getConfig().getEntityCollisionCategory();
        final EntityCollisionCategory globalCollCat = globalConfigAdapter.getConfig().getEntityCollisionCategory();

        this.setMaxCollisions(worldCollCat.getMaxEntitiesWithinAABB());

        boolean requiresSave = false;
        CollisionModCategory worldCollMod = worldCollCat.getModList().get(this.getModDataId());
        CollisionModCategory globalCollMod = globalCollCat.getModList().get(this.getModDataId());
        if (worldCollMod == null && worldCollCat.autoPopulateData()) {
            globalCollMod = new CollisionModCategory(this.getModDataId());
            globalCollCat.getModList().put(this.getModDataId(), globalCollMod);
            globalCollMod.getEntityList().put(this.getModDataName(), this.getMaxCollisions());
            globalConfigAdapter.save();
            return;
        } else if (worldCollMod != null) {
            if (!worldCollMod.isEnabled()) {
                this.setMaxCollisions(-1);
                return;
            }
            // check mod overrides
            Integer modCollisionMax = worldCollMod.getDefaultMaxCollisions().get("entities");
            if (modCollisionMax != null) {
                this.setMaxCollisions(modCollisionMax);
            }

            Integer entityMaxCollision = null;
            if ((Object) this instanceof EntityItem) {
                // check if all items are overridden
                entityMaxCollision = worldCollMod.getEntityList().get(this.getModDataName());
            }

            if (entityMaxCollision == null) {
                entityMaxCollision = worldCollMod.getEntityList().get(this.getModDataName());
            }

            // entity overrides
            if (entityMaxCollision == null && worldCollCat.autoPopulateData()) {
                globalCollMod.getEntityList().put(this.getModDataName(), this.getMaxCollisions());
                requiresSave = true;
            } else if (entityMaxCollision != null) {
                this.setMaxCollisions(entityMaxCollision);
            }
        }

        // don't bother saving for negative values
        if (this.getMaxCollisions() <= 0) {
            return;
        }

        if (requiresSave) {
            globalConfigAdapter.save();
        }
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
    public boolean isRunningCollideWithNearby() {
        return false;
    }
}
