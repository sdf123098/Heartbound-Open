package com.cuddly.heartbound.networking.S2C;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record ClothingArmorVisibilityS2CPacket(int entityId, List<Boolean> armor) implements CustomPayload {
   public static final Id<ClothingArmorVisibilityS2CPacket> ID = new Id<>(Identifier.of("heartbound", "sync_clothing_armor"));
   public static final PacketCodec<RegistryByteBuf, ClothingArmorVisibilityS2CPacket> CODEC = new PacketCodec<RegistryByteBuf, ClothingArmorVisibilityS2CPacket>() {
      public ClothingArmorVisibilityS2CPacket decode(RegistryByteBuf buf) {
         int entityId = buf.readVarInt();
         int size = buf.readVarInt();
         List<Boolean> armor = new ArrayList<>();

         for (int i = 0; i < size; i++) {
            armor.add(buf.readBoolean());
         }

         return new ClothingArmorVisibilityS2CPacket(entityId, armor);
      }

      public void encode(RegistryByteBuf buf, ClothingArmorVisibilityS2CPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeVarInt(value.armor().size());

         for (Boolean b : value.armor()) {
            buf.writeBoolean(b);
         }
      }
   };

   @Override
   public Id<? extends CustomPayload> getId() {
      return ID;
   }
}
