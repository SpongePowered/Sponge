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
package org.spongepowered.common.mixin.inventory.event.server.level.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.bridge.world.entity.EntityBridge;
import org.spongepowered.common.bridge.world.entity.player.PlayerInventoryBridge;
import org.spongepowered.common.bridge.world.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.context.transaction.EffectTransactor;
import org.spongepowered.common.event.tracking.context.transaction.TransactionalCaptureSupplier;
import org.spongepowered.common.event.tracking.phase.packet.PacketPhase;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.inventory.fabric.Fabric;
import org.spongepowered.common.inventory.lens.Lens;
import org.spongepowered.common.inventory.lens.impl.minecraft.PlayerInventoryLens;
import org.spongepowered.common.inventory.lens.slots.SlotLens;
import org.spongepowered.common.mixin.inventory.event.entity.player.PlayerMixin_Inventory;

import java.util.Map;
import java.util.OptionalInt;

@Mixin(value = ServerPlayer.class)
public abstract class ServerPlayerMixin_Inventory extends PlayerMixin_Inventory {

    @Nullable private EffectTransactor inventory$effectTransactor = null;

    // Ignore
    ServerPlayerMixin_Inventory(final EntityType<?> param0, final Level param1) {
        super(param0, param1);
    }

    // -- Overrides from PlayerMixin_Inventory

    @Override
    protected void impl$beforeSetItemSlot(final EquipmentSlot param0, final ItemStack param1, final CallbackInfo ci) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        this.inventory$effectTransactor = transactor.logPlayerInventoryChangeWithEffect((Player) (Object) this, SpongeEventFactory::createChangeInventoryEvent);
    }

    @Override
    protected void impl$afterSetItemSlot(final EquipmentSlot param0, final ItemStack param1, final CallbackInfo ci) {
        try (final EffectTransactor ignored = this.inventory$effectTransactor) {
            this.inventoryMenu.broadcastChanges(); // for capture
        } finally {
            this.inventory$effectTransactor = null;
        }
    }

    @Override
    protected void impl$onBroadcastCreativeActionResult(final boolean param0, final CallbackInfoReturnable<Boolean> cir) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        this.inventory$effectTransactor = transactor.logDropFromPlayerInventory((Player) (Object) this, param0);
    }

    @Override
    protected ItemEntity impl$onBroadcastCreativeActionResult(final Player player, final ItemStack param0, final boolean param1, final boolean param2, final boolean dropAll) {
        try (final EffectTransactor ignored = this.inventory$effectTransactor) {
            return player.drop(param0, param1, param2);
        } finally {
            this.inventory$effectTransactor = null;
        }
    }

    @Override
    protected void inventory$switchToCloseWindowState(final AbstractContainerMenu container, final Player player) {
        // Corner case where the server is shutting down on the client, the server player is also being killed off.
        if (Sponge.isServerAvailable() && Sponge.isClientAvailable()) {
            container.removed(player);
            return;
        }
        final ServerPlayer serverPlayer = (ServerPlayer) player;

        try (final PhaseContext<@NonNull ?> ctx = PacketPhase.General.CLOSE_WINDOW.createPhaseContext(PhaseTracker.SERVER)
            .source(serverPlayer)
            .packetPlayer(serverPlayer)
        ) {
            ctx.buildAndSwitch();
            try (final EffectTransactor ignored = ctx.getTransactor().logCloseInventory(player, true)) {
                container.removed(player); // Drop & capture cursor item
                this.inventoryMenu.broadcastChanges(); // capture
            }
        }
    }

    @Override
    protected void inventory$onTouch(final Entity entity, final Player player) {
        if (entity instanceof ItemEntity) {
            entity.playerTouch(player); // ItemEntityMixin_Inventory creates transactions for pickup event
            return;
        }
        if (!((EntityBridge) entity).bridge$isPlayerTouchDeclared()) {
            entity.playerTouch(player);
            return;
        }
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        try (final EffectTransactor ignored = context.getTransactor().logPlayerInventoryChangeWithEffect(player, SpongeEventFactory::createChangeInventoryEvent)) {
            entity.playerTouch(player);
            this.inventoryMenu.broadcastChanges(); // capture
        }

    }

    @Override
    protected void inventory$onHandleHandSwap(final Map<EquipmentSlot, ItemStack> map, final CallbackInfo ci) {
        // For players ChangeInventoryEvent.SwapHand is called somewhere else
    }

    @Override
    protected void inventory$onElytraUse(final CallbackInfo ci) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        final net.minecraft.server.level.ServerPlayer player = (net.minecraft.server.level.ServerPlayer) (Object) this;
        try (final EffectTransactor ignored = transactor.logPlayerInventoryChangeWithEffect(player, SpongeEventFactory::createChangeInventoryEvent)) {
            player.inventoryMenu.broadcastChanges(); // capture
        }
    }

    @Override
    protected void inventory$onUpdateUsingItem(final LivingEntity thisPlayer) {
        final PhaseContext<@NonNull ?> context = PhaseTracker.SERVER.getPhaseContext();
        final TransactionalCaptureSupplier transactor = context.getTransactor();
        try (final EffectTransactor ignored = transactor.logPlayerInventoryChangeWithEffect((ServerPlayer) (Object) this, SpongeEventFactory::createChangeInventoryEvent)) {
            this.shadow$completeUsingItem();
            this.inventoryMenu.broadcastChanges();
        }
    }

    // -- Normal redirects

    @Inject(
        method = "openMenu",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/level/ServerPlayer;containerMenu:Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            opcode = Opcodes.PUTFIELD,
            shift = At.Shift.AFTER
        )
    )
    private void impl$onOpenMenu(final MenuProvider param0, final CallbackInfoReturnable<OptionalInt> cir) {
        PhaseTracker.SERVER.getPhaseContext()
            .getTransactor()
            .logContainerSet((ServerPlayer) (Object) this);
    }

    @Inject(method = "openHorseInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AbstractContainerMenu;addSlotListener(Lnet/minecraft/world/inventory/ContainerListener;)V"))
    private void impl$onOpenHorseInventory(final AbstractHorse horse, final Container inventoryIn, final CallbackInfo ci) {
        ((TrackedContainerBridge) this.containerMenu).bridge$trackViewable(inventoryIn);
    }

    @Redirect(
        method = "openMenu",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"
        )
    )
    private AbstractContainerMenu impl$transactMenuCreationWithEffect(
        final MenuProvider menuProvider, final int var1, final net.minecraft.world.entity.player.Inventory var2,
        final Player var3
    ) {
        try (final EffectTransactor ignored = PhaseTracker.SERVER.getPhaseContext()
            .getTransactor()
            .logOpenInventory((ServerPlayer) (Object) this)
        ) {
            return menuProvider.createMenu(var1, var2, var3);
        }
    }

    // -- small bridge-like methods


    @SuppressWarnings("ConstantConditions")
    @Override
    protected Slot impl$getSpongeSlot(
        final EquipmentSlot equipmentSlot
    ) {
        final EquipmentType equipmentType = (EquipmentType) (Object) equipmentSlot;
        final PlayerInventoryBridge inventory = (PlayerInventoryBridge) ((net.minecraft.server.level.ServerPlayer) (Object) this).inventory;
        final Lens lens = ((InventoryAdapter) inventory).inventoryAdapter$getRootLens();
        final Fabric fabric = ((InventoryAdapter) inventory).inventoryAdapter$getFabric();
        if (lens instanceof PlayerInventoryLens) {
            final SlotLens slotLens = ((PlayerInventoryLens) lens).getEquipmentLens().getSlotLens(equipmentType);
            return slotLens.getAdapter(fabric, (Inventory) inventory);
        }
        throw new IllegalStateException("Unknown Lens for Player Inventory: " + lens.getClass().getName());
    }
}
