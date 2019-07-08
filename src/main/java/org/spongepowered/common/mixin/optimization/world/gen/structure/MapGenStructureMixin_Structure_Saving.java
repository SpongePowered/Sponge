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
package org.spongepowered.common.mixin.optimization.world.gen.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.MapGenStructureData;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.gen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.config.category.StructureModCategory;
import org.spongepowered.common.config.category.StructureSaveCategory;

import java.util.Locale;

import javax.annotation.Nullable;

@Mixin(MapGenStructure.class)
public abstract class MapGenStructureMixin_Structure_Saving extends MapGenBase {

    @Shadow @Nullable private MapGenStructureData structureData;
    @Shadow protected Long2ObjectMap<StructureStart> structureMap;
    @Shadow public abstract String getStructureName();

    private boolean structureSaving$canSaveStructures = true;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void structureSaving$InitializeState(final CallbackInfo ci) {
        final StructureSaveCategory structureSaveCategory = SpongeImpl.getGlobalConfigAdapter().getConfig().getOptimizations().getStructureSaveCategory();
        if (structureSaveCategory.isEnabled()) {
            final String modId = SpongeImplHooks.getModIdFromClass(this.getClass());
            final String structureName = this.getStructureName().toLowerCase(Locale.ENGLISH);
            StructureModCategory structureMod = structureSaveCategory.getModList().get(modId);
            if (structureMod == null) {
                if (structureSaveCategory.autoPopulateData()) {
                    structureMod = new StructureModCategory(modId);
                    structureSaveCategory.getModList().put(modId, structureMod);
                    final Boolean preDefined = structureMod.getStructureList().putIfAbsent(structureName, true);
                    if (preDefined != null) {
                        this.structureSaving$canSaveStructures = preDefined;
                    }
                    SpongeImpl.getGlobalConfigAdapter().save();
                }
                return;
            }
            if (!structureMod.isEnabled()) {
                this.structureSaving$canSaveStructures = false;
                if (structureSaveCategory.autoPopulateData()) {
                    structureMod.getStructureList().putIfAbsent(structureName, false);
                }
                SpongeImpl.getGlobalConfigAdapter().save();
                return;
            }
            final Boolean canSave = structureMod.getStructureList().get(structureName);
            if (canSave != null) {
                this.structureSaving$canSaveStructures = canSave;
            } else if (structureSaveCategory.autoPopulateData()) {
                structureMod.getStructureList().put(structureName, true);
                SpongeImpl.getGlobalConfigAdapter().save();
            }
        }
    }

    /**
     * @author blood - December 17th, 2016
     *
     * @reason Allows servers to opt-out of saving specific structures such as Mineshafts
     * which causes high CPU usage. An overwrite is used to avoid extra mixins since Forge
     * supports per-world storage.
     */
    @Overwrite
    protected void initializeStructureData(final World worldIn)
    {
        if (this.structureData == null)
        {
            // Sponge start - check if structure is allowed to save
            if (this.structureSaving$canSaveStructures) {
                // use hook since Forge supports per-world map storage
                this.structureData = (MapGenStructureData)SpongeImplHooks.getWorldMapStorage(worldIn).getOrLoadData(MapGenStructureData.class, this.getStructureName());
            }
            else
            {
                this.structureData = new MapGenStructureData(this.getStructureName());
            }
            // Sponge end

            if (this.structureData == null)
            {
                this.structureData = new MapGenStructureData(this.getStructureName());
                worldIn.setData(this.getStructureName(), this.structureData);
            }
            else
            {
                final NBTTagCompound nbttagcompound = this.structureData.getTagCompound();

                for (final String s : nbttagcompound.getKeySet())
                {
                    final NBTBase nbtbase = nbttagcompound.getTag(s);

                    if (nbtbase.getId() == 10)
                    {
                        final NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbtbase;

                        if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ"))
                        {
                            final int i = nbttagcompound1.getInteger("ChunkX");
                            final int j = nbttagcompound1.getInteger("ChunkZ");
                            final StructureStart structurestart = MapGenStructureIO.getStructureStart(nbttagcompound1, worldIn);

                            if (structurestart != null)
                            {
                                this.structureMap.put(ChunkPos.asLong(i, j), structurestart);
                            }
                        }
                    }
                }
            }
        }
    }
}