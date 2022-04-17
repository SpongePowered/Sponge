package org.spongepowered.test.blockentity;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.world.chunk.WorldChunk;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

@Plugin("blockentityvolumetest")
public class BlockEntityVolumeTest {

    private final PluginContainer plugin;
    private final Logger logger;

    @Inject
    public BlockEntityVolumeTest(final PluginContainer plugin, final Logger logger) {
        this.plugin = plugin;
        this.logger = logger;
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {
        final Command.Parameterized countBlockEntitiesInChunk = Command.builder()
                .executor(ctx -> {
                    final ServerPlayer player = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to execute this command")));
                    final WorldChunk chunk = player.serverLocation().world().chunk(player.serverLocation().chunkPosition());
                    final int count = chunk.blockEntities().size();

                    player.sendMessage(Component.text("Counted " + count + " tile entities in this chunk"));
                    return CommandResult.success();
                })
                .build();

        event.register(this.plugin, countBlockEntitiesInChunk, "countBlockEntitiesInChunk");

        final Command.Parameterized removeBlockEntity = Command.builder()
                .executor(ctx -> {
                    final ServerPlayer player = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to execute this command")));
                    WorldChunk chunk = player.serverLocation().world().chunk(player.serverLocation().chunkPosition());
                    Vector3i pos = getBlockStandingOn(player);
                    chunk.removeBlockEntity(pos);
                    player.sendMessage(Component.text("Removed the BlockEntity you were standing on."));
                    return CommandResult.success();
                })
                .build();

        event.register(this.plugin, removeBlockEntity, "removeBlockEntity");


        final Command.Parameterized testCopyPasteBlockEntity = Command.builder()
                .executor(ctx -> {
                    final ServerPlayer player = ctx.cause().first(ServerPlayer.class)
                            .orElseThrow(() -> new CommandException(Component.text("You must be a player to execute this command")));
                    final WorldChunk chunk = player.serverLocation().world().chunk(player.serverLocation().chunkPosition());
                    final Vector3i pos = getBlockStandingOn(player);
                    final BlockEntity blockEntity = chunk.blockEntity(pos)
                            .orElseThrow(() -> new CommandException(Component.text("You must be standing on a block entity to execute this command")));

                    chunk.addBlockEntity(pos.add(1, 0, 0), blockEntity);
                    player.sendMessage(Component.text("You should observe the blockentity being copy/pasted you are standing on be pasted 1 block in the x direction (try this with a chest with items in)"));
                    return CommandResult.success();
                })
               .build();

        event.register(this.plugin, testCopyPasteBlockEntity, "testCopyPasteBlockEntity");
    }

    public Vector3i getBlockStandingOn(final ServerPlayer player) {
        Vector3i pos = player.serverLocation().blockPosition();
        // Chests make you sink into their block a bit, so this means you don't have to float awkwardly for the commands to work.
        if (player.serverLocation().y() - player.serverLocation().blockY() < 0.3) {
            pos = pos.sub(0, 1, 0);
        }
        return pos;
    }
}