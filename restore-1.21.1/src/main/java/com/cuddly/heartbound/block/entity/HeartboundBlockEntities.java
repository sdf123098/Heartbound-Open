package com.cuddly.heartbound.block.entity;

import com.cuddly.heartbound.Heartbound;
import com.cuddly.heartbound.block.HeartboundBlocks;
import com.cuddly.heartbound.block.entity.entities.FusionTableBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class HeartboundBlockEntities {
   public static BlockEntityType<FusionTableBlockEntity> FUSION_TABLE_BLOCK_ENTITY = Registry.register(
      Registries.BLOCK_ENTITY_TYPE,
      Identifier.of("heartbound", "fusion_table"),
      FabricBlockEntityTypeBuilder.create(FusionTableBlockEntity::new, new Block[]{HeartboundBlocks.FUSION_TABLE}).build()
   );

   public static void registerBlockEntities() {
      Heartbound.LOGGER.info("Registering Block Entities for Heartbound");
   }
}
