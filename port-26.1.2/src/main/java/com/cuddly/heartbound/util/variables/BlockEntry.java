package com.cuddly.heartbound.util.variables;

import com.cuddly.heartbound.networking.codec.ExtraPacketCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

public record BlockEntry(BlockPos pos, BlockState state) {
   public static final Codec<BlockEntry> CODEC = RecordCodecBuilder.create(
      inst -> inst.group(BlockPos.CODEC.fieldOf("pos").forGetter(BlockEntry::pos), BlockState.CODEC.fieldOf("state").forGetter(BlockEntry::state))
            .apply(inst, BlockEntry::new)
   );
   public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntry> PACKET_CODEC = StreamCodec.composite(
      BlockPos.STREAM_CODEC, BlockEntry::pos, ExtraPacketCodecs.BLOCK_STATE_PACKET_CODEC, BlockEntry::state, BlockEntry::new
   );
}
