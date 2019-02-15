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

import com.google.common.collect.Maps;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntityComparator;
import net.minecraft.tileentity.TileEntityConduit;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityJukebox;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.tileentity.TileEntityTrappedChest;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.block.tileentity.TileEntityTypes;
import org.spongepowered.api.registry.ExtraClassCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;
import org.spongepowered.common.registry.type.AbstractPrefixAlternateCatalogTypeRegistryModule;

import java.util.Map;

@RegisterCatalog(TileEntityTypes.class)
public final class TileEntityTypeRegistryModule
        extends AbstractPrefixAlternateCatalogTypeRegistryModule<TileEntityType>
        implements ExtraClassCatalogRegistryModule<TileEntityType, TileEntity>,
        SpongeAdditionalCatalogRegistryModule<TileEntityType> {

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
        this.map.put(extraCatalog.getKey(), extraCatalog);
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
        this.doTileEntityRegistration(TileEntityFurnace.class, net.minecraft.tileentity.TileEntityType.FURNACE);
        this.doTileEntityRegistration(TileEntityChest.class, net.minecraft.tileentity.TileEntityType.CHEST);
        this.doTileEntityRegistration(TileEntityTrappedChest.class, net.minecraft.tileentity.TileEntityType.TRAPPED_CHEST);
        this.doTileEntityRegistration(TileEntityEnderChest.class, net.minecraft.tileentity.TileEntityType.ENDER_CHEST);
        this.doTileEntityRegistration(TileEntityJukebox.class, net.minecraft.tileentity.TileEntityType.JUKEBOX);
        this.doTileEntityRegistration(TileEntityDispenser.class, net.minecraft.tileentity.TileEntityType.DISPENSER);
        this.doTileEntityRegistration(TileEntityDropper.class, net.minecraft.tileentity.TileEntityType.DROPPER);
        this.doTileEntityRegistration(TileEntitySign.class, net.minecraft.tileentity.TileEntityType.SIGN);
        this.doTileEntityRegistration(TileEntityMobSpawner.class, net.minecraft.tileentity.TileEntityType.MOB_SPAWNER);
        this.doTileEntityRegistration(TileEntityPiston.class, net.minecraft.tileentity.TileEntityType.PISTON);
        this.doTileEntityRegistration(TileEntityBrewingStand.class, net.minecraft.tileentity.TileEntityType.BREWING_STAND);
        this.doTileEntityRegistration(TileEntityEnchantmentTable.class, net.minecraft.tileentity.TileEntityType.ENCHANTING_TABLE);
        this.doTileEntityRegistration(TileEntityEndPortal.class, net.minecraft.tileentity.TileEntityType.END_PORTAL);
        this.doTileEntityRegistration(TileEntityBeacon.class, net.minecraft.tileentity.TileEntityType.BEACON);
        this.doTileEntityRegistration(TileEntitySkull.class, net.minecraft.tileentity.TileEntityType.SKULL);
        this.doTileEntityRegistration(TileEntityDaylightDetector.class, net.minecraft.tileentity.TileEntityType.DAYLIGHT_DETECTOR);
        this.doTileEntityRegistration(TileEntityHopper.class, net.minecraft.tileentity.TileEntityType.HOPPER);
        this.doTileEntityRegistration(TileEntityComparator.class, net.minecraft.tileentity.TileEntityType.COMPARATOR);
        this.doTileEntityRegistration(TileEntityBanner.class, net.minecraft.tileentity.TileEntityType.BANNER);
        this.doTileEntityRegistration(TileEntityStructure.class, net.minecraft.tileentity.TileEntityType.STRUCTURE_BLOCK);
        this.doTileEntityRegistration(TileEntityEndGateway.class, net.minecraft.tileentity.TileEntityType.END_GATEWAY);
        this.doTileEntityRegistration(TileEntityCommandBlock.class, net.minecraft.tileentity.TileEntityType.COMMAND_BLOCK);
        this.doTileEntityRegistration(TileEntityShulkerBox.class, net.minecraft.tileentity.TileEntityType.SHULKER_BOX);
        this.doTileEntityRegistration(TileEntityBed.class, net.minecraft.tileentity.TileEntityType.BED);
        this.doTileEntityRegistration(TileEntityConduit.class, net.minecraft.tileentity.TileEntityType.CONDUIT);
    }

    @SuppressWarnings("unchecked")
    public <T extends TileEntity> TileEntityType doTileEntityRegistration(Class<T> clazz, net.minecraft.tileentity.TileEntityType<T> type) {
        final ResourceLocation id = IRegistry.BLOCK_ENTITY_TYPE.getKey(type);
        boolean canTick = true;
        try {
            if (ITickable.class.isAssignableFrom(clazz)) {
                String mapping = SpongeImplHooks.isDeobfuscatedEnvironment() ? "update" : "func_73660_a";
                Class<?> declaringClazz = clazz.getMethod(mapping).getDeclaringClass();
                if (declaringClazz.equals(TileEntityChest.class) || declaringClazz.equals(TileEntityEnderChest.class)) {
                    canTick = false;
                }
            }
        } catch (NoSuchMethodException e) {
            // do nothing
        }
        final TileEntityType tileEntityType =
                new SpongeTileEntityType((Class<? extends org.spongepowered.api.block.tileentity.TileEntity>) clazz, (CatalogKey) (Object) id, canTick);

        this.registerAdditionalCatalog(tileEntityType);
        return tileEntityType;
    }

    TileEntityTypeRegistryModule() {
        super("minecraft",
                new String[] {"minecraft:"},
                string -> string.equals("noteblock")
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
