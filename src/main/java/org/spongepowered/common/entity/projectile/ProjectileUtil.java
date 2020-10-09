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

import com.google.common.collect.Maps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.EnderPearlEntity;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.item.FireworkRocketEntity;
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.SnowballEntity;
import net.minecraft.entity.projectile.SpectralArrowEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.api.block.entity.carrier.Dispenser;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.golem.Shulker;
import org.spongepowered.api.entity.projectile.Egg;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.entity.projectile.ExperienceBottle;
import org.spongepowered.api.entity.projectile.FishingBobber;
import org.spongepowered.api.entity.projectile.LlamaSpit;
import org.spongepowered.api.entity.projectile.Potion;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.Snowball;
import org.spongepowered.api.entity.projectile.arrow.Arrow;
import org.spongepowered.api.entity.projectile.arrow.SpectralArrow;
import org.spongepowered.api.entity.projectile.explosive.FireworkRocket;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.entity.projectile.explosive.fireball.DragonFireball;
import org.spongepowered.api.entity.projectile.explosive.fireball.ExplosiveFireball;
import org.spongepowered.api.entity.projectile.explosive.fireball.SmallFireball;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.math.vector.Vector3d;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class ProjectileUtil {

    private static final Map<EntityType<? extends Projectile>, ProjectileLogic<?>> projectileLogic = Maps.newHashMap();
    private static final Map<Class<? extends ProjectileSource>, ProjectileSourceLogic<?>> projectileSourceLogic = Maps.newHashMap();

    public static <T extends Projectile> Optional<T> launch(EntityType<T> projectileType, ProjectileSource source, @Nullable Vector3d vel) {
        ProjectileLogic<T> logic = ProjectileUtil.getLogic(projectileType);
        if (logic == null) {
            return Optional.empty();
        }
        Optional<T> projectile = logic.launch(source);
        projectile.ifPresent(t -> {
            if (vel != null) {
                t.offer(Keys.VELOCITY, vel);
            }
            t.offer(Keys.SHOOTER, source);
        });
        return projectile;
    }

    public static <T extends Projectile, S extends ProjectileSource> Optional<T> launchWithArgs(EntityType<T> projectileType,
            Class<S> projectileSourceClass, S source, @Nullable Vector3d vel, Object... args) {
        ProjectileSourceLogic<S> sourceLogic = ProjectileUtil.getSourceLogic(projectileSourceClass);
        if (sourceLogic == null) {
            return Optional.empty();
        }

        ProjectileLogic<T> logic = ProjectileUtil.getLogic(projectileType);
        if (logic == null) {
            return Optional.empty();
        }

        Optional<T> projectile = sourceLogic.launch(logic, source, projectileType, args);
        projectile.ifPresent(t -> {
            if (vel != null) {
                t.offer(Keys.VELOCITY, vel);
            }
            t.offer(Keys.SHOOTER, source);
        });
        return projectile;
    }

    // From EntityThrowable constructor
    private static void configureThrowable(ThrowableEntity entity) {
        final double posX = entity.getPosX() - MathHelper.cos(entity.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        final double posY = entity.getPosY() - 0.1D;
        final double posZ = entity.getPosZ() - MathHelper.sin(entity.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        entity.setPosition(posX, posY, posZ);
        float f = 0.4F;
        final double motionX = -MathHelper.sin(entity.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f;
        final double motionZ = MathHelper.cos(entity.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f;
        final double motionY = -MathHelper.sin((entity.rotationPitch) / 180.0F * (float) Math.PI) * f;
        entity.setMotion(motionX, motionY, motionZ);
    }

    public static <T extends Projectile> void registerProjectileLogic(Supplier<EntityType<T>> projectileType, ProjectileLogic<T> logic) {
        ProjectileUtil.projectileLogic.put(projectileType.get(), logic);
    }

    public static <T extends ProjectileSource> void registerProjectileSourceLogic(Class<T> projectileSourceClass, ProjectileSourceLogic<T> logic) {
        ProjectileUtil.projectileSourceLogic.put(projectileSourceClass, logic);
    }

    @SuppressWarnings("unchecked")
    static <T extends ProjectileSource> ProjectileSourceLogic<T> getSourceLogic(Class<T> sourceClass) {
        return (ProjectileSourceLogic<T>) ProjectileUtil.projectileSourceLogic.get(sourceClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Projectile> ProjectileLogic<T> getLogic(EntityType<T> sourceClass) {
        return (ProjectileLogic<T>) ProjectileUtil.projectileLogic.get(sourceClass);
    }

    @SuppressWarnings("unchecked")
    static <P extends Projectile> Optional<P> defaultLaunch(ProjectileSource source, EntityType<P> opType, ServerLocation loc) {
        SpongeCommon.getRegistry().getCatalogRegistry().getRegistry(EntityType.class);

        Entity projectile = loc.getWorld().createEntity(opType, loc.getPosition());
        if (projectile instanceof ThrowableEntity) {
            ProjectileUtil.configureThrowable((ThrowableEntity) projectile);
        }
        return ProjectileUtil.doLaunch(loc.getWorld(), (P) projectile);
    }

    static <P extends Projectile> Optional<P> doLaunch(World extent, P projectile) {
        final SpawnEntityEvent spawnEvent = SpongeEventFactory.createSpawnEntityEvent(PhaseTracker.getCauseStackManager().getCurrentCause(), Collections.singletonList(projectile));
        SpongeCommon.getGame().getEventManager().post(spawnEvent);
        if (!spawnEvent.isCancelled() && extent.spawnEntity(projectile)) {
            return Optional.of(projectile);
        }
        return Optional.empty();
    }

    static {
        ProjectileUtil.registerProjectileSourceLogic(Dispenser.class, new DispenserSourceLogic());

        ProjectileUtil.registerProjectileSourceLogic(Shulker.class, new ShulkerSourceLogic());

        ProjectileUtil.registerProjectileLogic(EntityTypes.SPECTRAL_ARROW, new SimpleItemLaunchLogic<SpectralArrow>(EntityTypes.SPECTRAL_ARROW, Items.SPECTRAL_ARROW) {

            @Override
            protected Optional<SpectralArrow> createProjectile(LivingEntity source, ServerLocation loc) {
                SpectralArrow arrow = (SpectralArrow) new SpectralArrowEntity(source.world, source);
                ((SpectralArrowEntity) arrow).shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 3.0F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), arrow);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.ARROW, new SimpleItemLaunchLogic<Arrow>(EntityTypes.ARROW, Items.ARROW) {

            @Override
            protected Optional<Arrow> createProjectile(LivingEntity source, ServerLocation loc) {
                Arrow arrow = (Arrow) new ArrowEntity(source.world, source);
                ((ArrowEntity) arrow).shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 3.0F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), arrow);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.EGG, new SimpleItemLaunchLogic<Egg>(EntityTypes.EGG, Items.EGG) {

            @Override
            protected Optional<Egg> createProjectile(LivingEntity source, ServerLocation loc) {
                Egg egg = (Egg) new EggEntity(source.world, source);
                ((ThrowableEntity) egg).shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), egg);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.SMALL_FIREBALL, new SimpleItemLaunchLogic<SmallFireball>(EntityTypes.SMALL_FIREBALL, Items.FIRE_CHARGE) {

            @Override
            protected Optional<SmallFireball> createProjectile(LivingEntity source, ServerLocation loc) {
                Vec3d lookVec = source.getLook(1);
                SmallFireballEntity fireball = new SmallFireballEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setRawPosition(fireball.getPosX(), fireball.getPosY() - source.getEyeHeight(), fireball.getPosZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (SmallFireball) fireball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.FIREWORK_ROCKET, new SimpleItemLaunchLogic<FireworkRocket>(EntityTypes.FIREWORK_ROCKET, Items.FIREWORK_ROCKET) {

            @Override
            protected Optional<FireworkRocket> createProjectile(LivingEntity source, ServerLocation loc) {
                FireworkRocket firework = (FireworkRocket) new FireworkRocketEntity(source.world, loc.getX(), loc.getY(), loc.getZ(), ItemStack.EMPTY);
                return ProjectileUtil.doLaunch(loc.getWorld(), firework);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.SNOWBALL, new SimpleItemLaunchLogic<Snowball>(EntityTypes.SNOWBALL, Items.SNOWBALL) {

            @Override
            protected Optional<Snowball> createProjectile(LivingEntity source, ServerLocation loc) {
                Snowball snowball = (Snowball) new SnowballEntity(source.world, source);
                ((ThrowableEntity) snowball).shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), snowball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.EXPERIENCE_BOTTLE, new SimpleItemLaunchLogic<ExperienceBottle>(EntityTypes.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE) {

            @Override
            protected Optional<ExperienceBottle> createProjectile(LivingEntity source, ServerLocation loc) {
                ExperienceBottle expBottle = (ExperienceBottle) new ExperienceBottleEntity(source.world, source);
                ((ThrowableEntity) expBottle).shoot(source, source.rotationPitch, source.rotationYaw, -20.0F, 0.7F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), expBottle);
            }
        });

        ProjectileUtil.registerProjectileLogic(EntityTypes.ENDER_PEARL, new SimpleDispenserLaunchLogic<EnderPearl>(EntityTypes.ENDER_PEARL) {

            @Override
            protected Optional<EnderPearl> createProjectile(LivingEntity source, ServerLocation loc) {
                EnderPearl pearl = (EnderPearl) new EnderPearlEntity(source.world, source);
                ((ThrowableEntity) pearl).shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), pearl);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.FIREBALL, new SimpleDispenserLaunchLogic<ExplosiveFireball>(EntityTypes.FIREBALL) {

            @Override
            protected Optional<ExplosiveFireball> createProjectile(LivingEntity source, ServerLocation loc) {
                Vec3d lookVec = source.getLook(1);
                FireballEntity fireball = new FireballEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setRawPosition(fireball.getPosX(), fireball.getPosY() + source.getEyeHeight(), fireball.getPosZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (ExplosiveFireball)  fireball);
            }

            @Override
            public Optional<ExplosiveFireball> createProjectile(ProjectileSource source, EntityType<ExplosiveFireball> projectileClass, ServerLocation loc) {
                if (!(source instanceof DispenserTileEntity)) {
                    return super.createProjectile(source, projectileClass, loc);
                }
                DispenserTileEntity dispenser = (DispenserTileEntity) source;
                Direction enumfacing = DispenserSourceLogic.getFacing(dispenser);
                Random random = dispenser.getWorld().rand;
                double d3 = random.nextGaussian() * 0.05D + enumfacing.getXOffset();
                double d4 = random.nextGaussian() * 0.05D + enumfacing.getYOffset();
                double d5 = random.nextGaussian() * 0.05D + enumfacing.getZOffset();
                LivingEntity thrower = new ArmorStandEntity(dispenser.getWorld(), loc.getX() + enumfacing.getXOffset(),
                        loc.getY() + enumfacing.getYOffset(), loc.getZ() + enumfacing.getZOffset());
                ExplosiveFireball fireball = (ExplosiveFireball) new FireballEntity(dispenser.getWorld(), thrower, d3, d4, d5);
                return ProjectileUtil.doLaunch(loc.getWorld(), fireball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.WITHER_SKULL, new SimpleDispenserLaunchLogic<WitherSkull>(EntityTypes.WITHER_SKULL) {

            @Override
            protected Optional<WitherSkull> createProjectile(LivingEntity source, ServerLocation loc) {
                Vec3d lookVec = source.getLook(1);
                WitherSkullEntity skull = new WitherSkullEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                skull.setRawPosition(skull.getPosX(), skull.getPosY() + source.getEyeHeight(), skull.getPosZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (WitherSkull)  skull);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.EYE_OF_ENDER, new SimpleDispenserLaunchLogic<>(EntityTypes.EYE_OF_ENDER));
        ProjectileUtil.registerProjectileLogic(EntityTypes.FISHING_BOBBER, new SimpleDispenserLaunchLogic<FishingBobber>(EntityTypes.FISHING_BOBBER) {

            @Override
            protected Optional<FishingBobber> createProjectile(LivingEntity source, ServerLocation loc) {
                if (source instanceof PlayerEntity) {
                    FishingBobber hook = (FishingBobber) new FishingBobberEntity((PlayerEntity) source, source.world, 0, 0);
                    return ProjectileUtil.doLaunch(loc.getWorld(), hook);
                }
                return super.createProjectile(source, loc);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.POTION, new SimpleDispenserLaunchLogic<Potion>(EntityTypes.POTION) {

            @Override
            protected Optional<Potion> createProjectile(LivingEntity source, ServerLocation loc) {
                PotionEntity potion = new PotionEntity(source.world, source);
                potion.setItem(new ItemStack(Items.SPLASH_POTION, 1));
                potion.shoot(source, source.rotationPitch, source.rotationYaw, -20.0F, 0.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (Potion) potion);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.LLAMA_SPIT, new SimpleEntityLaunchLogic<LlamaSpit>(EntityTypes.LLAMA_SPIT) {

            @Override
            public Optional<LlamaSpit> launch(ProjectileSource source) {
                if (!(source instanceof LlamaEntity)) {
                    return Optional.empty();
                }
                return super.launch(source);
            }

            @Override
            public Optional<LlamaSpit> createProjectile(ProjectileSource source, EntityType<LlamaSpit> projectileClass, ServerLocation loc) {
                LlamaEntity llama = (LlamaEntity) source;
                LlamaSpit llamaSpit = (LlamaSpit) new LlamaSpitEntity(llama.world, (LlamaEntity) source);
                Vec3d lookVec = llama.getLook(1);
                ((LlamaSpitEntity) llamaSpit).shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), llamaSpit);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.DRAGON_FIREBALL, new SimpleDispenserLaunchLogic<DragonFireball>(EntityTypes.DRAGON_FIREBALL) {

            @Override
            protected Optional<DragonFireball> createProjectile(LivingEntity source, ServerLocation loc) {
                Vec3d lookVec = source.getLook(1);
                DragonFireballEntity fireball = new DragonFireballEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setRawPosition(fireball.getPosX(), fireball.getPosY() + source.getEyeHeight(), fireball.getPosZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (DragonFireball) fireball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.SHULKER_BULLET, new SimpleEntityLaunchLogic<>(EntityTypes.SHULKER_BULLET));
    }
}
