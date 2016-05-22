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
package org.spongepowered.common;


import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.Teleporter;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.common.event.CauseTracker;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinWorld;

/**
 * Utility that fires events that normally Forge fires at (in spots). Typically
 * our penultimate goal is to not remove spots where events occur but sometimes
 * it happens (in @Overwrites typically). Normally events that are in Forge are
 * called themselves in SpongeVanilla but when it can't really occur, we fix
 * this issue with Sponge by overwriting this class
 */
public final class SpongeImplHooks {

    public static LoadWorldEvent createLoadWorldEvent(World world) {
        return SpongeEventFactory.createLoadWorldEvent(Cause.of(NamedCause.source(SpongeImpl.getGame().getServer())), world);
    }

    public static boolean blockHasTileEntity(Block block, IBlockState state) {
        return block instanceof ITileEntityProvider;
    }

    public static int getBlockLightValue(Block block, BlockPos pos, IBlockAccess world) {
        return block.getLightValue();
    }

    public static int getBlockLightOpacity(Block block, IBlockAccess world, BlockPos pos) {
        return block.getLightOpacity();
    }

    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider)block).createNewTileEntity(world, block.getMetaFromState(state));
        }
        return null;
    }

    /**
     * A simple method to check attacks for the forge event factory.
     *
     * @param entityPlayer
     * @param targetEntity
     * @return
     */
    public static boolean checkAttackEntity(EntityPlayer entityPlayer, Entity targetEntity) {
        final ItemStack item = entityPlayer.getCurrentEquippedItem();
        if (item != null) {
            return true;
        }
        return true;
    }

    public static boolean isCreatureOfType(Entity entity, EnumCreatureType type) {
        if (entity instanceof EntityMob || entity instanceof EntitySlime) {
          return type == EnumCreatureType.MONSTER;
        } else if (entity instanceof EntityWaterMob) {
            return type == EnumCreatureType.WATER_CREATURE;
        } else if (entity instanceof EntityAmbientCreature) {
            return type == EnumCreatureType.AMBIENT;
        } else if (((entity instanceof EntityCreature))) {
          return type == EnumCreatureType.CREATURE;
        }

        return false;
    }

    public static boolean isFakePlayer(Entity entity) {
        return false;
    }

    public static boolean onDroppedByPlayer(Item item, ItemStack stack, EntityPlayer player) {
        return true;
    }

    public static EntityItem onPlayerToss(EntityPlayer player, ItemStack item, boolean includeName)  {
        IMixinEntity spongeEntity = (IMixinEntity) player;
        spongeEntity.setCaptureItemDrops(true);
        EntityItem ret = player.dropItem(item, false, includeName);
        spongeEntity.getCapturedItemDrops().clear();
        spongeEntity.setCaptureItemDrops(false);

        if (ret == null) {
            return null;
        }

        IMixinWorld spongeWorld = (IMixinWorld) player.worldObj;
        final CauseTracker causeTracker = spongeWorld.getCauseTracker();
        // We handle container drops in SpongeCommonEventFactory.handleClickInteractInventoryEvent
        if (!(causeTracker.getCurrentPlayerPacket() instanceof C0EPacketClickWindow)) {
            DropItemEvent.Dispense event = SpongeCommonEventFactory.callDropItemEventDispenseSingle(player, ret);
            if (event.isCancelled()) {
                return null;
            }

            EntityItem eventItem = (EntityItem) event.getEntities().get(0);
            spongeWorld.getCauseTracker().setIgnoreSpawnEvents(true);
            player.worldObj.spawnEntityInWorld(eventItem);
            spongeWorld.getCauseTracker().setIgnoreSpawnEvents(false);
            return eventItem;
        } else {
            player.worldObj.spawnEntityInWorld(ret);
            return ret;
        }
    }

    public static String getModIdFromClass(Class<?> clazz) {
        return "";
    }

    public static void registerPortalAgentType(Teleporter teleporter) {
        // plugins are required to register types
    }
}
