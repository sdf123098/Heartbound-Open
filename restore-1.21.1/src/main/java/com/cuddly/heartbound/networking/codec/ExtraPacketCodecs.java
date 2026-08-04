package com.cuddly.heartbound.networking.codec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public class ExtraPacketCodecs {
   public static final PacketCodec<RegistryByteBuf, BlockState> BLOCK_STATE_PACKET_CODEC = PacketCodec.ofStatic(
      (buf, state) -> buf.writeVarInt(Block.STATE_IDS.getRawId(state)), buf -> Block.STATE_IDS.get(buf.readVarInt())
   );
}
