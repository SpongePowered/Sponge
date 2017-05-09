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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.ITickable;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.Locale;
import java.util.Map;

@RegisterCatalog(TileEntityTypes.class)
public final class TileEntityTypeRegistryModule extends AbstractPrefixAlternateCatalogTypeRegistryModule<TileEntityType>
        implements ExtraClassCatalogRegistryModule<TileEntityType, TileEntity>, SpongeAdditionalCatalogRegistryModule<TileEntityType> {


    private static final Map<String, String> NAME_TO_ID_MAPPING = ImmutableMap.<String, String>builder()
        .put("enchanting_table", "enchantment_table")
        .put("noteblock", "note")
        .put("EndGateway", "end_gateway")
        .build();

    public static TileEntityTypeRegistryModule getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<Class<? extends TileEntity>, TileEntityType> tileClassToTypeMappings = Maps.newHashMap();

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerAdditionalCatalog(TileEntityType extraCatalog) {
        this.tileClassToTypeMappings.put((Class<? extends TileEntity>) extraCatalog.getTileEntityType(), extraCatalog);
        this.catalogTypeMap.put(extraCatalog.getId().toLowerCase(Locale.ENGLISH), extraCatalog);
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

    @SuppressWarnings("unchecked")
    public void doTileEntityRegistration(Class<?> clazz, String name) {
        final String id = TileEntityTypeRegistryModule.getInstance().getIdForName(name);
        boolean canTick = true;
        try {
            if (ITickable.class.isAssignableFrom(clazz)) {
                String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "update" : "func_73660_a";
                Class<?> declaringClazz = clazz.getMethod(mapping).getDeclaringClass();
                if (declaringClazz.equals(TileEntityChest.class) || declaringClazz.equals(TileEntityEnderChest.class)) {
                    canTick = false;
                }
            }
        } catch (Throwable e) {
            // ignore
        }
        final String modId = SpongeImplHooks.getModIdFromClass(clazz);
        final String tileId = modId + ":" + id;
        final TileEntityType tileEntityType =
                new SpongeTileEntityType((Class<? extends org.spongepowered.api.block.tileentity.TileEntity>) clazz, name, tileId, canTick, modId);

        TileEntityTypeRegistryModule.getInstance().registerAdditionalCatalog(tileEntityType);
    }

    TileEntityTypeRegistryModule() {
        super("minecraft", new String[] {"minecraft:"}, string -> string.equals("noteblock")
                ? "note"
                : string.equals("enchanting_table")
                        ? "enchantment_table"
                        : string.equals("structure_block")
                                ? "structure"
                                : string);
    }

    private static final class Holder {
        static final TileEntityTypeRegistryModule INSTANCE = new TileEntityTypeRegistryModule();
    }
}
