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

@Mixin(MapGenStructure.class)
public abstract class MixinMapGenStructure_Structure_Saving extends MapGenBase {

    private String modId = "";
    protected boolean canSaveStructures = true;

    @Shadow private MapGenStructureData structureData;
    @Shadow protected Long2ObjectMap<StructureStart> structureMap;
    @Shadow public abstract String getStructureName();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void onConstruction(CallbackInfo ci) {
        this.modId = SpongeImplHooks.getModIdFromClass(this.getClass());
        String structureName = this.getStructureName().toLowerCase();
        StructureSaveCategory structureSaveCategory = SpongeImpl.getGlobalConfig().getConfig().getOptimizations().getStructureSaveCategory();
        StructureModCategory structureMod = structureSaveCategory.getModList().get(this.modId);
        if (structureMod == null) {
            if (structureSaveCategory.autoPopulateData()) {
                structureMod = new StructureModCategory();
                structureSaveCategory.getModList().put(this.modId, structureMod);
            }
        } else {
            Boolean canSave = structureMod.getStructureList().get(structureName);
            if (canSave != null) {
                this.canSaveStructures = canSave;
            } else if (structureSaveCategory.autoPopulateData()) {
                structureMod.getStructureList().put(structureName, true);
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
    protected void initializeStructureData(World worldIn)
    {
        if (this.structureData == null)
        {
            // Sponge start - check if structure is allowed to save
            if (this.canSaveStructures) {
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
                NBTTagCompound nbttagcompound = this.structureData.getTagCompound();

                for (String s : nbttagcompound.getKeySet())
                {
                    NBTBase nbtbase = nbttagcompound.getTag(s);

                    if (nbtbase.getId() == 10)
                    {
                        NBTTagCompound nbttagcompound1 = (NBTTagCompound)nbtbase;

                        if (nbttagcompound1.hasKey("ChunkX") && nbttagcompound1.hasKey("ChunkZ"))
                        {
                            int i = nbttagcompound1.getInteger("ChunkX");
                            int j = nbttagcompound1.getInteger("ChunkZ");
                            StructureStart structurestart = MapGenStructureIO.getStructureStart(nbttagcompound1, worldIn);

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