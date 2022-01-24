package io.github.kay9.bundling;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@Mod(Bundling.MOD_ID)
public class Bundling
{
    public static final String MOD_ID = "bundling";
    public static final Logger LOG = LogManager.getLogger(MOD_ID);
    public static final SimpleChannel NETWORK;

    public static final DeferredRegister<Item> ITEM_REGISTRAR = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> LEATHER_BUNDLE = ITEM_REGISTRAR.register("leather_bundle", () -> new BundlingItem(80));
    public static final RegistryObject<Item> SILK_BUNDLE = ITEM_REGISTRAR.register("silk_bundle", () -> new BundlingItem(96));
    public static final RegistryObject<Item> DESIGNER_BUNDLE = ITEM_REGISTRAR.register("designer_bundle", () -> new BundlingItem(112));
    public static final RegistryObject<Item> SILK = ITEM_REGISTRAR.register("silk", () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_MATERIALS)));

    public Bundling()
    {
        ITEM_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());

        if (FMLEnvironment.dist.isClient())
            MinecraftForge.EVENT_BUS.addListener(Bundling::onMouseScroll);
    }

    public static ResourceLocation id(String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void onMouseScroll(ScreenEvent.MouseScrollEvent evt)
    {
        if (!Screen.hasShiftDown() && evt.getScreen() instanceof AbstractContainerScreen<?> screen)
        {
            var mouseOver = screen.getSlotUnderMouse();
            if (mouseOver != null)
            {
                var stack = mouseOver.getItem();
                if (stack.getItem() instanceof BundleItem)
                {
                    evt.setCanceled(true);
                    NETWORK.sendToServer(new CycleBundleIndexPacket((int) -evt.getScrollDelta(), mouseOver.getSlotIndex()));
                }
            }
        }
    }

    static
    {
        final String PROTOCOL_VERSION = "1.0";
        final SimpleChannel network = NetworkRegistry.ChannelBuilder
                .named(id("network"))
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .simpleChannel();

        network.registerMessage(0, CycleBundleIndexPacket.class, CycleBundleIndexPacket::encode, CycleBundleIndexPacket::new, CycleBundleIndexPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        NETWORK = network;
    }
}
