package me.mekb.slabplacement.client;

import me.mekb.slabplacement.SlabPlacement;
import me.mekb.slabplacement.SlabPlacement.SlabPlacementMode;
import me.mekb.slabplacement.SlabPlacementPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import static me.mekb.slabplacement.SlabPlacement.CONFIRMATION_PACKET_ID;

@Environment(EnvType.CLIENT)
public class SlabPlacementClient implements ClientModInitializer {
    private static KeyBinding switchSlabPlacementBind;

    public static SlabPlacementMode slabPlacementMode;
    private static boolean isServerInitialized = false;

    public static boolean getIsServerInitialized() {
        return isServerInitialized;
    }

    @Override
    public void onInitializeClient() {
        slabPlacementMode = SlabPlacementMode.DEFAULT;

        switchSlabPlacementBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + SlabPlacement.id + ".switch",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                KeyBinding.MISC_CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!isServerInitialized) return;

            while (switchSlabPlacementBind.wasPressed()) {
                // cycle to next slab placement mode
                slabPlacementMode = SlabPlacementMode.values()[(slabPlacementMode.ordinal() + 1) % SlabPlacementMode.values().length];

                // send action bar message
                if (client.player != null)
                    client.player.sendMessage(
                            Text.translatable(SlabPlacement.id + ".switch." + slabPlacementMode.toString().toLowerCase()),
                            true);

                sendSlabPlacementModePacket();
            }
        });

        // clear slab placement mode when player leaves or rejoins

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            isServerInitialized = true;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isServerInitialized = false;
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            isServerInitialized = false;
            sendSlabPlacementModePacket();
        });

        ClientPlayNetworking.registerGlobalReceiver(CONFIRMATION_PACKET_ID, (client, handler, buf, responseSender) -> {
            // confirm back from the server that it has the mod too
            isServerInitialized = true;
        });
    }

    private void sendSlabPlacementModePacket() {
        // send packet to server
        ClientPlayNetworking.send(SlabPlacementPacket.ID, SlabPlacementPacket.write(slabPlacementMode));
    }
}
