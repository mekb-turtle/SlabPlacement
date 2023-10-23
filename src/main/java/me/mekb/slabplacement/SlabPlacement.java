package me.mekb.slabplacement;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.UUID;

public class SlabPlacement implements ModInitializer {
    public static String id = "slabplacement";

    public enum SlabPlacementMode {
        DEFAULT,
        TOP,
        BOTTOM
    }

    private static final HashMap<UUID, SlabPlacementMode> slabPlacementModeMap = new HashMap<>();

    public static final Identifier CONFIRMATION_PACKET_ID = new Identifier(SlabPlacement.id, "confirm");

    public static SlabPlacementMode getSlabPlacementMode(PlayerEntity player) {
        SlabPlacementMode result = player == null ? null : slabPlacementModeMap.get(player.getUuid());
        return result == null ? SlabPlacementMode.DEFAULT : result;
    }

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(SlabPlacementPacket.ID, (server, player, handler, buf, responseSender) -> {
            SlabPlacementMode slabPlacementMode = SlabPlacementPacket.read(buf);
            if (slabPlacementMode == null) return;

            UUID uuid = player.getUuid();

            if (!slabPlacementModeMap.containsKey(uuid)) {
                // confirm back to the client that the server has the mod
                ServerPlayNetworking.send(player, CONFIRMATION_PACKET_ID, PacketByteBufs.empty());
            }

            slabPlacementModeMap.put(uuid, slabPlacementMode);
        });

        // clear slab placement mode when player leaves or rejoins

        ServerPlayConnectionEvents.INIT.register((handler, server) -> {
            slabPlacementModeMap.remove(handler.player.getUuid());
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            slabPlacementModeMap.remove(handler.player.getUuid());
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            slabPlacementModeMap.remove(handler.player.getUuid());
        });
    }
}
