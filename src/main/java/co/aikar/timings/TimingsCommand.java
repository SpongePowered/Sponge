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
package co.aikar.timings;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandPermissionException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TimingsCommand implements CommandCallable {

    public static final List<String> TIMINGS_SUBCOMMANDS = ImmutableList.of("report", "reset", "on", "off", "paste", "verbon", "verboff");
    private Text description;
    private Text usageMessage;

    public TimingsCommand() {
        this.description = Texts.of("Manages Spigot Timings data to see performance of the server.");
        this.usageMessage = Texts.of("/timings <reset|report|on|off|verbon|verboff>");
    }

    @Override
    public CommandResult process(CommandSource source, String arguments) throws CommandException {
        if (!testPermission(source)) {
            throw new CommandPermissionException();
        }
        String[] args = arguments.split(" ");
        if (args.length < 1) {
            source.sendMessage(Texts.of(TextColors.RED, "Usage: " + this.usageMessage));
            return CommandResult.empty();
        }
        final String arg = args[0];
        if ("on".equalsIgnoreCase(arg)) {
            Timings.setTimingsEnabled(true);
            source.sendMessage(Texts.of("Enabled Timings & Reset"));
            return CommandResult.success();
        } else if ("off".equalsIgnoreCase(arg)) {
            Timings.setTimingsEnabled(false);
            source.sendMessage(Texts.of("Disabled Timings"));
            return CommandResult.success();
        }

        if (!Timings.isTimingsEnabled()) {
            source.sendMessage(Texts.of("Please enable timings by typing /timings on"));
            CommandResult.empty();
        }
        if ("verbon".equalsIgnoreCase(arg)) {
            Timings.setVerboseTimingsEnabled(true);
            source.sendMessage(Texts.of("Enabled Verbose Timings"));
            return CommandResult.success();
        } else if ("verboff".equalsIgnoreCase(arg)) {
            Timings.setVerboseTimingsEnabled(false);
            source.sendMessage(Texts.of("Disabled Verbose Timings"));
            return CommandResult.success();
        } else if ("reset".equalsIgnoreCase(arg)) {
            TimingsManager.reset();
            source.sendMessage(Texts.of("Timings reset"));
        } else if ("cost".equals(arg)) {
            source.sendMessage(Texts.of("Timings cost: " + TimingsExport.getCost()));
        } else if ("paste".equalsIgnoreCase(arg) ||
                "report".equalsIgnoreCase(arg) ||
                "get".equalsIgnoreCase(arg) ||
                "merged".equalsIgnoreCase(arg) ||
                "separate".equalsIgnoreCase(arg)) {
            TimingsExport.reportTimings(source);
        } else {
            source.sendMessage(Texts.of(TextColors.RED, "Usage: " + this.usageMessage));
        }
        return CommandResult.empty();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        String[] args = arguments.split(" ");
        ArrayList<String> suggestions = new ArrayList<String>(TIMINGS_SUBCOMMANDS.size());
        if (args.length == 1) {
            for (String subCommand : TIMINGS_SUBCOMMANDS) {
                if (subCommand.length() >= args[0].length() && subCommand.regionMatches(true, 0, args[0], 0, args[0].length())) {
                    suggestions.add(subCommand);
                }
            }
        }
        return suggestions;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission("bukkit.command.timings");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(this.description);
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return this.usageMessage;
    }
}
