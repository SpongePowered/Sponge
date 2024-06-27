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
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.registry.DefaultedRegistryReference;
import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.annotation.CatalogedBy;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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

                        if (registryField instanceof DefaultedRegistryType<?> registryType) {
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
                                this.logger.error("Class {} in registry {} is not annotated with CatalogedBy", catalogEntryClass.getSimpleName(), registryType.location());
                                continue;
                            }

                            final var catalogClass = catalogedByAnnotation.value()[0];
                            if (!Modifier.isFinal(catalogClass.getModifiers())) {
                                this.logger.error("{} is not final", catalogClass.getSimpleName());
                            }

                            if (Arrays.stream(catalogClass.getDeclaredConstructors()).anyMatch(ctor -> !Modifier.isPrivate(ctor.getModifiers()))) {
                                this.logger.error("{} has non-private constructors", catalogClass.getSimpleName());
                            }

                            final Method registryMethod;
                            try {
                                registryMethod = catalogClass.getDeclaredMethod("registry");
                            } catch (final NoSuchMethodException e) {
                                this.logger.error("{}.registry() does not exist", catalogClass.getSimpleName());
                                continue;
                            }

                            final Object registryReturn;
                            try {
                                registryReturn = registryMethod.invoke(null);
                            } catch (final Throwable e) {
                                this.logger.error("{}.registry() failed: {}", catalogClass.getSimpleName(), e.getMessage());
                                if (this.verbose) {
                                    this.logger.error("Exception", e);
                                }
                                continue;
                            }

                            if (registryReturn == null) {
                                this.logger.error("{}.registry() returned null", catalogClass.getSimpleName());
                                continue;
                            }

                            if (registryReturn != registryType.get()) {
                                this.logger.error("{}.registry() returned a different registry than the one specified in RegistryTypes", catalogClass.getSimpleName());
                                continue;
                            }

                            for (Field catalogField : catalogClass.getDeclaredFields()) {
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

                                if (catalogObj instanceof DefaultedRegistryReference<?> reference) {
                                    if (reference.find().isEmpty()) {
                                        this.logger.error("{}.{}.find() is empty", catalogClass.getSimpleName(), catalogField.getName());
                                    }
                                }
                            }

                        } else {
                            this.logger.error("{} is not a DefaultedRegistryType", field.getName());
                        }
                    }
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
                    for (final Field field : entityTypeClass.getDeclaredFields()) {
                        if (!field.getType().getSimpleName().equals("EntityType")) {
                            if (this.verbose) {
                                this.logger.info("Skipping field {} of type {}", field.getName(), field.getType().getSimpleName());
                            }
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

                        final var mc = "minecraft";
                        final var sponge = "sponge";

                        final var unknownClasses = Set.of(
                            "net.minecraft.world.entity.npc.InventoryCarrier",
                            "net.minecraft.world.entity.npc.VillagerDataHolder",
                            "net.minecraft.world.entity.Attackable",
                            "net.minecraft.world.entity.HasCustomInventoryScreen,",
                            "net.minecraft.world.entity.EquipmentUser",
                            "net.minecraft.world.entity.PlayerRideable",
                            "net.minecraft.world.entity.PlayerRideableJumping",
                            "net.minecraft.world.entity.Saddleable",
                            "net.minecraft.world.entity.Targeting",
                            "net.minecraft.world.entity.TraceableEntity",
                            "net.minecraft.world.entity.VariantHolder",
                            "net.minecraft.world.level.entity.EntityAccess",
                            "net.minecraft.world.scores.ScoreHolder",
                            "net.minecraft.world.Clearable",
                            "net.minecraft.world.ContainerListener",
                            "net.minecraft.world.Nameable",
                            "org.spongepowered.api.entity.attribute.AttributeHolder",
                            "org.spongepowered.api.entity.living.Humanoid",
                            "org.spongepowered.api.projectile.source.EntityProjectileSource",
                            "org.spongepowered.api.projectile.source.ProjectileSource",
                            "org.spongepowered.api.scoreboard.TeamMember",
                            "org.spongepowered.api.util.locale.LocaleSource",
                            "org.spongepowered.api.util.Identifiable",
                            "org.spongepowered.api.util.RandomProvider",
                            "org.spongepowered.api.world.Locatable"
                        );

                        final var interfaces = this.interfacesAndSuperclasses((Class<?>) typeArg)
                            .filter(clazz -> clazz.getPackageName().startsWith("org.spongepowered.api") || clazz.getPackageName().startsWith("net.minecraft"))
                            .filter(clazz -> !clazz.getPackageName().startsWith("org.spongepowered.api.data"))
                            .filter(clazz -> !clazz.getPackageName().startsWith("org.spongepowered.api.item.inventory"))
                            .filter(clazz -> !clazz.getPackageName().startsWith("net.minecraft.commands"))
                            .filter(clazz -> !clazz.getPackageName().startsWith("net.minecraft.network"))
                            .filter(clazz -> !unknownClasses.contains(clazz.getName()))
                            .distinct()
                            .collect(Collectors.groupingBy(
                                clazz -> {
                                    if (clazz.getPackageName().startsWith("net.minecraft")) {
                                        return mc;
                                    }
                                    if (clazz.getPackageName().startsWith("org.spongepowered")) {
                                        return sponge;
                                    }
                                    return "";
                                }
                            ));

                        final var mcInterfaces = interfaces.getOrDefault(mc, Collections.emptyList());
                        mcInterfaces.sort(Comparator.comparing(Class::getSimpleName));

                        final var spongeInterfaces = interfaces.getOrDefault(sponge, Collections.emptyList());
                        spongeInterfaces.sort(Comparator.comparing(Class::getSimpleName));

                        final var mcToSpongeMapping = Map.ofEntries(
                            Map.entry("AbstractArrow", "ArrowEntity"),
                            Map.entry("AbstractChestedHorse", "PackHorse"),
                            Map.entry("AbstractFish", "Fish"),
                            Map.entry("AbstractGolem", "Golem"),
                            Map.entry("AbstractHorse", "HorseLike"),
                            Map.entry("AbstractHurtingProjectile", "DamagingProjectile"),
                            Map.entry("AbstractIllager", "Illager"),
                            Map.entry("AbstractMinecart", "MinecartLike"),
                            Map.entry("AbstractMinecartContainer", "CarrierMinecart"),
                            Map.entry("AbstractSchoolingFish", "SchoolingFish"),
                            Map.entry("AbstractSkeleton", "SkeletonLike"),
                            Map.entry("AbstractVillager", "Trader"),
                            Map.entry("AbstractWindCharge", "WindChargeLike"),
                            Map.entry("AgeableMob", "Ageable"),
                            Map.entry("BlockAttachedEntity", "Hanging"),
                            Map.entry("Display", "DisplayEntity"),
                            Map.entry("FallingBlockEntity", "FallingBlock"),
                            Map.entry("Fireball", "FireballEntity"),
                            Map.entry("FireworkRocketEntity", "FireworkRocket"),
                            Map.entry("FishingHook", "FishingBobber"),
                            Map.entry("FlyingMob", "Aerial"),
                            Map.entry("ItemEntity", "Item"),
                            Map.entry("LargeFireball", "ExplosiveFireball"),
                            Map.entry("LeashFenceKnotEntity", "LeashKnot"),
                            Map.entry("LivingEntity", "Living"),
                            Map.entry("MinecartChest", "ChestMinecart"),
                            Map.entry("MinecartCommandBlock", "CommandBlockMinecart"),
                            Map.entry("MinecartFurnace", "FurnaceMinecart"),
                            Map.entry("MinecartHopper", "HopperMinecart"),
                            Map.entry("MinecartSpawner", "SpawnerMinecart"),
                            Map.entry("MinecartTNT", "TNTMinecart"),
                            Map.entry("Mob", "Agent"),
                            Map.entry("MushroomCow", "Mooshroom"),
                            Map.entry("PathfinderMob", "Creature"),
                            Map.entry("PatrollingMonster", "Patroller"),
                            Map.entry("SpellcasterIllager", "Spellcaster"),
                            Map.entry("TamableAnimal", "TameableAnimal"),
                            Map.entry("ThrownEgg", "Egg"),
                            Map.entry("ThrownEnderpearl", "EnderPearl"),
                            Map.entry("ThrownExperienceBottle", "ExperienceBottle"),
                            Map.entry("ThrownPotion", "Potion"),
                            Map.entry("ThrownTrident", "Trident"),
                            Map.entry("WaterAnimal", "Aquatic"),
                            Map.entry("WitherBoss", "Wither")
                        );

                        final AtomicBoolean brokenMapping = new AtomicBoolean(false);
                        mcToSpongeMapping.forEach((key, value) -> {
                            if (!mcToSpongeMapping.containsKey(value) && mcInterfaces.stream().anyMatch(c -> c.getSimpleName().equalsIgnoreCase(value))) {
                                this.logger.error("Duplicat mapping: {}->{} ; {}->{}", key, value, value, value);
                                brokenMapping.set(true);
                            }
                        });
                        if (brokenMapping.get()) {
                            continue;
                        }

                        final var baseClass = mcToSpongeMapping.getOrDefault(((Class<?>) typeArg).getSimpleName(), ((Class<?>) typeArg).getSimpleName());
                        if (!spongeInterfaces.removeIf(spongeInterface -> spongeInterface.getSimpleName().equalsIgnoreCase(baseClass))) {
                            this.logger.error("{} does not implement matching Sponge interface", ((Class<?>) typeArg).getName());
                            continue;
                        }

                        mcInterfaces.removeIf(mcInterface ->
                            spongeInterfaces.removeIf(spongeClass ->
                                spongeClass.getSimpleName().equals(mcToSpongeMapping.getOrDefault(mcInterface.getSimpleName(), mcInterface.getSimpleName()))
                            )
                        );

                        if (this.verbose) {
                            if (!mcInterfaces.isEmpty()) {
                                this.logger.info("extra unmapped mc interfaces for {}: {}", ((Class<?>) typeArg).getSimpleName(), mcInterfaces);
                            }
                            if (!spongeInterfaces.isEmpty()) {
                                this.logger.info("extra unmapped sponge interfaces for {}: {}", ((Class<?>) typeArg).getSimpleName(), spongeInterfaces);
                            }
                        }
                    }
                    return CommandResult.success();
                })
                .build(),
            "checkentities");
    }

    private Stream<Class<?>> interfacesAndSuperclasses(final Class<?> clazz) {
        return Stream.concat(
            Stream.of(clazz.getInterfaces()).flatMap(interfaceType -> Stream.concat(Stream.of(interfaceType), this.interfacesAndSuperclasses(interfaceType))),
            Stream.ofNullable(clazz.getSuperclass()).flatMap(superclass -> Stream.concat(Stream.of(superclass), this.interfacesAndSuperclasses(superclass)))
        );
    }
}
