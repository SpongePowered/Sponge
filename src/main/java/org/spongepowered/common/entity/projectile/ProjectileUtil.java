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
import net.minecraft.entity.passive.horse.LlamaEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
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

public final class ProjectileUtil {

    private static final Map<ResourceKey, ProjectileLogic<?>> projectileLogic = Maps.newHashMap();
    private static final Map<Class<? extends ProjectileSource>, ProjectileSourceLogic<?>> projectileSourceLogic = Maps.newHashMap();

    public static <T extends Projectile> Optional<T> launch(final EntityType<T> projectileType, final ProjectileSource source,
            @Nullable final Vector3d vel) {
        final ProjectileLogic<T> logic = ProjectileUtil.getLogic(projectileType);
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
        final ProjectileSourceLogic<S> sourceLogic = ProjectileUtil.getSourceLogic(projectileSourceClass);
        if (sourceLogic == null) {
            return Optional.empty();
        }

        final ProjectileLogic<T> logic = ProjectileUtil.getLogic(projectileType);
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
        final double x = entity.getX() - MathHelper.cos(entity.yRot / 180.0F * (float) Math.PI) * 0.16F;
        final double y = entity.getY() - 0.1D;
        final double z = entity.getZ() - MathHelper.sin(entity.yRot / 180.0F * (float) Math.PI) * 0.16F;
        entity.setPos(x, y, z);
        final float f = 0.4F;
        final double motionX = -MathHelper.sin(entity.yRot / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.xRot / 180.0F * (float) Math.PI) * f;
        final double motionZ = MathHelper.cos(entity.yRot / 180.0F * (float) Math.PI)
                * MathHelper.cos(entity.xRot / 180.0F * (float) Math.PI) * f;
        final double motionY = -MathHelper.sin((entity.xRot) / 180.0F * (float) Math.PI) * f;
        entity.setDeltaMovement(motionX, motionY, motionZ);
    }

    public static <T extends Projectile> void registerProjectileLogic(final Supplier<EntityType<T>> projectileType, final ProjectileLogic<T> logic) {
        ProjectileUtil.projectileLogic.put(projectileType.get().key(), logic);
    }

    public static <T extends ProjectileSource> void registerProjectileSourceLogic(final Class<T> projectileSourceClass,
            final ProjectileSourceLogic<T> logic) {
        ProjectileUtil.projectileSourceLogic.put(projectileSourceClass, logic);
    }

    @SuppressWarnings("unchecked")
    static <T extends ProjectileSource> ProjectileSourceLogic<T> getSourceLogic(final Class<T> sourceClass) {
        return (ProjectileSourceLogic<T>) ProjectileUtil.projectileSourceLogic.get(sourceClass);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Projectile> ProjectileLogic<T> getLogic(final EntityType<T> projectileType) {
        return (ProjectileLogic<T>) ProjectileUtil.projectileLogic.get(projectileType.key());
    }

    @SuppressWarnings("unchecked")
    static <P extends Projectile> Optional<P> defaultLaunch(final ProjectileSource source,
            final EntityType<P> projectileType, final ServerLocation loc) {
        final Entity projectile = loc.getWorld().createEntity(projectileType, loc.getPosition());
        if (projectile instanceof ThrowableEntity) {
            ProjectileUtil.configureThrowable((ThrowableEntity) projectile);
        }
        return ProjectileUtil.doLaunch(loc.getWorld(), (P) projectile);
    }

    static <P extends Projectile> Optional<P> doLaunch(final World extent, final P projectile) {
        if (extent.spawnEntity(projectile)) {
            return Optional.of(projectile);
        }
        return Optional.empty();
    }

    static {
        ProjectileUtil.registerProjectileSourceLogic(Dispenser.class, new DispenserSourceLogic());

        ProjectileUtil.registerProjectileSourceLogic(Shulker.class, new ShulkerSourceLogic());

        ProjectileUtil.registerProjectileLogic(EntityTypes.ARROW, new SimpleItemLaunchLogic<Arrow>(EntityTypes.ARROW, Items.ARROW) {

            @Override
            protected Optional<Arrow> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final ArrowEntity arrow = new ArrowEntity(source.level, source);
                arrow.shoot(source.xRot, source.yRot, 0.0F, 3.0F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (Arrow) arrow);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.SPECTRAL_ARROW, new SimpleItemLaunchLogic<SpectralArrow>(
                EntityTypes.SPECTRAL_ARROW, Items.SPECTRAL_ARROW) {

            @Override
            protected Optional<SpectralArrow> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final SpectralArrowEntity arrow = new SpectralArrowEntity(source.level, source);
                arrow.shoot(source.xRot, source.yRot, 0.0F, 3.0F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (SpectralArrow) arrow);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.EGG, new SimpleItemLaunchLogic<Egg>(EntityTypes.EGG, Items.EGG) {

            @Override
            protected Optional<Egg> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final EggEntity egg = new EggEntity(source.level, source);
                egg.shoot(source.xRot, source.yRot, 0.0F, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (Egg) egg);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.SMALL_FIREBALL, new SimpleItemLaunchLogic<SmallFireball>(
                EntityTypes.SMALL_FIREBALL, Items.FIRE_CHARGE) {

            @Override
            protected Optional<SmallFireball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final net.minecraft.util.math.vector.Vector3d lookVec = source.getViewVector(1);
                final SmallFireballEntity fireball = new SmallFireballEntity(source.level, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setPos(fireball.getX(), fireball.getY() + source.getEyeHeight(), fireball.getZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (SmallFireball) fireball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.FIREWORK_ROCKET, new SimpleItemLaunchLogic<FireworkRocket>(
                EntityTypes.FIREWORK_ROCKET, Items.FIREWORK_ROCKET) {

            @Override
            protected Optional<FireworkRocket> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final FireworkRocketEntity firework = new FireworkRocketEntity(source.level, loc.getX(), loc.getY(), loc.getZ(), ItemStack.EMPTY);
                return ProjectileUtil.doLaunch(loc.getWorld(), (FireworkRocket) firework);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.SNOWBALL, new SimpleItemLaunchLogic<Snowball>(EntityTypes.SNOWBALL, Items.SNOWBALL) {

            @Override
            protected Optional<Snowball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final SnowballEntity snowball = new SnowballEntity(source.level, source);
                snowball.shoot(source.xRot, source.yRot, 0.0F, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (Snowball) snowball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.EXPERIENCE_BOTTLE, new SimpleItemLaunchLogic<ExperienceBottle>(
                EntityTypes.EXPERIENCE_BOTTLE, Items.EXPERIENCE_BOTTLE) {

            @Override
            protected Optional<ExperienceBottle> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final ExperienceBottleEntity expBottle = new ExperienceBottleEntity(source.level, source);
                expBottle.shoot(source.xRot, source.yRot, -20.0F, 0.7F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (ExperienceBottle) expBottle);
            }
        });

        ProjectileUtil.registerProjectileLogic(EntityTypes.ENDER_PEARL, new SimpleItemLaunchLogic<EnderPearl>(
                EntityTypes.ENDER_PEARL, Items.ENDER_PEARL) {

            @Override
            protected Optional<EnderPearl> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final EnderPearlEntity pearl = new EnderPearlEntity(source.level, source);
                pearl.shoot(source.xRot, source.yRot, 0.0F, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (EnderPearl) pearl);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.FIREBALL, new SimpleDispenserLaunchLogic<ExplosiveFireball>(EntityTypes.FIREBALL) {

            @Override
            protected Optional<ExplosiveFireball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final net.minecraft.util.math.vector.Vector3d lookVec = source.getViewVector(1);
                final FireballEntity fireball = new FireballEntity(source.level, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setPos(fireball.getX(), fireball.getY() + source.getEyeHeight(), fireball.getZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (ExplosiveFireball) fireball);
            }

            @Override
            public Optional<ExplosiveFireball> createProjectile(final ProjectileSource source, final EntityType<ExplosiveFireball> projectileType,
                    final ServerLocation loc) {
                if (!(source instanceof DispenserTileEntity)) {
                    return super.createProjectile(source, projectileType, loc);
                }
                final DispenserTileEntity dispenser = (DispenserTileEntity) source;
                final Direction enumfacing = DispenserSourceLogic.getFacing(dispenser);
                final LivingEntity thrower = new ArmorStandEntity(dispenser.getLevel(), loc.getX() + enumfacing.getStepX(),
                        loc.getY() + enumfacing.getStepY(), loc.getZ() + enumfacing.getStepZ());
                final FireballEntity fireball = new FireballEntity(dispenser.getLevel(), thrower, 0, 0, 0);
                // Acceleration is set separately because the constructor applies a random value to it
                // As for 0.1;  it is a reasonable default value
                fireball.xPower = enumfacing.getStepX() * 0.1;
                fireball.yPower = enumfacing.getStepY() * 0.1;
                fireball.zPower = enumfacing.getStepZ() * 0.1;
                return ProjectileUtil.doLaunch(loc.getWorld(), (ExplosiveFireball) fireball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.WITHER_SKULL, new SimpleDispenserLaunchLogic<WitherSkull>(EntityTypes.WITHER_SKULL) {

            @Override
            protected Optional<WitherSkull> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final net.minecraft.util.math.vector.Vector3d lookVec = source.getViewVector(1);
                final WitherSkullEntity skull = new WitherSkullEntity(source.level, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                skull.setPos(skull.getX(), skull.getY() + source.getEyeHeight(), skull.getZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (WitherSkull) skull);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.EYE_OF_ENDER, new SimpleDispenserLaunchLogic<>(EntityTypes.EYE_OF_ENDER));
        ProjectileUtil.registerProjectileLogic(EntityTypes.FISHING_BOBBER, new SimpleDispenserLaunchLogic<FishingBobber>(
                EntityTypes.FISHING_BOBBER) {

            @Override
            protected Optional<FishingBobber> createProjectile(final LivingEntity source, final ServerLocation loc) {
                if (source instanceof PlayerEntity) {
                    final FishingBobberEntity hook = new FishingBobberEntity(source.level, (PlayerEntity) source, loc.getX(), loc.getY(), loc.getZ());
                    return ProjectileUtil.doLaunch(loc.getWorld(), (FishingBobber) hook);
                }
                return super.createProjectile(source, loc);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.POTION, new SimpleItemLaunchLogic<Potion>(EntityTypes.POTION, Items.SPLASH_POTION) {

            @Override
            protected Optional<Potion> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final PotionEntity potion = new PotionEntity(source.level, source);
                potion.setItem(new ItemStack(Items.SPLASH_POTION, 1));
                potion.shoot(source.xRot, source.yRot, -20.0F, 0.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (Potion) potion);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.LLAMA_SPIT, new SimpleEntityLaunchLogic<LlamaSpit>(EntityTypes.LLAMA_SPIT) {

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
                final LlamaSpitEntity llamaSpit = new LlamaSpitEntity(llama.level, (LlamaEntity) source);
                final net.minecraft.util.math.vector.Vector3d lookVec = llama.getViewVector(1);
                llamaSpit.shoot(lookVec.x, lookVec.y, lookVec.z, 1.5F, 0);
                return ProjectileUtil.doLaunch(loc.getWorld(), (LlamaSpit) llamaSpit);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.DRAGON_FIREBALL, new SimpleDispenserLaunchLogic<DragonFireball>(
                EntityTypes.DRAGON_FIREBALL) {

            @Override
            protected Optional<DragonFireball> createProjectile(final LivingEntity source, final ServerLocation loc) {
                final net.minecraft.util.math.vector.Vector3d lookVec = source.getViewVector(1);
                final DragonFireballEntity fireball = new DragonFireballEntity(source.level, source,
                        lookVec.x * 4, lookVec.y * 4, lookVec.z * 4);
                fireball.setPos(fireball.getX(), fireball.getY() + source.getEyeHeight(), fireball.getZ());
                return ProjectileUtil.doLaunch(loc.getWorld(), (DragonFireball) fireball);
            }
        });
        ProjectileUtil.registerProjectileLogic(EntityTypes.SHULKER_BULLET, new SimpleEntityLaunchLogic<>(EntityTypes.SHULKER_BULLET));
    }
}
