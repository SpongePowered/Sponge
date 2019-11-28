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
import net.minecraft.world.gen.feature.Structure;
import net.minecraft.world.gen.feature.StructureIO;
import net.minecraft.world.gen.feature.StructureSavedData;
import net.minecraft.world.gen.feature.StructureStart;
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

@Mixin(Structure.class)
public abstract class MapGenStructureMixin_Structure_Saving extends MapGenBase {

    @Shadow @Nullable private StructureSavedData structureData;
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
                this.structureData = (StructureSavedData)SpongeImplHooks.getWorldMapStorage(worldIn).func_75742_a(StructureSavedData.class, this.getStructureName());
            }
            else
            {
                this.structureData = new StructureSavedData(this.getStructureName());
            }
            // Sponge end

            if (this.structureData == null)
            {
                this.structureData = new StructureSavedData(this.getStructureName());
                worldIn.func_72823_a(this.getStructureName(), this.structureData);
            }
            else
            {
                final NBTTagCompound nbttagcompound = this.structureData.func_143041_a();

                for (final String s : nbttagcompound.func_150296_c())
                {
                    final NBTBase nbtbase = nbttagcompound.func_74781_a(s);

                    if (nbtbase.func_74732_a() == 10)
                    {
                        final NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbtbase;

                        if (nbttagcompound1.func_74764_b("ChunkX") && nbttagcompound1.func_74764_b("ChunkZ"))
                        {
                            final int i = nbttagcompound1.func_74762_e("ChunkX");
                            final int j = nbttagcompound1.func_74762_e("ChunkZ");
                            final StructureStart structurestart = StructureIO.func_143035_a(nbttagcompound1, worldIn);

                            if (structurestart != null)
                            {
                                this.structureMap.put(ChunkPos.func_77272_a(i, j), structurestart);
                            }
                        }
                    }
                }
            }
        }
    }
}