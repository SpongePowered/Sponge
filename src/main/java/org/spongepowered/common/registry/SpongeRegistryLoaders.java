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
package org.spongepowered.common.registry;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.arguments.BlockStateArgument;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.CatalogedValueParameters;
import org.spongepowered.api.data.type.BodyPart;
import org.spongepowered.api.data.type.BodyParts;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanType;
import org.spongepowered.api.service.ban.BanTypes;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.service.economy.account.AccountDeletionResultTypes;
import org.spongepowered.common.ban.SpongeBanType;
import org.spongepowered.common.command.brigadier.argument.StandardCatalogedArgumentParser;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigDecimalValueParameter;
import org.spongepowered.common.command.parameter.managed.standard.SpongeBigIntegerValueParameter;
import org.spongepowered.common.data.type.SpongeBodyPart;
import org.spongepowered.common.economy.SpongeAccountDeletionResultType;

@SuppressWarnings("unchecked")
public final class SpongeRegistryLoaders {

    // @formatter:off

    public static RegistryLoader<AccountDeletionResultType> accountDeletionResultType() {
        return RegistryLoader.of(loader -> {
            loader .mapping(SpongeAccountDeletionResultType::new, mapper -> mapper.add(
                  AccountDeletionResultTypes.ABSENT,
                  AccountDeletionResultTypes.FAILED,
                  AccountDeletionResultTypes.SUCCESS,
                  AccountDeletionResultTypes.UNDELETABLE
            ));
        });
    }

    public static RegistryLoader<BanType> banType() {
        return RegistryLoader.of(loader -> {
            loader.add(BanTypes.IP, key -> new SpongeBanType(key, Ban.IP.class));
            loader.add(BanTypes.PROFILE, key -> new SpongeBanType(key, Ban.Profile.class));
        });
    }

    public static RegistryLoader<BodyPart> bodyPart() {
        return RegistryLoader.of(loader -> {
            loader.mapping(SpongeBodyPart::new, mapper -> mapper.add(
                  BodyParts.CHEST,
                  BodyParts.HEAD,
                  BodyParts.LEFT_ARM,
                  BodyParts.LEFT_LEG,
                  BodyParts.RIGHT_ARM,
                  BodyParts.RIGHT_LEG
            ));
        });
    }

    public static RegistryLoader<CatalogedValueParameter<?>> catalogedValueParameter() {
        return RegistryLoader.of(loader -> {
            loader.add(CatalogedValueParameters.BIG_DECIMAL, SpongeBigDecimalValueParameter::new);
            loader.add(CatalogedValueParameters.BIG_INTEGER, SpongeBigIntegerValueParameter::new);
            loader.add(CatalogedValueParameters.BLOCK_STATE, k -> StandardCatalogedArgumentParser.createConverter(k, BlockStateArgument.block(), (reader, cause, state) -> (BlockState) state.getState()));
            loader.add(CatalogedValueParameters.BOOLEAN, k -> StandardCatalogedArgumentParser.createIdentity(k, BoolArgumentType.bool()));
        });
    }

    // @formatter:on
}
