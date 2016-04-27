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

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.SpongeConfig.CollisionModNode;
import org.spongepowered.common.config.SpongeConfig.EntityCollisionCategory;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.IModData_Collisions;

import java.util.List;
import java.util.Optional;

@Mixin(net.minecraft.world.chunk.Chunk.class)
public class MixinChunk_Collisions {

    @Shadow @Final private World worldObj;

    @Inject(method = "getEntitiesWithinAABBForEntity", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), cancellable = true)
    public void onAddCollisionEntity(Entity entityIn, AxisAlignedBB aabb, List<Entity> listToFill, Predicate<? super Entity> predicate,
            CallbackInfo ci) {
        // ignore players
        if (entityIn instanceof EntityPlayer) {
            return;
        }

        if (!allowEntityCollision(listToFill)) {
            ci.cancel();
        }
    }

    @Inject(method = "getEntitiesOfTypeWithinAAAB", at = @At(value = "INVOKE", args = "log=true", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), cancellable = true)
    public <T extends Entity> void onAddCollisionEntity(Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill,
            Predicate<? super T> p_177430_4_, CallbackInfo ci) {
        // ignore player checks
        // ignore item check (ex. Hoppers)
        if (EntityPlayer.class.isAssignableFrom(entityClass) || EntityItem.class == entityClass) {
            return;
        }

        if (!allowEntityCollision(listToFill)) {
            ci.cancel();
        }
    }

    private <T extends Entity> boolean allowEntityCollision(List<T> listToFill) {
        IMixinWorld spongeWorld = (IMixinWorld) this.worldObj;
        if (spongeWorld.isProcessingExplosion()) {
            // allow explosions
            return true;
        }

        Optional<org.spongepowered.api.entity.Entity> tickEntity = spongeWorld.getCauseTracker().getCurrentTickEntity();
        Optional<BlockSnapshot> tickBlock = spongeWorld.getCauseTracker().getCurrentTickBlock();
        if (!tickEntity.isPresent()) {// && !tickBlock.isPresent()) {
            if (tickBlock.isPresent()) {
                BlockType blockType = tickBlock.get().getState().getType();
                IModData_Collisions spongeBlock = (IModData_Collisions) blockType;
                if (spongeBlock.requiresCacheRefresh()) {
                    initializeCollisionState(blockType);
                    spongeBlock.requiresCacheRefresh(false);
                }

                if ((spongeBlock.getMaxCollisions() >= 0) && (listToFill.size() >= spongeBlock.getMaxCollisions())) {
                    return false;
                }
            }
            return true;
        }

        IModData_Collisions spongeEntity = (IModData_Collisions) tickEntity.get();
        if ((spongeEntity.getMaxCollisions() >= 0) && (listToFill.size() >= spongeEntity.getMaxCollisions())) {
            return false;
        }

        return true;
    }

    private void initializeCollisionState(BlockType type) {
        SpongeConfig<?> activeConfig = ((IMixinWorld) this.worldObj).getActiveConfig();
        EntityCollisionCategory collisionCat = activeConfig.getConfig().getEntityCollisionCategory();
        IModData_Collisions spongeBlock = (IModData_Collisions) type;
        spongeBlock.setMaxCollisions(collisionCat.getMaxEntitiesWithinAABB());
        String[] ids = type.getId().split(":");
        String modId = ids[0];
        String name = ids[1];
        CollisionModNode collisionMod = collisionCat.getModList().get(modId);
        if (collisionMod == null && activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
            collisionMod = new CollisionModNode(modId);
            collisionCat.getModList().put(modId, collisionMod);
            collisionMod.getBlockList().put(name, spongeBlock.getMaxCollisions());
            if (activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
                activeConfig.save();
            }

            return;
        } else if (collisionMod != null) {
            // check mod overrides
            Integer modCollisionMax = collisionMod.getDefaultMaxCollisions().get("blocks");
            if (modCollisionMax != null) {
                spongeBlock.setMaxCollisions(modCollisionMax);
            }

            Integer blockMaxCollision = collisionMod.getBlockList().get(name);
            // entity overrides
            if (blockMaxCollision == null && activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
                collisionMod.getBlockList().put(name, spongeBlock.getMaxCollisions());
            } else if (blockMaxCollision != null) {
                spongeBlock.setMaxCollisions(blockMaxCollision);
            }
        }

        if (spongeBlock.getMaxCollisions() <= 0) {
            return;
        }

        if (activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
            activeConfig.save();
        }
    }
}
