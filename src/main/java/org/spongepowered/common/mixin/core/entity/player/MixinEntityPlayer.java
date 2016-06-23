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

import com.flowpowered.math.vector.Vector3d;
import com.mojang.authlib.GameProfile;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.damage.DamageModifier;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.entity.AttackEntityEvent;
import org.spongepowered.api.event.entity.ConstructEntityEvent;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.event.DamageEventHandler;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.ITargetedLocation;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayer;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.mixin.core.entity.MixinEntityLivingBase;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.serializer.LegacyTexts;
import org.spongepowered.common.util.VecHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends MixinEntityLivingBase implements IMixinEntityPlayer, ITargetedLocation {

    private static final String WORLD_SPAWN_PARTICLE = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V";
    private static final String WORLD_PLAY_SOUND_AT =
            "Lnet/minecraft/world/World;playSoundToNearExcept(Lnet/minecraft/entity/player/EntityPlayer;Ljava/lang/String;FF)V";
    private static final String PLAYER_COLLIDE_ENTITY = "Lnet/minecraft/entity/Entity;onCollideWithPlayer(Lnet/minecraft/entity/player/EntityPlayer;)V";

    @Shadow public Container inventoryContainer;
    @Shadow public Container openContainer;
    @Shadow public int experienceLevel;
    @Shadow public int experienceTotal;
    @Shadow public float experience;
    @Shadow public PlayerCapabilities capabilities;
    @Shadow public InventoryPlayer inventory;
    @Shadow private BlockPos spawnChunk;
    @Shadow private BlockPos playerLocation;
    @Shadow protected FoodStats foodStats;

    @Shadow public abstract int xpBarCap();
    @Shadow public abstract GameProfile getGameProfile();
    @Shadow public abstract void addExperience(int amount);
    @Shadow public abstract Scoreboard getWorldScoreboard();
    @Shadow public abstract boolean isSpectator();
    @Shadow public abstract ItemStack getHeldItem();
    @Shadow public abstract void onCriticalHit(net.minecraft.entity.Entity entityHit);
    @Shadow public abstract void onEnchantmentCritical(net.minecraft.entity.Entity entityHit);
    @Shadow public abstract void triggerAchievement(StatBase achievementIn);
    @Shadow public abstract ItemStack getCurrentEquippedItem();
    @Shadow public abstract void addExhaustion(float p_71020_1_);
    @Shadow public abstract void addStat(StatBase stat, int amount);
    @Shadow public abstract void destroyCurrentEquippedItem();
    @Shadow public abstract Team getTeam();
    @Shadow public abstract String getName();
    @Shadow public abstract EntityItem dropItem(ItemStack droppedItem, boolean dropAround, boolean traceItem);
    @Shadow public abstract void func_175145_a(StatBase p_175145_1_);

    private boolean affectsSpawning = true;
    private UUID collidingEntityUuid = null;
    private Vector3d targetedLocation;

    @Inject(method = "<init>(Lnet/minecraft/world/World;Lcom/mojang/authlib/GameProfile;)V", at = @At("RETURN"))
    public void construct(World worldIn, GameProfile gameProfileIn, CallbackInfo ci) {
        this.targetedLocation = VecHelper.toVector3d(worldIn.getSpawnPoint());
    }

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

    /**
     * @author blood - May 12th, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead. This handles player death events.
     */
    @Override
    @Overwrite
    public void onDeath(DamageSource cause) {

        super.onDeath(cause);
        this.setSize(0.2F, 0.2F);
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionY = 0.10000000149011612D;

        this.captureItemDrops = true;
        this.capturedItemDrops.clear();

        if (this.getName().equals("Notch")) {
            this.dropItem(new ItemStack(Items.apple, 1), true, false);
        }

        if (!this.worldObj.getGameRules().getBoolean("keepInventory")) {
            this.inventory.dropAllItems();
        }

        this.captureItemDrops = false;
        if (this.capturedItemDrops.size() > 0) {
            IMixinWorld spongeWorld = (IMixinWorld) this.worldObj;
            final CauseTracker causeTracker = spongeWorld.getCauseTracker();
            causeTracker.setIgnoreSpawnEvents(true);
            DropItemEvent.Destruct event = SpongeCommonEventFactory.callDropItemEventDestruct((EntityPlayerMP)(Object) this, cause, this.capturedItemDrops);
            if (!event.isCancelled()) {
                for (net.minecraft.entity.item.EntityItem item : this.capturedItemDrops) {
                    this.worldObj.spawnEntityInWorld(item);
                }
                this.inventory.clear();
            }
            causeTracker.setIgnoreSpawnEvents(false);
        }

        if (cause != null) {
            this.motionX = (double)(-MathHelper.cos((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
            this.motionZ = (double)(-MathHelper.sin((this.attackedAtYaw + this.rotationYaw) * (float)Math.PI / 180.0F) * 0.1F);
        } else {
            this.motionX = this.motionZ = 0.0D;
        }

        this.triggerAchievement(StatList.deathsStat);
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
        if (!this.isVanished()) {
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
        if (!this.isVanished()) {
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

    @Override
    public Vector3d getTargetedLocation() {
        return this.targetedLocation;
    }

    @Override
    public void setTargetedLocation(@Nullable Vector3d vec) {
        this.targetedLocation = vec != null ? vec : VecHelper.toVector3d(this.worldObj.getSpawnPoint());
        if (!((Object) this instanceof EntityPlayerMP)) {
            this.worldObj.setSpawnPoint(VecHelper.toBlockPos(this.targetedLocation));
        }
    }

    /**
     * @author blood - May 13th, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead.
     */
    @Overwrite
    public EntityItem dropOneItem(boolean dropAll) {
        if (this.worldObj.isRemote) {
            return this.dropItem(this.inventory.decrStackSize(this.inventory.currentItem, dropAll && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1), false, true);
        }

        ItemStack stack = inventory.getCurrentItem();

        if (stack == null) {
            return null;
        }

        if (SpongeImplHooks.onDroppedByPlayer(stack.getItem(), stack, (EntityPlayer)(Object) this)) {
            int count = dropAll && this.inventory.getCurrentItem() != null ? this.inventory.getCurrentItem().stackSize : 1;
            return SpongeImplHooks.onPlayerToss((EntityPlayer)(Object) this, inventory.decrStackSize(inventory.currentItem, count), true);
        }

        return null;
    }

    /**
     * @author blood - May 13th, 2016
     *
     * @reason SpongeForge requires an overwrite so we do it here instead.
     */
    @Overwrite
    public EntityItem dropPlayerItemWithRandomChoice(ItemStack itemStackIn, boolean unused) {
        if (this.worldObj.isRemote) {
            return this.dropItem(itemStackIn, false, false);
        }

        return SpongeImplHooks.onPlayerToss((EntityPlayer)(Object) this, itemStackIn, unused);
    }

    @Inject(method = "dropItem", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/entity/player/EntityPlayer;posY:D"), cancellable = true)
    private void onDropTop(ItemStack itemStack, boolean a, boolean b, CallbackInfoReturnable<EntityItem> callbackInfoReturnable) {
        final double height = this.posY - 0.3D + (double)this.getEyeHeight();
        Transform<org.spongepowered.api.world.World> transform = new Transform<>(this.getWorld(), new Vector3d(this.posX, height, this.posZ));
        SpawnCause cause = EntitySpawnCause.builder()
                .entity(this)
                .type(SpawnTypes.DROPPED_ITEM)
                .build();
        ConstructEntityEvent.Pre event = SpongeEventFactory.createConstructEntityEventPre(Cause.of(NamedCause.source(cause)), EntityTypes.ITEM, transform);
        SpongeImpl.postEvent(event);
        if (event.isCancelled()) {
            callbackInfoReturnable.setReturnValue(null);
        }
    }

    /**
     * @author gabizou - January 30th, 2016
     * @author blood - May 12th, 2016
     *
     * @reason If capturing is enabled, captures the item and avoids spawn.
     *         If capturing is not enabled, redirects the dropped item spawning to use our world spawning since we know the cause.
     *
     */
    @Overwrite
    public void joinEntityItemWithWorld(EntityItem itemIn) {
        if (this.worldObj.isRemote) {
            this.worldObj.spawnEntityInWorld(itemIn);
            return;
        }

        if (this.captureItemDrops) {
            this.capturedItemDrops.add(itemIn);
            return;
        }

        SpawnCause spawnCause = EntitySpawnCause.builder()
                .entity(this)
                .type(SpawnTypes.DROPPED_ITEM)
                .build();
        ((org.spongepowered.api.world.World) this.worldObj).spawnEntity((Entity) itemIn, Cause.of(NamedCause.source(spawnCause)));
    }

    @Redirect(method = "collideWithPlayer", at = @At(value = "INVOKE", target = PLAYER_COLLIDE_ENTITY))
    public void onPlayerCollideEntity(net.minecraft.entity.Entity entity, EntityPlayer player) {
        this.collidingEntityUuid = entity.getUniqueID();
        entity.onCollideWithPlayer(player);
        this.collidingEntityUuid = null;
    }

    @Override
    public UUID getCollidingEntityUuid() {
        return this.collidingEntityUuid;
    }

    /**
     * @author gabizou - April 8th, 2016
     * @reason Rewrites the attackTargetEntityWithCurrentItem to throw an {@link AttackEntityEvent} prior
     * to the ensuing {@link DamageEntityEvent}. This should cover all cases where players are
     * attacking entities and those entities override {@link EntityLivingBase#attackEntityFrom(DamageSource, float)}
     * and effectively bypass our damage event hooks.
     *
     * @param targetEntity The target entity
     */
    @Overwrite
    public void attackTargetEntityWithCurrentItem(net.minecraft.entity.Entity targetEntity) {
        // Sponge Start - Add SpongeImpl hook to override in forge as necessary
        if (!SpongeImplHooks.checkAttackEntity((EntityPlayer) (Object) this, targetEntity)) {
            return;
        }
        // Sponge End
        if (targetEntity.canAttackWithItem()) {
            if (!targetEntity.hitByEntity((EntityPlayer) (Object) this)) {
                // Sponge Start - Prepare our event values
                // float baseDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                final double originalBaseDamage = this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                float baseDamage = (float) originalBaseDamage;
                // Sponge End
                int knockbackModifier = 0;
                float enchantmentModifierAmount = 0.0F;

                // Sponge Start - gather the attack modifiers
                final List<Tuple<DamageModifier, Function<? super Double, Double>>> originalFunctions = new ArrayList<>();

                final EnumCreatureAttribute creatureAttribute = targetEntity instanceof EntityLivingBase
                                                                ? ((EntityLivingBase) targetEntity).getCreatureAttribute()
                                                                : EnumCreatureAttribute.UNDEFINED;
                final List<Tuple<DamageModifier, Function<? super Double, Double>>> enchantmentModifierFunctions =
                        DamageEventHandler.createAttackEnchamntmentFunction(this.getHeldItem(), creatureAttribute);
                // if (targetEntity instanceof EntityLivingBase) {
                //     enchantmentModifierAmount = EnchantmentHelper.getModifierForCreature(this.getHeldItem(), creatureAttribute);
                // } else {
                //     enchantmentModifierAmount = EnchantmentHelper.getModifierForCreature(this.getHeldItem(), EnumCreatureAttribute.UNDEFINED);
                // }
                enchantmentModifierAmount = (float) enchantmentModifierFunctions.stream()
                        .map(Tuple::getSecond)
                        .mapToDouble(function -> function.apply(originalBaseDamage))
                        .sum();
                originalFunctions.addAll(enchantmentModifierFunctions);
                // Sponge End

                knockbackModifier = knockbackModifier + EnchantmentHelper.getKnockbackModifier((EntityPlayer) (Object) this);

                if (this.isSprinting()) {
                    ++knockbackModifier;
                }

                if (baseDamage > 0.0F || enchantmentModifierAmount > 0.0F) {
                    boolean fallingCriticalHit = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(
                            Potion.blindness) && this.ridingEntity == null && targetEntity instanceof EntityLivingBase;

                    if (fallingCriticalHit && baseDamage > 0.0F) {
                        // Sponge - Add the function for critical attacking
                        originalFunctions.add(DamageEventHandler.provideCriticalAttackTuple((EntityPlayer) (Object) this));
                        // baseDamage *= 1.5F; Sponge - remove since it's handled in the event
                    }

                    // baseDamage = baseDamage + enchantmentModifierAmount; // Sponge - remove since it is delegated through the event.
                    boolean targetLitOnFire = false;
                    int fireAspectLevel = EnchantmentHelper.getFireAspectModifier((EntityPlayer) (Object) this);

                    if (targetEntity instanceof EntityLivingBase && fireAspectLevel > 0 && !targetEntity.isBurning()) {
                        targetLitOnFire = true;
                        targetEntity.setFire(1);
                    }

                    double targetMotionX = targetEntity.motionX;
                    double targetMotionY = targetEntity.motionY;
                    double targetMotionZ = targetEntity.motionZ;

                    // Sponge Start - Create the event and throw it
                    final DamageSource damageSource = DamageSource.causePlayerDamage((EntityPlayer) (Object) this);
                    final AttackEntityEvent event = SpongeEventFactory.createAttackEntityEvent(Cause.source(damageSource).build(), originalFunctions,
                            EntityUtil.fromNative(targetEntity), knockbackModifier, originalBaseDamage);
                    SpongeImpl.postEvent(event);
                    if (event.isCancelled()) {
                        if (targetLitOnFire) {
                            targetEntity.extinguish();
                        }
                        return;
                    }
                    baseDamage = (float) event.getFinalOutputDamage();
                    knockbackModifier = event.getKnockbackModifier();
                    boolean attackSucceded = targetEntity.attackEntityFrom(damageSource, (float) event.getFinalOutputDamage());
                    // Sponge End
                    if (attackSucceded) {
                        if (knockbackModifier > 0) {
                            targetEntity.addVelocity((double) (-MathHelper.sin(this.rotationYaw * (float) Math.PI / 180.0F) * (float) knockbackModifier * 0.5F), 0.1D,
                                    (double) (MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * (float) knockbackModifier * 0.5F));
                            this.motionX *= 0.6D;
                            this.motionZ *= 0.6D;
                            this.setSprinting(false);
                        }

                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                            ((EntityPlayerMP) targetEntity).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = targetMotionX;
                            targetEntity.motionY = targetMotionY;
                            targetEntity.motionZ = targetMotionZ;
                        }

                        if (fallingCriticalHit) {
                            this.onCriticalHit(targetEntity);
                        }

                        if (enchantmentModifierAmount > 0.0F) {
                            this.onEnchantmentCritical(targetEntity);
                        }

                        if (baseDamage >= 18.0F) {
                            this.triggerAchievement(AchievementList.overkill);
                        }

                        this.setLastAttacker(targetEntity);

                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, (EntityPlayer) (Object) this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments((EntityPlayer) (Object) this, targetEntity);
                        ItemStack itemstack = this.getCurrentEquippedItem();
                        net.minecraft.entity.Entity entity = targetEntity;

                        if (targetEntity instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart) targetEntity).entityDragonObj;

                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if (itemstack != null && entity instanceof EntityLivingBase) {
                            itemstack.hitEntity((EntityLivingBase) entity, (EntityPlayer) (Object) this);

                            if (itemstack.stackSize <= 0) {
                                this.destroyCurrentEquippedItem();
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            this.addStat(StatList.damageDealtStat, Math.round(baseDamage * 10.0F));

                            if (fireAspectLevel > 0) {
                                targetEntity.setFire(fireAspectLevel * 4);
                            }
                        }

                        this.addExhaustion(0.3F);
                    } else if (targetLitOnFire) {
                        targetEntity.extinguish();
                    }
                }
            }
        }
    }


}
