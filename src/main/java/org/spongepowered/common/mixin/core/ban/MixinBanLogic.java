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
package org.spongepowered.common.mixin.core.ban;

import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.BanList;
import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListBans;
import net.minecraft.server.management.UserListEntry;
import org.apache.commons.io.IOUtils;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.interfaces.ban.IMixinBanLogic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Mixin(value = {UserListBans.class, BanList.class})
public abstract class MixinBanLogic extends UserList implements IMixinBanLogic {

    protected Multimap<Object, BanEntry> entries = ArrayListMultimap.create();

    public MixinBanLogic(File saveFile) {
        super(saveFile);
    }

    @Override
    public Multimap<Object, BanEntry> getEntries() {
        return this.entries;
    }

    @Override
    public void addEntry(UserListEntry entry) {
        Object key = this.getObjectKey(entry.getValue());

        this.entries.put(key, (BanEntry) entry);
        if (!this.hasEntry(entry.getValue())) {
            this.getValues().put(key, entry);
        }

        this.writeOutChanges();
    }

    private void writeOutChanges() {
        try {
            this.writeChanges();
        } catch (IOException e) {
            logger.warn("Could not save the list after adding a user.", e);
        }
    }

    @Override
    public void writeChanges() throws IOException {
        Collection collection = this.entries.values();
        String s = this.gson.toJson(collection);
        BufferedWriter bufferedwriter = null;

        try {
            bufferedwriter = Files.newWriter(this.saveFile, Charsets.UTF_8);
            bufferedwriter.write(s);
        } finally {
            IOUtils.closeQuietly(bufferedwriter);
        }
    }

    @Override
    public boolean hasEntry(Object entry) {
        this.removeExpired();
        return this.getEntry(entry) != null;
    }

    @Override
    public UserListEntry getEntry(Object obj) {
        this.removeExpired();

        Object key = this.getObjectKey(obj);
        if (!this.getValues().containsKey(key) && this.entries.containsKey(key)) {
            logger.warn(String.format("Key %s is missing from the values map, but is present in the entries map! This shouldn't happen"
                        + "(maybe a mod is interfering?)", key));
            this.values.put(key, Iterables.getFirst(this.entries.get(key), null));
            this.writeOutChanges();
        }
        return (UserListEntry)this.getValues().get(key);
    }

    @Override
    public void removeEntry(Object obj) {
        // This is invoked from the pardon commands, so we're going
        // to pardon *all* bans, since the pardon command
        // isn't aware of multiple bans. This avoids the violation
        // of the implicit Vanilla pardon contract that would
        // occur if a player/ip was still banned after pardoning it/them.
        Object key = this.getObjectKey(obj);
        this.values.remove(key);
        this.entries.removeAll(key);
        this.writeOutChanges();
    }

    @Override
    public void readSavedFile() throws IOException {
        Collection<UserListEntry> userListEntries = null;
        BufferedReader reader = null;

        try {
            reader = Files.newReader(this.saveFile, Charsets.UTF_8);
            userListEntries = (Collection) this.gson.fromJson(reader, saveFileFormat);
        } finally {
            IOUtils.closeQuietly(reader);
        }

        if (userListEntries != null) {
            this.values.clear();
            this.entries.clear();
            for (UserListEntry userListEntry : userListEntries) {
                if (userListEntry.getValue() != null) {
                    Object key = this.getObjectKey(userListEntry.getValue());
                    this.values.put(key, userListEntry);
                    this.entries.put(key, (BanEntry) userListEntry);
                }

            }
        }
    }

    @Override
    public Collection<BanEntry> getBans() {
        this.removeExpired();
        return this.entries.values();
    }

    @Override
    public void pardon(Ban ban) {
        this.removeEntry(((BanEntry) ban).getValue());
    }

    @Override
    public void ban(Ban ban) {
        this.addEntry((UserListEntry) ban);
    }

    @Override
    public boolean hasBan(Ban ban) {
        this.removeExpired();
        return this.hasEntry(((UserListEntry) ban).getValue());
    }

    @Override
    public void removeExpired() {
        this.removeFromIterator(this.getValues().values().iterator());
        this.removeFromIterator(this.entries.values().iterator());
    }

    public void removeFromIterator(Iterator<? extends UserListEntry> i) {
        while (i.hasNext()) {
            BanEntry entry = (BanEntry) i.next();
            if (entry.hasBanExpired()) {
                System.out.println("Removing expired: " + entry.getValue() + " "+ entry.getBanEndDate() +  " " + entry.getBanReason());
                i.remove();
            }
        }
    }

}
