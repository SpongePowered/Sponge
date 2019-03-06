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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.InstrumentProperty;
import org.spongepowered.api.data.type.InstrumentType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

@SuppressWarnings("ConstantConditions")
@Plugin(id = "instrumenttest", name = "InstrumentTest", description = "Tests instrument types and properties.", version = "0.0.0")
public class InstrumentTestPlugin implements LoadableModule {

    @Inject private PluginContainer container;

    private final InstrumentListener listener = new InstrumentListener();

    @Listener
    public void onInitialization(final GameInitializationEvent event) {
        Sponge.getCommandManager().register(this,
                CommandSpec.builder()
                        .executor((src, args) -> {
                            PaginationList.builder()
                                    .title(Text.of(TextColors.GREEN, "Instrument Types"))
                                    .padding(Text.of(TextColors.DARK_GREEN, "="))
                                    .contents(Sponge.getRegistry().getAllOf(InstrumentType.class).stream()
                                            .map(i -> Text.of(TextColors.GREEN, "Id: ", TextColors.GRAY, i.getId(),
                                                    TextColors.GREEN, " Name: ", TextColors.GRAY, i.getName(),
                                                    TextColors.GREEN, " SoundType: ", TextColors.GRAY, i.getSound().getName()))
                                            .collect(ImmutableList.toImmutableList()))
                                    .sendTo(src);
                            return CommandResult.success();
                        })
                        .build(),
                "instrumenttypes");
    }

    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class InstrumentListener {

        @Listener
        public void onUseItem(InteractBlockEvent.Secondary.MainHand event, @First Player player) {
            if (!player.get(Keys.IS_SNEAKING).get()) {
                return;
            }
            final BlockSnapshot snapshot = event.getTargetBlock();
            if (snapshot.getState().getType().equals(BlockTypes.NOTEBLOCK)) {
                final InstrumentProperty instrumentProperty = player.getWorld().getBlock(snapshot.getPosition().sub(0, 1, 0)).getProperty(InstrumentProperty.class).orElse(null);
                if (instrumentProperty != null) {
                    final InstrumentType instrument = instrumentProperty.getValue();
                    player.sendMessage(Text.of(TextColors.DARK_GREEN, "Clicked on a note block with instrument: ", TextColors.GREEN, instrument.getName()));
                } else {
                    player.sendMessage(Text.of(TextColors.DARK_GREEN, "Clicked on a note block that strangely did not have any instrument type."));
                }
            } else {
                final InstrumentProperty instrumentProperty = snapshot.getProperty(InstrumentProperty.class).orElse(null);
                if (instrumentProperty != null) {
                    final InstrumentType instrument = instrumentProperty.getValue();
                    player.sendMessage(Text.of(TextColors.DARK_GREEN, "Clicked on a block with instrument type: ", TextColors.GREEN, instrument.getName()));
                } else {
                    player.sendMessage(Text.of(TextColors.DARK_GREEN, "Clicked on a block which had no instrument type."));
                }
            }
        }
    }
}
