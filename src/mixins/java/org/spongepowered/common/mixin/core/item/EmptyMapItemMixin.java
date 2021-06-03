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
package org.spongepowered.common.mixin.core.item;

import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.world.storage.MapItemSavedDataBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.Constants;
import org.spongepowered.math.vector.Vector2i;

import java.util.Optional;
import java.util.Set;

@Mixin(EmptyMapItem.class)
public abstract class EmptyMapItemMixin {

    @Redirect(method = "use",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/MapItem;create(Lnet/minecraft/world/level/Level;IIBZZ)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack impl$createMapWithSpongeData(final Level level, final int x, final int y, final byte scale,
                                                   final boolean trackingPosition, final boolean unlimitedTracking,
                                                   final Level level2,
                                                   final net.minecraft.world.entity.player.Player playerIn,
                                                   final InteractionHand usedHand) {
        final Player player = (Player) playerIn;

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.addContext(EventContextKeys.PLAYER, player);
            final HandType handType = (HandType) (Object) usedHand;
            frame.addContext(EventContextKeys.USED_HAND, handType);
            frame.addContext(EventContextKeys.USED_ITEM, player.itemInHand(handType).createSnapshot());

            final Set<Value<?>> mapValues = Sets.newHashSet(
                    Value.immutableOf(Keys.MAP_LOCATION, Vector2i.from((int)playerIn.getX(), (int)playerIn.getZ())),
                    Value.immutableOf(Keys.MAP_WORLD, ((ServerWorld)level).key()),
                    Value.immutableOf(Keys.MAP_TRACKS_PLAYERS, trackingPosition),
                    Value.immutableOf(Keys.MAP_UNLIMITED_TRACKING, unlimitedTracking),
                    Value.immutableOf(Keys.MAP_SCALE, (int)scale)
                    // No need to have the defaults
                    //Value.immutableOf(Keys.MAP_CANVAS, MapCanvas.blank()),
                    //Value.immutableOf(Keys.MAP_LOCKED, Constants.Map.DEFAULT_MAP_LOCKED),
                    //Value.immutableOf(Keys.MAP_DECORATIONS, Sets.newHashSet())
            );

            final Optional<MapInfo> optMapInfo = SpongeCommonEventFactory.fireCreateMapEvent(frame.currentCause(), mapValues);
            if (!optMapInfo.isPresent()) {
                return ItemStack.EMPTY;
            }
            final int id = ((MapItemSavedDataBridge) optMapInfo.get()).bridge$getMapId();

            final ItemStack newMap = new ItemStack(Items.FILLED_MAP, 1);
            final CompoundTag nbt = newMap.getOrCreateTag();
            nbt.putInt(Constants.Map.MAP_ID, id);

            return newMap;
        }
    }

    @Inject(method = "use", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "net/minecraft/world/entity/player/Player.getItemInHand (Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void impl$returnFailResultIfMapWasNotCreated(final Level level,
                                                         final net.minecraft.world.entity.player.Player playerIn,
                                                         final InteractionHand handIn,
                                                         final CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir,
                                                         final ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            cir.cancel();
            cir.setReturnValue(InteractionResultHolder.fail(playerIn.getItemInHand(handIn)));
        }
    }

}
