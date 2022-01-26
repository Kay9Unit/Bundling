package io.github.kay9.bundling.mixins;

import io.github.kay9.bundling.BundlingItem;
import io.github.kay9.bundling.access.BundleItemAccess;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Purpose of this mixin is to eradicate literally everything hardcoded that we need.
 * The two most nostable issues within {@link BundleItem} is the hardcoded item limit,
 * and the direct references to {@code Items.BUNDLE}
 *
 * This mixin attempts to resolve that by making everything instance based.
 */
@Mixin(BundleItem.class)
public class BundleItemMixin implements BundleItemAccess
{
    @ModifyConstant(method = {
            "overrideStackedOnOther(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/inventory/Slot;Lnet/minecraft/world/inventory/ClickAction;Lnet/minecraft/world/entity/player/Player;)Z",
            "getBarWidth(Lnet/minecraft/world/item/ItemStack;)I",
            "appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"
    },
            constant = @Constant(intValue = 64))
    private int bundling_overrideWeightLimits(int original)
    {
        return getMaxWeight();
    }

    @ModifyConstant(method = "add(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)I", constant = @Constant(intValue = 64))
    private static int bundling_overrideWeightLimits(int original, ItemStack bundle, ItemStack inserted)
    {
        return ((BundleItemAccess) bundle.getItem()).getMaxWeight();
    }

    @ModifyConstant(method = "removeOne(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;", constant = @Constant(intValue = 0))
    private static int bundling_useSelectedIndex(int original, ItemStack stack)
    {
        return BundlingItem.getIndex(stack);
    }

    /**
     * Ensures the index pointer is at or below the item list size
     */
    @Inject(method = "removeOne(Lnet/minecraft/world/item/ItemStack;)Ljava/util/Optional;", at = @At("TAIL"))
    private static void bundling_resetIndex(ItemStack i, CallbackInfoReturnable<Optional<ItemStack>> cir)
    {
        BundlingItem.setIndex(i, BundlingItem.getIndex(i));
    }

    @Redirect(method = {
            "getMatchingItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/nbt/ListTag;)Ljava/util/Optional;",
            "getWeight(Lnet/minecraft/world/item/ItemStack;)I"
    },
    at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean bundling_replaceHardcodedChecks(ItemStack stack, Item pItem)
    {
        return stack.getItem() instanceof BundleItem;
    }

    @Override
    public int getMaxWeight()
    {
        return 64;
    }

    // needs to be in its own class to be exposed. hate it.
    @Mixin(BundleItem.class)
    public interface Exposure
    {
        @Invoker("getContentWeight")
        static int getContentWeight(ItemStack stack)
        {
            throw new AssertionError();
        }
    }
}
