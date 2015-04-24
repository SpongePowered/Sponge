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
package org.spongepowered.common.data.utils;

import com.google.common.base.Optional;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.block.tile.Sign;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulators.tileentities.SignData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.SpongeDataUtil;
import org.spongepowered.common.data.manipulators.tiles.SpongeSignData;
import org.spongepowered.common.text.SpongeChatComponent;
import org.spongepowered.common.text.SpongeText;

import java.util.Locale;

public class SpongeSignDataBuilder implements SpongeDataUtil<SignData> {

    @Override
    public Optional<SignData> build(DataView container) throws InvalidDataException {
        return null;
    }

    @Override
    public SignData create() {
        return new SpongeSignData();
    }

    @Override
    public Optional<SignData> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntitySign) {
            final SignData data = create();
            final IChatComponent[] rawTexts = ((TileEntitySign) dataHolder).signText;
            final Text[] signTexts = new Text[rawTexts.length];
            for (int i = 0; i < rawTexts.length; i++) {
                signTexts[i] = ((SpongeChatComponent) rawTexts[i]).toText();
            }
            data.setLines(signTexts);
            return Optional.of(data);
        }
        return Optional.absent();
    }

    @Override
    public Optional<SignData> fillData(DataHolder holder, SignData manipulator, DataPriority priority) {
        return null;
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, SignData manipulator, DataPriority priority) {
        if (dataHolder instanceof TileEntitySign) {
            final SignData oldData = ((Sign) dataHolder).getSignData();
            DataTransactionBuilder builder = DataTransactionBuilder.builder();
            builder.replace(oldData);
            for (int i = 0; i < 4; i++) {
                ((TileEntitySign) dataHolder).signText[i] = ((SpongeText) manipulator.getLine(0)).toComponent(Locale.ENGLISH);
            }
            builder.result(DataTransactionResult.Type.SUCCESS);
            return builder.build();
        }

        return DataTransactionBuilder.fail(manipulator);
    }

    @Override
    public DataTransactionResult remove(DataHolder dataHolder, SignData manipulator) {
        if (dataHolder instanceof TileEntitySign) {
            final SignData oldData = ((Sign) dataHolder).getSignData();
            DataTransactionBuilder builder = DataTransactionBuilder.builder();
            builder.replace(oldData);
            for (int i = 0; i < 4; i++) {
                ((TileEntitySign) dataHolder).signText[i] = new ChatComponentText("");
            }
            builder.result(DataTransactionResult.Type.SUCCESS);
            return builder.build();
        }
        return DataTransactionBuilder.fail(manipulator);
    }
}
