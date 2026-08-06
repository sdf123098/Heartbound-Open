package com.cuddly.heartbound.networking.S2C;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ClothingArmorVisibilityS2CPacket(int entityId, List<Boolean> armor) implements CustomPacketPayload {
   public static final Type<ClothingArmorVisibilityS2CPacket> ID = new Type<>(Identifier.fromNamespaceAndPath("heartbound", "sync_clothing_armor"));
   public static final StreamCodec<RegistryFriendlyByteBuf, ClothingArmorVisibilityS2CPacket> CODEC = new StreamCodec<RegistryFriendlyByteBuf, ClothingArmorVisibilityS2CPacket>() {
      public ClothingArmorVisibilityS2CPacket decode(RegistryFriendlyByteBuf buf) {
         int entityId = buf.readVarInt();
         int size = buf.readVarInt();
         List<Boolean> armor = new ArrayList<>();

         for (int i = 0; i < size; i++) {
            armor.add(buf.readBoolean());
         }

         return new ClothingArmorVisibilityS2CPacket(entityId, armor);
      }

      public void encode(RegistryFriendlyByteBuf buf, ClothingArmorVisibilityS2CPacket value) {
         buf.writeVarInt(value.entityId());
         buf.writeVarInt(value.armor().size());

         for (Boolean b : value.armor()) {
            buf.writeBoolean(b);
         }
      }
   };

   @Override
   public Type<? extends CustomPacketPayload> type() {
      return ID;
   }
}
