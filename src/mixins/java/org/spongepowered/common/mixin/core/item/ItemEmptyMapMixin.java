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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
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
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.math.vector.Vector2i;

import java.util.Optional;
import java.util.Set;

@Mixin(net.minecraft.item.MapItem.class)
public abstract class ItemEmptyMapMixin {

    @Redirect(method = "onItemRightClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/FilledMapItem;setupNewMap(Lnet/minecraft/world/World;IIBZZ)Lnet/minecraft/item/ItemStack;",
                    ordinal = 0))
    private ItemStack impl$createMapWithSpongeData(World worldIn, int worldX, int worldZ, byte scale, boolean trackingPosition, boolean unlimitedTracking,
                                                   World worldIn2, PlayerEntity playerIn, Hand handIn) {
        final Player player = (Player) playerIn;

        try (final CauseStackManager.StackFrame frame = PhaseTracker.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.PLAYER, player);
            final HandType handType = (HandType) (Object) handIn;
            frame.addContext(EventContextKeys.USED_HAND, handType);
            frame.addContext(EventContextKeys.USED_ITEM, player.getItemInHand(handType).createSnapshot());

            final Set<Value<?>> mapValues = Sets.newHashSet(
                    Value.immutableOf(Keys.MAP_LOCATION.get(), Vector2i.from(worldX, worldZ)),
                    Value.immutableOf(Keys.MAP_WORLD, ((ServerWorld)worldIn).getKey()),
                    Value.immutableOf(Keys.MAP_TRACKS_PLAYERS, trackingPosition),
                    Value.immutableOf(Keys.MAP_UNLIMITED_TRACKING, unlimitedTracking),
                    Value.immutableOf(Keys.MAP_SCALE, (int)scale)
                    // No need to have the defaults
                    //Value.immutableOf(Keys.MAP_CANVAS, MapCanvas.blank()),
                    //Value.immutableOf(Keys.MAP_LOCKED, Constants.Map.DEFAULT_MAP_LOCKED),
                    //Value.immutableOf(Keys.MAP_DECORATIONS, Sets.newHashSet())
            );

            final Optional<MapInfo> optMapInfo = MapUtil.fireCreateMapEvent(frame.getCurrentCause(), mapValues);
            if (!optMapInfo.isPresent()) {
                return ItemStack.EMPTY;
            }
            final int id = ((MapDataBridge) optMapInfo.get()).bridge$getMapId();

            final ItemStack newMap = new ItemStack(Items.FILLED_MAP, 1);
            final CompoundNBT nbt = newMap.getOrCreateTag();
            nbt.putInt("map", id);

            return newMap;
        }
    }

    @Inject(method = "onItemRightClick", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
            target = "Lnet/minecraft/entity/player/PlayerEntity;getHeldItem(Lnet/minecraft/util/Hand;)Lnet/minecraft/item/ItemStack;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void impl$returnFailResultIfMapWasNotCreated(World worldIn, PlayerEntity playerIn, Hand handIn, CallbackInfoReturnable<ActionResult<ItemStack>> cir, ItemStack itemstack) {
        if (itemstack.isEmpty()) {
            cir.cancel();
            cir.setReturnValue(new ActionResult<>(ActionResultType.FAIL, playerIn.getHeldItem(handIn)));
        }
    }

}
