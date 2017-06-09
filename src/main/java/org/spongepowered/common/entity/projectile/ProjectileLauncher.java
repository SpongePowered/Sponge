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
package org.spongepowered.common.entity.projectile;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityLlamaSpit;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.Egg;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.entity.projectile.EyeOfEnder;
import org.spongepowered.api.entity.projectile.Firework;
import org.spongepowered.api.entity.projectile.FishHook;
import org.spongepowered.api.entity.projectile.LlamaSpit;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.Snowball;
import org.spongepowered.api.entity.projectile.ThrownExpBottle;
import org.spongepowered.api.entity.projectile.ThrownPotion;
import org.spongepowered.api.entity.projectile.arrow.TippedArrow;
import org.spongepowered.api.entity.projectile.explosive.DragonFireball;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.entity.projectile.explosive.fireball.LargeFireball;
import org.spongepowered.api.entity.projectile.explosive.fireball.SmallFireball;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;
import org.spongepowered.common.registry.type.event.InternalSpawnTypes;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class ProjectileLauncher {

    private static final Map<Class<? extends Projectile>, ProjectileLogic<?>> projectileLogic = Maps.newHashMap();
    private static final Map<Class<? extends ProjectileSource>, ProjectileSourceLogic<?>> projectileSourceLogic = Maps.newHashMap();

    public static <T extends Projectile> Optional<T> launch(Class<T> projectileClass, ProjectileSource source, Vector3d vel) {
        ProjectileLogic<T> logic = getLogic(projectileClass);
        if (logic == null) {
            return Optional.empty();
        }
        Optional<T> projectile = logic.launch(source);
        projectile.ifPresent(t -> {
            if (vel != null) {
                t.offer(Keys.VELOCITY, vel);
            }
            t.setShooter(source);
        });
        if (projectile.isPresent()) {
            if (vel != null) {
                projectile.get().offer(Keys.VELOCITY, vel);
            }
            projectile.get().setShooter(source);
        }
        return projectile;
    }

    // From EntityThrowable constructor
    private static void configureThrowable(EntityThrowable entity) {
        entity.posX -= (double) (MathHelper.cos(entity.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
        entity.posY -= 0.1D;
        entity.posZ -= (double) (MathHelper.sin(entity.rotationYaw / 180.0F * (float) Math.PI) * 0.16F);
        entity.setPosition(entity.posX, entity.posY, entity.posZ);
        float f = 0.4F;
        entity.motionX = (double) (-MathHelper.sin(entity.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f);
        entity.motionZ = (double) (MathHelper.cos(entity.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f);
        entity.motionY = (double) (-MathHelper.sin((entity.rotationPitch) / 180.0F * (float) Math.PI) * f);
    }

    public static <T extends Projectile> void registerProjectileLogic(Class<T> projectileClass, ProjectileLogic<T> logic) {
        projectileLogic.put(projectileClass, logic);
    }

    public static <T extends ProjectileSource> void registerProjectileSourceLogic(Class<T> projectileSourceClass, ProjectileSourceLogic<T> logic) {
        projectileSourceLogic.put(projectileSourceClass, logic);
    }

    @SuppressWarnings("unchecked")
    static <T extends ProjectileSource> ProjectileSourceLogic<T> getSourceLogic(Class<T> sourceClass) {
        return (ProjectileSourceLogic<T>) projectileSourceLogic.get(sourceClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Projectile> ProjectileLogic<T> getLogic(Class<T> sourceClass) {
        // If a concrete class is handed to us, find the API interface
        if (!sourceClass.isInterface() && net.minecraft.entity.Entity.class.isAssignableFrom(sourceClass)) {
            for (Class<?> iface : sourceClass.getInterfaces()) {
                if (Projectile.class.isAssignableFrom(iface)) {
                    sourceClass = (Class<T>) iface;
                    break;
                }
            }
        }
        return (ProjectileLogic<T>) projectileLogic.get(sourceClass);
    }

    @SuppressWarnings("unchecked")
    static <P extends Projectile> Optional<P> defaultLaunch(ProjectileSource source, Class<P> projectileClass, Location<?> loc) {
        Optional<EntityType> opType = EntityTypeRegistryModule.getInstance().getEntity(projectileClass);
        if (!opType.isPresent()) {
            return Optional.empty();
        }
        Entity projectile = loc.getExtent().createEntity(opType.get(), loc.getPosition());
        if (projectile instanceof EntityThrowable) {
            configureThrowable((EntityThrowable) projectile);
        }
        return doLaunch(loc.getExtent(), (P) projectile, createCause(source));
    }

    // Internal
    private static Cause createCause(Object source) {
        return Cause.source(SpawnCause.builder().type(InternalSpawnTypes.PROJECTILE).build())
                .named("ProjectileSource", source).build();
    }

    private static <P extends Projectile> Optional<P> doLaunch(Extent extent, P projectile, Cause cause) {
        SpongeCommonEventFactory.checkSpawnEvent(projectile, cause);
        LaunchProjectileEvent event = SpongeEventFactory.createLaunchProjectileEvent(cause, projectile);
        SpongeImpl.getGame().getEventManager().post(event);
        if (!event.isCancelled() && extent.spawnEntity(projectile, cause)) {
            return Optional.of(projectile);
        }
        return Optional.empty();
    }

    static {
        registerProjectileSourceLogic(Dispenser.class, new DispenserSourceLogic());

        registerProjectileLogic(TippedArrow.class, new SimpleItemLaunchLogic<TippedArrow>(TippedArrow.class, Items.ARROW) {

            @Override
            protected Optional<TippedArrow> createProjectile(EntityLivingBase source, Location<?> loc) {
                TippedArrow arrow = (TippedArrow) new EntityTippedArrow(source.world, source);
                ((EntityTippedArrow) arrow).setAim(source, source.rotationPitch, source.rotationYaw, 0.0F, 3.0F, 0);
                return doLaunch(loc.getExtent(), arrow, createCause(source));
            }
        });
        registerProjectileLogic(Egg.class, new SimpleItemLaunchLogic<Egg>(Egg.class, Items.EGG) {

            @Override
            protected Optional<Egg> createProjectile(EntityLivingBase source, Location<?> loc) {
                Egg egg = (Egg) new EntityEgg(source.world, source);
                ((EntityThrowable) egg).setHeadingFromThrower(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return doLaunch(loc.getExtent(), egg, createCause(source));
            }
        });
        registerProjectileLogic(SmallFireball.class, new SimpleItemLaunchLogic<SmallFireball>(SmallFireball.class, Items.FIRE_CHARGE) {

            @Override
            protected Optional<SmallFireball> createProjectile(EntityLivingBase source, Location<?> loc) {
                Vec3d lookVec = source.getLook(1);
                SmallFireball fireball = (SmallFireball) new EntitySmallFireball(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                ((EntitySmallFireball) fireball).posY += source.getEyeHeight();
                return doLaunch(loc.getExtent(), fireball, createCause(source));
            }
        });
        registerProjectileLogic(Firework.class, new SimpleItemLaunchLogic<Firework>(Firework.class, Items.FIREWORKS) {

            @Override
            protected Optional<Firework> createProjectile(EntityLivingBase source, Location<?> loc) {
                Firework firework = (Firework) new EntityFireworkRocket(source.world, loc.getX(), loc.getY(), loc.getZ(), ItemStack.EMPTY);
                return doLaunch(loc.getExtent(), firework, createCause(source));
            }
        });
        registerProjectileLogic(Snowball.class, new SimpleItemLaunchLogic<Snowball>(Snowball.class, Items.SNOWBALL) {

            @Override
            protected Optional<Snowball> createProjectile(EntityLivingBase source, Location<?> loc) {
                Snowball snowball = (Snowball) new EntitySnowball(source.world, source);
                ((EntityThrowable) snowball).setHeadingFromThrower(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return doLaunch(loc.getExtent(), snowball, createCause(source));
            }
        });
        registerProjectileLogic(ThrownExpBottle.class, new SimpleItemLaunchLogic<ThrownExpBottle>(ThrownExpBottle.class, Items.EXPERIENCE_BOTTLE) {

            @Override
            protected Optional<ThrownExpBottle> createProjectile(EntityLivingBase source, Location<?> loc) {
                ThrownExpBottle expBottle = (ThrownExpBottle) new EntityExpBottle(source.world, source);
                ((EntityThrowable) expBottle).setHeadingFromThrower(source, source.rotationPitch, source.rotationYaw, -20.0F, 0.7F, 0);
                return doLaunch(loc.getExtent(), expBottle, createCause(source));
            }
        });

        registerProjectileLogic(EnderPearl.class, new SimpleDispenserLaunchLogic<EnderPearl>(EnderPearl.class) {

            @Override
            protected Optional<EnderPearl> createProjectile(EntityLivingBase source, Location<?> loc) {
                EnderPearl pearl = (EnderPearl) new EntityEnderPearl(source.world, source);
                ((EntityThrowable) pearl).setHeadingFromThrower(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return doLaunch(loc.getExtent(), pearl, createCause(source));
            }
        });
        registerProjectileLogic(LargeFireball.class, new SimpleDispenserLaunchLogic<LargeFireball>(LargeFireball.class) {

            @Override
            protected Optional<LargeFireball> createProjectile(EntityLivingBase source, Location<?> loc) {
                Vec3d lookVec = source.getLook(1);
                LargeFireball fireball = (LargeFireball) new EntityLargeFireball(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                ((EntityLargeFireball) fireball).posY += source.getEyeHeight();
                return doLaunch(loc.getExtent(), fireball, createCause(source));
            }

            @Override
            public Optional<LargeFireball> createProjectile(ProjectileSource source, Class<LargeFireball> projectileClass, Location<?> loc) {
                if (!(source instanceof TileEntityDispenser)) {
                    return super.createProjectile(source, projectileClass, loc);
                }
                TileEntityDispenser dispenser = (TileEntityDispenser) source;
                EnumFacing enumfacing = DispenserSourceLogic.getFacing(dispenser);
                Random random = dispenser.getWorld().rand;
                double d3 = random.nextGaussian() * 0.05D + enumfacing.getFrontOffsetX();
                double d4 = random.nextGaussian() * 0.05D + enumfacing.getFrontOffsetY();
                double d5 = random.nextGaussian() * 0.05D + enumfacing.getFrontOffsetZ();
                EntityLivingBase thrower = new EntityArmorStand(dispenser.getWorld(), loc.getX() + enumfacing.getFrontOffsetX(),
                        loc.getY() + enumfacing.getFrontOffsetY(), loc.getZ() + enumfacing.getFrontOffsetZ());
                LargeFireball fireball = (LargeFireball) new EntityLargeFireball(dispenser.getWorld(), thrower, d3, d4, d5);
                return doLaunch(loc.getExtent(), fireball, createCause(source));
            }
        });
        registerProjectileLogic(WitherSkull.class, new SimpleDispenserLaunchLogic<WitherSkull>(WitherSkull.class) {

            @Override
            protected Optional<WitherSkull> createProjectile(EntityLivingBase source, Location<?> loc) {
                Vec3d lookVec = source.getLook(1);
                WitherSkull skull = (WitherSkull) new EntityWitherSkull(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                ((EntityWitherSkull) skull).posY += source.getEyeHeight();
                return doLaunch(loc.getExtent(), skull, createCause(source));
            }
        });
        registerProjectileLogic(EyeOfEnder.class, new SimpleDispenserLaunchLogic<>(EyeOfEnder.class));
        registerProjectileLogic(FishHook.class, new SimpleDispenserLaunchLogic<FishHook>(FishHook.class) {

            @Override
            protected Optional<FishHook> createProjectile(EntityLivingBase source, Location<?> loc) {
                if (source instanceof EntityPlayer) {
                    FishHook hook = (FishHook) new EntityFishHook(source.world, (EntityPlayer) source);
                    return doLaunch(loc.getExtent(), hook, createCause(source));
                }
                return super.createProjectile(source, loc);
            }
        });
        registerProjectileLogic(ThrownPotion.class, new SimpleDispenserLaunchLogic<ThrownPotion>(ThrownPotion.class) {

            @Override
            protected Optional<ThrownPotion> createProjectile(EntityLivingBase source, Location<?> loc) {
                ThrownPotion potion = (ThrownPotion) new EntityPotion(source.world, source, new ItemStack(Items.SPLASH_POTION, 1));
                ((EntityThrowable) potion).setHeadingFromThrower(source, source.rotationPitch, source.rotationYaw, -20.0F, 0.5F, 0);
                return doLaunch(loc.getExtent(), potion, createCause(source));
            }
        });
        registerProjectileLogic(LlamaSpit.class, new SimpleEntityLaunchLogic<LlamaSpit>(LlamaSpit.class) {

            @Override
            public Optional<LlamaSpit> launch(ProjectileSource source) {
                if (!(source instanceof EntityLlama)) {
                    return Optional.empty();
                }
                return super.launch(source);
            }

            @Override
            public Optional<LlamaSpit> createProjectile(ProjectileSource source, Class<LlamaSpit> projectileClass, Location<?> loc) {
                EntityLlama llama = (EntityLlama) source;
                LlamaSpit llamaSpit = (LlamaSpit) new EntityLlamaSpit(llama.world, (EntityLlama) source);
                Vec3d lookVec = llama.getLook(1);
                ((EntityLlamaSpit) llamaSpit).setThrowableHeading(lookVec.x, lookVec.y, lookVec.z, 1.5F, 0);
                return doLaunch(loc.getExtent(), llamaSpit, createCause(source));
            }
        });
        registerProjectileLogic(DragonFireball.class, new SimpleDispenserLaunchLogic<DragonFireball>(DragonFireball.class) {

            @Override
            protected Optional<DragonFireball> createProjectile(EntityLivingBase source, Location<?> loc) {
                Vec3d lookVec = source.getLook(1);
                DragonFireball fireball = (DragonFireball) new EntityDragonFireball(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                ((EntityDragonFireball) fireball).posY += source.getEyeHeight();
                return doLaunch(loc.getExtent(), fireball, createCause(source));
            }
        });
    }

}
