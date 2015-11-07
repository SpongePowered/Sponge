package org.spongepowered.common.data.processor.value.item;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;

import java.util.Optional;

public final class ItemLockTokenValueProcessor extends AbstractSpongeValueProcessor<ItemStack, String, Value<String>> {

    public ItemLockTokenValueProcessor() {
        super(ItemStack.class, Keys.LOCK_TOKEN);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (supports(container)) {
            set((ItemStack) container, "");
            return DataTransactionBuilder.successNoData();
        }
        return DataTransactionBuilder.failNoData();
    }
    
    @Override
    public boolean supports(ValueContainer<?> container) {
        if (!(container instanceof ItemStack)) {
            return false;
        }
        Item item = ((ItemStack) container).getItem();
        if (!(item instanceof ItemBlock)) {
            return false;
        }
        Block block = ((ItemBlock) item).getBlock();
        if (!(block instanceof ITileEntityProvider)) {
            return false;
        }
        TileEntity tile = ((ITileEntityProvider) block).createNewTileEntity(null, item.getMetadata(((ItemStack) container).getItemDamage()));
        return tile instanceof TileEntityLockable;
    }

    @Override
    protected Value<String> constructValue(String actualValue) {
        return new SpongeValue<String>(Keys.LOCK_TOKEN, "", actualValue);
    }

    @Override
    protected boolean set(ItemStack stack, String value) {
        NBTTagCompound mainCompound = NbtDataUtil.getOrCreateCompound(stack);
        NBTTagCompound tileCompound = NbtDataUtil.getOrCreateSubCompound(mainCompound, NbtDataUtil.BLOCK_ENTITY_TAG);
        LockCode code = new LockCode(value);
        if (code.isEmpty()) {
            tileCompound.removeTag("Lock");
        } else {
            code.toNBT(tileCompound);
        }
        return true;
    }

    @Override
    protected Optional<String> getVal(ItemStack container) {
        if (container.getTagCompound() == null) {
            return Optional.of("");
        }
        NBTTagCompound tileCompound = container.getTagCompound().getCompoundTag(NbtDataUtil.BLOCK_ENTITY_TAG);
        LockCode code = LockCode.fromNBT(tileCompound);
        if (code.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(code.getLock());
    }

    @Override
    protected ImmutableValue<String> constructImmutableValue(String value) {
        return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
    }

}
