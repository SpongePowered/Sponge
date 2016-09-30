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
package org.spongepowered.common.entity.player;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Implements things that are not implemented by mixins into this class. <p>This
 * class is concrete in order to create instances of User.</p>
 *
 * <p>List of mixins mixing into this class: <ul>
 * <li>MixinSpongeUser</li><li>MixinDataHolder</li><li>MixinSubject</li> </ul>
 *
 * TODO Future note about data: The following data manipulators are always
 * applicable to User: BanData, WhitelistData, JoinData
 */
public class SpongeUser implements ArmorEquipable, Tamer, DataSerializable, Carrier, ISpongeUser {

    public static final Set<SpongeUser> dirtyUsers = Sets.newHashSet();

    private final User self = (User) this; // convenient access
    private final GameProfile profile;

    private final Map<UUID, RespawnLocation> spawnLocations = Maps.newHashMap();

    public SpongeUser(GameProfile profile) {
        this.profile = profile;
    }

    private void reset() {
        this.spawnLocations.clear();
    }

    public void readFromNbt(NBTTagCompound compound) {
        this.reset();

        // See EntityPlayer#readEntityFromNBT
        final NBTTagCompound spongeCompound = compound.getCompoundTag(NbtDataUtil.FORGE_DATA).getCompoundTag(NbtDataUtil.SPONGE_DATA);
        if (!spongeCompound.hasNoTags()) {
            final NBTTagList spawnList = spongeCompound.getTagList(NbtDataUtil.USER_SPAWN_LIST, NbtDataUtil.TAG_COMPOUND);

            for (int i = 0; i < spawnList.tagCount(); i++) {
                final NBTTagCompound spawnCompound = spawnList.getCompoundTagAt(i);

                final UUID uuid = spawnCompound.getUniqueId(NbtDataUtil.UUID);

                if (uuid.getLeastSignificantBits() != 0 && uuid.getMostSignificantBits() != 0) {
                    final double xPos = spawnCompound.getDouble(NbtDataUtil.USER_SPAWN_X);
                    final double yPos = spawnCompound.getDouble(NbtDataUtil.USER_SPAWN_Y);
                    final double zPos = spawnCompound.getDouble(NbtDataUtil.USER_SPAWN_Z);
                    final boolean forced = spawnCompound.getBoolean(NbtDataUtil.USER_SPAWN_FORCED);
                    this.spawnLocations.put(uuid, new RespawnLocation.Builder()
                            .forceSpawn(forced)
                            .position(new Vector3d(xPos, yPos, zPos))
                            .world(uuid)
                            .build());
                }
            }
        }


        // TODO Read: inventory, any other data that should be
        // available through data manipulators.
    }

    public void writeToNbt(NBTTagCompound compound) {

        final NBTTagCompound forgeCompound = compound.getCompoundTag(NbtDataUtil.FORGE_DATA);
        final NBTTagCompound spongeCompound = forgeCompound.getCompoundTag(NbtDataUtil.SPONGE_DATA);
        spongeCompound.removeTag(NbtDataUtil.USER_SPAWN_LIST);

        final NBTTagList spawnList = new NBTTagList();
        for (Entry<UUID, RespawnLocation> entry : this.spawnLocations.entrySet()) {
            final RespawnLocation respawn = entry.getValue();

            final NBTTagCompound spawnCompound = new NBTTagCompound();
            spawnCompound.setUniqueId(NbtDataUtil.UUID, entry.getKey());
            spawnCompound.setDouble(NbtDataUtil.USER_SPAWN_X, respawn.getPosition().getX());
            spawnCompound.setDouble(NbtDataUtil.USER_SPAWN_Y, respawn.getPosition().getY());
            spawnCompound.setDouble(NbtDataUtil.USER_SPAWN_Z, respawn.getPosition().getZ());
            spawnCompound.setBoolean(NbtDataUtil.USER_SPAWN_FORCED, false); // No way to know
            spawnList.appendTag(spawnCompound);
        }

        if (!spawnList.hasNoTags()) {
            spongeCompound.setTag(NbtDataUtil.USER_SPAWN_LIST, spawnList);
            forgeCompound.setTag(NbtDataUtil.SPONGE_DATA, spongeCompound);
            compound.setTag(NbtDataUtil.FORGE_DATA, forgeCompound);
        }
    }

    @Override
    public UUID getUniqueId() {
        return this.profile.getId();
    }

    @Override
    public String getName() {
        return this.profile.getName();
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        // TODO More data
        return new MemoryDataContainer()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.USER_UUID, this.profile.getId())
                .set(DataQueries.USER_NAME, this.profile.getName())
                .set(DataQueries.USER_SPAWNS, this.spawnLocations);
    }

    private Player getUserIfOnlineForInventory() {
        return this.self.getPlayer().orElseThrow(() -> new UnsupportedOperationException("User is offline, offline inventory API yet to be implemented"));
    }

    @Override
    public boolean canEquip(EquipmentType type) {
        return getUserIfOnlineForInventory().canEquip(type); // TODO Inventory API
    }

    @Override
    public boolean canEquip(EquipmentType type, ItemStack equipment) {
        return getUserIfOnlineForInventory().canEquip(type, equipment); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getEquipped(EquipmentType type) {
        return getUserIfOnlineForInventory().getEquipped(type); // TODO Inventory API
    }

    @Override
    public boolean equip(EquipmentType type, ItemStack equipment) {
        return getUserIfOnlineForInventory().equip(type, equipment); // TODO Inventory API
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        return getUserIfOnlineForInventory().getInventory(); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getHelmet() {
        return getUserIfOnlineForInventory().getHelmet(); // TODO Inventory API
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        getUserIfOnlineForInventory().setHelmet(helmet); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getChestplate() {
        return getUserIfOnlineForInventory().getChestplate(); // TODO Inventory API
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        getUserIfOnlineForInventory().setChestplate(chestplate); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getLeggings() {
        return getUserIfOnlineForInventory().getLeggings(); // TODO Inventory API
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        getUserIfOnlineForInventory().setLeggings(leggings); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getBoots() {
        return getUserIfOnlineForInventory().getBoots(); // TODO Inventory API
    }

    @Override
    public void setBoots(ItemStack boots) {
        getUserIfOnlineForInventory().setBoots(boots); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getItemInHand(HandType handType) {
        return getUserIfOnlineForInventory().getItemInHand(handType); // TODO Inventory API
    }

    @Override
    public void setItemInHand(HandType handType, @Nullable ItemStack itemInHand) {
        getUserIfOnlineForInventory().setItemInHand(handType, itemInHand); // TODO Inventory API
    }

    @Override
    public Map<UUID, RespawnLocation> getBedlocations() {
        Optional<Player> player = this.self.getPlayer();
        if (player.isPresent()) {
            return ((ISpongeUser) player.get()).getBedlocations();
        }
        return this.spawnLocations;
    }

    @Override
    public boolean setBedLocations(Map<UUID, RespawnLocation> value) {
        Optional<Player> player = this.self.getPlayer();
        if (player.isPresent()) {
            return ((ISpongeUser) player.get()).setBedLocations(value);
        }
        this.spawnLocations.clear();
        this.spawnLocations.putAll(value);
        this.markDirty();
        return true;
    }

    @Override
    public ImmutableMap<UUID, RespawnLocation> removeAllBeds() {
        Optional<Player> player = this.self.getPlayer();
        if (player.isPresent()) {
            return ((ISpongeUser) player.get()).removeAllBeds();
        }
        ImmutableMap<UUID, RespawnLocation> locations = ImmutableMap.copyOf(this.spawnLocations);
        this.spawnLocations.clear();
        this.markDirty();
        return locations;
    }

    private void markDirty() {
        dirtyUsers.add(this);
    }

    public void save() {
        SaveHandler saveHandler = (SaveHandler) WorldManager.getWorldByDimensionId(0).get().getSaveHandler();
        File dataFile = new File(saveHandler.playersDirectory, getUniqueId() + ".dat");
        NBTTagCompound tag;
        if (dataFile.isFile()) {
            try {
                tag = CompressedStreamTools.readCompressed(new FileInputStream(dataFile));
            } catch (IOException ignored) {
                // Nevermind
                tag = new NBTTagCompound();
            }
        } else {
            tag = new NBTTagCompound();
        }
        writeToNbt(tag);
        try {
            CompressedStreamTools.writeCompressed(tag, new FileOutputStream(dataFile));
            dirtyUsers.remove(this);
        } catch (IOException e) {
            SpongeImpl.getLogger().warn("Failed to save user file [{}]!", dataFile, e);
        }
    }

}
