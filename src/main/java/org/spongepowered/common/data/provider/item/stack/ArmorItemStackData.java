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
import java.util.stream.Collectors;

public final class ArmorItemStackData {

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
                                .map(e -> (EntityType<?>) e)
                                .collect(Collectors.toSet());
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            final HolderSet<net.minecraft.world.entity.EntityType<?>> holderSet = HolderSet.direct(
                                e -> BuiltInRegistries.ENTITY_TYPE.wrapAsHolder((net.minecraft.world.entity.EntityType<?>) e),
                                v
                            );
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setAllowedEntities(holderSet).build());
                            return true;
                        })
                        .deleteAnd(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
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
                            return true;
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
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            final ResourceKey key = RegistryTypes.ARMOR_MATERIAL.get().valueKey(v);
                            final net.minecraft.resources.ResourceKey<EquipmentAsset> assetKey =
                                net.minecraft.resources.ResourceKey.create(EquipmentAssets.ROOT_ID, (Identifier) (Object) key);
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setAsset(assetKey).build());
                            return true;
                        })
                        .deleteAnd(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
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
                            return true;
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
                    .create(Keys.CAMERA_OVERLAY)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null || equippable.cameraOverlay().isEmpty()) {
                                return null;
                            }
                            return (ResourceKey) (Object) equippable.cameraOverlay().get();
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setCameraOverlay((Identifier) (Object) v).build());
                            return true;
                        })
                        .deleteAnd(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
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
                            return true;
                        })
                    .create(Keys.CAN_BE_SHEARED)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.canBeSheared();
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setCanBeSheared(v).build());
                            return true;
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
                    .create(Keys.DAMAGE_ON_HURT)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.damageOnHurt();
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setDamageOnHurt(v).build());
                            return true;
                        })
                    .create(Keys.EQUIP_ON_INTERACT)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.equipOnInteract();
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setEquipOnInteract(v).build());
                            return true;
                        })
                    .create(Keys.EQUIP_SOUND)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return (SoundType) (Object) equippable.equipSound().value();
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setEquipSound(Holder.direct((SoundEvent) (Object) v)).build());
                            return true;
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
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            final EquipmentSlot slot = (EquipmentSlot) (Object) v;
                            if (equippable == null) {
                                h.set(DataComponents.EQUIPPABLE, Equippable.builder(slot).build());
                                return;
                            }
                            h.set(DataComponents.EQUIPPABLE, new Equippable(
                                slot,
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
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setDispensable(v).build());
                            return true;
                        })
                    .create(Keys.IS_SWAPPABLE)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return equippable.swappable();
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setSwappable(v).build());
                            return true;
                        })
                    .create(Keys.SHEARING_SOUND)
                        .get(h -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return null;
                            }
                            return (SoundType) (Object) equippable.shearingSound().value();
                        })
                        .setAnd((h, v) -> {
                            final @Nullable Equippable equippable = h.get(DataComponents.EQUIPPABLE);
                            if (equippable == null) {
                                return false;
                            }
                            h.set(DataComponents.EQUIPPABLE, ArmorItemStackData.asBuilder(equippable).setShearingSound(Holder.direct((SoundEvent) (Object) v)).build());
                            return true;
                        });
    }
    // @formatter:on

    private static Equippable.Builder asBuilder(final Equippable equippable) {
        final Equippable.Builder builder = Equippable.builder(equippable.slot())
            .setEquipSound(equippable.equipSound())
            .setDispensable(equippable.dispensable())
            .setSwappable(equippable.swappable())
            .setDamageOnHurt(equippable.damageOnHurt())
            .setEquipOnInteract(equippable.equipOnInteract())
            .setCanBeSheared(equippable.canBeSheared())
            .setShearingSound(equippable.shearingSound());
        equippable.assetId().ifPresent(builder::setAsset);
        equippable.cameraOverlay().ifPresent(builder::setCameraOverlay);
        equippable.allowedEntities().ifPresent(builder::setAllowedEntities);
        return builder;
    }

}
