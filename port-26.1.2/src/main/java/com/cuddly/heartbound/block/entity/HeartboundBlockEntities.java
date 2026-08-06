package com.cuddly.heartbound.block.entity;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.block.entity.entities.FusionTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class HeartboundBlockEntities {
   public static BlockEntityType<FusionTableBlockEntity> FUSION_TABLE_BLOCK_ENTITY = Registry.register(
      BuiltInRegistries.BLOCK_ENTITY_TYPE,
      Identifier.fromNamespaceAndPath("heartbound", "fusion_table"),
      FabricBlockEntityTypeBuilder.create(FusionTableBlockEntity::new, new Block[]{HeartboundBlocks.FUSION_TABLE}).build()
   );

   public static void registerBlockEntities() {
      Heartbound.LOGGER.info("Registering Block Entities for Heartbound");
   }
}
