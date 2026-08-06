package com.cuddly.heartbound.networking.codec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ExtraPacketCodecs {
   public static final StreamCodec<RegistryFriendlyByteBuf, BlockState> BLOCK_STATE_PACKET_CODEC = StreamCodec.of(
      (buf, state) -> buf.writeVarInt(Block.BLOCK_STATE_REGISTRY.getId(state)), buf -> Block.BLOCK_STATE_REGISTRY.byId(buf.readVarInt())
   );
}
