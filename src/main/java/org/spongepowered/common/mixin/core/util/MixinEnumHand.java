package org.spongepowered.common.mixin.core.util;

import net.minecraft.util.EnumHand;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Locale;

@Mixin(EnumHand.class)
public abstract class MixinEnumHand implements HandType {


    @Override
    public String getId() {
        return ((EnumHand) (Object) this).name().toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String getName() {
        return ((EnumHand) (Object) this).name();
    }

    @Override
    public Translation getTranslation() {
        return null; // Uhh.... what?
    }
}
