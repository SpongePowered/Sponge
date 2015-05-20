package org.spongepowered.common.data.processor.item;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.common.data.DataTransactionBuilder.fail;
import static org.spongepowered.common.data.DataTransactionBuilder.successNoData;

import com.google.common.base.Optional;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagString;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataPriority;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.item.AuthorData;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.data.SpongeDataProcessor;
import org.spongepowered.common.data.manipulator.item.SpongeAuthorData;

public class SpongeAuthorProcessor implements SpongeDataProcessor<AuthorData> {

    @Override
    public Optional<AuthorData> getFrom(DataHolder holder) {
        if (!(holder instanceof ItemStack)) {
            return Optional.absent();
        }
        if (((ItemStack) holder).getItem() != Items.written_book) {
            return Optional.absent();
        }
        if (!((ItemStack) holder).hasTagCompound()) {
            return Optional.absent();
        }
        if (!((ItemStack) holder).getTagCompound().hasKey("author")) {
            return Optional.absent();
        }
        final String rawAuthor = ((ItemStack) holder).getTagCompound().getString("author");
        return Optional.of(create().setValue(Texts.of(rawAuthor)));
    }

    @Override
    public Optional<AuthorData> fillData(DataHolder holder, AuthorData manipulator, DataPriority priority) {
        if (holder instanceof org.spongepowered.api.item.inventory.ItemStack) {
            if (((ItemStack) holder).getItem() != Items.written_book) {
                return Optional.absent();
            }
            switch (checkNotNull(priority)) {
                case PRE_MERGE:
                case DATA_MANIPULATOR:
                    return Optional.of(manipulator);
                default:
                    manipulator.setValue(Texts.of(((ItemStack) holder).getTagCompound().getString("author")));
                    return Optional.of(manipulator);
            }
        }
        return Optional.absent();
    }

    @Override
    public DataTransactionResult setData(DataHolder dataHolder, AuthorData manipulator, DataPriority priority) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() == Items.written_book) {
            ((ItemStack) dataHolder).setTagInfo("author", new NBTTagString(Texts.toPlain(manipulator.getValue())));
            return successNoData();
        }
        return fail(manipulator);
    }

    @Override
    public boolean remove(DataHolder dataHolder) {
        if (dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem() == Items.written_book) {
            ((ItemStack) dataHolder).setTagInfo("author", new NBTTagString("invalid"));
            return true;
        }
        return false;
    }

    @Override
    public Optional<AuthorData> build(DataView container) throws InvalidDataException {
        if (!checkNotNull(container).contains(SpongeAuthorData.AUTHOR)) {
            throw new InvalidDataException("Missing author to construct an AuthorData.");
        }
        final String rawAuthor = container.getString(SpongeAuthorData.AUTHOR).get();
        final AuthorData data = create();
        data.setValue(Texts.of(rawAuthor));
        return Optional.of(data);
    }

    @Override
    public AuthorData create() {
        return new SpongeAuthorData();
    }

    @Override
    public Optional<AuthorData> createFrom(DataHolder dataHolder) {
        if (!(dataHolder instanceof ItemStack)) {
            return Optional.absent();
        }
        if (((ItemStack) dataHolder).getItem() != Items.written_book) {
            return Optional.absent();
        }
        if (!((ItemStack) dataHolder).hasTagCompound()) {
            return Optional.absent();
        }
        final String rawAuthor = ((ItemStack) dataHolder).getTagCompound().getString("author");
        return Optional.of(create().setValue(Texts.of(rawAuthor)));
    }
}