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
package org.spongepowered.common.applaunch.config.common;

import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;

@ConfigSerializable
public class OptimizationCategory {

    @Setting("drops-pre-merge")
    @Comment("If 'true', block item drops are pre-processed to avoid \n"
              + "having to spawn extra entities that will be merged post spawning. \n"
              + "Usually, Sponge is smart enough to determine when to attempt an item pre-merge \n"
              + "and when not to, however, in certain cases, some mods rely on items not being \n"
              + "pre-merged and actually spawned, in which case, the items will flow right through \n"
              + "without being merged.")
    private boolean preItemDropMerge = false;

    @Setting("cache-tameable-owners")
    @Comment("Caches tameable entities owners to avoid constant lookups against data watchers. If mods \n"
             + "cause issues, disable this.")
    private boolean cacheTameableOwners = true;

    @Setting("structure-saving")
    @Comment("Handles structures that are saved to disk. Certain structures can take up large amounts \n"
             + "of disk space for very large maps and the data for these structures is only needed while the \n"
             + "world around them is generating. Disabling saving of these structures can save disk space and \n"
             + "time during saves if your world is already fully generated. \n"
             + "Warning: disabling structure saving will break the vanilla locate command.")
    private StructureSaveCategory structureSaving = new StructureSaveCategory();

    @Setting("eigen-redstone")
    @Comment("Uses theosib's redstone algorithms to completely overhaul the way redstone works.")
    private EigenRedstoneCategory eigenRedstone = new EigenRedstoneCategory();

    @Setting("faster-thread-checks")
    @Comment("If 'true', allows for Sponge to make better assumptions on single threaded\n"
               + "operations with relation to various checks for server threaded operations.\n"
               + "This is default to true due to Sponge being able to precisely inject when\n"
               + "the server thread is available. This should make an already fast operation\n"
               + "much faster for better thread checks to ensure stability of sponge's systems.")
    private boolean fasterThreadChecks = true;

    @Setting("map-optimization")
    @Comment("If 'true', re-writes the incredibly inefficient Vanilla Map code.\n"
            + "This yields enormous performance enhancements when using many maps, but has a tiny chance of breaking mods that invasively modify Vanilla."
            + "It is strongly reccomended to keep this on, unless explicitly advised otherwise by a Sponge developer")
    private boolean mapOptimization = true;

    @Setting("optimize-hoppers")
    @Comment("Based on Aikar's optimizationo of Hoppers, setting this to 'true'\n"
           + "will allow for hoppers to save performing server -> client updates\n"
           + "when transferring items. Because hoppers can transfer items multiple\n"
           + "times per tick, these updates can get costly on the server, with\n"
           + "little to no benefit to the client. Because of the nature of the\n"
           + "change, the default will be 'false' due to the inability to pre-emptively\n"
           + "foretell whether mod compatibility will fail with these changes or not.\n"
           + "Refer to: https://github.com/PaperMC/Paper/blob/8175ec916f31dcd130fe0884fe46bdc187d829aa/Spigot-Server-Patches/0269-Optimize-Hoppers.patch\n"
           + "for more details.")
    private boolean optimizeHoppers = false;

    @Setting("use-active-chunks-for-collisions")
    @Comment("Vanilla performs a lot of is area loaded checks during\n"
               + "entity collision calculations with blocks, and because\n"
               + "these calculations require fetching the chunks to see\n"
               + "if they are loaded, before getting the block states\n"
               + "from those chunks, there can be some small performance\n"
               + "increase by checking the entity's owned active chunk\n"
               + "it may currently reside in. Essentially, instead of\n"
               + "asking the world if those chunks are loaded, the entity\n"
               + "would know whether it's chunks are loaded and that neighbor's\n"
               + "chunks are loaded.")
    private boolean useActiveChunkForCollisions = false;

    @Setting("tileentity-ticking-optimization")
    @Comment("Based on Paper's TileEntity Ticking optimization\n"
            + "setting this to 'true' prevents unnecessary ticking in Chests and EnderChests\n"
            + "See https://github.com/PaperMC/Paper/blob/bb4002d82e355f033906fc894cc2320f665ba72d/Spigot-Server-Patches/0022-Optimize-TileEntity-Ticking.patch")
    private boolean optimizeTileEntityTicking = true;

    @Setting("disable-failing-deserialization-log-spam")
    @Comment("Occasionally, some built in advancements, \n" +
            "recipes, etc. can fail to deserialize properly\n" +
            "which ends up potentially spamming the server log\n" +
            "and the original provider of the failing content\n" +
            "is not able to fix. This provides an option to\n" +
            "suppress the exceptions printing out in the log.")
    private boolean disableFailingAdvancementDeserialization = true;

    @Setting("disable-scheduled-updates-for-persistent-leaf-blocks")
    @Comment("Leaf blocks placed by players will normally schedule\n" +
        "updates for themselves after placement, and on neighboring\n" +
        "placement. This optimization is relatively small but effectively\n" +
        "disables scheduling updates and reactive updates to leaves that\n" +
        "are `persistent`. Does not drastically improve performance.")
    private boolean disableLeafBlockScheduledUpdatesForPersistedLeaves = true;

    public OptimizationCategory() {
        // Enabled by default on SpongeVanilla, disabled by default on SpongeForge.
        // Because of how early this constructor gets called, we can't use SpongeImplHooks or even Game
        this.preItemDropMerge = SpongeConfigs.getPluginEnvironment().getBlackboard().get(SpongeConfigs.IS_VANILLA_PLATFORM).orElse(true);
    }

    public StructureSaveCategory getStructureSaving() {
        return this.structureSaving;
    }

    public boolean useStructureSave() {
        return this.structureSaving.isEnabled();
    }

    public boolean useMapOptimization() {
        return this.mapOptimization;
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

    public EigenRedstoneCategory getEigenRedstoneCategory() {
        return this.eigenRedstone;
    }

    public boolean useEigenRedstone() {
        return this.eigenRedstone.isEnabled();
    }

    public boolean useFastThreadChecks() {
        return this.fasterThreadChecks;
    }

    public boolean isOptimizeHoppers() {
        return this.optimizeHoppers;
    }

    public boolean isUseActiveChunkForCollisions() {
        return this.useActiveChunkForCollisions;
    }

    public boolean isOptimizedTileEntityTicking() {
        return this.optimizeTileEntityTicking;
    }

    public boolean disableFailingAdvancementDeserialization() {
        return this.disableFailingAdvancementDeserialization;
    }

    public boolean isDisableLeafBlockScheduledUpdatesForPersistedLeaves() {
        return this.disableLeafBlockScheduledUpdatesForPersistedLeaves;
    }
}
