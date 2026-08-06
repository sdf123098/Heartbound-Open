package com.cuddly.heartbound.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public class HeartboundLangUtils {
   private static final Map<String, String> translations = new HashMap<>();
   private static final Gson GSON = new Gson();

   public static String getStringFromKey(String key, Object... args) {
      if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
         try {
            Class<?> i18n = Class.forName("net.minecraft.client.resource.language.I18n");
            Method translate = i18n.getMethod("translate", String.class, Object[].class);
            Object result = translate.invoke(null, key, args);
            if (result instanceof String) {
               return (String)result;
            }
         } catch (Throwable var5) {
         }
      }

      String base = translations.getOrDefault(key, key);
      return args.length > 0 ? String.format(base, args) : base;
   }

   static {
      try {
         Identifier id = Identifier.fromNamespaceAndPath("heartbound", "lang/en_us.json");

         try (InputStreamReader reader = new InputStreamReader(
               Objects.requireNonNull(HeartboundLangUtils.class.getClassLoader().getResourceAsStream("assets/" + id.getNamespace() + "/" + id.getPath())),
               StandardCharsets.UTF_8
            )) {
            Type type = (new TypeToken<Map<String, String>>() {
            }).getType();
            Map<String, String> data = (Map<String, String>)GSON.fromJson(reader, type);
            if (data != null) {
               translations.putAll(data);
            }
         }
      } catch (Exception var6) {
         System.err.println("[Heartbound] Failed to load server translations: " + var6.getMessage());
      }
   }
}
