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
package org.spongepowered.common.mixin.invalid.core.entity.player;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SEntityPropertiesPacket;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.network.play.server.SSpawnPositionPacket;
import net.minecraft.network.play.server.SUpdateHealthPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.entity.living.ChangeGameModeEvent;
import org.spongepowered.api.event.entity.living.player.PlayerChangeClientSettingsEvent;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.query.QueryTypes;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.user.UserManager;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.entity.living.player.chat.ChatType;
import org.spongepowered.api.entity.living.player.chat.ChatVisibility;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.entity.player.ServerPlayerEntityBridge;
import org.spongepowered.common.bridge.inventory.ViewableInventoryBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedContainerBridge;
import org.spongepowered.common.bridge.inventory.container.TrackedInventoryBridge;
import org.spongepowered.common.bridge.network.ServerPlayNetHandlerBridge;
import org.spongepowered.common.bridge.permissions.SubjectBridge;
import org.spongepowered.common.bridge.scoreboard.ScorePlayerTeamBridge;
import org.spongepowered.common.bridge.scoreboard.ServerScoreboardBridge;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.entity.living.human.HumanEntity;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.inventory.adapter.InventoryAdapter;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.common.accessor.network.play.client.CClientSettingsPacketAccessor;
import org.spongepowered.common.accessor.network.play.server.SChangeBlockPacketAccessor;
import org.spongepowered.common.mixin.core.entity.player.PlayerEntityMixin;
import org.spongepowered.common.user.SpongeUserManager;
import org.spongepowered.common.text.SpongeTexts;
import org.spongepowered.common.text.chat.ChatUtil;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.LocaleCache;
import org.spongepowered.common.util.NetworkUtil;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.border.PlayerOwnBorderListener;
import org.spongepowered.math.vector.Vector3d;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements SubjectBridge, ServerPlayerEntityBridge {

    @Shadow @Final public PlayerInteractionManager interactionManager;
    @Shadow public ServerPlayNetHandler connection;

    @Nullable private GameType impl$pendingGameType;



    /**
     * @author simon816 - 14th November, 2016
     *
     * @reason Redirect messages sent to the player to fire a message event. Once the
     * event is handled, it will send the message to
     * {@link Player#sendMessage(ChatType, Text)}.
     *
     * @param component The message
     */
    @Overwrite
    public void sendMessage(final ITextComponent component) {
        ChatUtil.sendMessage(component, MessageChannel.to((Player) this), (MessageReceiver) this.server, false);
    }

    /**
     * @author Minecrell - August 22nd, 2016
     * @reason Use InetSocketAddress#getHostString() where possible (instead of
     *     inspecting SocketAddress#toString()) to support IPv6 addresses
     */
    @Overwrite
    public String getPlayerIP() {
        return NetworkUtil.getHostString(this.connection.netManager.getRemoteAddress());
    }

    @Override
    public MessageChannel bridge$getDeathMessageChannel() {
        final ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
            @Nullable final Team team = player.getTeam();

            if (team != null && team.getDeathMessageVisibility() != Team.Visible.ALWAYS) {
                if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OTHER_TEAMS) {
                    return ((ScorePlayerTeamBridge) team).bridge$getTeamChannel(player);
                } else if (team.getDeathMessageVisibility() == Team.Visible.HIDE_FOR_OWN_TEAM) {
                    return ((ScorePlayerTeamBridge) team).bridge$getNonTeamChannel();
                }
            } else {
                return ((Player) this).getMessageChannel();
            }
        }

        return MessageChannel.toNone();
    }

    @Inject(method = "markPlayerActive()V", at = @At("HEAD"))
    private void impl$onPlayerActive(final CallbackInfo ci) {
        ((ServerPlayNetHandlerBridge) this.connection).bridge$resendLatestResourcePackRequest();
    }

    @Override
    public void bridge$setImplVelocity(final Vector3d velocity) {
        super.bridge$setImplVelocity(velocity);
        this.impl$velocityOverride = null;
    }

    @SuppressWarnings("ConstantConditions")
    @Inject(method = "setGameType(Lnet/minecraft/world/GameType;)V", at = @At("HEAD"), cancellable = true)
    private void impl$$onSetGameTypeThrowEvent(final GameType gameType, final CallbackInfo ci) {
        if (ShouldFire.CHANGE_GAME_MODE_EVENT) {
            try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                frame.pushCause(this);
                final ChangeGameModeEvent event =
                    SpongeEventFactory.createChangeGameModeEvent(frame.getCurrentCause(),
                        (GameMode) (Object) this.interactionManager.getGameType(), (GameMode) (Object) gameType, (Player) this);
                SpongeCommon.postEvent(event);
                if (event.isCancelled()) {
                    ci.cancel();
                }
                this.impl$pendingGameType = (GameType) (Object) event.getGameMode();
            }
        } else {
            this.impl$pendingGameType = gameType;
        }
    }

    /**
     * This injector must appear <b>after</b> {@link #impl$$onSetGameTypeThrowEvent} since it
     * assigns the {@link #impl$pendingGameType} returned by the event to the actual
     * local variable in the method.
     */
    @ModifyVariable(method = "setGameType(Lnet/minecraft/world/GameType;)V", at = @At(value = "HEAD", remap = false), argsOnly = true)
    private GameType impl$assignPendingGameType(final GameType gameType) {
        return this.impl$pendingGameType;
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$RuleKey;)Z", ordinal = 0))
    private boolean impl$SuppressDeathMessageDueToPriorEvent(final GameRules gameRules, final GameRules.RuleKey gameRule) {
        return false; // Suppress death messages since this is handled together with the event calling
    }


    @Override
    public void bridge$sendBlockChange(final BlockPos pos, final BlockState state) {
        final SChangeBlockPacket packet = new SChangeBlockPacket();
        SChangeBlockPacketAccessor accessor = (SChangeBlockPacketAccessor) packet;
        accessor.accessor$setState(state);
        accessor.accessor$setPos(pos);
        this.connection.sendPacket(packet);
    }

    /**
     * @author gabizou, April 7th, 2016
     *
     * Technically an overwrite of {@link PlayerEntity#dropItem(boolean)}
     * @param dropAll
     * @return
     */
    @Override
    @Nullable
    public ItemEntity shadow$dropItem(final boolean dropAll) {
        final ItemStack currentItem = this.inventory.getCurrentItem();
        if (currentItem.isEmpty()) {
            return null;
        }

        // Add SlotTransaction to PlayerContainer
        final org.spongepowered.api.item.inventory.Slot slot = ((Inventory) this.inventory)
                .query(QueryTypes.INVENTORY_TYPE.of(Hotbar.class))
                .getSlot(this.inventory.currentItem).get();
        final ItemStackSnapshot originalItem = ItemStackUtil.snapshotOf(currentItem);
        final int count = dropAll && !currentItem.isEmpty() ? currentItem.getCount() : 1;
        final ItemStack itemToDrop = this.inventory.decrStackSize(this.inventory.currentItem, count);
        final SlotTransaction transaction = new SlotTransaction(slot, originalItem, ItemStackUtil.snapshotOf(this.inventory.getCurrentItem()));
        ((TrackedInventoryBridge) this.inventory).bridge$getCapturedSlotTransactions().add(transaction);

        return this.shadow$dropItem(itemToDrop, false, true);
    }

    @Override
    public void shadow$stopActiveHand() { // stopActiveHand
        // Our using item state is probably desynced from the client (e.g. from the initial air interaction of a bow being cancelled).
        // We need to re-send the player's inventory to overwrite any client-side inventory changes that may have occured as a result
        // of the client (but not the server) calling Item#onPlayerStoppedUsing (which in the case of a bow, removes one arrow from the inventory).
        if (this.activeItemStack.isEmpty()) {
            ((ServerPlayerEntity) (Object) this).sendContainerToPlayer(((ServerPlayerEntity) (Object) this).container);
        }
        super.shadow$stopActiveHand();
    }

    @Inject(method = "closeContainer", at = @At("RETURN"))
    private void impl$captureOnCloseContainer(final CallbackInfo ci) {
        final TrackedInventoryBridge mixinContainer = (TrackedInventoryBridge) this.openContainer;
        // Safety measure to avoid memory leaks as mods may call this directly
        if (mixinContainer.bridge$capturingInventory()) {
            mixinContainer.bridge$setCaptureInventory(false);
            mixinContainer.bridge$getCapturedSlotTransactions().clear();
        }
    }



    @Redirect(method = "readEntityFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;getForceGamemode()Z"))
    private boolean onCheckForcedGameMode(final MinecraftServer minecraftServer) {
        return minecraftServer.getForceGamemode() && !this.bridge$hasForcedGamemodeOverridePermission();
    }

    @Override
    public boolean bridge$hasForcedGamemodeOverridePermission() {
        final Player player = (Player) this;
        return player.hasPermission(player.getActiveContexts(), Constants.Permissions.FORCE_GAMEMODE_OVERRIDE);
    }

    @Override
    public void bridge$setDelegateAfterRespawn(final ServerPlayerEntity delegate) {
        this.impl$delegate = delegate;
    }

    @SuppressWarnings("BoundedWildcard")
    @Inject(method = "canAttackPlayer", at = @At("HEAD"), cancellable = true)
    private void impl$useWorldBasedAttackRules(final PlayerEntity other, final CallbackInfoReturnable<Boolean> cir) {
        final boolean worldPVP = ((WorldProperties) other.world.getWorldInfo()).isPVPEnabled();

        if (!worldPVP) {
            cir.setReturnValue(false);
            return;
        }

        final boolean teamPVP = super.shadow$canAttackPlayer(other);
        cir.setReturnValue(teamPVP);
    }
}
