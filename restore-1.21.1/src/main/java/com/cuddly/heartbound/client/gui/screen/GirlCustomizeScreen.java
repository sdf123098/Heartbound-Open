package com.cuddly.heartbound.client.gui.screen;

import com.cuddly.heartbound.client.gui.screen.customize.CustomizeSection;
import com.cuddly.heartbound.client.gui.screen.customize.SliderSection;
import com.cuddly.heartbound.client.gui.screen.customize.Vec3dInputSection;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;
import com.cuddly.heartbound.networking.C2S.GirlCustomizeC2SPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class GirlCustomizeScreen extends CustomizeScreen<GirlSceneEntity> {
   private int breastSize;
   private Vec3d breastOffset;

   public GirlCustomizeScreen(int entityId, int previewEntityId) {
      super(Text.translatable("gui.heartbound.customizeGirl"), entityId, previewEntityId, GirlSceneEntity.class);
      this.entity.setGUIOpenState(true);
      if (this.entity != null) {
         this.breastSize = this.entity.getBreastSize();
         this.breastOffset = this.entity.getBreastOffset();
      }
   }

   @Override
   protected void addSections() {
      this.sections
         .add(
            new SliderSection<>(
               this.entity,
               this.previewEntity,
               Text.translatable("gui.heartbound.customize.breastSize"),
               this.entity.getBreastMinSize(),
               this.entity.getBreastMaxSize(),
               () -> this.breastSize,
               value -> this.breastSize = value,
               null
            )
         );
      this.sections
         .add(
            new Vec3dInputSection<>(
               this.entity,
               this.previewEntity,
               Text.translatable("gui.heartbound.customize.breastOffset"),
               () -> this.breastOffset,
               vec3d -> this.breastOffset = vec3d
            )
         );
      this.sections.add(new CustomizeSection<GirlSceneEntity>(this.entity, this.previewEntity) {
         @Override
         public void init(CustomizeScreen<GirlSceneEntity> screen, CustomizeScreen.LayoutConfig layout, int startY) {
         }

         @Override
         public int render(CustomizeScreen<GirlSceneEntity> screen, CustomizeScreen.LayoutConfig layout, int currentY) {
            screen.addWarmButton(layout.centerX, currentY, layout.contentWidth, 22, Text.translatable("gui.heartbound.button.clear"), -7718342, () -> {
               GirlCustomizeScreen.this.onClear();
               screen.close();
            });
            return currentY + 27;
         }
      });
   }

   public void onClear() {
      ClientPlayNetworking.send(new GirlCustomizeC2SPacket(this.entityId, 100, new Vec3d(0.0, 0.0, 0.0)));
   }

   @Override
   protected void onConfirm() {
      ClientPlayNetworking.send(new GirlCustomizeC2SPacket(this.entityId, this.breastSize, this.breastOffset));
   }

   @Override
   protected void applyToPreview() {
      this.previewEntity.setBreastSize(this.breastSize);
      this.previewEntity.setBreastOffset(this.breastOffset);
   }
}
