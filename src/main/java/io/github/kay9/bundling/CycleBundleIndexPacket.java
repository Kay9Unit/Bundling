package io.github.kay9.bundling;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CycleBundleIndexPacket
{
    private final int amount;
    private final int slotIndex;

    public CycleBundleIndexPacket(int amount, int inSlot)
    {
        this.amount = amount;
        this.slotIndex = inSlot;
    }

    public CycleBundleIndexPacket(FriendlyByteBuf buffer)
    {
        amount = buffer.readInt();
        slotIndex = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buf)
    {
        buf.writeInt(amount);
        buf.writeInt(slotIndex);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx)
    {
        ctx.get().enqueueWork(() ->
        {
            var player = ctx.get().getSender();
            var stack = player.getInventory().getItem(slotIndex);
            handle(stack, amount);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void handle(ItemStack stack, int amount)
    {
        if (stack.getItem() instanceof BundleItem) BundlingItem.cycleIndex(stack, amount);
        else Bundling.LOG.error("Sent packet to cycle bundle but wrong item was found: '{}'",  stack);
    }
}
