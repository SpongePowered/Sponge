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
package org.spongepowered.common.config.category;

import net.minecraft.launchwrapper.Launch;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.IOException;

@ConfigSerializable
public class OptimizationCategory extends ConfigCategory {

    @Setting(value = "drops-pre-merge", comment = "If 'true', block item drops are pre-processed to avoid \n"
                                                  + "having to spawn extra entities that will be merged post spawning. \n"
                                                  + "Usually, Sponge is smart enough to determine when to attempt an item pre-merge \n"
                                                  + "and when not to, however, in certain cases, some mods rely on items not being \n"
                                                  + "pre-merged and actually spawned, in which case, the items will flow right through \n"
                                                  + "without being merged.")
    private boolean preItemDropMerge = false;

    @Setting(value = "cache-tameable-owners", comment = "Caches tameable entities owners to avoid constant lookups against data watchers. If mods \n"
                                                      + "cause issues, disable this.")
    private boolean cacheTameableOwners = true;

    @Setting(value = "structure-saving", comment = "Handles structures that are saved to disk. Certain structures can take up large amounts \n"
                                                 + "of disk space for very large maps and the data for these structures is only needed while the \n"
                                                 + "world around them is generating. Disabling saving of these structures can save disk space and \n"
                                                 + "time during saves if your world is already fully generated. \n"
                                                 + "Warning: disabling structure saving will break the vanilla locate command.")
    private StructureSaveCategory structureSaveCategory = new StructureSaveCategory();

    @Setting(value = "async-lighting", comment = "Runs lighting updates asynchronously.")
    private AsyncLightingCategory asyncLightingCategory = new AsyncLightingCategory();

    @Setting(value = "panda-redstone", comment = "If 'true', uses Panda4494's redstone implementation which improves performance. \n"
                                               + "See https://bugs.mojang.com/browse/MC-11193 for more information. \n"
                                               + "Note: This optimization has a few issues which are explained in the bug report.")
    private boolean pandaRedstone = false;

    @Setting(value = "enchantment-helper-leak-fix", comment = "If 'true', provides a fix for possible leaks throug\n"
                                                              + "Minecraft's enchantment helper code that can leak\n"
                                                              + "entity and world references without much interaction\n"
                                                              + "Forge fixes this already, but Sponge is ensuring the leak\n"
                                                              + "is fixed in a second iterator."
                                                              + "See https://bugs.mojang.com/browse/MC-128547 for more information.\n"
                                                              + "Note that this should be enabled in SpongeVanilla as Forge is the\n"
                                                              + "only other platform having this fix.")
    private boolean enchantmentLeak = true;

    public OptimizationCategory() {  
        try {
            // Enabled by default on SpongeVanilla, disabled by default on SpongeForge.
            // Because of how early this constructor gets called, we can't use SpongeImplHooks or even Game
            this.preItemDropMerge = Launch.classLoader.getClassBytes("net.minecraftforge.common.ForgeVersion") == null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }  


    public StructureSaveCategory getStructureSaveCategory() {
        return this.structureSaveCategory;
    }

    public boolean useStructureSave() {
        return this.structureSaveCategory.isEnabled();
    }

    /**
     * This defines whether items can be pre-merged as item stacks, prior to spawning an entity. This has the ramification
     * that some items are simply "dropped" and some other items during particular contexts, say when a mod is performing
     * drops of their own, cannot be pre-merged as the item entity NEEDS to be created for them. In most cases, this is
     * perfectly fine to perform in vanilla, but in forge mod environments, it is highly incompatible with a majority of
     * more "advanced" or "complex" mods.
     *
     * @return Whether item pre-merging is enabled
     */
    public boolean doDropsPreMergeItemDrops() {
        return this.preItemDropMerge;
    }

    public boolean useCacheTameableOwners() {
        return this.cacheTameableOwners;
    }

    public AsyncLightingCategory getAsyncLightingCategory() {
        return this.asyncLightingCategory;
    }

    public boolean useAsyncLighting() {
        return this.asyncLightingCategory.isEnabled();
    }

    public boolean usePandaRedstone() {
        return this.pandaRedstone;
    }

    public boolean useEnchantmentHelperFix() {
        return this.enchantmentLeak;
    }
}
