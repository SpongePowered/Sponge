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
package org.spongepowered.common.mixin.api.mcp.entity.item;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityTNTPrimed;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.entity.item.TNTPrimedEntityBridge;
import org.spongepowered.common.bridge.explosives.FusedExplosiveBridge;
import org.spongepowered.common.mixin.api.mcp.entity.EntityMixin_API;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(EntityTNTPrimed.class)
public abstract class EntityTNTPrimedMixin_API extends EntityMixin_API implements PrimedTNT {

    private static final BlockType BLOCK_TYPE = BlockTypes.TNT;

    @Shadow private int fuse;
    @Shadow @Nullable private EntityLivingBase tntPlacedBy;
    @Shadow private void explode() { }

    private int fuseDuration = 80;
    @Override
    public Optional<Living> getDetonator() {
        return Optional.ofNullable((Living) this.tntPlacedBy);
    }

    // FusedExplosive Impl

    @Override
    public void defuse() {
        checkState(isPrimed(), "not primed");
        if (((FusedExplosiveBridge) this).bridge$shouldDefuse()) {
            setDead();
            // Place a TNT block at the Entity's position
            Sponge.getCauseStackManager().pushCause(this);
            getWorld().setBlock((int) this.posX, (int) this.posY, (int) this.posZ, BlockState.builder().blockType(BLOCK_TYPE).build(), BlockChangeFlags.ALL);
            Sponge.getCauseStackManager().popCause();
            ((FusedExplosiveBridge) this).bridge$postDefuse();
        }
    }

    @Override
    public void prime() {
        checkState(!isPrimed(), "already primed");
        checkState(this.isDead, "tnt about to be primed");
        getWorld().spawnEntity(this);
    }

    @Override
    public boolean isPrimed() {
        return this.fuse > 0 && this.fuse < this.fuseDuration && !this.isDead || ((TNTPrimedEntityBridge) this).bridge$isExploding();
    }

    @Override
    public void detonate() {
        setDead();
        explode();
    }



}
