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

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.entity.ArmorEquipable;
import org.spongepowered.api.entity.Tamer;
import org.spongepowered.api.item.inventory.Carrier;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.type.CarriedInventory;

import java.util.Optional;
import java.util.UUID;

/**
 * Implements things that are not implemented by mixins into this class. <p>This
 * class is concrete in order to create instances of User.</p>
 *
 * <p>List of mixins mixing into this class: <ul>
 * <li>MixinSpongeUser</li><li>MixinDataHolder</li><li>MixinSubject</li> </ul>
 *
 * TODO Future note about data: The following data manipulators are always
 * applicable to User: BanData, WhitelistData, JoinData, RespawnLocationData
 */
public class SpongeUser implements ArmorEquipable, Tamer, DataSerializable, Carrier {

    private final GameProfile profile;

    public SpongeUser(GameProfile profile) {
        this.profile = profile;
    }

    public void readFromNbt(NBTTagCompound compound) {
        // TODO Read: inventory, spawn locations, any other data that should be
        // available through data manipulators.
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
    public DataContainer toContainer() {
        // TODO More data
        return new MemoryDataContainer()
                .set(DataQuery.of("UUID"), this.profile.getId())
                .set(DataQuery.of("name"), this.profile.getName());
    }

    @Override
    public boolean canEquip(EquipmentType type) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public boolean canEquip(EquipmentType type, ItemStack equipment) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getEquipped(EquipmentType type) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public boolean equip(EquipmentType type, ItemStack equipment) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public CarriedInventory<? extends Carrier> getInventory() {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getHelmet() {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getChestplate() {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getLeggings() {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getBoots() {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public void setBoots(ItemStack boots) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public Optional<ItemStack> getItemInHand() {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

    @Override
    public void setItemInHand(ItemStack itemInHand) {
        throw new UnsupportedOperationException(); // TODO Inventory API
    }

}
