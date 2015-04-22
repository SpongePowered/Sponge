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
package org.spongepowered.common.data.manipulators.tiles;

import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.manipulators.tileentities.BannerData;
import org.spongepowered.api.data.manipulators.tileentities.BeaconData;
import org.spongepowered.api.data.manipulators.tileentities.BrewingData;
import org.spongepowered.api.data.manipulators.tileentities.CooldownData;
import org.spongepowered.api.data.manipulators.tileentities.FurnaceData;
import org.spongepowered.api.data.manipulators.tileentities.LockableData;
import org.spongepowered.api.data.manipulators.tileentities.NoteData;
import org.spongepowered.api.data.manipulators.tileentities.SignData;
import org.spongepowered.api.potion.PotionEffectType;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.types.SpongeNotePitch;

public final class TileManipulatorUtility {

    private TileManipulatorUtility() {
    }

    public static boolean fillBannerData(BannerData data, DataHolder holder) {
        // TODO Actually do this one.

        // TODO handle for data from ItemStacks.
        return false;
    }

    public static boolean fillBeaconData(BeaconData data, DataHolder holder) {
        if (holder instanceof TileEntityBeacon) {
            final int primaryId = ((TileEntityBeacon) holder).getField(1);
            final int secondaryId = ((TileEntityBeacon) holder).getField(2);
            if (primaryId == 0) {
                data.setPrimaryEffect(null);
            } else {
                data.setPrimaryEffect((PotionEffectType) Potion.potionTypes[primaryId]);
            }
            if (secondaryId == 0) {
                data.setSecondaryEffect(null);
            } else {
                data.setSecondaryEffect((PotionEffectType) Potion.potionTypes[secondaryId]);
            }
            return true;
        }
        // TODO handle for data from ItemStacks.
        return false;
    }

    public static boolean fillBrewingData(BrewingData data, DataHolder holder) {
        if (holder instanceof TileEntityBrewingStand) {
            final int remainingBrewTime = ((TileEntityBrewingStand) holder).getField(0);
            data.setRemainingBrewTime(remainingBrewTime);
            return true;
        }
        // TODO handle for data from ItemStacks.
        return false;
    }

    public static boolean fillCooldownData(CooldownData data, DataHolder holder) {
        if (holder instanceof TileEntityHopper) {
            final int transferCooldown = ((TileEntityHopper) holder).transferCooldown;
            data.setCooldown(transferCooldown);
            return true;
        }
        // TODO handle for data from ItemStacks.
        return false;
    }

    public static boolean fillFurnaceData(FurnaceData data, DataHolder holder) {
        if (holder instanceof TileEntityFurnace) {
            final int remainingBurnTime = ((TileEntityFurnace) holder).getField(0);
            final int remainingCookTime = ((TileEntityFurnace) holder).getField(3) - ((TileEntityFurnace) holder).getField(2);
            data.setRemainingBurnTime(remainingBurnTime);
            data.setRemainingCookTime(remainingCookTime);
            return true;
        }
        // TODO handle for data from ItemStacks.
        return false;
    }

    public static boolean fillLockableData(LockableData data, DataHolder holder) {
        if (holder instanceof TileEntityLockable) {
            final String lockToken = ((TileEntityLockable) holder).getLockCode().getLock();
            data.setLockToken(lockToken);
            return true;
        }
        // TODO handle for data from ItemStacks.
        return false;
    }

    public static boolean fillNoteData(NoteData data, DataHolder holder) {
        if (holder instanceof TileEntityNote) {
            final byte note = ((TileEntityNote) holder).note;
            data.setNote(new SpongeNotePitch(note, "No idea"));
            return true;
        }
        // TODO handle for data from ItemStacks.
        return false;
    }

    public static boolean fillSignData(SignData data, DataHolder holder) {
        if (holder instanceof TileEntitySign) {
            final IChatComponent[] rawTexts = ((TileEntitySign) holder).signText;
            final Text[] signTexts = new Text[rawTexts.length];
            for (int i = 0; i < rawTexts.length; i++) {
                signTexts[i] = (Text) rawTexts[i]; //TODO Make sure this is actually valid. If not, set something on fire.
            }
            data.setLines(signTexts);
            return true;
        }
        // TODO handle for data from ItemStacks.
        return false;
    }

    // TODO
    /*
    1) For all SpongeManipulators, their fill logic should depend on methods from
       here
    2) Accessing common data from NBTCompound form any "DataHolder" should take place with a
       simple method in here: getCompoundType(CompoundType.ENTITY).getFoo(String):Foo
    3) Accessing specific field variables should likely be left in a specific method
    4) This class will blow up in terms of length and size for each DataManipulator to be
       handled
     */
}
