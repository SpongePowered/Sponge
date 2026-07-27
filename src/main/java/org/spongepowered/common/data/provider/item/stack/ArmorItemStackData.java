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
package org.spongepowered.common.data.provider.item.stack;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.recipe.smithing.ArmorTrim;
import org.spongepowered.api.registry.RegistryEntry;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ArmorItemStackData {

    private static final Equippable EQUIPPABLE_DEFAULTS = Equippable.builder(EquipmentSlot.CHEST).build();

    private ArmorItemStackData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asMutable(ItemStack.class)
                    .create(Keys.ALLOWED_ENTITIES)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null || equippable.allowedEntities().isEmpty()) {
                                return null;
                            }
                            return equippable.allowedEntities().get().stream()
                                .map(Holder::value)
                                .map(e -> (EntityType<?>) (Object) e)
                                .collect(Collectors.toSet());
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            final HolderSet<net.minecraft.world.entity.EntityType<?>> holderSet = HolderSet.direct(
                                e -> BuiltInRegistries.ENTITY_TYPE.wrapAsHolder((net.minecraft.world.entity.EntityType<?>) (Object) e),
                                v.stream().map(e -> (net.minecraft.world.entity.EntityType<?>) (Object) e).toList()
                            );
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                Optional.of(holderSet),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                        .delete(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable != null) {
                                h.set(DataComponents.EQUIPPABLE, new Equippable(
                                    equippable.slot(),
                                    equippable.equipSound(),
                                    equippable.assetId(),
                                    equippable.cameraOverlay(),
                                    Optional.empty(),
                                    equippable.dispensable(),
                                    equippable.swappable(),
                                    equippable.damageOnHurt(),
                                    equippable.equipOnInteract(),
                                    equippable.canBeSheared(),
                                    equippable.shearingSound()
                                ));
                            }
                        })
                    .create(Keys.ARMOR_MATERIAL)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.assetId()
                                .map(rl -> (ResourceKey) (Object) rl.identifier())
                                .flatMap(rk -> RegistryTypes.ARMOR_MATERIAL.get().findEntry(rk))
                                .map(RegistryEntry::value)
                                .orElse(null);
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            final ResourceKey key = RegistryTypes.ARMOR_MATERIAL.get().valueKey(v);
                            final Optional<net.minecraft.resources.ResourceKey<EquipmentAsset>> assetId =
                                Optional.of(net.minecraft.resources.ResourceKey.create(EquipmentAssets.ROOT_ID, (Identifier) (Object) key));
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                assetId,
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                        .delete(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable != null) {
                                h.set(DataComponents.EQUIPPABLE, new Equippable(
                                    equippable.slot(),
                                    equippable.equipSound(),
                                    Optional.empty(),
                                    equippable.cameraOverlay(),
                                    equippable.allowedEntities(),
                                    equippable.dispensable(),
                                    equippable.swappable(),
                                    equippable.damageOnHurt(),
                                    equippable.equipOnInteract(),
                                    equippable.canBeSheared(),
                                    equippable.shearingSound()
                                ));
                            }
                        })
                    .create(Keys.ARMOR_TRIM)
                        .get(h -> {
                            final net.minecraft.world.item.equipment.trim.@Nullable ArmorTrim trim = h.get(DataComponents.TRIM);
                            if (trim != null) {
                                return (ArmorTrim) (Object) trim;
                            }
                            return null;
                        })
                        .set((h, v) -> {
                            if (v == null) {
                                h.remove(DataComponents.TRIM);
                                return;
                            }
                            h.set(DataComponents.TRIM, (net.minecraft.world.item.equipment.trim.ArmorTrim) (Object) v);
                        })
                        .delete(h -> h.remove(DataComponents.TRIM))
                        .supports(isArmorItem())
                    .create(Keys.CAMERA_OVERLAY)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null || equippable.cameraOverlay().isEmpty()) {
                                return null;
                            }
                            return (ResourceKey) (Object) equippable.cameraOverlay().get();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                Optional.of((Identifier) (Object) v),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                        .delete(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable != null) {
                                h.set(DataComponents.EQUIPPABLE, new Equippable(
                                    equippable.slot(),
                                    equippable.equipSound(),
                                    equippable.assetId(),
                                    Optional.empty(),
                                    equippable.allowedEntities(),
                                    equippable.dispensable(),
                                    equippable.swappable(),
                                    equippable.damageOnHurt(),
                                    equippable.equipOnInteract(),
                                    equippable.canBeSheared(),
                                    equippable.shearingSound()
                                ));
                            }
                        })
                    .create(Keys.CAN_BE_SHEARED)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.canBeSheared();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                v,
                                equippable.shearingSound()
                            ));
                        })
                    .create(Keys.DAMAGE_ABSORPTION)
                        .get(h -> {
                            final @Nullable ItemAttributeModifiers modifiersContainer = h.get(DataComponents.ATTRIBUTE_MODIFIERS);
                            if (modifiersContainer == null) {
                                return null;
                            }
                            return modifiersContainer.modifiers().stream()
                                .filter(e1 -> e1.attribute() == Attributes.ARMOR)
                                .findFirst()
                                .map(e -> e.modifier().amount())
                                .orElse(null);
                        })
                        .supports(isArmorItem())
                    .create(Keys.DAMAGE_ON_HURT)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.damageOnHurt();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                v,
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                    .create(Keys.EQUIP_ON_INTERACT)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.equipOnInteract();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                v,
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                    .create(Keys.EQUIP_SOUND)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return (SoundType) (Object) equippable.equipSound().value();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                Holder.direct((SoundEvent) (Object) v),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                    .create(Keys.EQUIPMENT_TYPE)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return (EquipmentType) (Object) equippable.slot();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                (EquipmentSlot) (Object) v,
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                        .delete(h -> h.remove(DataComponents.EQUIPPABLE))
                    .create(Keys.IS_DISPENSABLE)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.dispensable();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                v,
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                    .create(Keys.IS_SWAPPABLE)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.swappable();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                v,
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                equippable.shearingSound()
                            ));
                        })
                    .create(Keys.SHEARING_SOUND)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return (SoundType) (Object) equippable.shearingSound().value();
                        })
                        .set((h, v) -> {
                            final @Nullable Equippable equippable = h.getOrDefault(DataComponents.EQUIPPABLE, EQUIPPABLE_DEFAULTS);
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                equippable.slot(),
                                equippable.equipSound(),
                                equippable.assetId(),
                                equippable.cameraOverlay(),
                                equippable.allowedEntities(),
                                equippable.dispensable(),
                                equippable.swappable(),
                                equippable.damageOnHurt(),
                                equippable.equipOnInteract(),
                                equippable.canBeSheared(),
                                Holder.direct((SoundEvent) (Object) v)
                            ));
                        });
    }
    // @formatter:on

    private static @NonNull Function<ItemStack, Boolean> isArmorItem() {
        return h -> {
            final var components = h.getItem().components();
            final @Nullable Integer stackSize = components.get(DataComponents.MAX_STACK_SIZE);
            if (stackSize == null) {
                return false;
            }
            return components.has(DataComponents.EQUIPPABLE)
                   && components.has(DataComponents.ENCHANTABLE)
                   && components.has(DataComponents.MAX_DAMAGE)
                   && components.has(DataComponents.MAX_STACK_SIZE)
                   && (1 == stackSize);
        };
    }
}
