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

import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@Mixin(net.minecraft.item.ItemEmptyMap.class)
public abstract class ItemEmptyMapMixin {

    // World2 is the same as worldIn
    @Redirect(method = "onItemRightClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemMap;setupNewMap(Lnet/minecraft/world/World;DDBZZ)Lnet/minecraft/item/ItemStack;",
                    ordinal = 0))
    private ItemStack impl$createMapWithSpongeData(final World worldIn, final double x, final double z, final byte scale, final boolean trackPosition,
                                            final boolean unlimitedTracking, final World world2, final EntityPlayer playerIn, final EnumHand handIn) {
        final Player player = (Player) playerIn;

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.PLAYER, player);
            final HandType handType = (HandType) (Object) handIn;
            frame.addContext(EventContextKeys.USED_HAND, handType);
            frame.addContext(EventContextKeys.USED_ITEM,
                    player.getItemInHand(handType)
                            .map(org.spongepowered.api.item.inventory.ItemStack::createSnapshot)
                            .orElse(ItemStackSnapshot.NONE));

            final Vector2i center = this.impl$calculateMapCenter(x, z, scale);
            final MapInfoData mapInfoData = new SpongeMapInfoData(center,
                    (org.spongepowered.api.world.World) worldIn,
                    trackPosition,
                    unlimitedTracking,
                    scale,
                    MapCanvas.blank(),
                    Constants.Map.DEFAULT_MAP_LOCKED,
                    Sets.newHashSet());

            final Optional<MapInfo> optMapInfo = MapUtil.fireCreateMapEvent(mapInfoData, frame.getCurrentCause());
            if (!optMapInfo.isPresent()) {
                return ItemStack.EMPTY;
            }

            final org.spongepowered.api.item.inventory.ItemStack newMap =
                    (org.spongepowered.api.item.inventory.ItemStack) new ItemStack(Items.FILLED_MAP, 1,
                            ((MapDataBridge) optMapInfo.get()).bridge$getMapId());
            return (ItemStack) newMap;
        }
    }

    @Inject(method = "onItemRightClick", at = @At(value = "INVOKE",
            target = "net/minecraft/entity/player/EntityPlayer.getHeldItem(Lnet/minecraft/util/EnumHand;)Lnet/minecraft/item/ItemStack;"),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private void impl$returnFailResultIfMapWasNotCreated(final World worldIn, final EntityPlayer player, final EnumHand handIn,
            final CallbackInfoReturnable<ActionResult<ItemStack>> cir, final ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            cir.cancel();
            cir.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(handIn)));
        }
    }

    private Vector2i impl$calculateMapCenter(final double x, final double z, final int mapScale) {
        final int i = 128 * (1 << mapScale);
        final int j = MathHelper.floor((x + 64.0D) / (double)i);
        final int k = MathHelper.floor((z + 64.0D) / (double)i);
        final int xCenter = j * i + i / 2 - 64;
        final int zCenter = k * i + i / 2 - 64; // Copied pretty much directly from MapData but static.
        return new Vector2i(xCenter, zCenter);
    }
}
