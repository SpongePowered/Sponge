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

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.inventory.Container;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.interfaces.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.util.VecHelper;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase implements IMixinEntityPlayer {

    private static final String WORLD_SPAWN_PARTICLE = "Lnet/minecraft/world/World.spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V";
    private static final String WORLD_PLAY_SOUND_AT =
            "Lnet/minecraft/world/World;playSoundToNearExcept(Lnet/minecraft/entity/player/EntityPlayer;Ljava/lang/String;FF)V";
    @Shadow public Container inventoryContainer;
    @Shadow public Container openContainer;
    @Shadow public int experienceLevel;
    @Shadow public int experienceTotal;
    @Shadow public float experience;
    @Shadow public PlayerCapabilities capabilities;
    @Shadow public InventoryPlayer inventory;
    @Shadow public abstract int xpBarCap();
    @Shadow public abstract FoodStats getFoodStats();
    @Shadow public abstract GameProfile getGameProfile();
    @Shadow public abstract void addExperience(int amount);
    @Shadow public abstract Scoreboard getWorldScoreboard();
    @Shadow public abstract boolean isSpectator();
    @Shadow private BlockPos spawnChunk;
    @Shadow private BlockPos playerLocation;
    @Shadow protected FoodStats foodStats;
    private boolean affectsSpawning = true;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void onGetDisplayName(CallbackInfoReturnable<IChatComponent> ci, ChatComponentText component) {
        ci.setReturnValue(LegacyTexts.parseComponent(component, SpongeTexts.COLOR_CHAR));
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

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isPlayerSleeping()Z"))
    public boolean onIsPlayerSleeping(EntityPlayer self) {
        if (self.isPlayerSleeping()) {
            if (!this.worldObj.isRemote) {
                SpongeImpl.postEvent(SpongeEventFactory.
                        createSleepingEventTick(Cause.of(NamedCause.source(this)),
                                                this.getWorld().createSnapshot(VecHelper.toVector(this.playerLocation)), this));
            }
            return true;
        }
        return false;
    }

    /**
     * @author gabizou - January 4th, 2016
     * This is necessary for invisibility checks so that invisible players don't actually send the particle stuffs.
     */
    @Redirect(method = "updateItemUse", at = @At(value = "INVOKE", target = WORLD_SPAWN_PARTICLE))
    public void spawnItemParticle(World world, EnumParticleTypes particleTypes, double xCoord, double yCoord, double zCoord, double xOffset,
            double yOffset, double zOffset, int ... p_175688_14_) {
        if (!this.isReallyREALLYInvisible()) {
            this.worldObj.spawnParticle(particleTypes, xCoord, yCoord, zCoord, xOffset, yOffset, zOffset, p_175688_14_);
        }
    }

    /**
     * @author gabizou - January 4th, 2016
     *
     * This prevents sounds from being sent to the server by players who are invisible.
     */
    @Redirect(method = "playSound", at = @At(value = "INVOKE", target = WORLD_PLAY_SOUND_AT))
    public void playSound(World world, EntityPlayer player, String name, float volume, float pitch) {
        if (!this.isReallyREALLYInvisible()) {
            world.playSoundToNearExcept(player, name, volume, pitch);
        }
    }

    @Override
    public boolean affectsSpawning() {
        return this.affectsSpawning && !this.isSpectator();
    }

    @Override
    public void setAffectsSpawning(boolean affectsSpawning) {
        this.affectsSpawning = affectsSpawning;
    }
}
