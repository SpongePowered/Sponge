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
package org.spongepowered.common.mixin.core.entity.player;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.Container;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.CollideEntityEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.Sponge;
import org.spongepowered.common.interfaces.IMixinEntityPlayer;
import org.spongepowered.common.mixin.core.entity.living.MixinEntityLivingBase;
import org.spongepowered.common.util.VecHelper;

import java.util.List;

@NonnullByDefault
@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase implements IMixinEntityPlayer {

    @Shadow public Container inventoryContainer;
    @Shadow public Container openContainer;
    @Shadow public int experienceLevel;
    @Shadow public int experienceTotal;
    @Shadow public float experience;
    @Shadow public PlayerCapabilities capabilities;
    @Shadow public abstract int xpBarCap();
    @Shadow public abstract FoodStats getFoodStats();
    @Shadow public abstract GameProfile getGameProfile();
    @Shadow public abstract IChatComponent getDisplayName();
    @Shadow public abstract void addExperience(int amount);
    @Shadow public abstract Scoreboard getWorldScoreboard();
    @Shadow public abstract boolean isSpectator();
    @Shadow private BlockPos spawnChunk;
    @Shadow private BlockPos playerLocation;
    @Shadow protected FoodStats foodStats;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target="Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/AxisAlignedBB;)Ljava/util/List;"))
    public List onGetEntitiesWithinAABB(net.minecraft.world.World world, net.minecraft.entity.Entity entity, net.minecraft.util.AxisAlignedBB aabb) {
        List list = world.getEntitiesWithinAABBExcludingEntity(entity, aabb);
        CollideEntityEvent event = SpongeEventFactory.createCollideEntityEvent(Sponge.getGame(), Cause.of(this), (List<Entity>)(List<?>)ImmutableList.copyOf(list), (List<Entity>)(List<?>)list, (World) this.worldObj);
        Sponge.getGame().getEventManager().post(event);
        if (event.isCancelled()) {
            list.clear();
        }
        return list;
    }

    // utility method for getting the total experience at an arbitrary level
    // the formulas here are basically (slightly modified) integrals of those of EntityPlayer#xpBarCap()
    private int xpAtLevel(int level) {
        if (level > 30) {
            return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        } else if (level > 15) {
            return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else {
            return (int) (Math.pow(level, 2) + 6 * level);
        }
    }

    public int getExperienceSinceLevel() {
        return this.getTotalExperience() - xpAtLevel(this.getLevel());
    }

    public void setExperienceSinceLevel(int experience) {
        this.setTotalExperience(xpAtLevel(this.experienceLevel) + experience);
    }

    public int getExperienceBetweenLevels() {
        return this.xpBarCap();
    }

    public int getLevel() {
        return this.experienceLevel;
    }

    public void setLevel(int level) {
        this.experienceLevel = level;
    }

    public int getTotalExperience() {
        return this.experienceTotal;
    }

    public void setTotalExperience(int exp) {
        this.experienceTotal = exp;
    }

    public boolean isFlying() {
        return this.capabilities.isFlying;
    }

    public void setFlying(boolean flying) {
        this.capabilities.isFlying = flying;
    }
    
    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "isPlayerSleeping()Z"))
    public boolean onIsPlayerSleeping(EntityPlayer self) {
        if (self.isPlayerSleeping()) {
            Sponge.getGame().getEventManager().post(SpongeEventFactory.createSleepingEventTick(Sponge.getGame(),
                    Cause.of(this), this.getWorld().createSnapshot(VecHelper.toVector(this.playerLocation)), this));
            return true;
        }
        return false;
    }
}
