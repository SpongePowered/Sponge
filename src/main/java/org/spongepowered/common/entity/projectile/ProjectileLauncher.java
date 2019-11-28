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
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.ShulkerBullet;
import org.spongepowered.api.entity.living.golem.Shulker;
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
import org.spongepowered.api.entity.projectile.arrow.Arrow;
import org.spongepowered.api.entity.projectile.arrow.SpectralArrow;
import org.spongepowered.api.entity.projectile.arrow.TippedArrow;
import org.spongepowered.api.entity.projectile.explosive.DragonFireball;
import org.spongepowered.api.entity.projectile.explosive.WitherSkull;
import org.spongepowered.api.entity.projectile.explosive.fireball.LargeFireball;
import org.spongepowered.api.entity.projectile.explosive.fireball.SmallFireball;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.entity.projectile.LaunchProjectileEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.extent.Extent;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.type.entity.EntityTypeRegistryModule;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nullable;

public class ProjectileLauncher {

    private static final Map<Class<? extends Projectile>, ProjectileLogic<?>> projectileLogic = Maps.newHashMap();
    private static final Map<Class<? extends ProjectileSource>, ProjectileSourceLogic<?>> projectileSourceLogic = Maps.newHashMap();

    public static <T extends Projectile> Optional<T> launch(Class<T> projectileClass, ProjectileSource source, @Nullable Vector3d vel) {
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
        return projectile;
    }

    public static <T extends Projectile, S extends ProjectileSource> Optional<T> launchWithArgs(Class<T> projectileClass,
            Class<S> projectileSourceClass, S source, @Nullable Vector3d vel, Object... args) {
        ProjectileSourceLogic<S> sourceLogic = getSourceLogic(projectileSourceClass);
        if (sourceLogic == null) {
            return Optional.empty();
        }

        ProjectileLogic<T> logic = getLogic(projectileClass);
        if (logic == null) {
            return Optional.empty();
        }

        Optional<T> projectile = sourceLogic.launch(logic, source, projectileClass, args);
        projectile.ifPresent(t -> {
            if (vel != null) {
                t.offer(Keys.VELOCITY, vel);
            }
            t.setShooter(source);
        });
        return projectile;
    }

    // From EntityThrowable constructor
    private static void configureThrowable(ThrowableEntity entity) {
        entity.field_70165_t -= MathHelper.func_76134_b(entity.field_70177_z / 180.0F * (float) Math.PI) * 0.16F;
        entity.field_70163_u -= 0.1D;
        entity.field_70161_v -= MathHelper.func_76126_a(entity.field_70177_z / 180.0F * (float) Math.PI) * 0.16F;
        entity.func_70107_b(entity.field_70165_t, entity.field_70163_u, entity.field_70161_v);
        float f = 0.4F;
        entity.field_70159_w = -MathHelper.func_76126_a(entity.field_70177_z / 180.0F * (float) Math.PI)
                * MathHelper.func_76134_b(entity.field_70125_A / 180.0F * (float) Math.PI) * f;
        entity.field_70179_y = MathHelper.func_76134_b(entity.field_70177_z / 180.0F * (float) Math.PI)
                * MathHelper.func_76134_b(entity.field_70125_A / 180.0F * (float) Math.PI) * f;
        entity.field_70181_x = -MathHelper.func_76126_a((entity.field_70125_A) / 180.0F * (float) Math.PI) * f;
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
        if (projectile instanceof ThrowableEntity) {
            configureThrowable((ThrowableEntity) projectile);
        }
        return doLaunch(loc.getExtent(), (P) projectile);
    }

    static <P extends Projectile> Optional<P> doLaunch(Extent extent, P projectile) {
        LaunchProjectileEvent event = SpongeEventFactory.createLaunchProjectileEvent(Sponge.getCauseStackManager().getCurrentCause(), projectile);
        SpongeImpl.getGame().getEventManager().post(event);
        if (!event.isCancelled() && extent.spawnEntity(projectile)) {
            return Optional.of(projectile);
        }
        return Optional.empty();
    }

    static {
        registerProjectileSourceLogic(Dispenser.class, new DispenserSourceLogic());

        registerProjectileSourceLogic(Shulker.class, new ShulkerSourceLogic());

        registerProjectileLogic(TippedArrow.class, new SimpleItemLaunchLogic<TippedArrow>(TippedArrow.class, Items.field_151032_g) {

            @Override
            protected Optional<TippedArrow> createProjectile(LivingEntity source, Location<?> loc) {
                TippedArrow arrow = (TippedArrow) new ArrowEntity(source.field_70170_p, source);
                ((ArrowEntity) arrow).func_184547_a(source, source.field_70125_A, source.field_70177_z, 0.0F, 3.0F, 0);
                return doLaunch(loc.getExtent(), arrow);
            }
        });
        registerProjectileLogic(SpectralArrow.class, new SimpleItemLaunchLogic<SpectralArrow>(SpectralArrow.class, Items.field_185166_h) {

            @Override
            protected Optional<SpectralArrow> createProjectile(LivingEntity source, Location<?> loc) {
                SpectralArrow arrow = (SpectralArrow) new SpectralArrowEntity(source.field_70170_p, source);
                ((SpectralArrowEntity) arrow).func_184547_a(source, source.field_70125_A, source.field_70177_z, 0.0F, 3.0F, 0);
                return doLaunch(loc.getExtent(), arrow);
            }
        });
        registerProjectileLogic(Arrow.class, new SimpleItemLaunchLogic<Arrow>(Arrow.class, Items.field_151032_g) {

            @Override
            protected Optional<Arrow> createProjectile(LivingEntity source, Location<?> loc) {
                TippedArrow arrow = (TippedArrow) new ArrowEntity(source.field_70170_p, source);
                ((ArrowEntity) arrow).func_184547_a(source, source.field_70125_A, source.field_70177_z, 0.0F, 3.0F, 0);
                return doLaunch(loc.getExtent(), arrow);
            }
        });
        registerProjectileLogic(Egg.class, new SimpleItemLaunchLogic<Egg>(Egg.class, Items.field_151110_aK) {

            @Override
            protected Optional<Egg> createProjectile(LivingEntity source, Location<?> loc) {
                Egg egg = (Egg) new EggEntity(source.field_70170_p, source);
                ((ThrowableEntity) egg).func_184538_a(source, source.field_70125_A, source.field_70177_z, 0.0F, 1.5F, 0);
                return doLaunch(loc.getExtent(), egg);
            }
        });
        registerProjectileLogic(SmallFireball.class, new SimpleItemLaunchLogic<SmallFireball>(SmallFireball.class, Items.field_151059_bz) {

            @Override
            protected Optional<SmallFireball> createProjectile(LivingEntity source, Location<?> loc) {
                Vec3d lookVec = source.func_70676_i(1);
                SmallFireball fireball = (SmallFireball) new SmallFireballEntity(source.field_70170_p, source,
                        lookVec.field_72450_a * 4, lookVec.field_72448_b * 4, lookVec.field_72449_c * 4);
                ((SmallFireballEntity) fireball).field_70163_u += source.func_70047_e();
                return doLaunch(loc.getExtent(), fireball);
            }
        });
        registerProjectileLogic(Firework.class, new SimpleItemLaunchLogic<Firework>(Firework.class, Items.field_151152_bP) {

            @Override
            protected Optional<Firework> createProjectile(LivingEntity source, Location<?> loc) {
                Firework firework = (Firework) new FireworkRocketEntity(source.field_70170_p, loc.getX(), loc.getY(), loc.getZ(), ItemStack.field_190927_a);
                return doLaunch(loc.getExtent(), firework);
            }
        });
        registerProjectileLogic(Snowball.class, new SimpleItemLaunchLogic<Snowball>(Snowball.class, Items.field_151126_ay) {

            @Override
            protected Optional<Snowball> createProjectile(LivingEntity source, Location<?> loc) {
                Snowball snowball = (Snowball) new SnowballEntity(source.field_70170_p, source);
                ((ThrowableEntity) snowball).func_184538_a(source, source.field_70125_A, source.field_70177_z, 0.0F, 1.5F, 0);
                return doLaunch(loc.getExtent(), snowball);
            }
        });
        registerProjectileLogic(ThrownExpBottle.class, new SimpleItemLaunchLogic<ThrownExpBottle>(ThrownExpBottle.class, Items.field_151062_by) {

            @Override
            protected Optional<ThrownExpBottle> createProjectile(LivingEntity source, Location<?> loc) {
                ThrownExpBottle expBottle = (ThrownExpBottle) new ExperienceBottleEntity(source.field_70170_p, source);
                ((ThrowableEntity) expBottle).func_184538_a(source, source.field_70125_A, source.field_70177_z, -20.0F, 0.7F, 0);
                return doLaunch(loc.getExtent(), expBottle);
            }
        });

        registerProjectileLogic(EnderPearl.class, new SimpleDispenserLaunchLogic<EnderPearl>(EnderPearl.class) {

            @Override
            protected Optional<EnderPearl> createProjectile(LivingEntity source, Location<?> loc) {
                EnderPearl pearl = (EnderPearl) new EnderPearlEntity(source.field_70170_p, source);
                ((ThrowableEntity) pearl).func_184538_a(source, source.field_70125_A, source.field_70177_z, 0.0F, 1.5F, 0);
                return doLaunch(loc.getExtent(), pearl);
            }
        });
        registerProjectileLogic(LargeFireball.class, new SimpleDispenserLaunchLogic<LargeFireball>(LargeFireball.class) {

            @Override
            protected Optional<LargeFireball> createProjectile(LivingEntity source, Location<?> loc) {
                Vec3d lookVec = source.func_70676_i(1);
                LargeFireball fireball = (LargeFireball) new FireballEntity(source.field_70170_p, source,
                        lookVec.field_72450_a * 4, lookVec.field_72448_b * 4, lookVec.field_72449_c * 4);
                ((FireballEntity) fireball).field_70163_u += source.func_70047_e();
                return doLaunch(loc.getExtent(), fireball);
            }

            @Override
            public Optional<LargeFireball> createProjectile(ProjectileSource source, Class<LargeFireball> projectileClass, Location<?> loc) {
                if (!(source instanceof DispenserTileEntity)) {
                    return super.createProjectile(source, projectileClass, loc);
                }
                DispenserTileEntity dispenser = (DispenserTileEntity) source;
                Direction enumfacing = DispenserSourceLogic.getFacing(dispenser);
                Random random = dispenser.func_145831_w().field_73012_v;
                double d3 = random.nextGaussian() * 0.05D + enumfacing.func_82601_c();
                double d4 = random.nextGaussian() * 0.05D + enumfacing.func_96559_d();
                double d5 = random.nextGaussian() * 0.05D + enumfacing.func_82599_e();
                LivingEntity thrower = new ArmorStandEntity(dispenser.func_145831_w(), loc.getX() + enumfacing.func_82601_c(),
                        loc.getY() + enumfacing.func_96559_d(), loc.getZ() + enumfacing.func_82599_e());
                LargeFireball fireball = (LargeFireball) new FireballEntity(dispenser.func_145831_w(), thrower, d3, d4, d5);
                return doLaunch(loc.getExtent(), fireball);
            }
        });
        registerProjectileLogic(WitherSkull.class, new SimpleDispenserLaunchLogic<WitherSkull>(WitherSkull.class) {

            @Override
            protected Optional<WitherSkull> createProjectile(LivingEntity source, Location<?> loc) {
                Vec3d lookVec = source.func_70676_i(1);
                WitherSkull skull = (WitherSkull) new WitherSkullEntity(source.field_70170_p, source,
                        lookVec.field_72450_a * 4, lookVec.field_72448_b * 4, lookVec.field_72449_c * 4);
                ((WitherSkullEntity) skull).field_70163_u += source.func_70047_e();
                return doLaunch(loc.getExtent(), skull);
            }
        });
        registerProjectileLogic(EyeOfEnder.class, new SimpleDispenserLaunchLogic<>(EyeOfEnder.class));
        registerProjectileLogic(FishHook.class, new SimpleDispenserLaunchLogic<FishHook>(FishHook.class) {

            @Override
            protected Optional<FishHook> createProjectile(LivingEntity source, Location<?> loc) {
                if (source instanceof PlayerEntity) {
                    FishHook hook = (FishHook) new FishingBobberEntity(source.field_70170_p, (PlayerEntity) source);
                    return doLaunch(loc.getExtent(), hook);
                }
                return super.createProjectile(source, loc);
            }
        });
        registerProjectileLogic(ThrownPotion.class, new SimpleDispenserLaunchLogic<ThrownPotion>(ThrownPotion.class) {

            @Override
            protected Optional<ThrownPotion> createProjectile(LivingEntity source, Location<?> loc) {
                ThrownPotion potion = (ThrownPotion) new PotionEntity(source.field_70170_p, source, new ItemStack(Items.field_185155_bH, 1));
                ((ThrowableEntity) potion).func_184538_a(source, source.field_70125_A, source.field_70177_z, -20.0F, 0.5F, 0);
                return doLaunch(loc.getExtent(), potion);
            }
        });
        registerProjectileLogic(LlamaSpit.class, new SimpleEntityLaunchLogic<LlamaSpit>(LlamaSpit.class) {

            @Override
            public Optional<LlamaSpit> launch(ProjectileSource source) {
                if (!(source instanceof LlamaEntity)) {
                    return Optional.empty();
                }
                return super.launch(source);
            }

            @Override
            public Optional<LlamaSpit> createProjectile(ProjectileSource source, Class<LlamaSpit> projectileClass, Location<?> loc) {
                LlamaEntity llama = (LlamaEntity) source;
                LlamaSpit llamaSpit = (LlamaSpit) new LlamaSpitEntity(llama.field_70170_p, (LlamaEntity) source);
                Vec3d lookVec = llama.func_70676_i(1);
                ((LlamaSpitEntity) llamaSpit).func_70186_c(lookVec.field_72450_a, lookVec.field_72448_b, lookVec.field_72449_c, 1.5F, 0);
                return doLaunch(loc.getExtent(), llamaSpit);
            }
        });
        registerProjectileLogic(DragonFireball.class, new SimpleDispenserLaunchLogic<DragonFireball>(DragonFireball.class) {

            @Override
            protected Optional<DragonFireball> createProjectile(LivingEntity source, Location<?> loc) {
                Vec3d lookVec = source.func_70676_i(1);
                DragonFireball fireball = (DragonFireball) new DragonFireballEntity(source.field_70170_p, source,
                        lookVec.field_72450_a * 4, lookVec.field_72448_b * 4, lookVec.field_72449_c * 4);
                ((DragonFireballEntity) fireball).field_70163_u += source.func_70047_e();
                return doLaunch(loc.getExtent(), fireball);
            }
        });
        registerProjectileLogic(ShulkerBullet.class, new SimpleEntityLaunchLogic<>(ShulkerBullet.class));
    }
}
