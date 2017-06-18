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
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

@Plugin(id = "paginationtest", name = "Pagination Test", description = "A plugin to test the pagination service.")
public class PaginationServiceTest {

    @Inject
    private Logger logger;

    private PaginationList paginationList;

    @Listener
    public void onGamePreInitialization(final GamePreInitializationEvent event) {
        final Optional<PaginationService> paginationService = Sponge.getServiceManager().provide(PaginationService.class);
        if (paginationService.isPresent()) {
            // Defaults to normal amount of lines per page to guarantee it is of appropriate size
            this.paginationList = paginationService.get().builder()
                    .title(Text.of(TextColors.RED, "This Is A Test"))
                    .padding(Text.of(TextColors.GOLD, "="))
                    .header(Text.of("This is the header"))
                    .footer(Text.of("This is the footer"))
                    .contents(ImmutableList.of(
                            Text.of(TextColors.GRAY, "rhomboid"),
                            Text.of(TextColors.GRAY, "analytic"),
                            Text.of(TextColors.GRAY, "sandwich"),
                            Text.of(TextColors.GRAY, "wallpaper"),
                            Text.of(TextColors.GRAY, "fragmentation"),
                            Text.of(TextColors.GRAY, "elephant"),
                            Text.of(TextColors.GRAY, "idempotence"),
                            Text.of(TextColors.GRAY, "finger"),
                            Text.of(TextColors.GRAY, "licking"),
                            Text.of(TextColors.GRAY, "netherborn"),
                            Text.of(TextColors.GRAY, "facsimile"),
                            Text.of(TextColors.GRAY, "drainpipe"),
                            Text.of(TextColors.GRAY, "limerick"),
                            Text.of(TextColors.GRAY, "toadstool"),
                            Text.of(TextColors.GRAY, "talisman"),
                            Text.of(TextColors.GRAY, "alligator"),
                            Text.of(TextColors.GRAY, "whistle"),
                            Text.of(TextColors.GRAY, "bollard"),
                            Text.of(TextColors.GRAY, "slime"),
                            Text.of(TextColors.GRAY, "gallant"),
                            Text.of(TextColors.GRAY, "twisted"),
                            Text.of(TextColors.GRAY, "moist"),
                            Text.of(TextColors.GRAY, "himalayan"),
                            Text.of(TextColors.GRAY, "mortals"),
                            Text.of(TextColors.GRAY, "dollop"),
                            Text.of(TextColors.GRAY, "pompous"),
                            Text.of(TextColors.GRAY, "squeegee")
                    ))
                    .build();
        } else {
            this.logger.error("The pagination service was not properly registered for some reason :(");
            return;
        }

        Sponge.getCommandManager().register(this,
                Command.builder()
                        .parameter(Parameter.integerNumber()
                                .setKey("page")
                                .optionalWeak()
                                .onlyOne()
                                .build())
                        .setExecutor((cause, src, args) -> {
                            this.paginationList.sendTo(src, args.<Integer>getOne("page").orElse(1));

                            return CommandResult.success();
                        })
                        .build(),
                "paginationtest");
    }

}
