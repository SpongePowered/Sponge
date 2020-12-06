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

    @Setting(value = "drops-pre-merge", comment = ""
            + "If 'true', block item drops are pre-processed to avoid\n"
            + "having to spawn extra entities that will be merged post spawning.\n"
            + "Usually, Sponge is smart enough to determine when to attempt an item pre-merge and when not to,\n"
            + "however, in certain cases, some mods rely on items not being pre-merged and actually spawned,\n"
            + "in which case, the items will flow right through without being merged.")
    private boolean preItemDropMerge = false;

    @Setting(value = "cache-tameable-owners", comment = ""
            + "Caches tameable entities owners to avoid constant lookups against data watchers.\n"
            + "If mods cause issues, disable this.")
    private boolean cacheTameableOwners = true;

    @Setting(value = "structure-saving", comment = ""
            + "Handles structures that are saved to disk. Certain structures can take up large amounts\n"
            + "of disk space for very large maps and the data for these structures is only needed while the\n"
            + "world around them is generating. Disabling saving of these structures can save disk space and\n"
            + "time during saves if your world is already fully generated.\n"
            + "Warning: disabling structure saving will break the vanilla locate command.")
    private StructureSaveCategory structureSaveCategory = new StructureSaveCategory();

    @Setting(value = "async-lighting", comment = "Runs lighting updates asynchronously.")
    private AsyncLightingCategory asyncLightingCategory = new AsyncLightingCategory();

    @Setting(value = "eigen-redstone",
            comment = "Uses theosib's redstone algorithms to completely overhaul the way redstone works.")
    private EigenRedstoneCategory eigenRedstonCategory = new EigenRedstoneCategory();

    @Setting(value = "panda-redstone", comment = ""
            + "If 'true', uses Panda4494's redstone implementation which improves performance.\n"
            + "See https://bugs.mojang.com/browse/MC-11193 for more information.\n"
            + "Note: This optimization has a few issues which are explained in the bug report.\n"
            + "We strongly recommend using eigen redstone over this implementation as this will\n"
            + "be removed in a future release.")
    private boolean pandaRedstone = false;

    @Setting(value = "enchantment-helper-leak-fix", comment = ""
            + "If 'true', provides a fix for possible leaks through\n"
            + "Minecraft's enchantment helper code that can leak\n"
            + "entity and world references without much interaction\n"
            + "Forge native (so when running SpongeForge implementation)\n"
            + "has a similar patch, but Sponge's patch works a little harder\n"
            + "at it, but Vanilla (SpongeVanilla implementation) does NOT\n"
            + "have any of the patch, leading to the recommendation that this\n"
            + "patch is enabled \"for sure\" when using SpongeVanilla implementation.\n"
            + "See https://bugs.mojang.com/browse/MC-128547 for more information.\n")
    private boolean enchantmentLeak = true;

    @Setting(value = "faster-thread-checks", comment = ""
            + "If 'true', allows for Sponge to make better assumptinos on single threaded\n"
            + "operations with relation to various checks for server threaded operations.\n"
            + "This is default to true due to Sponge being able to precisely inject when\n"
            + "the server thread is available. This should make an already fast operation\n"
            + "much faster for better thread checks to ensure stability of sponge's systems.")
    private boolean fasterThreadChecks = true;

    @Setting(value = "map-optimization", comment = ""
            + "If 'true', re-writes the incredibly inefficient Vanilla Map code.\n"
            + "This yields enormous performance enhancements when using many maps,\n"
            + "but has a tiny chance of breaking mods that invasively modify Vanilla.\n"
            + "It is strongly recommended to keep this on, unless explicitly advised otherwise by a Sponge developer")
    private boolean mapOptimization = true;

    @Setting(value = "optimize-hoppers", comment = ""
            + "Based on Aikar's optimizations of Hoppers, setting this to 'true'\n"
            + "will allow for hoppers to save performing server -> client updates when transferring items.\n"
            + "Because hoppers can transfer items multiple times per tick, these updates can get costly on the server,\n"
            + "with little to no benefit to the client. Because of the nature of the change,\n"
            + "the default will be 'false' due to the inability to pre-emptively\n"
            + "foretell whether mod compatibility will fail with these changes or not.\n"
            + "Refer to: https://github.com/PaperMC/Paper/blob/8175ec916f31dcd130fe0884fe46bdc187d829aa/Spigot-Server-Patches/0269-Optimize-Hoppers.patch\n"
            + "for more details.")
    private boolean optimizeHoppers = false;

    @Setting(value = "use-active-chunks-for-collisions", comment = ""
            + "Vanilla performs a lot of \"is area loaded\" checks during entity collision calculations with blocks,\n"
            + "and because these calculations require fetching the chunks to see if they are loaded,\n"
            + "before getting the block states from those chunks, there can be some small performance\n"
            + "increase by checking the entity's owned active chunk it may currently reside in.\n"
            + "Essentially, instead of asking the world if those chunks are loaded, the entity\n"
            + "would know whether it's chunks are loaded and that neighbor's chunks are loaded.")
    private boolean useActiveChunkForCollisions = false;

    @Setting(value = "disable-failing-deserialization-log-spam", comment = ""
            + "Occasionally, some built in advancements, recipes, etc. can fail to deserialize properly\n"
            + "which ends up potentially spamming the server log and the original provider of the failing content\n"
            + "is not able to fix them. This provides an option to suppress the exceptions printing out in the log.")
    private boolean disableFailingAdvancementDeserialization = true;

    @Setting(value = "disable-pathfinding-chunk-loads", comment = "In vanilla, pathfinding may result in loading chunks.\n" +
                    "You can disable that here, which may result in a\n" +
                    "performance improvement. This may not work well\n" +
                    "with mods."
    )
    private boolean disablePathFindingChunkLoads = false;

    @Setting(value = "disable-raytracing-chunk-loads", comment = "In vanilla, ray tracing may result in loading chunks.\n" +
            "You can disable that here, which may result in a\n" +
            "performance improvement. This may not work well\n" +
            "with mods."
    )
    private boolean disableRayTracingChunkLoads = false;

    public OptimizationCategory() {
        try {
            // Enabled by default on SpongeVanilla, disabled by default on SpongeForge.
            // Because of how early this constructor gets called, we can't use
            // SpongeImplHooks or even Game
            this.preItemDropMerge = Launch.classLoader.getClassBytes("net.minecraftforge.common.ForgeVersion") == null;
        } catch (IOException | NoClassDefFoundError e) {
            e.printStackTrace();
        }
    }

    public StructureSaveCategory getStructureSaveCategory() {
        return this.structureSaveCategory;
    }

    public boolean useStructureSave() {
        return this.structureSaveCategory.isEnabled();
    }

    public boolean useMapOptimization() {
        return this.mapOptimization;
    }

    /**
     * This defines whether items can be pre-merged as item stacks, prior to
     * spawning an entity. This has the ramification that some items are simply
     * "dropped" and some other items during particular contexts, say when a mod is
     * performing drops of their own, cannot be pre-merged as the item entity NEEDS
     * to be created for them. In most cases, this is perfectly fine to perform in
     * vanilla, but in forge mod environments, it is highly incompatible with a
     * majority of more "advanced" or "complex" mods.
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

    public EigenRedstoneCategory getEigenRedstoneCategory() {
        return this.eigenRedstonCategory;
    }

    public boolean useEigenRedstone() {
        return this.eigenRedstonCategory.isEnabled();
    }

    public boolean usePandaRedstone() {
        return this.pandaRedstone;
    }

    public boolean useEnchantmentHelperFix() {
        return this.enchantmentLeak;
    }

    public boolean useFastThreadChecks() {
        return this.fasterThreadChecks;
    }

    public void setPandaRedstone(boolean pandaRedstone) {
        this.pandaRedstone = pandaRedstone;
    }

    public boolean isOptimizeHoppers() {
        return this.optimizeHoppers;
    }

    public boolean isUseActiveChunkForCollisions() {
        return this.useActiveChunkForCollisions;
    }

    public boolean disableFailingAdvancementDeserialization() {
        return this.disableFailingAdvancementDeserialization;
    }

    public boolean disablePathFindingChunkLoads() {
        return this.disablePathFindingChunkLoads;
    }

    public boolean isDisableRayTracingChunkLoads() {
        return this.disableRayTracingChunkLoads;
    }

}
