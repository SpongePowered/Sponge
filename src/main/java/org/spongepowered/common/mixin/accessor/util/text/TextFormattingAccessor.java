package org.spongepowered.common.mixin.accessor.util.text;

import net.minecraft.util.text.TextFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import javax.annotation.Nullable;

@Mixin(TextFormatting.class)
public interface TextFormattingAccessor {

    @Accessor("color") @Nullable Integer accessor$getColor();
}
