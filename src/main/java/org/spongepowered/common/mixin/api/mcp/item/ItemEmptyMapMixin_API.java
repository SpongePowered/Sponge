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
package org.spongepowered.common.mixin.api.mcp.item;

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
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.MapInfoData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.map.MapCanvas;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.api.world.map.MapStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.data.manipulator.mutable.SpongeMapInfoData;
import org.spongepowered.common.map.MapUtil;
import org.spongepowered.common.map.SpongeMapInfo;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

@Mixin(net.minecraft.item.ItemEmptyMap.class)
public abstract class ItemEmptyMapMixin_API {

    // World2 is the same as worldIn
    @Redirect(method = "onItemRightClick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/item/ItemMap;setupNewMap(Lnet/minecraft/world/World;DDBZZ)Lnet/minecraft/item/ItemStack;",
                    ordinal = 0))
    protected ItemStack setupNewMapRedirect(World worldIn, double x, double z, byte scale, boolean trackPosition,
                                            boolean unlimitedTracking, World world2, EntityPlayer playerIn, EnumHand handIn) {
        Player player = (Player)playerIn;

        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(player);
            frame.addContext(EventContextKeys.PLAYER, player);
            HandType handType = (HandType)(Object)handIn;
            frame.addContext(EventContextKeys.USED_HAND, handType);
            frame.addContext(EventContextKeys.USED_ITEM,
                    player.getItemInHand(handType)
                            .map(org.spongepowered.api.item.inventory.ItemStack::createSnapshot)
                            .orElse(ItemStackSnapshot.NONE));

            Vector2i center = calculateMapCenter(x, z, scale);
            MapInfoData mapInfoData = new SpongeMapInfoData(center,
                    (org.spongepowered.api.world.World) worldIn,
                    trackPosition,
                    unlimitedTracking,
                    scale,
                    MapCanvas.blank(),
                    Constants.Map.DEFAULT_MAP_LOCKED,
                    Sets.newHashSet());

            Optional<MapInfo> optMapInfo = MapUtil.fireCreateMapEvent(mapInfoData, frame.getCurrentCause());
            // Event Cancelled
            if (!optMapInfo.isPresent()) {
                return ItemStack.EMPTY;
            }

            org.spongepowered.api.item.inventory.ItemStack newMap = (org.spongepowered.api.item.inventory.ItemStack)new ItemStack(Items.FILLED_MAP, 1, ((SpongeMapInfo)optMapInfo.get()).getMapId());
            return (ItemStack) newMap;
        }
    }

    @Inject(method = "onItemRightClick", at = @At(value = "INVOKE", ordinal = 2),
            locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    protected void checkIfShouldReturn(World worldIn, EntityPlayer player, EnumHand handIn, CallbackInfoReturnable<ActionResult<ItemStack>> cir, ItemStack itemStack, ItemStack itemStack1) {
        if (itemStack.isEmpty()) {
            cir.cancel();
            cir.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, itemStack1));
        }
    }

    private static Vector2i calculateMapCenter(double x, double z, int mapScale) {
        int i = 128 * (1 << mapScale);
        int j = MathHelper.floor((x + 64.0D) / (double)i);
        int k = MathHelper.floor((z + 64.0D) / (double)i);
        int xCenter = j * i + i / 2 - 64;
        int zCenter = k * i + i / 2 - 64; // Copied pretty much directly from MapData but static.
        return new Vector2i(xCenter, zCenter);
    }
}
