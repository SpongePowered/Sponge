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
import org.spongepowered.api.ResourceKey;
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
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.world.ServerLocation;
import org.spongepowered.api.world.World;
import org.spongepowered.math.vector.Vector3d;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public final class ProjectileLauncher {

    private static final Map<ResourceKey, ProjectileLogic<?>> projectileLogic = Maps.newHashMap();
    private static final Map<Class<? extends ProjectileSource>, ProjectileSourceLogic<?>> projectileSourceLogic = Maps.newHashMap();

    public static <T extends Projectile> Optional<T> launch(final EntityType<T> projectileType, final ProjectileSource source,
            @Nullable final Vector3d vel) {
        final ProjectileLogic<T> logic = ProjectileLauncher.getLogic(projectileType);
        if (logic == null) {
            return Optional.empty();
        }
        final Optional<T> projectile = logic.launch(source);
        projectile.ifPresent(t -> {
            if (vel != null) {
                t.offer(Keys.VELOCITY, vel);
            }
            t.offer(Keys.SHOOTER, source);
        });
        return projectile;
    }

    public static <T extends Projectile, S extends ProjectileSource> Optional<T> launchWithArgs(final EntityType<T> projectileType,
            final Class<S> projectileSourceClass, final S source, @Nullable final Vector3d vel, final Object... args) {
        final ProjectileSourceLogic<S> sourceLogic = ProjectileLauncher.getSourceLogic(projectileSourceClass);
        if (sourceLogic == null) {
            return Optional.empty();
        }

        final ProjectileLogic<T> logic = ProjectileLauncher.getLogic(projectileType);
        if (logic == null) {
            return Optional.empty();
        }

        final Optional<T> projectile = sourceLogic.launch(logic, source, projectileType, args);
        projectile.ifPresent(t -> {
            if (vel != null) {
                t.offer(Keys.VELOCITY, vel);
            }
            t.offer(Keys.SHOOTER, source);
        });
        return projectile;
    }

    // From ThrowableEntity constructor
    private static void configureThrowable(final ThrowableEntity entity) {
        final double x = entity.getPosX() - MathHelper.cos(entity.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        final double y = entity.getPosY() - 0.1D;
        final double z = entity.getPosZ() - MathHelper.sin(entity.rotationYaw / 180.0F * (float) Math.PI) * 0.16F;
        entity.setPosition(x, y, z);
        final float f = 0.4F;
        final double motionX = -MathHelper.sin(entity.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f;
        final double motionZ = MathHelper.cos(entity.rotationYaw / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.rotationPitch / 180.0F * (float) Math.PI) * f;
        final double motionY = -MathHelper.sin((entity.rotationPitch) / 180.0F * (float) Math.PI) * f;
        entity.setMotion(motionX, motionY, motionZ);
    }

    public static <T extends Projectile> void registerProjectileLogic(final Supplier<EntityType<T>> projectileType, final ProjectileLogic<T> logic) {
        ProjectileLauncher.projectileLogic.put(projectileType.get().key(), logic);
    }

    public static <T extends ProjectileSource> void registerProjectileSourceLogic(final Class<T> projectileSourceClass,
            final ProjectileSourceLogic<T> logic) {
        ProjectileLauncher.projectileSourceLogic.put(projectileSourceClass, logic);
    }

    @SuppressWarnings("unchecked")
    static <T extends ProjectileSource> ProjectileSourceLogic<T> getSourceLogic(final Class<T> sourceClass) {
        return (ProjectileSourceLogic<T>) ProjectileLauncher.projectileSourceLogic.get(sourceClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Projectile> ProjectileLogic<T> getLogic(final EntityType<T> projectileType) {
        return (ProjectileLogic<T>) ProjectileLauncher.projectileLogic.get(projectileType.key());
    }

    @SuppressWarnings("unchecked")
    static <P extends Projectile> Optional<P> defaultLaunch(final ProjectileSource source,
            final EntityType<P> projectileType, final ServerLocation loc) {
        final Entity projectile = loc.getWorld().createEntity(projectileType, loc.getPosition());
        if (projectile instanceof ThrowableEntity) {
            ProjectileLauncher.configureThrowable((ThrowableEntity) projectile);
        }
        return ProjectileLauncher.doLaunch(loc.getWorld(), (P) projectile);
    }

    static <P extends Projectile> Optional<P> doLaunch(final World extent, final P projectile) {
        if (extent.spawnEntity(projectile)) {
            return Optional.of(projectile);
        }
        return Optional.empty();
    }

    static {
        ProjectileLauncher.registerProjectileSourceLogic(Dispenser.class, new DispenserSourceLogic());

        ProjectileLauncher.registerProjectileSourceLogic(Shulker.class, new ShulkerSourceLogic());

        ProjectileLauncher.registerProjectileLogic(EntityTypes.ARROW, new SimpleItemLaunchLogic<Arrow>(EntityTypes.ARROW, Items.ARROW) {

            @Override
            protected Optional<Arrow> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final ArrowEntity arrow = new ArrowEntity(source.world, source);
                arrow.shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 3.0F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (Arrow) arrow);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.SPECTRAL_ARROW, new SimpleItemLaunchLogic<SpectralArrow>(
                EntityTypes.SPECTRAL_ARROW, Items.SPECTRAL_ARROW) {

            @Override
            protected Optional<SpectralArrow> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final SpectralArrowEntity arrow = new SpectralArrowEntity(source.world, source);
                arrow.shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 3.0F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (SpectralArrow) arrow);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.EGG, new SimpleItemLaunchLogic<Egg>(EntityTypes.EGG, Items.EGG) {

            @Override
            protected Optional<Egg> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final EggEntity egg = new EggEntity(source.world, source);
                egg.shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (Egg) egg);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.SMALL_FIREBALL, new SimpleItemLaunchLogic<SmallFireball>(
                EntityTypes.SMALL_FIREBALL, Items.FIRE_CHARGE) {

            @Override
            protected Optional<SmallFireball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final Vec3d lookVec = source.getLook(1);
                final SmallFireballEntity fireball = new SmallFireballEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setPosition(fireball.getPosX(), fireball.getPosY() + source.getEyeHeight(), fireball.getPosZ());
                return ProjectileLauncher.doLaunch(loc.getWorld(), (SmallFireball) fireball);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.FIREWORK_ROCKET, new SimpleItemLaunchLogic<FireworkRocket>(
                EntityTypes.FIREWORK_ROCKET, Items.FIREWORK_ROCKET) {

            @Override
            protected Optional<FireworkRocket> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final FireworkRocketEntity firework = new FireworkRocketEntity(source.world, loc.getX(), loc.getY(), loc.getZ(), ItemStack.EMPTY);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (FireworkRocket) firework);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.SNOWBALL, new SimpleItemLaunchLogic<Snowball>(EntityTypes.SNOWBALL, Items.SNOWBALL) {

            @Override
            protected Optional<Snowball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final SnowballEntity snowball = new SnowballEntity(source.world, source);
                snowball.shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (Snowball) snowball);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.EXPERIENCE_BOTTLE, new SimpleItemLaunchLogic<ExperienceBottle>(
                EntityTypes.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE) {

            @Override
            protected Optional<ExperienceBottle> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final ExperienceBottleEntity expBottle = new ExperienceBottleEntity(source.world, source);
                expBottle.shoot(source, source.rotationPitch, source.rotationYaw, -20.0F, 0.7F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (ExperienceBottle) expBottle);
            }
        });

        ProjectileLauncher.registerProjectileLogic(EntityTypes.ENDER_PEARL, new SimpleItemLaunchLogic<EnderPearl>(
                EntityTypes.ENDER_PEARL, Items.ENDER_PEARL) {

            @Override
            protected Optional<EnderPearl> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final EnderPearlEntity pearl = new EnderPearlEntity(source.world, source);
                pearl.shoot(source, source.rotationPitch, source.rotationYaw, 0.0F, 1.5F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (EnderPearl) pearl);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.FIREBALL, new SimpleDispenserLaunchLogic<ExplosiveFireball>(EntityTypes.FIREBALL) {

            @Override
            protected Optional<ExplosiveFireball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final Vec3d lookVec = source.getLook(1);
                final FireballEntity fireball = new FireballEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setPosition(fireball.getPosX(), fireball.getPosY() + source.getEyeHeight(), fireball.getPosZ());
                return ProjectileLauncher.doLaunch(loc.getWorld(), (ExplosiveFireball) fireball);
            }

            @Override
            public Optional<ExplosiveFireball> createProjectile(final ProjectileSource source, final EntityType<ExplosiveFireball> projectileType,
                    final ServerLocation loc) {
                if (!(source instanceof DispenserTileEntity)) {
                    return super.createProjectile(source, projectileType, loc);
                }
                final DispenserTileEntity dispenser = (DispenserTileEntity) source;
                final Direction enumfacing = DispenserSourceLogic.getFacing(dispenser);
                final LivingEntity thrower = new ArmorStandEntity(dispenser.getWorld(), loc.getX() + enumfacing.getXOffset(),
                        loc.getY() + enumfacing.getYOffset(), loc.getZ() + enumfacing.getZOffset());
                final FireballEntity fireball = new FireballEntity(dispenser.getWorld(), thrower, 0, 0, 0);
                // Acceleration is set separately because the constructor applies a random value to it
                // As for 0.1;  it is a reasonable default value
                fireball.accelerationX = enumfacing.getXOffset() * 0.1;
                fireball.accelerationY = enumfacing.getYOffset() * 0.1;
                fireball.accelerationZ = enumfacing.getZOffset() * 0.1;
                return ProjectileLauncher.doLaunch(loc.getWorld(), (ExplosiveFireball) fireball);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.WITHER_SKULL, new SimpleDispenserLaunchLogic<WitherSkull>(EntityTypes.WITHER_SKULL) {

            @Override
            protected Optional<WitherSkull> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final Vec3d lookVec = source.getLook(1);
                final WitherSkullEntity skull = new WitherSkullEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                skull.setPosition(skull.getPosX(), skull.getPosY() + source.getEyeHeight(), skull.getPosZ());
                return ProjectileLauncher.doLaunch(loc.getWorld(), (WitherSkull) skull);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.EYE_OF_ENDER, new SimpleDispenserLaunchLogic<>(EntityTypes.EYE_OF_ENDER));
        ProjectileLauncher.registerProjectileLogic(EntityTypes.FISHING_BOBBER, new SimpleDispenserLaunchLogic<FishingBobber>(
                EntityTypes.FISHING_BOBBER) {

            @Override
            protected Optional<FishingBobber> createProjectile(final LivingEntity source, final ServerLocation loc) {
                if (source instanceof PlayerEntity) {
                    final FishingBobberEntity hook = new FishingBobberEntity(source.world, (PlayerEntity) source, loc.getX(), loc.getY(), loc.getZ());
                    return ProjectileLauncher.doLaunch(loc.getWorld(), (FishingBobber) hook);
                }
                return super.createProjectile(source, loc);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.POTION, new SimpleItemLaunchLogic<Potion>(EntityTypes.POTION, Items.SPLASH_POTION) {

            @Override
            protected Optional<Potion> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final PotionEntity potion = new PotionEntity(source.world, source);
                potion.setItem(new ItemStack(Items.SPLASH_POTION, 1));
                potion.shoot(source, source.rotationPitch, source.rotationYaw, -20.0F, 0.5F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (Potion) potion);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.LLAMA_SPIT, new SimpleEntityLaunchLogic<LlamaSpit>(EntityTypes.LLAMA_SPIT) {

            @Override
            public Optional<LlamaSpit> launch(final ProjectileSource source) {
                if (!(source instanceof LlamaEntity)) {
                    return Optional.empty();
                }
                return super.launch(source);
            }

            @Override
            public Optional<LlamaSpit> createProjectile(final ProjectileSource source,
                    final EntityType<LlamaSpit> projectileType, final ServerLocation loc) {
                final LlamaEntity llama = (LlamaEntity) source;
                final LlamaSpitEntity llamaSpit = new LlamaSpitEntity(llama.world, (LlamaEntity) source);
                final Vec3d lookVec = llama.getLook(1);
                llamaSpit.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 0);
                return ProjectileLauncher.doLaunch(loc.getWorld(), (LlamaSpit) llamaSpit);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.DRAGON_FIREBALL, new SimpleDispenserLaunchLogic<DragonFireball>(
                EntityTypes.DRAGON_FIREBALL) {

            @Override
            protected Optional<DragonFireball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final Vec3d lookVec = source.getLook(1);
                final DragonFireballEntity fireball = new DragonFireballEntity(source.world, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setPosition(fireball.getPosX(), fireball.getPosY() + source.getEyeHeight(), fireball.getPosZ());
                return ProjectileLauncher.doLaunch(loc.getWorld(), (DragonFireball) fireball);
            }
        });
        ProjectileLauncher.registerProjectileLogic(EntityTypes.SHULKER_BULLET, new SimpleEntityLaunchLogic<>(EntityTypes.SHULKER_BULLET));
    }
}
