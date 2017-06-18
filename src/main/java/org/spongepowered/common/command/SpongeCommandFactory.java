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
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandMessageFormatting;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.managed.CommandExecutor;
import org.spongepowered.api.command.managed.TargetedCommandExecutor;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.flag.Flags;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.SpongeApiTranslationHelper;
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

public class SpongeCommandFactory {

    public static final String INDENT = "    ";
    public static final String LONG_INDENT = INDENT + INDENT;
    public static final List<String> CONTAINER_LIST_STATICS = Lists.newArrayList("minecraft", "mcp", "spongeapi", "sponge");

    protected static final Text SEPARATOR_TEXT = Text.of(", ");
    static final Text INDENT_TEXT = Text.of(INDENT);
    static final Text NEWLINE_TEXT = Text.NEW_LINE;
    static final Text LIST_ITEM_TEXT = Text.of(TextColors.GRAY, "- ");
    static final Text UNKNOWN = Text.of("UNKNOWN");

    private static final DecimalFormat THREE_DECIMAL_DIGITS_FORMATTER = new DecimalFormat("########0.000");
    private static final Comparator<CommandMapping> COMMAND_COMPARATOR = Comparator.comparing(CommandMapping::getPrimaryAlias);
    private static final Text PAGE_KEY = Text.of("page");
    private static final Text COMMAND_KEY = Text.of("command");
    private static final Text NOT_FOUND = Text.of("notFound");

    private static final Flags FLAGS = Flags.builder().flag("-global", "g")
            .valueFlag(Parameter.worldProperties().setKey(Text.of("world")).build(), "-world", "w")
            .valueFlag(Parameter.dimension().setKey(Text.of("dimension")).build(), "-dimension", "d")
            .build();

    /**
     * Create a new instance of the Sponge command structure.
     *
     * @return The newly created command
     */
    public static Command createSpongeCommand() {
        Command.Builder commandBuilder = Command.builder()
                .child(createSpongeVersionCommand(), "version")
                .child(createSpongeBlockInfoCommand(), "blockInfo")
                .child(createSpongeEntityInfoCommand(), "entityInfo")
                .child(createSpongeAuditCommand(), "audit")
                .child(createSpongeHeapCommand(), "heap")
                .child(createSpongePluginsCommand(), "plugins")
                .child(createSpongeTimingsCommand(), "timings")
                .child(createSpongeWhichCommand(), "which")
                .child(createSpongeChunksCommand(), "chunks")
                .child(createSpongeConfigCommand(), "config")
                .child(createSpongeReloadCommand(), "reload") // TODO: Should these two be subcommands of config, and what is now config be set?
                .child(createSpongeSaveCommand(), "save")
                .child(createSpongeTpsCommand(), "tps")
                .setShortDescription(Text.of("General Sponge command"));

        SpongeImplHooks.registerAdditionalCommands(commandBuilder);
        return commandBuilder.build();
    }

    // TODO: Have some sort of separator between outputs for each world/dimension/global/whatever (that are exactly one line?)
    private abstract static class ConfigUsingExecutor implements TargetedCommandExecutor<CommandSource> {
        private boolean requireWorldLoaded;

        ConfigUsingExecutor(boolean requireWorldLoaded) {
            this.requireWorldLoaded = requireWorldLoaded;
        }

        @Override
        public CommandResult execute(Cause cause, CommandSource src, CommandContext args) throws CommandException {
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

    private static Command createSpongeChunksCommand() {
        return Command.builder()
                .parameters(
                        Parameter.literal("dump").optional().setKey("dump").build(),
                        Parameter.literal("all").optional().setKey("dump-all").build()
                )
                .setFlags(FLAGS)
                .setShortDescription(Text.of("Prints chunk data for a specific dimension or world(s)"))
                .setPermission("sponge.command.chunks")
                .setExecutor(new ConfigUsingExecutor(true) {
                    @Override
                    public CommandResult execute(Cause cause, CommandSource src, CommandContext args) throws CommandException {
                        CommandResult res = super.execute(cause, src, args);
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

    private static Command createSpongeConfigCommand() {
        return Command.builder()
                .setShortDescription(Text.of("Inspect the Sponge config"))
                .setFlags(FLAGS)
                .parameters(
                        Parameter.string().setKey("key").build(),
                        Parameter.string().optional().setKey("value").build()
                )
                .setPermission("sponge.command.config")
                .setExecutor(new ConfigUsingExecutor(false) {
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

    private static Command createSpongeReloadCommand() {
        return Command.builder()
                .setFlags(FLAGS)
                .setShortDescription(Text.of("Reloads a global, dimension, or world config"))
                .setPermission("sponge.command.reload")
                .setExecutor(new ConfigUsingExecutor(false) {
                    @Override
                    protected Text process(SpongeConfig<? extends GeneralConfigBase> config, CommandSource source, CommandContext args) throws CommandException {
                        config.reload();
                        SpongeHooks.refreshActiveConfigs();
                        return Text.of("Reloaded configuration");
                    }
                })
                .build();
    }

    private static Command createSpongeSaveCommand() {
        return Command.builder()
                .setFlags(FLAGS)
                .setShortDescription(Text.of("Saves a global, dimension, or world config"))
                .setPermission("sponge.command.save")
                .setExecutor(new ConfigUsingExecutor(false) {
                    @Override
                    protected Text process(SpongeConfig<? extends GeneralConfigBase> config, CommandSource source, CommandContext args) throws CommandException {
                        config.save();
                        return Text.of("Saved");
                    }
                })
                .build();
    }

    // Non-flag children

    private static Command createSpongeHeapCommand() {
        return Command.builder()
                .setShortDescription(Text.of("Generate a dump of the Sponge heap"))
                .setPermission("sponge.command.heap")
                .setExecutor((cause, src, args) -> {
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

    private static Command createSpongeVersionCommand() {
        return Command.builder()
                .setShortDescription(Text.of("Display Sponge's current version"))
                .setPermission("sponge.command.version")
                .setExecutor((cause, src, args) -> {
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

    private static Command createSpongeBlockInfoCommand() {
        return Command.builder()
                .setShortDescription(Text.of("Display the tracked information of the Block you are looking at."))
                .setPermission("sponge.command.blockinfo")
                .setTargetedExecutorErrorMessage(Text.of(TextColors.RED, "Players must execute this command!"))
                .targetedExecutor((cause, src, args) -> {
                    final EntityPlayerMP entityPlayerMP = EntityUtil.toNative(src);
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
                }, Player.class)
                .build();
    }

    private static Command createSpongeEntityInfoCommand() {
        return Command.builder()
                .setShortDescription(Text.of("Display the tracked information of the Entity you are looking at."))
                .setPermission("sponge.command.entityinfo")
                .setTargetedExecutorErrorMessage(Text.of(TextColors.RED, "Players must execute this command!"))
                .targetedExecutor((cause, src, args) -> {
                    final EntityPlayerMP entityPlayerMP = EntityUtil.toNative(src);
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
                }, Player.class)
                .build();
    }

    private static Command createSpongeAuditCommand() {
        return Command.builder()
                .setShortDescription(Text.of("Audit Mixin classes for implementation"))
                .setPermission("sponge.command.audit")
                .setExecutor((src, args) -> {
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

    private static Command createSpongePluginsCommand() {
        return Command.builder()
                .setShortDescription(Text.of("List currently installed plugins"))
                .setPermission("sponge.command.plugins")
                .parameters(
                        Parameter.literal("reload").optionalWeak().setKey("reload").build(),
                        Parameter.plugin().optionalWeak().setKey("plugin").build()
                )
                .setExecutor((cause, src, args) -> {
                    if (args.hasAny("reload") && args.getSubject().map(x -> x.hasPermission("sponge.command.plugins.reload")).orElse(true)) {
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
                                                "ID: ", container.getId(), Text.NEW_LINE,
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

    private static Command createSpongeTimingsCommand() {
        return Command.builder()
                .setPermission("sponge.command.timings")
                .setRequirePermissionForChildren(true)
                .setShortDescription(Text.of("Manages Sponge Timings data to see performance of the server."))
                .child(Command.builder()
                        .setExecutor((cause, src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.reset();
                            src.sendMessage(Text.of("Timings reset"));
                            return CommandResult.success();
                        })
                        .build(), "reset")
                .child(Command.builder()
                        .setExecutor((cause, src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.generateReport(src);
                            return CommandResult.success();
                        })
                        .build(), "report", "paste")
                .child(Command.builder()
                        .setExecutor((cause, src, args) -> {
                            Timings.setTimingsEnabled(true);
                            src.sendMessage(Text.of("Enabled Timings & Reset"));
                            return CommandResult.success();
                        })
                        .build(), "on")
                .child(Command.builder()
                        .setExecutor((cause, src, args) -> {
                            Timings.setTimingsEnabled(false);
                            src.sendMessage(Text.of("Disabled Timings"));
                            return CommandResult.success();
                        })
                        .build(), "off")
                .child(Command.builder()
                        .setExecutor((cause, src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(true);
                            src.sendMessage(Text.of("Enabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verbon")
                .child(Command.builder()
                        .setExecutor((cause, src, args) -> {
                            if (!Timings.isTimingsEnabled()) {
                                src.sendMessage(Text.of("Please enable timings by typing /sponge timings on"));
                                return CommandResult.empty();
                            }
                            Timings.setVerboseTimingsEnabled(false);
                            src.sendMessage(Text.of("Disabled Verbose Timings"));
                            return CommandResult.success();
                        })
                        .build(), "verboff")
                .child(Command.builder()
                        .setExecutor((cause, src, args) -> {
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

    private static Command createSpongeWhichCommand() {
        return Command.builder()
                .setPermission("sponge.command.which")
                .setShortDescription(Text.of("List plugins that own a specific command"))
                .parameters(
                        Parameter.choices(() -> Sponge.getCommandManager().getAll().keySet(), Function.identity()).setKey("command").build()
                )
                .setExecutor((cause, src, args) -> {
                    CommandManager mgr = Sponge.getCommandManager();
                    String commandName = args.<String>getOne("command").get();
                    CommandMapping primary = mgr.get(commandName, cause)
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

    private static Command createSpongeTpsCommand() {
        return Command.builder()
                .setPermission("sponge.command.tps")
                .setShortDescription(Text.of("Provides TPS (ticks per second) data for loaded worlds."))
                .parameters(Parameter.worldProperties().setKey("world").optional().build())
                .setExecutor((cause, src, args) -> {
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

    // Not registered under the 'sponge' alias but kept here for consistency
    /**
     * Creates a new instance of the Sponge help command.
     *
     * @return The created help command
     */
    public static Command createHelpCommand() {
        return Command
                .builder()
                .setPermission(CommandPermissions.SPONGE_HELP_PERMISSION)
                .parameter(
                        Parameter.firstOf(Parameter.builder()
                                .setKey(COMMAND_KEY)
                                .setParser((commandSource, args, context) -> {
                                    String input = args.next();
                                    Optional<? extends CommandMapping> cmd = SpongeImpl.getGame().getCommandManager().get(input, commandSource);
                                    if (cmd.isPresent()) {
                                        return cmd;
                                    }

                                    throw args.createError(SpongeApiTranslationHelper.t("No such command: ", input));
                                })
                                .onlyOne()
                                .optional()
                                .build())
                                .or(Parameter.integerNumber().setKey(PAGE_KEY).onlyOne().optional().build())
                                .or(Parameter.string().setKey(NOT_FOUND).onlyOne().setUsage((t, c) -> Text.EMPTY).build())
                                .optional()
                                .build()
                )
                .setShortDescription(Text.of("View a list of all commands."))
                .setExtendedDescription(
                        Text.of("View a list of all commands. Hover over a command to view its description. Click a command to insert it into your chat"
                                + " bar."))
                .setExecutor((cause, src, args) -> {
                    if (args.hasAny(NOT_FOUND)) {
                        throw new CommandException(SpongeApiTranslationHelper.t("No such command: "), args.getOneUnchecked(NOT_FOUND));
                    }

                    Optional<CommandMapping> command = args.getOne(COMMAND_KEY);

                    if (command.isPresent()) {
                        Command targetCommand = command.get().getCommand();
                        PaginationList.Builder plb = PaginationList.builder()
                                .title(Text.of(TextColors.DARK_GREEN, "Help for /" + command.get().getPrimaryAlias()))
                                .padding(Text.of(TextColors.DARK_GREEN, "="));

                        Optional<? extends Text> desc = targetCommand.getHelp(cause);
                        if (desc.isPresent()) {
                            plb.contents(desc.get());
                        } else {
                            plb.contents(Text.of("Usage: /", command.get().getPrimaryAlias(),
                                    CommandMessageFormatting.SPACE_TEXT, targetCommand.getUsage(cause)));
                        }

                        plb.build().sendTo(src, 1);
                        return CommandResult.success();
                    }

                    int page = Math.max(1, args.<Integer>getOne(PAGE_KEY).orElse(1));
                    final ImmutableList<Text> contents = ImmutableList.<Text>builder()
                        .add(Text.of(Sponge.getRegistry().getTranslationById("commands.help.footer").get()))
                        .addAll(commands(cause).stream().map(input -> createDescription(cause, input)).collect(Collectors.toList()))
                        .build();

                    PaginationList.builder()
                            .title(Text.of(TextColors.DARK_GREEN, "Showing Help (/page <page>):"))
                            .padding(Text.of(TextColors.DARK_GREEN, "="))
                            .contents(contents)
                            .build().sendTo(src, page);

                    return CommandResult.success();
                }).build();
    }

    /**
     * Gets a tree set of command mappings the command source has access to.
     *
     * @param cause The {@link Cause} to test permissions against
     * @return A set of command mappings, sorted by primary alias
     */
    private static TreeSet<CommandMapping> commands(Cause cause) {
        final TreeSet<CommandMapping> commands = new TreeSet<>(COMMAND_COMPARATOR);
        commands.addAll(Sponge.getCommandManager().getAll().values().stream().filter(input -> input.getCommand().testPermission(cause))
                .collect(Collectors.toList()));
        return commands;
    }

    /**
     * Creates a short description for display on the help index.
     *
     * @param cause The {@link Cause} the description will be shown to for
     *     translation purposes
     * @param mapping The command mapping to generate a description from
     * @return A text representing the command mapping, formatted for display
     *     on the help index
     */
    private static Text createDescription(Cause cause, CommandMapping mapping) {
        @SuppressWarnings("unchecked")
        final Optional<Text> description = mapping.getCommand().getShortDescription(cause);
        final Text.Builder text = Text.builder("/" + mapping.getPrimaryAlias());
        text.color(TextColors.GREEN);
        // End with a space, so tab completion works immediately.
        text.onClick(TextActions.suggestCommand("/" + mapping.getPrimaryAlias() + " "));
        Optional<? extends Text> longDescription = mapping.getCommand().getHelp(cause);
        if (longDescription.isPresent()) {
            text.onHover(TextActions.showText(longDescription.get()));
        }
        return Text.of(text, " ", description.orElse(mapping.getCommand().getUsage(cause)));
    }

}
