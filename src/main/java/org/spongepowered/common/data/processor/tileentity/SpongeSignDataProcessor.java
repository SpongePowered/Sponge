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
package org.spongepowered.common.data.processor.tileentity;

import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.base.Optional;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.component.tileentity.SignComponent;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.DataTransactionBuilder;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.component.tileentity.SpongeSignComponent;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;

public class SpongeSignDataProcessor implements SpongeDataProcessor<SignComponent> {

    private static final DataQuery LINES = of("Lines");

    @Override
    public Optional<SignComponent> build(DataView container) throws InvalidDataException {
        final Optional<List<String>> optRawLines  = container.getStringList(LINES);
        final SignComponent data = new SpongeSignComponent();
        if (optRawLines.isPresent()) {
            final List<String> rawLines = optRawLines.get();
            for (int i = 0; i < rawLines.size(); i++) {
                data.setLine(i, Texts.of(rawLines.get(i)));
            }
        }
        return Optional.of(data);
    }

    @Override
    public SignComponent create() {
        return new SpongeSignComponent();
    }

    @Override
    public Optional<SignComponent> createFrom(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntitySign) {
            final SignComponent signData = new SpongeSignComponent();
            final IChatComponent[] rawLines = ((TileEntitySign) dataHolder).signText;
            for (int i = 0; i < rawLines.length; i++) {
                signData.setLine(i, SpongeTexts.toText(rawLines[i]));
            }
            return Optional.of(signData);
        }
        return Optional.absent();
    }

    @Override
    public Optional<SignComponent> fillData(DataHolder dataHolder, SignComponent manipulator, DataPriority priority) {
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, SignComponent manipulator, DataPriority priority) {
        if (dataHolder instanceof TileEntitySign) {
            final Optional<SignComponent> oldData = ((Sign) dataHolder).getData();
            if (oldData.isPresent()) {
                DataTransactionBuilder builder = DataTransactionBuilder.builder();
                builder.replace(oldData.get());
                for (int i = 0; i < 4; i++) {
                    ((TileEntitySign) dataHolder).signText[i] = SpongeTexts.toComponent(manipulator.getLine(i));
                }
                builder.result(DataTransactionResult.Type.SUCCESS);
                return builder.build();
            }
        }

        return DataTransactionBuilder.fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (dataHolder instanceof TileEntitySign) {
            for (int i = 0; i < 4; i++) {
                ((TileEntitySign) dataHolder).signText[i] = new ChatComponentText("");
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<SignComponent> getFrom(DataHolder dataHolder) {
        return Optional.absent();
    }
}
