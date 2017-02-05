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
package org.spongepowered.common.text.action;

import static com.google.common.base.Preconditions.checkState;

import net.minecraft.util.text.event.ClickEvent;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.action.ClickAction;

import java.net.URL;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public abstract class SpongeClickTextAction<R> extends SpongeTextAction<R> implements ClickAction<R> {

    @Nullable private ClickEvent event;

    SpongeClickTextAction(R result) {
        super(result);
    }

    public final ClickEvent asEvent() {
        if (this.event == null) {
            this.event = this.createEvent();
        }
        return this.event;
    }

    abstract ClickEvent createEvent();

    public static final class SpongeOpenUrl extends SpongeClickTextAction<URL> implements ClickAction.OpenUrl {

        SpongeOpenUrl(URL result) {
            super(result);
        }

        @Override
        ClickEvent createEvent() {
            return new ClickEvent(ClickEvent.Action.OPEN_URL, this.result.toString());
        }

        public static class Builder implements ClickAction.OpenUrl.Builder {

            @Nullable private URL url;

            @Override
            public ClickAction.OpenUrl.Builder url(URL url) {
                this.url = url;
                return this;
            }

            @Override
            public ClickAction.OpenUrl.Builder from(ClickAction.OpenUrl value) {
                this.url = value.getResult();
                return this;
            }

            @Override
            public ClickAction.OpenUrl.Builder reset() {
                this.url = null;
                return this;
            }

            @Override
            public ClickAction.OpenUrl build() {
                checkState(this.url != null, "url not set");
                return new SpongeOpenUrl(this.url);
            }
        }
    }

    public static final class SpongeRunCommand extends SpongeClickTextAction<String> implements ClickAction.RunCommand {

        SpongeRunCommand(String result) {
            super(result);
        }

        @Override
        ClickEvent createEvent() {
            return new ClickEvent(ClickEvent.Action.RUN_COMMAND, this.result);
        }

        public static class Builder implements ClickAction.RunCommand.Builder {

            @Nullable private String command;

            @Override
            public ClickAction.RunCommand.Builder command(String command) {
                this.command = command;
                return this;
            }

            @Override
            public ClickAction.RunCommand.Builder from(ClickAction.RunCommand value) {
                this.command = value.getResult();
                return this;
            }

            @Override
            public ClickAction.RunCommand.Builder reset() {
                this.command = null;
                return this;
            }

            @Override
            public ClickAction.RunCommand build() {
                checkState(this.command != null, "command not set");
                return new SpongeRunCommand(this.command);
            }
        }
    }

    public static final class SpongeChangePage extends SpongeClickTextAction<Integer> implements ClickAction.ChangePage {

        SpongeChangePage(int result) {
            super(result);
        }

        @Override
        ClickEvent createEvent() {
            return new ClickEvent(ClickEvent.Action.CHANGE_PAGE, this.result.toString());
        }

        public static class Builder implements ClickAction.ChangePage.Builder {

            private OptionalInt page;

            @Override
            public ClickAction.ChangePage.Builder page(int page) {
                this.page = OptionalInt.of(page);
                return this;
            }

            @Override
            public ClickAction.ChangePage.Builder from(ClickAction.ChangePage value) {
                this.page = OptionalInt.of(value.getResult());
                return this;
            }

            @Override
            public ClickAction.ChangePage.Builder reset() {
                this.page = OptionalInt.empty();
                return this;
            }

            @Override
            public ClickAction.ChangePage build() {
                checkState(this.page.isPresent(), "page not set");
                return new SpongeChangePage(this.page.getAsInt());
            }
        }
    }

    public static final class SpongeSuggestCommand extends SpongeClickTextAction<String> implements ClickAction.SuggestCommand {

        SpongeSuggestCommand(String result) {
            super(result);
        }

        @Override
        ClickEvent createEvent() {
            return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, this.result);
        }

        public static class Builder implements ClickAction.SuggestCommand.Builder {

            @Nullable private String command;

            @Override
            public ClickAction.SuggestCommand.Builder command(String command) {
                this.command = command;
                return this;
            }

            @Override
            public ClickAction.SuggestCommand.Builder from(ClickAction.SuggestCommand value) {
                this.command = value.getResult();
                return this;
            }

            @Override
            public ClickAction.SuggestCommand.Builder reset() {
                this.command = null;
                return this;
            }

            @Override
            public ClickAction.SuggestCommand build() {
                checkState(this.command != null, "command not set");
                return new SpongeSuggestCommand(this.command);
            }
        }
    }

    public static final class SpongeExecuteCallback extends SpongeClickTextAction<Consumer<CommandSource>> implements ClickAction.ExecuteCallback {

        SpongeExecuteCallback(Consumer<CommandSource> result) {
            super(result);
        }

        @Override
        ClickEvent createEvent() {
            final UUID id = SpongeCallbackHolder.getInstance().getOrCreateIdForCallback(this.result);
            return new ClickEvent(ClickEvent.Action.RUN_COMMAND, SpongeCallbackHolder.CALLBACK_COMMAND_QUALIFIED + " " + id);
        }

        public static class Builder implements ClickAction.ExecuteCallback.Builder {

            @Nullable private Consumer<CommandSource> callback;

            @Override
            public ClickAction.ExecuteCallback.Builder callback(Consumer<CommandSource> callback) {
                this.callback = callback;
                return this;
            }

            @Override
            public ClickAction.ExecuteCallback.Builder from(ClickAction.ExecuteCallback value) {
                this.callback = value.getResult();
                return this;
            }

            @Override
            public ClickAction.ExecuteCallback.Builder reset() {
                this.callback = null;
                return this;
            }

            @Override
            public ClickAction.ExecuteCallback build() {
                checkState(this.callback != null, "callback not set");
                return new SpongeExecuteCallback(this.callback);
            }
        }
    }
}
