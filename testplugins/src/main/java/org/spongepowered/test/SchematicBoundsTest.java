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
package org.spongepowered.test;

import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;
import org.spongepowered.api.world.storage.WorldProperties;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.math.vector.Vector3i;

import java.util.logging.Logger;

import javax.annotation.Nullable;

@Plugin(id = SchematicBoundsTest.ID, name = SchematicBoundsTest.NAME, description = SchematicBoundsTest.DESCRIPTION, version = SchematicBoundsTest.VERSION)
public class SchematicBoundsTest implements LoadableModule {

    public static final String ID = "schematicboundstest";
    public static final String NAME = "Schematic Bounds Test";
    public static final String DESCRIPTION = "Testing schematic bounds.";
    public static final String VERSION = "0.0.0";

    @Inject
    private Logger logger;

    @Nullable private CommandMapping mapping;

    @Override
    public void disable(final MessageReceiver src) {
        if (this.mapping != null) {
            Sponge.getCommandManager().unregister(this.mapping);
            src.sendMessage(Text.of("Removed /schbuild command"));
        }
    }

    @Override
    public void enable(final MessageReceiver src) {
        if (this.mapping == null) {
            Parameter.Value<WorldProperties> paramWorld = Parameter.worldProperties().setKey("world").build();
            Parameter.Value<Integer> x1 = Parameter.integerNumber().setKey("x1").build();
            Parameter.Value<Integer> y1 = Parameter.integerNumber().setKey("y1").build();
            Parameter.Value<Integer> z1 = Parameter.integerNumber().setKey("z1").build();
            Parameter.Value<Integer> x2 = Parameter.integerNumber().setKey("x2").build();
            Parameter.Value<Integer> y2 = Parameter.integerNumber().setKey("y2").build();
            Parameter.Value<Integer> z2 = Parameter.integerNumber().setKey("z2").build();
            this.mapping = Sponge.getCommandManager()
                    .register(this,
                            Command.builder().parameters(paramWorld, x1, y1, z1, x2, y2, z2)
                                    .setExecutor((context) -> {
                                Vector3i first = new Vector3i(
                                        context.requireOne(x1),
                                        context.requireOne(y1),
                                        context.requireOne(z1)
                                );

                                Vector3i second = new Vector3i(
                                        context.requireOne(x2),
                                        context.requireOne(y2),
                                        context.requireOne(z2)
                                );

                                Vector3i min = first.min(second);
                                Vector3i max = first.max(second);

                                WorldProperties properties = context.requireOne(paramWorld);
                                // Extent extent = Sponge.getServer().getWorld(properties.getUniqueId()).get().getExtentView(min, max);
                                ArchetypeVolume extent = Sponge.getServer().getWorldManager().getWorld(properties.getUniqueId()).get()
                                        .createArchetypeVolume(min, max, min);

                                Schematic sch = Schematic.builder()
                                        .volume(extent)
                                        .blockPaletteType(PaletteTypes.LOCAL_BLOCKS.get())
                                        .biomePaletteType(PaletteTypes.LOCAL_BIOMES.get())
                                        .build();
                                Vector3i size = sch.getBlockSize();
                                context.getMessageReceiver().sendMessage(Text.of("Block size: " + size.getX() * size.getY() * size.getZ()));
                                context.getMessageReceiver().sendMessage(Text.of("Block dimensions: " + size));

                                CountingIterator it = new CountingIterator();
                                sch.getBlockWorker().iterate(it);
                                context.getMessageReceiver().sendMessage(Text.of("Iterated size: " + it.count));
                                return CommandResult.success();
                            })
                            .build(),
                        "schbuild").orElse(null);
            if (this.mapping == null) {
                src.sendMessage(Text.of("Could not register schbuild"));
            } else {
                src.sendMessage(Text.of("Registered schbuild"));
            }
        }
    }

    private static class CountingIterator implements BlockVolumeVisitor<Schematic> {

        public int count = 0;

        @Override
        public void visit(Schematic volume, int x, int y, int z) {
            ++count;
        }
    }

}
