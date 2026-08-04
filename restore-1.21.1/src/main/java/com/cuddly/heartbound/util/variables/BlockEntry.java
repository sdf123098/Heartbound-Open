package com.cuddly.heartbound.util.variables;

import com.cuddly.heartbound.networking.codec.ExtraPacketCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.BlockPos;

public record BlockEntry(BlockPos pos, BlockState state) {
   public static final Codec<BlockEntry> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(BlockPos.CODEC.fieldOf("pos").forGetter(BlockEntry::pos), BlockState.CODEC.fieldOf("state").forGetter(BlockEntry::state))
            .apply(inst, BlockEntry::new)
   );
   public static final PacketCodec<RegistryByteBuf, BlockEntry> PACKET_CODEC = PacketCodec.tuple(
      BlockPos.PACKET_CODEC, BlockEntry::pos, ExtraPacketCodecs.BLOCK_STATE_PACKET_CODEC, BlockEntry::state, BlockEntry::new
   );
}
