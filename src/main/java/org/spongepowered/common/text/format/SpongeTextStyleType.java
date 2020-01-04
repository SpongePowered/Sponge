package org.spongepowered.common.text.format;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.registry.MappedRegistry;

public final class SpongeTextStyleType extends SpongeTextStyle implements TextStyle.Type {

    public static SpongeTextStyleType of(TextFormatting formatting) {
        final SimpleRegistry<TextStyle.Type> registry = SpongeImpl.getRegistry().getCatalogRegistry().getRegistry(TextStyle.Type.class);
        TextStyle.Type style = ((MappedRegistry<TextStyle.Type, TextFormatting>) registry).getReverseMapping(formatting);
        if (style == null) {
            style = TextStyles.NONE.get();
        }

        return (SpongeTextStyleType) style;
    }

    private final CatalogKey key;

    public SpongeTextStyleType(CatalogKey key, TextFormatting handle) {
        super(handle);
        this.key = checkNotNull(key);
    }

    @Override
    public CatalogKey getKey() {
        return this.key;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpongeTextStyleType)) {
            return false;
        }

        final SpongeTextStyleType that = (SpongeTextStyleType) o;

        return this.key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(SpongeTextStyle.class)
            .add("key", this.key)
            .toString();
    }
}
