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
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKey;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.util.Stack;

import javax.annotation.Nullable;

@Plugin(id = "frames-timing", name = "Cause Stack Frames Timing", description = "A plugin to time cause stack.", version = "0.0.0")
public class CauseFramesTimingTest {

    private final Text numberOfFrames = Text.of("number of frames");
    private final Text numberOfRepeats = Text.of("repetitions");
    @Nullable private EventContextKey<Integer> testContextKey;

    @Listener
    public void init(GameInitializationEvent event) {
        this.testContextKey = EventContextKey.builder(Integer.class)
                .id("frames-timing:test")
                .name("Test numberOfFrames")
                .type(Integer.class)
                .build();

        Sponge.getCommandManager().register(
                this,
                CommandSpec.builder()
                        .arguments(
                                GenericArguments.flags()
                                        .valueFlag(GenericArguments.integer(this.numberOfFrames), "-frames", "f")
                                        .valueFlag(GenericArguments.integer(this.numberOfRepeats), "-repeats", "r")
                                        .buildWith(GenericArguments.none()))
                        .executor((src, context) -> {
                            int noOfFrames = context.<Integer>getOne(this.numberOfFrames).orElse(50);
                            if (noOfFrames < 1) {
                                throw new CommandException(Text.of("There must be a positive number of frames!"));
                            }

                            int noOfRepeats = context.<Integer>getOne(this.numberOfRepeats).orElse(1);
                            if (noOfRepeats < 1) {
                                throw new CommandException(Text.of("There must be a positive number of repeats!"));
                            }

                            // create the objects for putting on the stack, this is not part of the test.
                            // We'll put 5 objects into each frame.
                            Stack<Object> causes = new Stack<>();
                            for (int i = 0; i < noOfFrames * noOfRepeats * 5; ++i) {
                                causes.push(new Object());
                            }

                            CauseStackManager csm = Sponge.getCauseStackManager();
                            long startTime = System.nanoTime();
                            for (int r = 0; r < noOfRepeats; ++r) {
                                CauseStackManager.StackFrame[] frames = new CauseStackManager.StackFrame[noOfFrames];
                                for (int i = 0; i < noOfFrames; ++i) {
                                    frames[i] = csm.pushCauseFrame();
                                    for (int j = 0; j < 5; ++j) {
                                        csm.pushCause(causes.pop());
                                    }
                                    csm.addContext(testContextKey, i);
                                }

                                // Create a cause
                                Cause cause = csm.getCurrentCause();
                                for (int i = noOfFrames - 1; i >= 0; --i) {
                                    csm.popCauseFrame(frames[i]);
                                }
                            }
                            long endTime = System.nanoTime();

                            src.sendMessage(Text.of("Test completed in: ", endTime - startTime, "ns."));
                            return CommandResult.success();
                        }).build(),
                "timecsm"
        );
    }

}
