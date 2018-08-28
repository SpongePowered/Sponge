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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.SignSource;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;

import java.util.Collections;
import java.util.Optional;

@Plugin(id = "signsourcetest", name = "SignSourceTest", description = SignSourceTest.DESCRIPTION, version = "0.0.0")
public class SignSourceTest {

    public static final String DESCRIPTION = "A plugin to test the item SignSource as a ProxySource.";

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, CommandSpec.builder().executor((src, args) -> {
            if (!(src instanceof Player)) {
                throw new CommandException(Text.of("Only players can use this command"));
            }

            Player source = (Player) src;
            source.getLocation().setBlockType(BlockTypes.STANDING_SIGN);

            Text signText = Text.builder()
                    .append(Text.of("Click Me!"))
                    .onClick(TextActions.runCommand("/doclicksign"))
                    .build();
            Optional<TileEntity> maybeTileEntities = source.getLocation().getTileEntity();
            maybeTileEntities.ifPresent(tileEntity -> tileEntity.offer(Keys.SIGN_LINES, Collections.singletonList(signText)));

            return CommandResult.success();
        }).build(), "placeclicksign");

        Sponge.getCommandManager().register(this, CommandSpec.builder().executor((src, args) -> {
            if (!(src instanceof SignSource)) {
                src.sendMessage(Text.of("You did not click a sign."));
                return CommandResult.empty();
            }

            CommandSource originalSource = ((SignSource) src).getOriginalSource();
            originalSource.sendMessage(Text.of("You clicked me!"));
            return CommandResult.success();
        }).build(), "doclicksign");
    }
}
