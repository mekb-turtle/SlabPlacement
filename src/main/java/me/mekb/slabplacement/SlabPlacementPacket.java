package me.mekb.slabplacement;

import me.mekb.slabplacement.SlabPlacement.SlabPlacementMode;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import static me.mekb.slabplacement.SlabPlacement.SlabPlacementMode.DEFAULT;

// client -> server packet
public class SlabPlacementPacket {
    public static final Identifier ID = new Identifier(SlabPlacement.id, "mode");

    public static PacketByteBuf write(SlabPlacementMode value) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(value.ordinal());
        return buf;
    }

    public static SlabPlacementMode read(PacketByteBuf buf) {
        int ordinal = buf.readInt();

        SlabPlacementMode[] values = SlabPlacementMode.values();
        if (ordinal < 0 || ordinal >= values.length) return DEFAULT;
        return values[ordinal];
    }
}
