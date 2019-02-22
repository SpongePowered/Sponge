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
import com.mojang.authlib.GameProfile;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.SaveHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.type.CarriedInventory;
import org.spongepowered.api.util.RespawnLocation;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.nbt.CustomDataNbtUtil;
import org.spongepowered.common.data.type.SpongeEquipmentType;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public static final Set<SpongeUser> dirtyUsers = ConcurrentHashMap.newKeySet();

    private final User self = (User) this; // convenient access
    private final GameProfile profile;

    private final Map<UUID, RespawnLocation> spawnLocations = Maps.newHashMap();

    private double posX;
    private double posY;
    private double posZ;
    private DimensionType dimensionType;
    private float rotationYaw;
    private float rotationPitch;


    private SpongeUserInventory inventory; // lazy load when accessing inventory
    private InventoryEnderChest enderChest; // lazy load when accessing inventory
    private NBTTagCompound nbt;

    public SpongeUser(GameProfile profile) {
        this.profile = profile;
    }

    private void reset() {
        this.spawnLocations.clear();
    }

    public void readFromNbt(NBTTagCompound compound) {
        this.reset();
        this.nbt = compound;

        NBTTagList position = compound.getList(NbtDataUtil.ENTITY_POSITION, NbtDataUtil.TAG_DOUBLE);
        this.posX = position.getDouble(0);
        this.posY = position.getDouble(1);
        this.posZ = position.getDouble(2);
        this.dimensionType = DimensionType.OVERWORLD;
        if (compound.contains(NbtDataUtil.ENTITY_DIMENSION)) {
            this.dimensionType = DimensionType.getById(compound.getInt(NbtDataUtil.ENTITY_DIMENSION));
            if (this.dimensionType == null) {
                // TODO (1.13) - Log this maybe?
                this.dimensionType = DimensionType.OVERWORLD;
            }
        }
        NBTTagList rotation = compound.getList(NbtDataUtil.ENTITY_ROTATION, NbtDataUtil.TAG_FLOAT);
        this.rotationYaw = rotation.getFloat(0);
        this.rotationPitch = rotation.getFloat(1);

        // See EntityPlayer#readEntityFromNBT

        final NBTTagCompound spongeCompound = compound.getCompound(NbtDataUtil.FORGE_DATA).getCompound(NbtDataUtil.SPONGE_DATA);
        CustomDataNbtUtil.readCustomData(spongeCompound, ((DataHolder) this));
        if (!spongeCompound.isEmpty()) {
            final NBTTagList spawnList = spongeCompound.getList(NbtDataUtil.USER_SPAWN_LIST, NbtDataUtil.TAG_COMPOUND);

            for (int i = 0; i < spawnList.size(); i++) {
                final NBTTagCompound spawnCompound = spawnList.getCompound(i);

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


        // TODO Read: any other data that should be available through data manipulators.
    }

    private SpongeUser loadInventory() {
        if (this.inventory == null) {
            this.inventory = new SpongeUserInventory(this);
            NBTTagList nbttaglist = this.nbt.getList(NbtDataUtil.Minecraft.INVENTORY, 10);
            this.inventory.readFromNBT(nbttaglist);
            this.inventory.currentItem = this.nbt.getInt(NbtDataUtil.Minecraft.SELECTED_ITEM_SLOT);
        }
        return this;
    }

    private SpongeUser loadEnderInventory() {
        if (this.enderChest == null) {
            this.enderChest = new SpongeUserInventoryEnderchest(this);
            if (this.nbt.contains(NbtDataUtil.Minecraft.ENDERCHEST_INVENTORY, 9))
            {
                NBTTagList nbttaglist1 = this.nbt.getList(NbtDataUtil.Minecraft.ENDERCHEST_INVENTORY, 10);
                this.enderChest.read(nbttaglist1);
            }
        }
        return this;
    }

    public void writeToNbt(NBTTagCompound compound) {

        this.loadInventory();
        this.loadEnderInventory();
        compound.put(NbtDataUtil.Minecraft.INVENTORY, this.inventory.writeToNBT(new NBTTagList()));
        compound.put(NbtDataUtil.Minecraft.ENDERCHEST_INVENTORY, this.enderChest.write());
        compound.putInt(NbtDataUtil.Minecraft.SELECTED_ITEM_SLOT, this.inventory.currentItem);

        compound.put(NbtDataUtil.ENTITY_POSITION, NbtDataUtil.newDoubleNBTList(this.posX, this.posY, this.posZ));
        compound.putInt(NbtDataUtil.ENTITY_DIMENSION, this.dimensionType.getId());
        compound.put(NbtDataUtil.ENTITY_ROTATION, NbtDataUtil.newFloatNBTList(this.rotationYaw, this.rotationPitch));

        final NBTTagCompound forgeCompound = compound.getCompound(NbtDataUtil.FORGE_DATA);
        final NBTTagCompound spongeCompound = forgeCompound.getCompound(NbtDataUtil.SPONGE_DATA);
        spongeCompound.remove(NbtDataUtil.USER_SPAWN_LIST);

        final NBTTagList spawnList = new NBTTagList();
        for (Entry<UUID, RespawnLocation> entry : this.spawnLocations.entrySet()) {
            final RespawnLocation respawn = entry.getValue();

            final NBTTagCompound spawnCompound = new NBTTagCompound();
            spawnCompound.putUniqueId(NbtDataUtil.UUID, entry.getKey());
            spawnCompound.putDouble(NbtDataUtil.USER_SPAWN_X, respawn.getPosition().getX());
            spawnCompound.putDouble(NbtDataUtil.USER_SPAWN_Y, respawn.getPosition().getY());
            spawnCompound.putDouble(NbtDataUtil.USER_SPAWN_Z, respawn.getPosition().getZ());
            spawnCompound.putBoolean(NbtDataUtil.USER_SPAWN_FORCED, false); // No way to know
            spawnList.add(spawnCompound);
        }

        if (!spawnList.isEmpty()) {
            spongeCompound.put(NbtDataUtil.USER_SPAWN_LIST, spawnList);
            forgeCompound.put(NbtDataUtil.SPONGE_DATA, spongeCompound);
            compound.put(NbtDataUtil.FORGE_DATA, forgeCompound);
        }

        CustomDataNbtUtil.writeCustomData(spongeCompound, ((DataHolder) this));
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
        return DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(DataQueries.USER_UUID, this.profile.getId())
                .set(DataQueries.USER_NAME, this.profile.getName())
                .set(DataQueries.USER_SPAWNS, this.spawnLocations);
    }

    @Override
    public boolean canEquip(EquipmentType type) {
        return this.getForInventory(p -> p.canEquip(type), u -> true);
    }

    @Override
    public boolean canEquip(EquipmentType type, ItemStack equipment) {
        return this.getForInventory(p -> p.canEquip(type, equipment), u -> true);
    }

    @Override
    public Optional<ItemStack> getEquipped(EquipmentType type) {
        return this.getForInventory(p -> p.getEquipped(type), u -> u.getEquippedItem(type));
    }

    @Override
    public boolean equip(EquipmentType type, ItemStack equipment) {
        if (this.canEquip(type, equipment)) {
            this.setForInventory(p -> p.equip(type, equipment), u -> u.setEquippedItem(type, equipment));
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public CarriedInventory<?> getInventory() {
        return this.getForInventory(Player::getInventory, u -> ((CarriedInventory) u.inventory));
    }

    @Override
    public ItemStack getItemInHand(HandType handType) {
        if (handType == HandTypes.MAIN_HAND) {
            return this.getForInventory(p -> p.getItemInHand(handType), u -> u.getEquipped(EquipmentTypes.MAIN_HAND).orElseThrow(IllegalStateException::new));
        } else if (handType == HandTypes.OFF_HAND) {
            return this.getForInventory(p -> p.getItemInHand(handType), u -> u.getEquipped(EquipmentTypes.OFF_HAND).orElseThrow(IllegalStateException::new));
        }
        throw new IllegalArgumentException("Invalid hand " + handType);
    }

    @Override
    public void setItemInHand(HandType handType, ItemStack itemInHand) {
        if (handType == HandTypes.MAIN_HAND) {
            this.setForInventory(p -> p.setItemInHand(handType, itemInHand), u -> u.setEquippedItem(EquipmentTypes.MAIN_HAND, itemInHand));
        } else if (handType == HandTypes.OFF_HAND) {
            this.setForInventory(p -> p.setItemInHand(handType, itemInHand), u -> u.setEquippedItem(EquipmentTypes.OFF_HAND, itemInHand));
        } else {
            throw new IllegalArgumentException("Invalid hand " + handType);
        }
    }

    @Override
    public ItemStack getHelmet() {
        return this.getEquipped(EquipmentTypes.HEADWEAR).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        this.equip(EquipmentTypes.HEADWEAR, helmet);
    }

    @Override
    public ItemStack getChestplate() {
        return this.getEquipped(EquipmentTypes.CHESTPLATE).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        this.equip(EquipmentTypes.CHESTPLATE, chestplate);
    }

    @Override
    public ItemStack getLeggings() {
        return this.getEquipped(EquipmentTypes.LEGGINGS).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        this.equip(EquipmentTypes.LEGGINGS, leggings);
    }

    @Override
    public ItemStack getBoots() {
        return this.getEquipped(EquipmentTypes.BOOTS).orElseThrow(IllegalStateException::new);
    }

    @Override
    public void setBoots(ItemStack boots) {
        this.equip(EquipmentTypes.BOOTS, boots);
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

    protected void markDirty() {
        dirtyUsers.add(this);
    }

    public void save() {
        SaveHandler saveHandler =
            (SaveHandler) ((IMixinMinecraftServer) Sponge.getServer()).getWorldLoader().getWorld(this.dimensionType).get().getSaveHandler();
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

    // Helpers for UserInventory

    private <T> T getForInventory(Function<Player, T> playerFunction, Function<SpongeUser, T> userFunction) {
        if (this.self.getPlayer().isPresent()) {
            return playerFunction.apply(this.self.getPlayer().get());
        }
        return userFunction.apply(this.loadInventory()); // Load Inventory if not yet loaded
    }

    private void setForInventory(Consumer<Player> playerFunction, Consumer<SpongeUser> userFunction) {
        if (this.self.getPlayer().isPresent()) {
            playerFunction.accept(this.self.getPlayer().get());
            return;
        }
        userFunction.accept(this.loadInventory()); // Load Inventory if not yet loaded
    }


    // Helpers for Equipment:

    private Optional<ItemStack> getEquippedItem(EquipmentType type) {
        if (type instanceof SpongeEquipmentType) {
            List<EntityEquipmentSlot> slots = ((SpongeEquipmentType) type).getSlots();
            if (slots.size() == 1) {
                net.minecraft.item.ItemStack nmsItem = this.getItemStackFromSlot(slots.get(0));
                if (!nmsItem.isEmpty()) {
                    return Optional.of(ItemStackUtil.fromNative(nmsItem));
                }
            }
        }
        return Optional.empty();
    }

    private void setEquippedItem(EquipmentType type, ItemStack item) {
        if (type instanceof SpongeEquipmentType) {
            List<EntityEquipmentSlot> slots = ((SpongeEquipmentType) type).getSlots();
            for (EntityEquipmentSlot slot : slots) {
                this.setItemStackToSlot(slot, ItemStackUtil.toNative(item));
                // TODO check canequip
                return;
            }
        }
    }

    private net.minecraft.item.ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            return this.inventory.getCurrentItem();
        } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
            return this.inventory.offHandInventory.get(0);
        } else {
            return slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR ? this.inventory.armorInventory.get(slotIn.getIndex()) :
                    net.minecraft.item.ItemStack.EMPTY;
        }
    }

    private void setItemStackToSlot(EntityEquipmentSlot slotIn, net.minecraft.item.ItemStack stack) {
        if (slotIn == EntityEquipmentSlot.MAINHAND) {
            this.inventory.mainInventory.set(this.inventory.currentItem, stack);
        } else if (slotIn == EntityEquipmentSlot.OFFHAND) {
            this.inventory.offHandInventory.set(0, stack);
        } else if (slotIn.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
            this.inventory.armorInventory.set(slotIn.getIndex(), stack);
        }
    }

}
