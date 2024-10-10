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
package org.spongepowered.common.inventory.util;

import net.minecraft.world.CompoundContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.carrier.chest.Chest;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.crafting.CraftingGridInventory;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.item.recipe.crafting.RecipeInput;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.accessor.world.inventory.CraftingMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.SmithingMenuAccessor;
import org.spongepowered.common.accessor.world.inventory.StonecutterMenuAccessor;
import org.spongepowered.common.bridge.world.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.entity.player.SpongeUserData;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.hooks.PlatformHooks;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.adapter.impl.BasicInventoryAdapter;
import org.spongepowered.common.inventory.custom.CarriedWrapperInventory;
import org.spongepowered.common.inventory.custom.CustomInventory;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.launch.Launch;
import org.spongepowered.plugin.PluginContainer;

import java.util.Optional;
import java.util.Set;

public final class InventoryUtil {

    private InventoryUtil() {}

    public static CraftingGridInventory toSpongeInventory(final CraftingContainer inv) {
        return (CraftingGridInventory) inv;
    }

    public static RecipeInput.Crafting toSponge(final CraftingInput input) {
        return (RecipeInput.Crafting) input;
    }

    public static RecipeInput.Smithing toSponge(final SmithingRecipeInput input) {
        return (RecipeInput.Smithing) (Object) input;
    }

    public static RecipeInput.Single toSponge(final SingleRecipeInput input) {
        return (RecipeInput.Single) (Object) input;
    }


    @SuppressWarnings("unchecked")
    public static <I extends CraftingInput> Optional<I> toCraftingInput(final Inventory inv) {
        final var recipeInput = switch (inv) {
            case AbstractFurnaceBlockEntity furnace -> new SingleRecipeInput(furnace.getItem(0));
            case CampfireBlockEntity campfire -> campfire.getItems().stream().filter(stack -> !stack.isEmpty()).findFirst()
                    .map(SingleRecipeInput::new).orElse(null);
            case CraftingMenuAccessor menu -> menu.accessor$craftSlots().asCraftInput();
            case InventoryMenu menu -> menu.getCraftSlots().asCraftInput();
            case StonecutterMenu menu -> StonecutterMenuAccessor.invoker$createRecipeInput(menu.container);
            case SmithingMenuAccessor menu -> menu.invoker$createRecipeInput();
            default -> null;
        };
        return Optional.ofNullable((I) recipeInput);
    }

    @SuppressWarnings("unchecked")
    public static <I extends CraftingInput> I toCraftingInputOrThrow(final Inventory inv) {
       return (I) toCraftingInput(inv).orElseThrow(() -> new IllegalStateException("Invalid CraftingGridInventory. Could not find CraftingInventory.\nInventory was: " + inv.getClass().getSimpleName()));
    }

    public static Optional<Inventory> getDoubleChestInventory(final ChestBlockEntity chest) {
        final Optional<Chest> connectedChestOptional = ((Chest) chest).connectedChest();
        if (!connectedChestOptional.isPresent()) {
            return Optional.empty();
        }

        final ChestType chestType = chest.getBlockState().getValue(ChestBlock.TYPE);
        final ChestBlockEntity connectedChest = (ChestBlockEntity) connectedChestOptional.get();
        // Logic in the instanceof check of ChestBlock.getChestInventory but with exploded ternary operators.
        if (chestType == ChestType.RIGHT) {
            return Optional.of((Inventory) new CompoundContainer(chest, connectedChest));
        } else {
            return Optional.of((Inventory) new CompoundContainer(connectedChest, chest));
        }
    }

    // Utility
    public static Inventory toInventory(final net.minecraft.world.Container inventory) {
        return InventoryUtil.toInventory(inventory, null);
    }

    public static Inventory toInventory(Object inventory, final @Nullable Object forgeItemHandler) {
        if (forgeItemHandler == null) {
            if (inventory instanceof ChestBlockEntity) {
                inventory = InventoryUtil.getDoubleChestInventory(((ChestBlockEntity) inventory)).orElse(((Inventory) inventory));
            }
            if (inventory instanceof Inventory) {
                return ((Inventory) inventory);
            }
        }
        if (forgeItemHandler instanceof Inventory) {
            return ((Inventory) forgeItemHandler);
        }
        return PlatformHooks.INSTANCE.getInventoryHooks().toInventory(inventory, forgeItemHandler);
    }

    public static InventoryAdapter findAdapter(final Object inventory) {
        if (inventory instanceof InventoryAdapter) {
            return ((InventoryAdapter) inventory);
        }
        if (inventory instanceof Fabric) {
            return new BasicInventoryAdapter((Fabric) inventory, null, null);
        }
        return PlatformHooks.INSTANCE.getInventoryHooks().findInventoryAdapter(inventory);
    }

    public static TrackedInventoryBridge forCapture(final Object toCapture) {
        if (toCapture instanceof TrackedInventoryBridge) {
            return ((TrackedInventoryBridge) toCapture);
        }
        return null;
    }

    public static PluginContainer getPluginContainer(Object inventory) {
        // TODO maybe caching?
        final PluginContainer container;

        if (inventory instanceof CustomInventory) {
            return ((CustomInventory)inventory).getPlugin();
        }

        if (inventory instanceof CarriedInventory) {
            final Optional<?> carrier = ((CarriedInventory<?>) inventory).carrier();
            if (carrier.isPresent()) {
                inventory = carrier.get();
            }
        }

        final Object base = inventory;

        if (base instanceof BlockEntity) {
            final ResourceKey key = Sponge.game().registry(RegistryTypes.BLOCK_ENTITY_TYPE).valueKey(((BlockEntity) base).type());
            final String pluginId = key.namespace();
            container = Sponge.pluginManager().plugin(pluginId)
                    .orElseThrow(() -> new AssertionError("Missing plugin " + pluginId + " for block " + key.namespace() + ":" + key.value()));
        } else if (base instanceof Entity) {
            final ResourceKey key = (ResourceKey) (Object) EntityType.getKey((EntityType<?>) ((Entity) base).type());
            final String pluginId = key.namespace();
            container = Sponge.pluginManager().plugin(pluginId).orElseGet(() -> {
                SpongeCommon.logger().debug("Unknown plugin for [{}]", base);
                return Launch.instance().minecraftPlugin();
            });
        } else if (base instanceof SpongeUserData) {
            container = Launch.instance().minecraftPlugin();
        } else {
            container = Sponge.pluginManager().plugin(PlatformHooks.INSTANCE
                .getInventoryHooks()
                .getModIdFromInventory(base.getClass()))
                .orElseGet(() -> {
                    SpongeCommon.logger().debug("Unknown plugin for [{}]", base);
                    return Launch.instance().minecraftPlugin();
                });
        }
        return container;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Carrier> CarriedInventory<T> carriedWrapperInventory(
        final net.minecraft.world.Container inventory, final T carrier) {
        return (CarriedInventory<T>) new CarriedWrapperInventory(inventory, carrier);
    }

    public static void postContainerEvents(final Set<AbstractContainerMenu> containers, final TransactionalCaptureSupplier transactor) {
        for (final AbstractContainerMenu container : containers) {
            try (final EffectTransactor ignore = transactor.logInventoryTransaction(container)) {
                container.broadcastChanges();
            }
        }
    }
}
