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
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
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
import org.spongepowered.common.bridge.world.storage.MapItemSavedDataBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.math.vector.Vector2i;

import java.util.Optional;
import java.util.Set;

@Mixin(EmptyMapItem.class)
public abstract class EmptyMapItemMixin {

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"), cancellable = true)
    private void impl$prepareMap(final Level level, final net.minecraft.world.entity.player.Player player,
                                 final InteractionHand usedHand, final CallbackInfoReturnable<InteractionResult> cir,
                                 @Local final ItemStack usedItem, @Share("id") final LocalIntRef mapId) {
        try (final CauseStackManager.StackFrame frame = PhaseTracker.getInstance().pushCauseFrame()) {
            frame.addContext(EventContextKeys.PLAYER, (Player) player);
            frame.addContext(EventContextKeys.USED_HAND, (HandType) (Object) usedHand);
            frame.addContext(EventContextKeys.USED_ITEM, ItemStackUtil.snapshotOf(usedItem));

            final Set<Value<?>> mapValues = Sets.newHashSet(
                Value.immutableOf(Keys.MAP_LOCATION, Vector2i.from((int) player.getX(), (int) player.getZ())),
                Value.immutableOf(Keys.MAP_WORLD, ((ServerWorld) level).key())
            );

            final Optional<MapInfo> mapInfo = SpongeCommonEventFactory.fireCreateMapEvent(frame.currentCause(), mapValues);
            if (mapInfo.isEmpty()) {
                cir.setReturnValue(InteractionResult.FAIL);
                return;
            }

            mapId.set(((MapItemSavedDataBridge) mapInfo.get()).bridge$getMapId());
        }
    }

    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/MapItem;create(Lnet/minecraft/world/level/Level;IIBZZ)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack impl$createMap(final Level level, final int blockX, final int blockZ,
                                     final byte scale, final boolean trackingPosition, final boolean unlimitedTracking,
                                     @Share("id") final LocalIntRef mapId) {
        final ItemStack newMap = new ItemStack(Items.FILLED_MAP, 1);
        newMap.set(DataComponents.MAP_ID, new MapId(mapId.get()));
        return newMap;
    }
}
