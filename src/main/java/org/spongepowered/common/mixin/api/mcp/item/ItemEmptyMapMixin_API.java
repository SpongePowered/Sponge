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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.item.MapItemData;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.action.CreateMapEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.world.map.MapStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.storage.MapStorageBridge;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeMapItemData;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.world.WorldManager;

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

            int id = Sponge.getServer().getMapStorage().flatMap(MapStorage::getHighestMapId).orElse(-1);
            id++;

            org.spongepowered.api.item.inventory.ItemStack newMap = (org.spongepowered.api.item.inventory.ItemStack)new ItemStack(Items.FILLED_MAP, 1, id);
            HandType handType = (HandType) (Object) handIn;
            // We have to do a call to this ourselves due to the order of methods.
            // (its called straight after in main method body)
            org.spongepowered.api.item.inventory.ItemStack heldItem = player.getItemInHand(handType).get();

            MapItemData mapItemData = new SpongeMapItemData(
                    calculateMapCenter(x,z,scale), (org.spongepowered.api.world.World)worldIn,
                    trackPosition, unlimitedTracking, scale);

            CreateMapEvent event = SpongeCommonEventFactory.callCreateMapEvent(
                    frame.getCurrentCause(), player, mapItemData,
                    (org.spongepowered.api.world.World) worldIn, handType,
                    newMap.createSnapshot(), heldItem.createSnapshot(), id);
            if (event.isCancelled()) {
                return ItemStack.EMPTY; // Injection checks for this
            }
            World targetWorld;
            if (worldIn.isRemote) {
                targetWorld = worldIn;
            }
            else {
                targetWorld = WorldManager.getWorld(Sponge.getServer().getDefaultWorldName()).get();
            }
            // Call getUniqueDataId to advance the map ids.
            int mcId = targetWorld.getUniqueDataId("map");
            if (id != mcId) {
                // Short has overflown.
                SpongeImpl.getLogger().warn("Map size corruption, vanilla only allows " + Short.MAX_VALUE + "! Expected next number was not equal to the true next number.");
                SpongeImpl.getLogger().warn("Expected: " + id + ". Got: " + mcId);
                SpongeImpl.getLogger().warn("Automatically cancelling map creation");
                ((MapStorageBridge)Sponge.getServer().getMapStorage().get()).bridge$setHighestMapId((short)(id - 1));
                return ItemStack.EMPTY;
            }
            String s = Constants.ItemStack.MAP_PREFIX + id;
            MapData mapData = new MapData(s);
            if (worldIn.isRemote) {
                worldIn.setData(s, mapData);
            }
            else {
                targetWorld.setData(s, mapData);
            }
            setMapData(mapData, mapItemData);
            SpongeImpl.getLogger().info(mapItemData.world().get().getName());
            SpongeImpl.getLogger().warn("about to offer");
            newMap.offer(mapItemData);
            //mapData.updateMapData(0,127);
            //mapData.markDirty();
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

    public void setMapData(MapData mapData, MapItemData mapItemData) {
        Vector2i loc = mapItemData.location().get();
        mapData.xCenter = loc.getX();
        mapData.zCenter = loc.getY();
        mapData.dimension = (byte)((WorldServerBridge)mapItemData.world().get()).bridge$getDimensionId();
        mapData.scale = mapItemData.scale().get().byteValue();
        mapData.trackingPosition = mapItemData.trackPosition().get();
        mapData.unlimitedTracking = mapItemData.unlimitedTracking().get();
    }

    // Based off minecraft's code MapData.calculateMapCenter to ensure
    // that they work the same
    public Vector2i calculateMapCenter(double x, double z, int mapScale) {
        int i = 128 * (1 << mapScale);
        int j = (int)Math.floor((x + 64.0D) / (double)i);
        int k = (int)Math.floor((z + 64.0D) / (double)i);
        return new Vector2i(j * i + i / 2 - 64, k * i + i / 2 - 64);
    }
}
