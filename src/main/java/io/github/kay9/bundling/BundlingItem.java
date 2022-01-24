package io.github.kay9.bundling;

import io.github.kay9.bundling.access.BundleItemAccess;
import io.github.kay9.bundling.mixins.BundleItemMixin;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class BundlingItem extends BundleItem implements BundleItemAccess
{
    private static final String ITEM_INDEX_NBT = "Index";

    private final int maxWeight;

    public BundlingItem(int maxWeight)
    {
        super(new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_TOOLS));
        this.maxWeight = maxWeight;

        if (FMLEnvironment.dist.isClient())
            ItemProperties.register(this, Bundling.id("filled"), (stack, level, entity, seed) ->
                    ((BundlingItem) stack.getItem()).getWeightRatio(stack));
    }

    @Override
    public int getMaxWeight()
    {
        return maxWeight;
    }

    public float getWeightRatio(ItemStack stack)
    {
        return BundleItemMixin.Exposure.getContentWeight(stack) / (float) getMaxWeight();
    }

    public static void cycleIndex(ItemStack stack, int amount)
    {
        var prev = stack.getOrCreateTag().getInt(ITEM_INDEX_NBT);
        setIndex(stack, amount + prev);
    }

    public static void setIndex(ItemStack stack, int index)
    {
        var tag = stack.getOrCreateTag();
        var max = Math.max(tag.getList("Items", 10).size() - 1, 0);
        tag.putInt(ITEM_INDEX_NBT, Mth.clamp(index, 0, max));
    }

    public static int getIndex(ItemStack stack)
    {
        var tag = stack.getTag();
        return tag != null? tag.getInt(ITEM_INDEX_NBT) : 0;
    }
}
