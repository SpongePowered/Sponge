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
package org.spongepowered.common.command;

import static org.spongepowered.api.Platform.Component.IMPLEMENTATION;
import static org.spongepowered.api.command.args.GenericArguments.choices;
import static org.spongepowered.api.command.args.GenericArguments.dimension;
import static org.spongepowered.api.command.args.GenericArguments.firstParsing;
import static org.spongepowered.api.command.args.GenericArguments.flags;
import static org.spongepowered.api.command.args.GenericArguments.literal;
import static org.spongepowered.api.command.args.GenericArguments.optional;
import static org.spongepowered.api.command.args.GenericArguments.optionalWeak;
import static org.spongepowered.api.command.args.GenericArguments.plugin;
import static org.spongepowered.api.command.args.GenericArguments.seq;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.command.args.GenericArguments.world;

import co.aikar.timings.SpongeTimingsFactory;
import co.aikar.timings.Timings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.SpongeApiTranslationHelper;
import org.spongepowered.api.util.StartsWithPredicate;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.type.DimensionConfig;
import org.spongepowered.common.config.type.GeneralConfigBase;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.IMixinMinecraftServer;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.util.SpongeHooks;
import org.spongepowered.common.world.WorldManager;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class SpongeCommandFactory {

    public static final String INDENT = "    ";
    public static final String LONG_INDENT = INDENT + INDENT;
    public static final List<String> CONTAINER_LIST_STATICS = Lists.newArrayList("minecraft", "mcp", "spongeapi", "sponge");

    protected static final Text SEPARATOR_TEXT = Text.of(", ");
    static final Text INDENT_TEXT = Text.of(INDENT);
    static final Text NEWLINE_TEXT = Text.newLine();
    static final Text LIST_ITEM_TEXT = Text.of(TextColors.GRAY, "- ");
    static final Text UNKNOWN = Text.of("UNKNOWN");

    private static final DecimalFormat THREE_DECIMAL_DIGITS_FORMATTER = new DecimalFormat("########0.000");
    private static final Comparator<CommandMapping> COMMAND_COMPARATOR = Comparator.comparing(CommandMapping::getPrimaryAlias);
    private static final Text PAGE_KEY = Text.of("page");
    private static final Text COMMAND_KEY = Text.of("command");
    private static final CommandElement COMMAND_ARGUMENT = new CommandElement(COMMAND_KEY) {

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            String input = args.next();
            Optional<? extends CommandMapping> cmd = SpongeImpl.getGame().getCommandManager().get(input, source);

            if (!cmd.isPresent()) {
                throw args.createError(SpongeApiTranslationHelper.t("No such command: ", input));
            }
            return cmd.orElse(null);
        }

        @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            final String prefix = args.nextIfPresent().orElse("");
            return commandsStr(src).stream()
                    .filter(new StartsWithPredicate(prefix))
                    .collect(ImmutableList.toImmutableList());
        }
    };
    private static final Text NOT_FOUND = Text.of("notFound");

    /**
     * Create a new instance of the Sponge command structure.
     *
     * @return The newly created command
     */
    public static CommandSpec createSpongeCommand() {
        final ChildCommandElementExecutor flagChildren = new ChildCommandElementExecutor(null);
        final ChildCommandElementExecutor nonFlagChildren = new ChildCommandElementExecutor(flagChildren);
        nonFlagChildren.register(createSpongeVersionCommand(), "version");
        nonFlagChildren.register(createSpongeBlockInfoCommand(), "blockInfo");
        nonFlagChildren.register(createSpongeEntityInfoCommand(), "entityInfo");
        nonFlagChildren.register(createSpongeAuditCommand(), "audit");
        nonFlagChildren.register(createSpongeHeapCommand(), "heap");
        nonFlagChildren.register(createSpongePluginsCommand(), "plugins");
        nonFlagChildren.register(createSpongeTimingsCommand(), "timings");
        nonFlagChildren.register(createSpongeWhichCommand(), "which");
        flagChildren.register(createSpongeChunksCommand(), "chunks");
        flagChildren.register(createSpongeConfigCommand(), "config");
        flagChildren.register(createSpongeReloadCommand(), "reload"); // TODO: Should these two be subcommands of config, and what is now config be set?
        flagChildren.register(createSpongeSaveCommand(), "save");
        flagChildren.register(createSpongeTpsCommand(), "tps");

        SpongeImplHooks.registerAdditionalCommands(flagChildren, nonFlagChildren);

        return CommandSpec.builder()
                .description(Text.of("General Sponge command"))
                .extendedDescription(Text.of("commands:\n", // TODO: Automatically generate from child executors (wait for help system on this)
                        INDENT, title("chunks"), LONG_INDENT, "Prints chunk data for a specific dimension or world(s)\n",
                        INDENT, title("conf"), LONG_INDENT, "Configure sponge settings\n",
                        INDENT, title("heap"), LONG_INDENT, "Dump live JVM heap\n",
                        INDENT, title("reload"), LONG_INDENT, "Reloads a global, dimension, or world config\n",
                        INDENT, title("save"), LONG_INDENT, "Saves a global, dimension, or world config\n",
                        INDENT, title("version"), LONG_INDENT, "Prints current Sponge version\n",
                        INDENT, title("audit"), LONG_INDENT, "Audit mixin classes for implementation\n",
                        INDENT, title("plugins"), LONG_INDENT, "List currently installed plugins\n",
                        INDENT, title("which"), LONG_INDENT, "List plugins that own a specific command\n",
                        INDENT, title("tps"), LONG_INDENT, "Provides TPS (ticks per second) data for loaded worlds\n",
                        SpongeImplHooks.getAdditionalCommandDescriptions()))
                .arguments(firstParsing(nonFlagChildren, flags()
                        .flag("-global", "g")
                        .valueFlag(world(Text.of("world")), "-world", "w")
                        .valueFlag(dimension(Text.of("dimension")), "-dimension", "d")
                        .buildWith(flagChildren)))
                .executor(nonFlagChildren)
                .build();
    }

    // TODO: Have some sort of separator between outputs for each world/dimension/global/whatever (that are exactly one line?)
    private abstract static class ConfigUsingExecutor implements CommandExecutor {
        private boolean requireWorldLoaded;

        ConfigUsingExecutor(boolean requireWorldLoaded) {
            this.requireWorldLoaded = requireWorldLoaded;
        }

        @Override
        public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
            int successes = 0;
            if (args.hasAny("global")) {
                src.sendMessage(Text.of("Global: ", processGlobal(SpongeImpl.getGlobalConfig(), src, args)));
                ++successes;
            }
            if (args.hasAny("dimension")) {
                for (DimensionType dimensionType : args.<DimensionType>getAll("dimension")) {
                    src.sendMessage(Text.of("Dimension ", dimensionType.getName(), ": ", processDimension(((IMixinDimensionType) dimensionType).
                            getDimensionConfig(), dimensionType, src, args)));
                    ++successes;
                }
            }
            if (args.hasAny("world")) {
                for (WorldProperties properties : args.<WorldProperties>getAll("world")) {
                    Optional<World> world = SpongeImpl.getGame().getServer().getWorld(properties.getUniqueId());
                    if (!world.isPresent() && this.requireWorldLoaded) {
                        throw new CommandException(Text.of("World ", properties.getWorldName(), " is not loaded, cannot work with it"));
                    }
                    src.sendMessage(Text.of("World ", properties.getWorldName(), ": ", processWorld(((IMixinWorldInfo) properties).getOrCreateWorldConfig(),
                            world.orElse(null), src, args)));
                    ++successes;
                }
            }
            if (successes == 0) {
                throw new CommandException(Text.of("At least one target flag must be specified"));
            }
            return CommandResult.builder().successCount(successes).build(); // TODO: How do we handle results?
        }

        protected Text processGlobal(SpongeConfig<GlobalConfig> config, CommandSource source, CommandContext args)
                throws CommandException {
            return process(config, source, args);
        }

        protected Text processDimension(SpongeConfig<DimensionConfig> config, DimensionType dim, CommandSource source,
                CommandContext args) throws CommandException {
            return process(config, source, args);
        }

        protected Text processWorld(SpongeConfig<WorldConfig> config, World world, CommandSource source,
                CommandContext args) throws CommandException {
            return process(config, source, args);
        }

        protected Text process(SpongeConfig<? extends GeneralConfigBase> config, CommandSource source, CommandContext args) throws CommandException {
            return Text.of("Unimplemented");
        }
    }

    // Flag children

    private static CommandSpec createSpongeChunksCommand() {
        return CommandSpec.builder()
                .description(Text.of("Print chunk information, optionally dump"))
                .arguments(optional(seq(literal(Text.of("dump"), "dump"), optional(literal(Text.of("dump-all"), "all")))))
                .permission("sponge.command.chunks")
                .executor(new ConfigUsingExecutor(true) {
                    @Override
                    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
                        CommandResult res = super.execute(src, args);
                        if (args.hasAny("dump")) {
                            File file = new File(new File(new File("."), "chunk-dumps"),
                                "chunk-info-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(Instant.now()) + "-server.txt");
                            src.sendMessage(Text.of("Writing chunk info to: ", file));
                            ChunkSaveHelper.writeChunks(file, args.hasAny("dump-all"));
                            src.sendMessage(Text.of("Chunk info complete"));
                        }
                        return res;
                    }

                    @Override
                    protected Text processGlobal(SpongeConfig<GlobalConfig> config, CommandSource source, CommandContext args)
                            throws CommandException {
                        for (World world : SpongeImpl.getGame().getServer().getWorlds()) {
                            source.sendMessage(Text.of("World ", Text.of(TextStyles.BOLD, world.getName()),
                                    getChunksInfo(((WorldServer) world))));
                        }
                        return Text.of("Printed chunk info for all worlds ");
                    }

                    @Override
                    protected Text processDimension(SpongeConfig<DimensionConfig> config, DimensionType dim, CommandSource source,
                            CommandContext args)
                            throws CommandException {
                        SpongeImpl.getGame().getServer().getWorlds().stream().filter(world -> world.getDimension().getType().equals(dim))
                            .forEach(world -> source.sendMessage(Text.of("World ", Text.of(TextStyles.BOLD, world.getName()),
                                                                      getChunksInfo(((WorldServer) world)))));
                        return Text.of("Printed chunk info for all worlds in dimension ", dim.getName());
                    }

                    @Override
                    protected Text processWorld(SpongeConfig<WorldConfig> config, World world, CommandSource source, CommandContext args)
                            throws CommandException {
                        return getChunksInfo((WorldServer) world);
                    }

                    protected Text key(Object text) {
                        return Text.of(TextColors.GOLD, text);
                    }

                    protected Text value(Object text) {
                        return Text.of(TextColors.GRAY, text);
                    }

                    protected Text getChunksInfo(WorldServer worldserver) {
                        return Text.of(NEWLINE_TEXT, key("DimensionId: "), value(WorldManager.getDimensionId(worldserver)), NEWLINE_TEXT,
                                key("Loaded chunks: "), value(worldserver.getChunkProvider().getLoadedChunkCount()), NEWLINE_TEXT,
                                key("Active chunks: "), value(worldserver.getChunkProvider().getLoadedChunks().size()), NEWLINE_TEXT,
                                key("Entities: "), value(worldserver.loadedEntityList.size()), NEWLINE_TEXT,
                                key("Tile Entities: "), value(worldserver.loadedTileEntityList.size()), NEWLINE_TEXT,
                                key("Removed Entities:"), value(worldserver.unloadedEntityList.size()), NEWLINE_TEXT,
                                key("Removed Tile Entities: "), value(worldserver.tileEntitiesToBeRemoved), NEWLINE_TEXT
                        );
                    }
                })
                .build();
    }

    private static CommandSpec createSpongeConfigCommand() {
        return CommandSpec.builder()
                .description(Text.of("Inspect the Sponge config"))
                .arguments(seq(string(Text.of("key")), optional(string(Text.of("value")))))
                .permission("sponge.command.config")
                .executor(new ConfigUsingExecutor(false) {
                    @Override
                    protected Text process(SpongeConfig<? extends GeneralConfigBase> config, CommandSource source, CommandContext args) throws CommandException {
                        final Optional<String> key = args.getOne("key");
                        final Optional<String> value = args.getOne("value");
                        if (config.getSetting(key.get()) == null || config.getSetting(key.get()).isVirtual()) {
                            throw new CommandException(Text.of("Key ", Text.builder(key.get()).color(TextColors.GREEN).build(), " is not "
                                    + "valid"));
                        }

                        if (value.isPresent()) { // Set
                            config.updateSetting(key.get(), value.get());

                            return Text.builder().append(Text.of(TextColors.GOLD, key), Text.of(" set to "),
                                    title(value.get())).build();
                        }
                        return Text.builder().append(Text.of(TextColors.GOLD, key), Text.of(" is "),
                                title(String.valueOf(config.getSetting(key.get()).getValue()))).build();
                    }
                })
                .build();
    }

    private static CommandSpec createSpongeReloadCommand() {
        return CommandSpec.builder()
                .description(Text.of("Reload the Sponge game"))
                .permission("sponge.command.reload")
                .executor(new ConfigUsingExecutor(false) {
                    @Override
                    protected Text process(SpongeConfig<? extends GeneralConfigBase> config, CommandSource source, CommandContext args) throws CommandException {
                        config.reload();
                        SpongeHooks.refreshActiveConfigs();
                        return Text.of("Reloaded configuration");
                    }
                })
                .build();
    }

    private static CommandSpec createSpongeSaveCommand() {
        return CommandSpec.builder()
                .description(Text.of("Save the configuration"))
                .permission("sponge.command.save")
                .executor(new ConfigUsingExecutor(false) {
                    @Override
                    protected Text process(SpongeConfig<? extends GeneralConfigBase> config, CommandSource source, CommandContext args) throws CommandException {
                        config.save();
                        return Text.of("Saved");
                    }
                })
                .build();
    }

    // Non-flag children

    private static CommandSpec createSpongeHeapCommand() {
        return CommandSpec.builder()
                .description(Text.of("Generate a dump of the Sponge heap"))
                .permission("sponge.command.heap")
                .executor((src, args) -> {
                    File file = new File(new File(new File("."), "dumps"),
                            "heap-dump-" + DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss").format(LocalDateTime.now()) + "-server.hprof");
                    src.sendMessage(Text.of("Writing JVM heap data to: ", file));
                    SpongeHooks.dumpHeap(file, true);
                    src.sendMessage(Text.of("Heap dump complete"));
                    return CommandResult.success();
                })
                .build();

    }

    private static final Text IMPLEMENTATION_NAME = Text.of(TextColors.YELLOW, TextStyles.BOLD,
            Sponge.getPlatform().getContainer(IMPLEMENTATION).getName());

    private static CommandSpec createSpongeVersionCommand() {
        return CommandSpec.builder()
                .description(Text.of("Display Sponge's current version"))
                .permission("sponge.command.version")
                .executor((src, args) -> {
                    Text.Builder builder = Text.builder().append(IMPLEMENTATION_NAME);

                    for (PluginContainer container : SpongeImpl.getInternalPlugins()) {
                        builder.append(NEWLINE_TEXT, Text.of(TextColors.GRAY, INDENT + container.getName(), ": "), container.getVersion().isPresent
                                () ? Text.of(container.getVersion().get()) : UNKNOWN);
                    }

                    src.sendMessage(builder.build());
                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec createSpongeBlockInfoCommand() {
        return CommandSpec.builder()
                .description(Text.of("Display the tracked information of the Block you are looking at."))
                .permission("sponge.command.blockinfo")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        src.sendMessage(Text.of(TextColors.RED, "Players must execute this command!"));
                        return CommandResult.empty();
                    }
                    final EntityPlayerMP entityPlayerMP = EntityUtil.toNative((Player) src);
                    final RayTraceResult rayTraceResult = EntityUtil.rayTraceFromEntity(entityPlayerMP, 5, 1.0F);
                    if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
                        src.sendMessage(Text.of(TextColors.RED, TextStyles.ITALIC,
                                "Failed to find a block! Please execute the command when looking at a block!"));
                        return CommandResult.empty();
                    }
                    final WorldServer worldServer = (WorldServer) entityPlayerMP.world;
                    final Chunk chunk = worldServer.getChunkFromBlockCoords(rayTraceResult.getBlockPos());
                    final IMixinChunk mixinChunk = (IMixinChunk) chunk;
                    final IBlockState blockState = worldServer.getBlockState(rayTraceResult.getBlockPos());
                    final BlockState spongeState = BlockUtil.fromNative(blockState);
                    src.sendMessage(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "Block Type: ", TextColors.BLUE, TextStyles.RESET, spongeState.getId()));
                    src.sendMessage(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "Block Owner: ", TextColors.BLUE, TextStyles.RESET, mixinChunk.getBlockOwner(rayTraceResult.getBlockPos())));
                    src.sendMessage(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "Block Notifier: ", TextColors.BLUE, TextStyles.RESET, mixinChunk.getBlockNotifier(rayTraceResult.getBlockPos())));
                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec createSpongeEntityInfoCommand() {
        return CommandSpec.builder()
                .description(Text.of("Display the tracked information of the Entity you are looking at."))
                .permission("sponge.command.entityinfo")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        return CommandResult.empty();
                    }
                    final EntityPlayerMP entityPlayerMP = EntityUtil.toNative((Player) src);
                    final RayTraceResult rayTraceResult = EntityUtil.rayTraceFromEntity(entityPlayerMP, 5, 1.0F, true);
                    if (rayTraceResult.typeOfHit != RayTraceResult.Type.ENTITY) {
                        src.sendMessage(Text.of(TextColors.RED, TextStyles.ITALIC,
                                "Failed to find an entity! Please execute the command when looking at an entity!"));
                        return CommandResult.empty();
                    }
                    final Entity entityHit = rayTraceResult.entityHit;
                    final IMixinEntity mixinEntity = EntityUtil.toMixin(entityHit);
                    final org.spongepowered.api.entity.Entity spongeEntity = EntityUtil.fromNative(entityHit);
                    final Text.Builder builder = Text.builder();
                    builder.append(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "EntityType: "))
                            .append(Text.of(TextColors.BLUE, TextStyles.RESET, spongeEntity.getType().getId()));
                    src.sendMessage(builder.build());
                    final Optional<User> owner = mixinEntity.getCreatorUser();
                    final Optional<User> notifier = mixinEntity.getNotifierUser();
                    src.sendMessage(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "Owner: ", TextColors.BLUE, TextStyles.RESET,
                            owner));
                    src.sendMessage(Text.of(TextColors.DARK_GREEN, TextStyles.BOLD, "Notifier: ", TextColors.BLUE, TextStyles.RESET,
                            notifier));
                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec createSpongeAuditCommand() {
        return CommandSpec.builder()
                .description(Text.of("Audit Mixin classes for implementation"))
                .permission("sponge.command.audit")
                .executor((src, args) -> {
                    MixinEnvironment.getCurrentEnvironment().audit();
                    return CommandResult.empty();
                })
                .build();
    }

    public static Text title(String title) {
        return Text.of(TextColors.GREEN, title);
    }

    public static Text hl(String toHighlight) {
        return Text.of(TextColors.DARK_GREEN, toHighlight);
    }

    private static CommandSpec createSpongePluginsCommand() {
        return CommandSpec.builder()
                .description(Text.of("List currently installed plugins"))
                .permission("sponge.command.plugins")
                .arguments(optionalWeak(literal(Text.of("reload"), "reload")), optional(plugin(Text.of("plugin"))))
                .executor((src, args) -> {
                    if (args.hasAny("reload") && src.hasPermission("sponge.command.plugins.reload")) {
                        src.sendMessage(Text.of("Sending reload event to all plugins. Please wait."));
                        Sponge.getCauseStackManager().pushCause(src);
                        SpongeImpl.postEvent(SpongeEventFactory.createGameReloadEvent(Sponge.getCauseStackManager().getCurrentCause()));
                        Sponge.getCauseStackManager().popCause();
                        src.sendMessage(Text.of("Reload complete!"));
                    } else if (args.hasAny("plugin")) {
                        sendContainerMeta(src, args, "plugin");
                    } else {
                        final Collection<PluginContainer> containers = SpongeImpl.getGame().getPluginManager().getPlugins();
                        final List<PluginContainer> sortedContainers = new ArrayList<>();

                        // Add static listings first
                        CONTAINER_LIST_STATICS.forEach(containerId -> containers.stream()
                                .filter(container -> container.getId().equalsIgnoreCase(containerId))
                                .findFirst()
                                .ifPresent(sortedContainers::add));

                        containers.stream()
                                .filter(SpongeImplHooks.getPluginFilterPredicate())
                                .sorted(Comparator.comparing(PluginContainer::getName))
                                .forEachOrdered(sortedContainers::add);

                        if (src instanceof Player) {
                            final List<Text> containerList = new ArrayList<>();

                            final PaginationList.Builder builder = PaginationList.builder();
                            builder.title(Text.of(TextColors.YELLOW, "Plugins", TextColors.WHITE, " (", sortedContainers.size(), ")"))
                                    .padding(Text.of(TextColors.DARK_GREEN, "="));

                            for (PluginContainer container : sortedContainers) {
                                final Text.Builder containerBuilder = Text.builder()
                                        .append(Text.of(TextColors.RESET, " - ", TextColors.GREEN, container.getName()))
                                        .onClick(TextActions.runCommand("/sponge:sponge plugins " + container.getId()))
                                        .onHover(TextActions.showText(Text.of(
                                                TextColors.RESET,
                                                "ID: ", container.getId(), Text.newLine(),
                                                "Version: ", container.getVersion().orElse("Unknown"))));

                                containerList.add(containerBuilder.build());
                            }

                            builder.contents(containerList).build().sendTo(src);
                        } else {
                            final Text.Builder builder = Text.builder();
                            builder.append(Text.of(TextColors.YELLOW, "Plugins", TextColors.WHITE, " (", sortedContainers.size(), "): "));

                            boolean first = true;
                            for (PluginContainer container : sortedContainers) {
                                if (!first) {
                                    builder.append(SEPARATOR_TEXT);
                                }
                                first = false;

                                builder.append(Text.of(TextColors.GREEN, container.getName()));
                            }

                            src.sendMessage(builder.build());
                        }
                    }
                    return CommandResult.success();
                }).build();
    }

    public static void appendPluginMeta(Text.Builder builder, String key, Optional<?> value) {
        if (value.isPresent()) {
            appendPluginMeta(builder, key, value.get());
        }
    }

    public static void appendPluginMeta(Text.Builder builder, String key, Object value) {
        builder.append(NEWLINE_TEXT, INDENT_TEXT, title(key + ": "), Text.of(value));
    }

    public static void sendContainerMeta(CommandSource src, CommandContext args, String argumentName) {
        for (PluginContainer container : args.<PluginContainer>getAll(argumentName)) {
            Text.Builder builder = Text.builder().append(title(container.getName()));
            container.getVersion().ifPresent(version -> builder.append(Text.of((" v" + version))));

            appendPluginMeta(builder, "ID", container.getId());
            appendPluginMeta(builder, "Description", container.getDescription());
            appendPluginMeta(builder, "URL", container.getUrl().map(url -> {
                ClickAction.OpenUrl action = null;
                try {
                    // make the url clickable
                    action = TextActions.openUrl(new URL(url));
                } catch (MalformedURLException e) {
                    // or not
                }
                return Text.builder(url).onClick(action);
            }));
            if (!container.getAuthors().isEmpty()) {
                appendPluginMeta(builder, "Authors", String.join(", ", container.getAuthors()));
            }

            appendPluginMeta(builder, "Main class", container.getInstance().map(instance -> instance.getClass().getCanonicalName()));

            src.sendMessage(builder.build());
        }
    }

    private static CommandCallable createSpongeTimingsCommand() {
        return CommandSpec.builder()
                .permission("sponge.command.timings")
                .description(Text.of("Manages Sponge Timings data to see performance of the server."))
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.reset();
                            src.sendMessage(Text.of("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.generateReport(src);
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            Timings.setTimingsEnabled(true);
                            src.sendMessage(Text.of("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            Timings.setTimingsEnabled(false);
                            src.sendMessage(Text.of("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(true);
                            src.sendMessage(Text.of("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(false);
                            src.sendMessage(Text.of("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .child(CommandSpec.builder()
                        .executor((src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            src.sendMessage(Text.of("Timings cost: " + SpongeTimingsFactory.getCost()));
                            return CommandResult.success();
                        })
                        .build(), "cost")
                .build();
    }

    private static CommandSpec createSpongeWhichCommand() {
        return CommandSpec.builder()
                .permission("sponge.command.which")
                .description(Text.of("List plugins that own a specific command"))
                .arguments(choices(Text.of("command"), () -> Sponge.getCommandManager().getAll().keySet(), Function.identity()))
                .executor((src, args) -> {
                    CommandManager mgr = Sponge.getCommandManager();
                    String commandName = args.<String>getOne("command").get();
                    CommandMapping primary = mgr.get(commandName, src)
                            .orElseThrow(() -> new CommandException(Text.of("Invalid command ", commandName)));
                    Collection<? extends CommandMapping> all = mgr.getAll(commandName);
                    src.sendMessage(Text.of(title("Primary: "),  "Aliases ", hl(primary.getAllAliases().toString()), " owned by ",
                            hl(mgr.getOwner(primary).map(PluginContainer::getName).orElse("unknown"))));
                    if (all.size() > 1 || all.iterator().next() != primary) {
                        src.sendMessage(title("Others:"));
                        all.stream()
                                .filter(map -> !map.equals(primary))
                                .forEach(mapping -> {
                                    src.sendMessage(Text.of(LIST_ITEM_TEXT, "Aliases ", hl(mapping.getAllAliases().toString()), " owned by ",
                                            hl(mgr.getOwner(mapping).map(PluginContainer::getName).orElse("unknown"))));
                                });
                    }

                    return CommandResult.success();
                })
                .build();
    }

    private static CommandSpec createSpongeTpsCommand() {
        return CommandSpec.builder()
                .permission("sponge.command.tps")
                .description(Text.of("Provides TPS (ticks per second) data for loaded worlds."))
                .arguments(optional(world(Text.of("world"))))
                .executor((src, args) -> {
                    if (args.hasAny("world")) {
                        for (WorldProperties properties : args.<WorldProperties>getAll("world")) {
                            final Optional<World> optWorld = Sponge.getServer().getWorld(properties.getWorldName());
                            if (!optWorld.isPresent()) {
                                src.sendMessage(Text.of(properties.getWorldName() + " has no TPS as it is offline!"));
                            } else {
                                printWorldTickTime(src, optWorld.get());
                            }
                        }
                    } else {
                        Sponge.getServer().getWorlds().forEach(world -> printWorldTickTime(src, world));
                    }
                    final double serverMeanTickTime = mean(SpongeImpl.getServer().tickTimeArray) * 1.0e-6d;
                    src.sendMessage(Text.of("Overall TPS: ", TextColors.LIGHT_PURPLE,
                            THREE_DECIMAL_DIGITS_FORMATTER.format(Math.min(1000.0 / (serverMeanTickTime), 20)),
                            TextColors.RESET, ", Mean: ", TextColors.RED, THREE_DECIMAL_DIGITS_FORMATTER.
                                    format(serverMeanTickTime), "ms"));
                    return CommandResult.success();
                })
                .build();
    }

    private static void printWorldTickTime(CommandSource src, World world) {
        final long[] worldTickTimes = ((IMixinMinecraftServer) SpongeImpl.getServer()).
                getWorldTickTimes(((IMixinWorldServer) world).getDimensionId());
        final double worldMeanTickTime = mean(worldTickTimes) * 1.0e-6d;
        final double worldTps = Math.min(1000.0 / worldMeanTickTime, 20);
        src.sendMessage(Text.of("World [", TextColors.DARK_GREEN, world.getName(), TextColors.RESET, "] (DIM",
                ((IMixinWorldServer) world).getDimensionId(), ") TPS: ", TextColors.LIGHT_PURPLE,
                THREE_DECIMAL_DIGITS_FORMATTER.format(worldTps), TextColors.RESET,  ", Mean: ", TextColors.RED,
                THREE_DECIMAL_DIGITS_FORMATTER.format(worldMeanTickTime), "ms"));
    }

    private static Long mean(long[] values) {
        Long mean = 0L;
        if (values.length > 0) {
            for (long value : values) {
                mean += value;
            }

            mean = mean / values.length;
        }

        return mean;
    }

    /**
     * Creates a new instance of the Sponge help command.
     *
     * @return The created help command
     */
    public static CommandSpec createHelpCommand() {
        return CommandSpec
                .builder()
                .permission(CommandPermissions.SPONGE_HELP_PERMISSION)
                .arguments(
                        optional(
                                firstParsing(
                                        GenericArguments.integer(PAGE_KEY),
                                        COMMAND_ARGUMENT,
                                        string(NOT_FOUND)
                                )
                        )
                )
                .description(Text.of("View a list of all commands."))
                .extendedDescription(
                        Text.of("View a list of all commands. Hover over\n" + " a command to view its description. Click\n"
                                + " a command to insert it into your chat bar."))
                .executor((src, args) -> {
                    if(args.getOne(NOT_FOUND).isPresent()){
                        throw new CommandException(Text.of("No such command: ", args.getOne(NOT_FOUND).get()));
                    }

                    final Optional<CommandMapping> command = args.getOne(COMMAND_KEY);
                    Optional<Integer> page = args.getOne(PAGE_KEY);

                    if (command.isPresent()) {
                        CommandCallable callable = command.get().getCallable();
                        Optional<? extends Text> desc = callable.getHelp(src);
                        if (desc.isPresent()) {
                            src.sendMessage(desc.get());
                        } else {
                            src.sendMessage(Text.of("Usage: /", command.get(), callable.getUsage(src)));
                        }
                        return CommandResult.success();
                    }

                    final ImmutableList<Text> contents = ImmutableList.<Text>builder()
                            .add(Text.of(Sponge.getRegistry().getTranslationById("commands.help.footer").get()))
                            .addAll(commands(src).stream().map(input -> createDescription(src, input)).collect(Collectors.toList()))
                            .build();

                    PaginationList.builder()
                            .title(Text.of(TextColors.DARK_GREEN, "Showing Help (/page <page>):"))
                            .padding(Text.of(TextColors.DARK_GREEN, "="))
                            .contents(contents)
                            .build().sendTo(src, page.orElse(1));

                    return CommandResult.success();
                }).build();
    }

    /**
     * Gets a collection of the primary aliases of commands that
     * the source has access to.
     *
     * @param src The command source to permission check
     * @return A collection of primary aliases
     */
    private static Collection<String> commandsStr(CommandSource src) {
        return commands(src).stream().map(CommandMapping::getPrimaryAlias).collect(Collectors.toList());
    }

    /**
     * Gets a tree set of command mappings the command source has access to.
     *
     * @param src The command source to test permissions against
     * @return A set of command mappings, sorted by primary alias
     */
    private static TreeSet<CommandMapping> commands(CommandSource src) {
        final TreeSet<CommandMapping> commands = new TreeSet<>(COMMAND_COMPARATOR);
        commands.addAll(Sponge.getCommandManager().getAll().values().stream().filter(input -> input.getCallable().testPermission(src)).collect(Collectors.toList()));
        return commands;
    }

    /**
     * Creates a short description for display on the help index.
     *
     * @param source The source the description will be shown to for
     *     translation purposes
     * @param mapping The command mapping to generate a description from
     * @return A text representing the command mapping, formatted for display
     *     on the help index
     */
    private static Text createDescription(CommandSource source, CommandMapping mapping) {
        @SuppressWarnings("unchecked")
        final Optional<Text> description = mapping.getCallable().getShortDescription(source);
        final Text.Builder text = Text.builder("/" + mapping.getPrimaryAlias());
        text.color(TextColors.GREEN);
        // End with a space, so tab completion works immediately.
        text.onClick(TextActions.suggestCommand("/" + mapping.getPrimaryAlias() + " "));
        Optional<? extends Text> longDescription = mapping.getCallable().getHelp(source);
        if (longDescription.isPresent()) {
            text.onHover(TextActions.showText(longDescription.get()));
        }
        return Text.of(text, " ", description.orElse(mapping.getCallable().getUsage(source)));
    }
}
