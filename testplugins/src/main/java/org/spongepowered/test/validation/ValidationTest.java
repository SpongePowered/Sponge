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
package org.spongepowered.test.validation;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.effect.Viewer;
import org.spongepowered.api.entity.Aerial;
import org.spongepowered.api.entity.Ageable;
import org.spongepowered.api.entity.Angerable;
import org.spongepowered.api.entity.Breedable;
import org.spongepowered.api.entity.Chargeable;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.ItemRepresentable;
import org.spongepowered.api.entity.Ownable;
import org.spongepowered.api.entity.Ranger;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.attribute.AttributeHolder;
import org.spongepowered.api.entity.display.DisplayEntity;
import org.spongepowered.api.entity.explosive.Explosive;
import org.spongepowered.api.entity.explosive.fused.FusedExplosive;
import org.spongepowered.api.entity.hanging.Hanging;
import org.spongepowered.api.entity.hanging.LeashKnot;
import org.spongepowered.api.entity.living.Agent;
import org.spongepowered.api.entity.living.Ambient;
import org.spongepowered.api.entity.living.ComplexLiving;
import org.spongepowered.api.entity.living.Hostile;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.PathfinderAgent;
import org.spongepowered.api.entity.living.RangedAgent;
import org.spongepowered.api.entity.living.animal.Sittable;
import org.spongepowered.api.entity.living.animal.TameableAnimal;
import org.spongepowered.api.entity.living.animal.cow.Mooshroom;
import org.spongepowered.api.entity.living.animal.horse.HorseLike;
import org.spongepowered.api.entity.living.animal.horse.PackHorse;
import org.spongepowered.api.entity.living.aquatic.Aquatic;
import org.spongepowered.api.entity.living.aquatic.fish.Fish;
import org.spongepowered.api.entity.living.aquatic.fish.school.SchoolingFish;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.entity.living.monster.Patroller;
import org.spongepowered.api.entity.living.monster.boss.Boss;
import org.spongepowered.api.entity.living.monster.boss.Wither;
import org.spongepowered.api.entity.living.monster.piglin.PiglinLike;
import org.spongepowered.api.entity.living.monster.raider.illager.Illager;
import org.spongepowered.api.entity.living.monster.raider.illager.spellcaster.Spellcaster;
import org.spongepowered.api.entity.living.monster.skeleton.SkeletonLike;
import org.spongepowered.api.entity.living.trader.VillagerLike;
import org.spongepowered.api.entity.projectile.AcceleratingProjectile;
import org.spongepowered.api.entity.projectile.Egg;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.entity.projectile.ExperienceBottle;
import org.spongepowered.api.entity.projectile.FishingBobber;
import org.spongepowered.api.entity.projectile.IgnitingProjectile;
import org.spongepowered.api.entity.projectile.Potion;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.arrow.ArrowLike;
import org.spongepowered.api.entity.projectile.arrow.Trident;
import org.spongepowered.api.entity.projectile.explosive.FireworkRocket;
import org.spongepowered.api.entity.projectile.explosive.fireball.ExplosiveFireball;
import org.spongepowered.api.entity.projectile.windcharge.WindChargeLike;
import org.spongepowered.api.entity.vehicle.Vehicle;
import org.spongepowered.api.entity.vehicle.minecart.BlockOccupiedMinecart;
import org.spongepowered.api.entity.vehicle.minecart.CommandBlockMinecart;
import org.spongepowered.api.entity.vehicle.minecart.FurnaceMinecart;
import org.spongepowered.api.entity.vehicle.minecart.MinecartLike;
import org.spongepowered.api.entity.vehicle.minecart.SpawnerMinecart;
import org.spongepowered.api.entity.vehicle.minecart.TNTMinecart;
import org.spongepowered.api.entity.vehicle.minecart.carrier.CarrierMinecart;
import org.spongepowered.api.entity.vehicle.minecart.carrier.ChestMinecart;
import org.spongepowered.api.entity.vehicle.minecart.carrier.HopperMinecart;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.item.inventory.ArmorEquipable;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Equipable;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.inventory.type.ViewableInventory;
import org.spongepowered.api.projectile.source.EntityProjectileSource;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.scoreboard.TeamMember;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.spawner.Spawner;
import org.spongepowered.api.util.Identifiable;
import org.spongepowered.api.util.Nameable;
import org.spongepowered.api.util.RandomProvider;
import org.spongepowered.api.util.annotation.CatalogedBy;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Plugin("validationtest")
public final class ValidationTest implements LoadableModule {

    private final PluginContainer plugin;
    private final Logger logger;
    private boolean verbose;

    @Inject
    public ValidationTest(final PluginContainer plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Override
    public void enable(final CommandContext ctx) {
    }

    @Listener
    private void onRegisterSpongeCommand(final RegisterCommandEvent<Command.Parameterized> event) {
        event.register(
            this.plugin,
            Command.builder()
                .executor(context -> {
                    this.verbose = !this.verbose;
                    context.sendMessage(Component.text("Verbose flag set to " + this.verbose));
                    return CommandResult.success();
                })
                .build(),
            "toggleverbose");
        event.register(
            this.plugin,
            Command.builder()
                .executor(context -> {
                    for (final Field field : RegistryTypes.class.getDeclaredFields()) {
                        final Object registryField;
                        try {
                            registryField = field.get(null);
                        } catch (final IllegalAccessException e) {
                            this.logger.error("Failed to get field {}: {}", field.getName(), e.getMessage());
                            if (this.verbose) {
                                this.logger.error("Exception", e);
                            }
                            continue;
                        }

                        if (registryField instanceof final DefaultedRegistryType<?> registryType) {
                            if (registryType.find().isEmpty()) {
                                this.logger.error("Registry {} is empty", registryType.location());
                                continue;
                            }

                            final var typeArg = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                            final Class<?> catalogEntryClass;
                            switch (typeArg) {
                                case final ParameterizedType parameterizedTypeArg ->
                                    catalogEntryClass = (Class<?>) parameterizedTypeArg.getRawType();
                                case final Class<?> clazz -> catalogEntryClass = clazz;
                                case null, default -> {
                                    this.logger.error("Unhandled catalog entry arg type: {}", typeArg);
                                    continue;
                                }
                            }

                            final var catalogedByAnnotation = catalogEntryClass.getDeclaredAnnotation(CatalogedBy.class);

                            if (catalogedByAnnotation == null) {
                                this.logger.error("Class {} in registry {} is not annotated with CatalogedBy", catalogEntryClass.getName(), registryType.location());
                                continue;
                            }

                            final var catalogClass = catalogedByAnnotation.value()[0];
                            if (!Modifier.isFinal(catalogClass.getModifiers())) {
                                this.logger.error("{} is not final", catalogClass.getName());
                            }

                            if (Arrays.stream(catalogClass.getDeclaredConstructors()).anyMatch(ctor -> !Modifier.isPrivate(ctor.getModifiers()))) {
                                this.logger.error("{} has non-private constructors", catalogClass.getName());
                            }

                            final Method registryMethod;
                            try {
                                registryMethod = catalogClass.getDeclaredMethod("registry");
                            } catch (final NoSuchMethodException e) {
                                this.logger.error("{}.registry() does not exist", catalogClass.getName());
                                continue;
                            }

                            final Object registryReturn;
                            try {
                                registryReturn = registryMethod.invoke(null);
                            } catch (final Throwable e) {
                                this.logger.error("{}.registry() failed: {}", catalogClass.getName(), e.getMessage());
                                if (this.verbose) {
                                    this.logger.error("Exception", e);
                                }
                                continue;
                            }

                            if (registryReturn == null) {
                                this.logger.error("{}.registry() returned null", catalogClass.getName());
                                continue;
                            }

                            if (registryReturn != registryType.get()) {
                                this.logger.error("{}.registry() returned a different registry than the one specified in RegistryTypes", catalogClass.getName());
                                continue;
                            }

                            for (final Field catalogField : catalogClass.getDeclaredFields()) {
                                final Object catalogObj;
                                try {
                                    catalogObj = catalogField.get(null);
                                } catch (final Throwable e) {
                                    this.logger.error("Failed to get field {}: {}", catalogField.getName(), e.getMessage());
                                    if (this.verbose) {
                                        this.logger.error("Exception", e);
                                    }
                                    continue;
                                }

                                if (catalogObj instanceof final DefaultedRegistryReference<?> reference) {
                                    if (reference.find().isEmpty()) {
                                        this.logger.error("{}.{}.find() is empty", catalogClass.getName(), catalogField.getName());
                                    }
                                }
                            }

                        } else {
                            this.logger.error("{} is not a DefaultedRegistryType", field.getName());
                        }
                    }
                    context.sendMessage(Component.text("Done!"));
                    return CommandResult.success();
                })
                .build(),
            "checkregistries");
        event.register(
            this.plugin,
            Command.builder()
                .executor(context -> {
                    final Class<?> entityTypeClass;
                    try {
                        entityTypeClass = Class.forName("net.minecraft.world.entity.EntityType");
                    } catch (final ClassNotFoundException e) {
                        this.logger.error("Failed to get EntityType class: {}", e.getMessage());
                        if (this.verbose) {
                            this.logger.error("Exception", e);
                        }
                        return CommandResult.success();
                    }

                    final Set<Class<?>> seen = Collections.newSetFromMap(new IdentityHashMap<>());
                    for (final Field field : entityTypeClass.getDeclaredFields()) {
                        if (!field.getType().getSimpleName().equals("EntityType")) {
                            continue;
                        }

                        if (!(field.getGenericType() instanceof ParameterizedType)) {
                            this.logger.error("Non ParameterizedType EntityType field: {}", field.getName());
                            continue;
                        }

                        final var typeArg = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];

                        if (!(typeArg instanceof Class<?>)) {
                            this.logger.error("Non ParameterizedType EntityType field {}: {}", field.getName(), typeArg.getClass().getName());
                            continue;
                        }

                        final var unmappedMinecraftClasses = Set.of(
                            "net.minecraft.commands.CommandSource",
                            "net.minecraft.network.syncher.SyncedDataHolder",
                            "net.minecraft.world.Container",
                            "net.minecraft.world.MenuProvider",
                            "net.minecraft.world.entity.animal.Bucketable",
                            "net.minecraft.world.entity.animal.ShoulderRidingEntity",
                            "net.minecraft.world.entity.decoration.HangingEntity",
                            "net.minecraft.world.entity.monster.CrossbowAttackMob",
                            "net.minecraft.world.entity.monster.hoglin.HoglinBase",
                            "net.minecraft.world.entity.npc.InventoryCarrier",
                            "net.minecraft.world.entity.npc.Npc",
                            "net.minecraft.world.entity.npc.VillagerDataHolder",
                            "net.minecraft.world.entity.projectile.ThrowableProjectile",
                            "net.minecraft.world.entity.vehicle.ContainerEntity",
                            "net.minecraft.world.entity.HasCustomInventoryScreen",
                            "net.minecraft.world.entity.ItemSteerable",
                            "net.minecraft.world.entity.EquipmentUser",
                            "net.minecraft.world.entity.LerpingModel",
                            "net.minecraft.world.entity.PlayerRideable",
                            "net.minecraft.world.entity.PlayerRideableJumping",
                            "net.minecraft.world.entity.ReputationEventHandler",
                            "net.minecraft.world.entity.Shearable",
                            "net.minecraft.world.entity.TraceableEntity",
                            "net.minecraft.world.entity.VariantHolder",
                            "net.minecraft.world.inventory.MenuConstructor",
                            "net.minecraft.world.level.block.entity.Hopper",
                            "net.minecraft.world.level.entity.EntityAccess",
                            "net.minecraft.world.level.gameevent.vibrations.VibrationSystem",
                            "net.minecraft.world.scores.ScoreHolder",
                            "net.minecraft.world.Clearable",
                            "net.minecraft.world.ContainerListener",
                            "net.minecraft.world.Nameable"
                        );

                        final var unmappedSpongeClasses = Set.of(
                            AttributeHolder.class,
                            BlockOccupiedMinecart.class,
                            Boss.class,
                            ComplexLiving.class,
                            EntityProjectileSource.class,
                            FusedExplosive.class,
                            Humanoid.class,
                            Identifiable.class,
                            Nameable.class,
                            RangedAgent.class,
                            Tamer.class,
                            // TODO
                            ArmorEquipable.class,
                            Equipable.class,
                            Explosive.class
                        );

                        final Map<String, Class<?>> minecraftToSpongeMapping = Map.ofEntries(
                            Map.entry("AbstractArrow", ArrowLike.class),
                            Map.entry("AbstractChestedHorse", PackHorse.class),
                            Map.entry("AbstractFish", Fish.class),
                            Map.entry("AbstractGolem", Golem.class),
                            Map.entry("AbstractHorse", HorseLike.class),
                            Map.entry("AbstractHurtingProjectile", AcceleratingProjectile.class),
                            Map.entry("AbstractIllager", Illager.class),
                            Map.entry("AbstractMinecart", MinecartLike.class),
                            Map.entry("AbstractMinecartContainer", CarrierMinecart.class),
                            Map.entry("AbstractPiglin", PiglinLike.class),
                            Map.entry("AbstractSchoolingFish", SchoolingFish.class),
                            Map.entry("AbstractSkeleton", SkeletonLike.class),
                            Map.entry("AbstractVillager", VillagerLike.class),
                            Map.entry("AbstractWindCharge", WindChargeLike.class),
                            Map.entry("AgeableMob", Ageable.class),
                            Map.entry("AmbientCreature", Ambient.class),
                            Map.entry("BlockAttachedEntity", Hanging.class),
                            Map.entry("Display", DisplayEntity.class),
                            Map.entry("Enemy", Hostile.class),
                            Map.entry("FallingBlockEntity", FallingBlock.class),
                            Map.entry("Fireball", IgnitingProjectile.class),
                            Map.entry("FireworkRocketEntity", FireworkRocket.class),
                            Map.entry("FishingHook", FishingBobber.class),
                            Map.entry("FlyingAnimal", Aerial.class),
                            Map.entry("FlyingMob", Aerial.class),
                            Map.entry("ItemEntity", Item.class),
                            Map.entry("ItemSupplier", ItemRepresentable.class),
                            Map.entry("LargeFireball", ExplosiveFireball.class),
                            Map.entry("LeashFenceKnotEntity", LeashKnot.class),
                            Map.entry("LivingEntity", Living.class),
                            Map.entry("MinecartChest", ChestMinecart.class),
                            Map.entry("MinecartCommandBlock", CommandBlockMinecart.class),
                            Map.entry("MinecartFurnace", FurnaceMinecart.class),
                            Map.entry("MinecartHopper", HopperMinecart.class),
                            Map.entry("MinecartSpawner", SpawnerMinecart.class),
                            Map.entry("MinecartTNT", TNTMinecart.class),
                            Map.entry("Mob", Agent.class),
                            Map.entry("MushroomCow", Mooshroom.class),
                            Map.entry("NeutralMob", Angerable.class),
                            Map.entry("OwnableEntity", Ownable.class),
                            Map.entry("PathfinderMob", PathfinderAgent.class),
                            Map.entry("PatrollingMonster", Patroller.class),
                            Map.entry("PowerableMob", Chargeable.class),
                            Map.entry("RangedAttackMob", Ranger.class),
                            Map.entry("SpellcasterIllager", Spellcaster.class),
                            Map.entry("TamableAnimal", TameableAnimal.class),
                            Map.entry("ThrownEgg", Egg.class),
                            Map.entry("ThrownEnderpearl", EnderPearl.class),
                            Map.entry("ThrownExperienceBottle", ExperienceBottle.class),
                            Map.entry("ThrownPotion", Potion.class),
                            Map.entry("ThrownTrident", Trident.class),
                            Map.entry("VehicleEntity", Vehicle.class),
                            Map.entry("WaterAnimal", Aquatic.class),
                            Map.entry("WitherBoss", Wither.class)
                        );

                        this.interfacesAndSuperclasses((Class<?>) typeArg)
                            .filter(clazz -> clazz.getPackageName().startsWith("net.minecraft"))
                            .forEach(minecraftClass -> {
                                if (seen.add(minecraftClass)) {
                                    final Set<Class<?>> minecraftClasses = Collections.newSetFromMap(new IdentityHashMap<>());
                                    final Set<Class<?>> implSpongeClasses = Collections.newSetFromMap(new IdentityHashMap<>());

                                    // unrelated to the entity system
                                    final var unrelatedSpongeClasses = Set.of(
                                        Carrier.class,
                                        CarriedInventory.class,
                                        Contextual.class,
                                        Inventory.class,
                                        LocaleSource.class,
                                        Locatable.class,
                                        ProjectileSource.class,
                                        RandomProvider.class,
                                        Spawner.class,
                                        Subject.class,
                                        TeamMember.class,
                                        ViewableInventory.class,
                                        Viewer.class
                                    );

                                    this.interfacesAndSuperclasses(minecraftClass)
                                        .filter(c -> c.getPackageName().startsWith("org.spongepowered.api") || c.getPackageName().startsWith("net.minecraft"))
                                        .filter(c -> !c.getPackageName().startsWith("org.spongepowered.api.data"))
                                        .filter(c -> !unrelatedSpongeClasses.contains(c))
                                        .forEach(c -> {
                                            if (c.getPackageName().startsWith("net.minecraft")) {
                                                minecraftClasses.add(c);
                                            }
                                            if (c.getPackageName().startsWith("org.spongepowered")) {
                                                implSpongeClasses.add(c);
                                            }
                                        });

                                    final var spongeConceptClasses = Set.of(
                                        Aerial.class,
                                        Ageable.class,
                                        Breedable.class,
                                        ItemRepresentable.class,
                                        Projectile.class,
                                        Ranger.class,
                                        Sittable.class
                                    );

                                    if (!unmappedMinecraftClasses.contains(minecraftClass.getName())) {
                                        var apiSpongeClass = minecraftToSpongeMapping.get(minecraftClass.getSimpleName());
                                        if (apiSpongeClass == null) {
                                            apiSpongeClass = implSpongeClasses.stream()
                                                .filter(c -> c.getSimpleName().equalsIgnoreCase(minecraftClass.getSimpleName()))
                                                .findAny()
                                                .orElse(null);
                                        }
                                        if (apiSpongeClass == null) {
                                            this.logger.error("{} cannot find matching Sponge interface", minecraftClass.getName());
                                            return;
                                        }

                                        if (!spongeConceptClasses.contains(apiSpongeClass)) {
                                            final var apiSpongeClasses = this.interfacesAndSuperclasses(apiSpongeClass)
                                                .filter(c -> c.getPackageName().startsWith("org.spongepowered.api"))
                                                .filter(c -> !c.getPackageName().startsWith("org.spongepowered.api.data"))
                                                .collect(Collectors.toSet());

                                            if (implSpongeClasses.isEmpty()) {
                                                this.logger.error("{} does not implement expected Sponge interface: {}", minecraftClass.getName(), apiSpongeClass.getName());
                                                return;
                                            }

                                            if (!apiSpongeClasses.containsAll(implSpongeClasses)) {
                                                implSpongeClasses.removeAll(apiSpongeClasses);
                                                this.logger.error("{}: extra sponge classes compared to the API: {}", minecraftClass.getName(), implSpongeClasses);
                                                return;
                                            }
                                        }
                                    }

                                    minecraftClasses.removeIf(c -> unmappedMinecraftClasses.contains(c.getName()));
                                    implSpongeClasses.removeAll(unmappedSpongeClasses);

                                    minecraftClasses.removeIf(mClass -> {
                                        final var targetSpongeClass = minecraftToSpongeMapping.get(mClass.getSimpleName());
                                        final var targetSpongeName = mClass.getSimpleName();
                                        return targetSpongeClass != null
                                            ? implSpongeClasses.remove(targetSpongeClass)
                                            : implSpongeClasses.removeIf(sClass -> sClass.getSimpleName().equalsIgnoreCase(targetSpongeName));
                                    });

                                    implSpongeClasses.removeAll(spongeConceptClasses);

                                    if (minecraftClasses.isEmpty() && implSpongeClasses.size() == 1 && implSpongeClasses.contains(Entity.class)) {
                                        return;
                                    }

                                    if (this.verbose) {
                                        if (!minecraftClasses.isEmpty()) {
                                            this.logger.info("{}: extra unmapped minecraft classes: {}", minecraftClass.getName(), minecraftClasses);
                                        }
                                        if (!implSpongeClasses.isEmpty()) {
                                            this.logger.info("{}: extra unmapped sponge classes: {}", minecraftClass.getName(), implSpongeClasses);
                                        }
                                    }
                                }
                            });
                    }
                    context.sendMessage(Component.text("Done!"));
                    return CommandResult.success();
                })
                .build(),
            "checkentities");
    }

    private Stream<Class<?>> interfacesAndSuperclasses(final Class<?> clazz) {
        return Stream.concat(
            Stream.of(clazz),
            Stream.concat(
                Stream.of(clazz.getInterfaces()).flatMap(interfaceType -> Stream.concat(Stream.of(interfaceType), this.interfacesAndSuperclasses(interfaceType))),
                Stream.ofNullable(clazz.getSuperclass()).flatMap(superclass -> Stream.concat(Stream.of(superclass), this.interfacesAndSuperclasses(superclass)))
            )
        );
    }
}
