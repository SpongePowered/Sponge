package org.spongepowered.common.data.manipulator.item;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.manipulator.item.AuthorData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.manipulator.AbstractSingleValueData;

public class SpongeAuthorData extends AbstractSingleValueData<Text, AuthorData> implements AuthorData {

    public static final DataQuery AUTHOR = DataQuery.of("Author");

    public SpongeAuthorData() {
        super(AuthorData.class, Texts.of());
    }

    @Override
    public AuthorData copy() {
        return new SpongeAuthorData().setValue(this.getValue());
    }

    @Override
    public int compareTo(AuthorData o) {
        return Texts.toPlain(getValue()).compareTo(Texts.toPlain(o.getValue()));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(AUTHOR, getValue());
    }

    @Override
    public Text getAuthor() {
        return this.getValue();
    }

    @Override
    public AuthorData setAuthor(Text author) {
        return this.setValue(author);
    }
}