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
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.MapItemData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.map.InitializeMapEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.map.MapView;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

@Plugin(id = "maptest", name = "Map Test", description = "A plugin to test maps")
public class MapTest {

    @Listener
    public void onInit(GameInitializationEvent event) {
        Sponge.getCommandManager().register(this, saveMap(), "savemap");
        Sponge.getCommandManager().register(this, drawImage(), "drawimage");
    }

    @Listener
    public void onMapCreate(InitializeMapEvent.Create event) {
        System.out.println("Event ran");
        event.setCancelled(true);
        event.getCause().first(Player.class).ifPresent(player -> player.setItemInHand(HandTypes.MAIN_HAND, null));
    }

    private static CommandCallable drawImage() {
        final LiteralText url = Text.of("url");
        return CommandSpec.builder()
                .arguments(GenericArguments.onlyOne(GenericArguments.string(url)))
                .description(Text.of(TextColors.DARK_AQUA, "Draws the provided image on your map!"))
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        return CommandResult.empty();
                    }
                    if (!(args.getOne(url).isPresent()))  {
                        return CommandResult.empty();
                    }
                    Player player = (Player) src;
                    ItemStack heldItem = player.getItemInHand(HandTypes.MAIN_HAND).orElse(null);
                    if (heldItem == null) {
                        return CommandResult.empty();
                    }
                    if (!(heldItem.supports(Keys.ATTACHED_MAP))) {
                        return CommandResult.empty();
                    }
                    String mapId = heldItem.getOrNull(Keys.ATTACHED_MAP);
                    if (mapId == null) {
                        return CommandResult.empty();
                    }
                    MapView view = Sponge.getServer().getMapViewStorage().getMap(mapId).orElse(null);
                    if (view == null) {
                        return CommandResult.empty();
                    }

                    try {
                        URL properURL = new URI((String) args.getOne(url).get()).toURL();
                        BufferedImage image = ImageIO.read(properURL);
                        view.drawImage(0, 0, image);
                        return CommandResult.success();
                    } catch (Exception e) {
                        return CommandResult.empty();
                    }
                })
                .build();
    }
    private static CommandCallable saveMap() {
        return CommandSpec.builder()
                .description(Text.of(TextColors.DARK_AQUA, "Saves your map and returns a link"))
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        return CommandResult.empty();
                    }
                    Player player = (Player) src;
                    Optional<ItemStack> heldItemOpt = player.getItemInHand(HandTypes.MAIN_HAND);
                    if (!heldItemOpt.isPresent()) {
                        return CommandResult.empty();
                    }
                    ItemStack heldItem = heldItemOpt.get();
                    if (heldItem.getType() != ItemTypes.FILLED_MAP) {
                        return CommandResult.empty();
                    }

                    System.out.println(heldItem.get(MapItemData.class));

                    String mapId = heldItem.get(Keys.ATTACHED_MAP).orElse(null);

                    player.sendMessage(Text.of(TextColors.AQUA, "Map ID: ", mapId));
                    if (mapId == null) {
                        return CommandResult.empty();
                    }
                    Optional<MapView> mapViewOpt = Sponge.getServer().getMapViewStorage().getMap(mapId);
                    if (!mapViewOpt.isPresent()) {
                        return CommandResult.empty();
                    }
                    MapView mapView = mapViewOpt.get();
                    BufferedImage mapImage = mapView.toImage();
                    File savedMap = new File(mapId + ".png");
                    try {
                        ImageIO.write(mapImage, "PNG", savedMap);
                    } catch (IOException e) {
                        player.sendMessage(
                                Text.of(TextColors.RED, "Failed to write image", TextActions.showText(Text.of(e.getMessage())))
                        );
                        return CommandResult.empty();
                    }

                    try {
                       player.sendMessage(
                               Text.of(TextColors.DARK_AQUA, mapId+".png", TextActions.openUrl(savedMap.toURI().toURL()))
                       );
                       return CommandResult.success();
                    } catch (MalformedURLException e) {
                        return CommandResult.empty();
                    }
                })
                .build();
    }

}
