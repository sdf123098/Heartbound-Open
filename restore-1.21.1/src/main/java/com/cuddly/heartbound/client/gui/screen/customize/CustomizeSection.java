package com.cuddly.heartbound.client.gui.screen.customize;

import com.cuddly.heartbound.client.gui.screen.CustomizeScreen;
import com.cuddly.heartbound.entity.base.GirlSceneEntity;

public abstract class CustomizeSection<T extends GirlSceneEntity> {
   protected final T entity;
   protected final T previewEntity;

   public CustomizeSection(T entity, T previewEntity) {
      this.entity = entity;
      this.previewEntity = previewEntity;
   }

   public abstract void init(CustomizeScreen<T> var1, CustomizeScreen.LayoutConfig var2, int var3);

   public abstract int render(CustomizeScreen<T> var1, CustomizeScreen.LayoutConfig var2, int var3);

   public int getSpacing() {
      return 10;
   }
}
