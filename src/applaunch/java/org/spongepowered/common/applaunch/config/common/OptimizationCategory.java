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

import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;

@ConfigSerializable
public final class OptimizationCategory {

    @Setting("drops-pre-merge")
    @Comment("If 'true', block item drops are pre-processed to avoid \n"
              + "having to spawn extra entities that will be merged post spawning. \n"
              + "Usually, Sponge is smart enough to determine when to attempt an item pre-merge \n"
              + "and when not to, however, in certain cases, some mods rely on items not being \n"
              + "pre-merged and actually spawned, in which case, the items will flow right through \n"
              + "without being merged.")
    public boolean dropsPreMerge;

    @Setting("cache-tameable-owners")
    @Comment("Caches tameable entities owners to avoid constant lookups against data watchers. If mods \n"
             + "cause issues, disable this.")
    public boolean cacheTameableOwners = true;

    @Setting("eigen-redstone")
    @Comment("Uses theosib's redstone algorithms to completely overhaul the way redstone works.")
    public final EigenRedstoneCategory eigenRedstone = new EigenRedstoneCategory();

    @Setting("faster-thread-checks")
    @Comment("If 'true', allows for Sponge to make better assumptions on single threaded\n"
               + "operations with relation to various checks for server threaded operations.\n"
               + "This is default to true due to Sponge being able to precisely inject when\n"
               + "the server thread is available. This should make an already fast operation\n"
               + "much faster for better thread checks to ensure stability of sponge's systems.")
    public boolean fasterThreadChecks = true;

    @Setting("optimize-maps")
    @Comment("If 'true', re-writes the incredibly inefficient Vanilla Map code.\n"
            + "This yields enormous performance enhancements when using many maps, but has a tiny chance of breaking mods that modify Vanilla."
            + "It is strongly recommended to keep this on, unless explicitly advised otherwise by a Sponge developer")
    public boolean optimizeMaps = true;

    @Setting("optimize-hoppers")
    @Comment("Based on Aikar's optimization of Hoppers, setting this to 'true'\n"
           + "will allow for hoppers to save performing server -> client updates\n"
           + "when transferring items. Because hoppers can transfer items multiple\n"
           + "times per tick, these updates can get costly on the server, with\n"
           + "little to no benefit to the client. Because of the nature of the\n"
           + "change, the default will be 'false' due to the inability to pre-emptively\n"
           + "foretell whether mod compatibility will fail with these changes or not.\n"
           + "Refer to: https://github.com/PaperMC/Paper/blob/8175ec916f31dcd130fe0884fe46bdc187d829aa/Spigot-Server-Patches/0269-Optimize-Hoppers.patch\n"
           + "for more details.")
    public boolean optimizeHoppers = false;

    @Setting("optimize-block-entity-ticking")
    @Comment("Based on Paper's TileEntity Ticking optimization\n"
        + "setting this to 'true' prevents unnecessary ticking in Chests and EnderChests\n"
        + "See https://github.com/PaperMC/Paper/blob/bb4002d82e355f033906fc894cc2320f665ba72d/Spigot-Server-Patches/0022-Optimize-TileEntity-Ticking.patch")
    public boolean optimizeBlockEntityTicking = true;

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
    public boolean useActiveChunksForCollisions = false;

    @Setting("disable-failing-deserialization-log-spam")
    @Comment("Occasionally, some built in advancements, \n" +
            "recipes, etc. can fail to deserialize properly\n" +
            "which ends up potentially spamming the server log\n" +
            "and the original provider of the failing content\n" +
            "is not able to fix. This provides an option to\n" +
            "suppress the exceptions printing out in the log.")
    public boolean disableFailingDeserializationLogSpam = true;

    @Setting("disable-scheduled-updates-for-persistent-leaf-blocks")
    @Comment("Leaf blocks placed by players will normally schedule\n" +
        "updates for themselves after placement, and on neighboring\n" +
        "placement. This optimization is relatively small but effectively\n" +
        "disables scheduling updates and reactive updates to leaves that\n" +
        "are `persistent`. Does not drastically improve performance.")
    public boolean disableScheduledUpdatesForPersistentLeafBlocks = true;

    public OptimizationCategory() {
        // Enabled by default on SpongeVanilla, disabled by default on SpongeForge.
        // Because of how early this constructor gets called, we can't use SpongeImplHooks or even Game
        this.dropsPreMerge = AppLaunch.pluginPlatform().vanilla();
    }
}
