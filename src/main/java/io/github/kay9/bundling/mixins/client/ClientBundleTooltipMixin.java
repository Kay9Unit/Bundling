package io.github.kay9.bundling.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kay9.bundling.BundlingItem;
import io.github.kay9.bundling.access.BundleItemAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BundleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ClientBundleTooltip.class)
public class ClientBundleTooltipMixin
{
    @ModifyConstant(method = "renderImage(Lnet/minecraft/client/gui/Font;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/ItemRenderer;I)V",
            constant = @Constant(intValue = 64))
    private int bundling_overrideWeightLimits(int original)
    {
        var slot = ((AbstractContainerScreen<?>) Minecraft.getInstance().screen).getSlotUnderMouse();
        if (slot != null && slot.getItem().getItem() instanceof BundleItemAccess b) return b.getMaxWeight();
        return 64;
    }

    @ModifyVariable(method = "renderSlot", at = @At(value = "LOAD", ordinal = 3), ordinal = 2, argsOnly = true)
    private int bundling_overrideHighlightIndex(int original, int pX, int pY, int pItemIndex, boolean pIsBundleFull, Font pFont, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset)
    {
        var slot = ((AbstractContainerScreen<?>) Minecraft.getInstance().screen).getSlotUnderMouse();
        if (slot != null && slot.getItem().getItem() instanceof BundleItem)
            return pItemIndex == BundlingItem.getIndex(slot.getItem())? 0 : -1;
        return original;
    }
}
