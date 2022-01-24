package io.github.kay9.bundling.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kay9.bundling.BundlingItem;
import io.github.kay9.bundling.access.BundleItemAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientBundleTooltip.class)
public class ClientBundleTooltipMixin
{
    @ModifyConstant(method = "renderImage(Lnet/minecraft/client/gui/Font;IILcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/ItemRenderer;I)V",
            constant = @Constant(intValue = 64))
    private int bundling_overrideWeightLimits(int original)
    {
        return ((BundleItemAccess) ((AbstractContainerScreen<?>) Minecraft.getInstance().screen)
                .getSlotUnderMouse()
                .getItem()
                .getItem())
                .getMaxWeight();
    }

//    @Inject(method = "renderSlot(IIIZLnet/minecraft/client/gui/Font;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/entity/ItemRenderer;I)V",
//            cancellable = true,
//            at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V",
//                    shift = At.Shift.AFTER))
//    private void bundling_overrideHighlightIndex(int pX, int pY, int pItemIndex, boolean pIsBundleFull, Font pFont, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset, CallbackInfo ci)
//    {
//        ci.cancel();
//        int index = BundlingItem.getIndex(((AbstractContainerScreen<?>) Minecraft.getInstance().screen).getSlotUnderMouse().getItem());
//        if (pItemIndex == index)
//            AbstractContainerScreen.renderSlotHighlight(pPoseStack, pX + 1, pY + 1, pBlitOffset);
//    }

    @ModifyVariable(method = "renderSlot", at = @At(value = "LOAD", ordinal = 3), ordinal = 2, argsOnly = true)
    private int bundling_overrideHighlightIndex(int original, int pX, int pY, int pItemIndex, boolean pIsBundleFull, Font pFont, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset)
    {
        var index = BundlingItem.getIndex(((AbstractContainerScreen<?>) Minecraft.getInstance().screen).getSlotUnderMouse().getItem());
        return pItemIndex == index? 0 : -1;
    }
}
