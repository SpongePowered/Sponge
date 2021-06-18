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

import java.util.HashMap;
import java.util.Map;

@ConfigSerializable
public final class PhaseTrackerCategory {

    @Setting
    @Comment("If 'true', the phase tracker will print out when there are too many phases \n"
                                        + "being entered, usually considered as an issue of phase re-entrance and \n"
                                        + "indicates an unexpected issue of tracking phases not to complete. \n"
                                        + "If this is not reported yet, please report to Sponge. If it has been \n"
                                        + "reported, you may disable this.")
    public boolean verbose = true;

    @Setting("verbose-errors")
    @Comment("If 'true', the phase tracker will dump extra information about the current phases \n"
                                               + "when certain non-PhaseTracker related exceptions occur. This is usually not necessary, as the information \n"
                                               + "in the exception itself can normally be used to determine the cause of the issue")
    public boolean verboseErrors = false;

    @Setting("capture-async-spawning-entities")
    @Comment("If set to 'true', when a mod or plugin attempts to spawn an entity \n"
                                                                + "off the main server thread, Sponge will automatically \n"
                                                                + "capture said entity to spawn it properly on the main \n"
                                                                + "server thread. The catch to this is that some mods are \n"
                                                                + "not considering the consequences of spawning an entity \n"
                                                                + "off the server thread, and are unaware of potential race \n"
                                                                + "conditions they may cause. If this is set to false, \n"
                                                                + "Sponge will politely ignore the entity being spawned, \n"
                                                                + "and emit a warning about said spawn anyways.")
    public boolean captureAsyncSpawningEntities = true;

    @Setting("capture-async-commands")
    @Comment("If set to 'true', when a mod or plugin attempts to submit a command\n"
                                                             + "asynchronously, Sponge will automatically capture said command\n"
                                                             + "and submit it for processing on the server thread. The catch to\n"
                                                             + "this is that some mods are performing these commands in vanilla\n"
                                                             + "without considering the possible consequences of such commands\n"
                                                             + "affecting any thread-unsafe parts of Minecraft, such as worlds,\n"
                                                             + "block edits, entity spawns, etc. If this is set to false, Sponge\n"
                                                             + "will politely ignore the command being executed, and emit a warning\n"
                                                             + "about said command anyways.")
    public boolean captureAsyncCommands = true;

    @Setting("generate-stack-trace-per-phase")
    @Comment("If 'true', more thorough debugging for PhaseStates \n"
                                                              + "such that a StackTrace is created every time a PhaseState \n"
                                                              + "switches, allowing for more fine grained troubleshooting \n"
                                                              + "in the cases of runaway phase states. Note that this is \n"
                                                              + "not extremely performant and may have some associated costs \n"
                                                              + "with generating the stack traces constantly.")
    public boolean generateStackTracePerPhase = false;

    @Setting("maximum-printed-runaway-counts")
    @Comment("If verbose is not enabled, this restricts the amount of \n"
                                                               + "runaway phase state printouts, usually happens on a server \n"
                                                               + "where a PhaseState is not completing. Although rare, it should \n"
                                                               + "never happen, but when it does, sometimes it can continuously print \n"
                                                               + "more and more. This attempts to placate that while a fix can be worked on \n"
                                                               + "to resolve the runaway. If verbose is enabled, they will always print.")
    public int maximumPrintedRunawayCounts = 3;

    @Setting("max-block-processing-depth")
    @Comment("The maximum number of times to recursively process transactions in a single phase.\n"
                                                           + "Some mods may interact badly with Sponge's block capturing system, causing Sponge to\n"
                                                           + "end up capturing block transactions every time it tries to process an existing batch.\n"
                                                            + "Due to the recursive nature of the depth-first processing that Sponge uses to handle block transactions,\n"
                                                            + "this can result in a stack overflow, which causes us to lose all information about the original cause of the issue.\n"
                                                            + "To prevent a stack overflow, Sponge tracks the current processing depth, and aborts processing when it exceeds\n"
                                                            + "this threshold.\n"
                                                            + "The default value should almost always work properly -  it's unlikely you'll ever have to change it.")
    public int maxBlockProcessingDepth = 1000;

    @Setting("report-null-source-blocks-on-neighbor-notifications")
    @Comment("If true, when a mod attempts to perform a neighbor notification\n"
             + "on a block, some mods do not know to perform a 'null' check\n"
             + "on the source block of their TileEntity. This usually goes by\n"
             + "unnoticed by other mods, because they may perform '==' instance\n"
             + "equality checks instead of calling methods on the potentially\n"
             + "null Block, but Sponge uses the block to build information to\n"
             + "help tracking. This has caused issues in the past. Generally,\n"
             + "this can be useful for leaving \"true\" so a proper report is\n"
             + "generated once for your server, and can be reported to the\n"
             + "offending mod author.\n"
             + "This is 'false' by default in SpongeVanilla.\n"
             + "Review the following links for more info:\n"
             + " https://gist.github.com/gabizou/ad570dc09dfed259cac9d74284e78e8b\n"
             + " https://github.com/SpongePowered/SpongeForge/issues/2787\n"
    )
    public boolean reportNullSourceBlocksOnNeighborNotifications = this.isVanilla();

    @Setting("auto-fix-null-source-block-providing-block-entities")
    @Comment("A mapping that is semi-auto-populating for TileEntities whose types\n"
             + "are found to be providing \"null\" Block sources as neighbor notifications\n"
             + "that end up causing crashes or spam reports. If the value is set to \n"
             + "\"true\", then a \"workaround\" will be attempted. If not, the \n"
             + "\ncurrent BlockState at the target source will be queried from the world.\n"
             + "This map having a specific\n"
             + "entry of a TileEntity will prevent a log or warning come up to any logs\n"
             + "when that \"null\" arises, and Sponge will self-rectify the TileEntity\n"
             + "by calling the method \"getBlockType()\". It is advised that if the mod\n"
             + "id in question is coming up, that the mod author is notified about the\n"
             + "error-prone usage of the field \"blockType\". You can refer them to\n"
             + "the following links for the issue:\n"
             + " https://gist.github.com/gabizou/ad570dc09dfed259cac9d74284e78e8b\n"
             + " https://github.com/SpongePowered/SpongeForge/issues/2787\n"
             + "Also, please provide them with these links for the example PR to\n"
             + "fix the issue itself, as the fix is very simple:\n"
             + "https://github.com/TehNut/Soul-Shards-Respawn/pull/24\n"
             + "https://github.com/Epoxide-Software/Enchanting-Plus/pull/135\n")
    public final Map<String, Boolean> autoFixNullSourceBlockProvidingBlockEntities = new HashMap<>();

    private boolean isVanilla() {
        return SpongeConfigs.getPluginEnvironment().blackboard().get(SpongeConfigs.IS_VANILLA_PLATFORM).orElse(true);
    }
}
