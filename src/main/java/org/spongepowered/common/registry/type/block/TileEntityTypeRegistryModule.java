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
package org.spongepowered.common.registry.type.block;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public final class TileEntityTypeRegistryModule implements ExtraClassCatalogRegistryModule<TileEntityType, TileEntity>,
        SpongeAdditionalCatalogRegistryModule<TileEntityType> {


    private static final Map<String, String> NAME_TO_ID_MAPPING = ImmutableMap.<String, String>builder()
        .put("Cauldron", "brewing_stand")
        .put("Control", "command_block")
        .put("DLDetector", "daylight_detector")
        .put("Trap", "dispenser")
        .put("EnchantTable", "enchantment_table")
        .put("EnderChest", "ender_chest")
        .put("Airportal", "end_portal")
        .put("FlowerPot", "flower_pot")
        .put("RecordPlayer", "jukebox")
        .put("MobSpawner", "mob_spawner")
        .put("Music", "note")
        .put("EndGateway", "end_gateway")
        .build();

    public static TileEntityTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    public final Map<Class<? extends TileEntity>, TileEntityType> tileClassToTypeMappings = Maps.newHashMap();

    @RegisterCatalog(TileEntityTypes.class)
    public final Map<String, TileEntityType> tileEntityTypeMappings = Maps.newHashMap();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerAdditionalCatalog(TileEntityType extraCatalog) {
        this.tileClassToTypeMappings.put((Class<? extends TileEntity>) extraCatalog.getTileEntityType(), extraCatalog);
        this.tileEntityTypeMappings.put(extraCatalog.getId().toLowerCase(), extraCatalog);
    }

    @Override
    public boolean hasRegistrationFor(Class<? extends TileEntity> mappedClass) {
        return this.tileClassToTypeMappings.containsKey(mappedClass);
    }

    @Override
    public TileEntityType getForClass(Class<? extends TileEntity> clazz) {
        return this.tileClassToTypeMappings.get(clazz);
    }

    @Override
    public Optional<TileEntityType> getById(String id) {
        return Optional.ofNullable(this.tileEntityTypeMappings.get(checkNotNull(id).toLowerCase()));
    }

    @Override
    public Collection<TileEntityType> getAll() {
        return ImmutableList.copyOf(this.tileEntityTypeMappings.values());
    }

    @Override
    public void registerDefaults() {
        try {
            // We just need to class load TileEntity so that it registers,
            // otherwise this is delayed until either pre-init or loading.
            Class<?> clazz = Class.forName("net.minecraft.tileentity.TileEntity");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getIdForName(String name) {
        final String id = NAME_TO_ID_MAPPING.get(name);
        return id == null ? name : id;
    }

    private TileEntityTypeRegistryModule() { }

    private static final class Holder {
        private static final TileEntityTypeRegistryModule INSTANCE = new TileEntityTypeRegistryModule();
    }
}
