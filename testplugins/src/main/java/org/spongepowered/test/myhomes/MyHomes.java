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
package org.spongepowered.test.myhomes;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.data.value.mutable.MapValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameRegistryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;
import org.spongepowered.test.LoadableModule;
import org.spongepowered.test.myhomes.data.friends.FriendsData;
import org.spongepowered.test.myhomes.data.friends.ImmutableFriendsData;
import org.spongepowered.test.myhomes.data.friends.impl.FriendsDataBuilder;
import org.spongepowered.test.myhomes.data.friends.impl.FriendsDataImpl;
import org.spongepowered.test.myhomes.data.friends.impl.ImmutableFriendsDataImpl;
import org.spongepowered.test.myhomes.data.home.Home;
import org.spongepowered.test.myhomes.data.home.HomeData;
import org.spongepowered.test.myhomes.data.home.ImmutableHomeData;
import org.spongepowered.test.myhomes.data.home.impl.HomeBuilder;
import org.spongepowered.test.myhomes.data.home.impl.HomeDataBuilder;
import org.spongepowered.test.myhomes.data.home.impl.HomeDataImpl;
import org.spongepowered.test.myhomes.data.home.impl.ImmutableHomeDataImpl;

import java.util.UUID;

@Plugin(id = "myhomes", name = "MyHomes", version = "0.0.0", description = "A simple homes plugin")
public class MyHomes implements LoadableModule {

    // TODO - make this an actual home plugin that would work...
    // for now it just registers data and data related stuff.

    public static Key<Value<Home>> DEFAULT_HOME = DummyObjectProvider.createExtendedFor(Key.class, "DEFAULT_HOME");
    public static Key<MapValue<String, Home>> HOMES = DummyObjectProvider.createExtendedFor(Key.class, "HOMES");
    public static Key<ListValue<UUID>> FRIENDS = DummyObjectProvider.createExtendedFor(Key.class, "FRIENDS");

    @Inject private PluginContainer container;
    @Inject private Logger logger;

    private DataRegistration<FriendsData, ImmutableFriendsData> friendsDataRegistration;
    private DataRegistration<HomeData, ImmutableHomeData> homeDataRegistration;

    private final MyHomeListener listener = new MyHomeListener();

    @Listener
    public void onKeyRegistration(GameRegistryEvent.Register<Key<?>> event) {
        this.logger.info("onKeyRegistration");
        DEFAULT_HOME = Key.builder()
            .type(new TypeToken<Value<Home>>() { public static final long serialVersionUID = 1L; })
            .id("default_home")
            .name("Default Home")
            .query(DataQuery.of("DefaultHome"))
            .build();
        event.register(DEFAULT_HOME);

        HOMES = Key.builder()
            .type(new TypeToken<MapValue<String, Home>>() { public static final long serialVersionUID = 1L; })
            .id("homes")
            .name("Homes")
            .query(DataQuery.of("Homes"))
            .build();
        event.register(HOMES);

        FRIENDS = Key.builder()
            .type(new TypeToken<ListValue<UUID>>() { public static final long serialVersionUID = 1L; })
            .id("friends")
            .name("Friends")
            .query(DataQuery.of("Friends"))
            .build();
        event.register(FRIENDS);
    }

    @Listener
    public void onDataRegistration(GameRegistryEvent.Register<DataRegistration<?, ?>> event) {
        this.logger.info("onDataRegistration");
        final DataManager dataManager = Sponge.getDataManager();
        // Home stuff
        dataManager.registerBuilder(Home.class, new HomeBuilder());
        dataManager.registerContentUpdater(Home.class, new HomeBuilder.NameUpdater());
        dataManager.registerContentUpdater(HomeData.class, new HomeDataBuilder.HomesUpdater());

        this.homeDataRegistration = DataRegistration.builder()
            .dataClass(HomeData.class)
            .immutableClass(ImmutableHomeData.class)
            .dataImplementation(HomeDataImpl.class)
            .immutableImplementation(ImmutableHomeDataImpl.class)
            .builder(new HomeDataBuilder())
            .name("Home Data")
            .id("home")
            .build();

        // Friends stuff
        this.friendsDataRegistration = DataRegistration.builder()
            .dataClass(FriendsData.class)
            .immutableClass(ImmutableFriendsData.class)
            .dataImplementation(FriendsDataImpl.class)
            .immutableImplementation(ImmutableFriendsDataImpl.class)
            .builder(new FriendsDataBuilder())
            .name("Friends Data")
            .id("friends")
            .build();
    }



    @Override
    public void enable(CommandSource src) {
        Sponge.getEventManager().registerListeners(this.container, this.listener);
    }

    public static class MyHomeListener {

        @Listener
        public void onClientConnectionJoin(ClientConnectionEvent.Join event) {
            Player player = event.getTargetEntity();
            player.get(DEFAULT_HOME).ifPresent(home -> {
                player.setTransform(home.getTransform());
                player.sendMessage(ChatTypes.ACTION_BAR, Text.of("Teleported to home - ", TextStyles.BOLD, home.getName()));
            });
        }

    }
}
