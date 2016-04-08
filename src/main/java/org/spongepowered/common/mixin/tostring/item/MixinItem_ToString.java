package org.spongepowered.common.mixin.tostring.item;

import com.google.common.base.Objects;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public class MixinItem_ToString {

    @Shadow private String unlocalizedName;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("Name", this.unlocalizedName)
                .toString();
    }
}
