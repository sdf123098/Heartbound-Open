package com.cuddly.heartbound.util.variables;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public class Scene {
   private final String displayName;
   private final int requiredRelationshipLevel;
   private final Scene.SceneOptions options;
   private final Scene.SceneAnimations animations;
   private final SceneType sceneType;
   public static final Scene EMPTY = new Scene("", 0, Scene.SceneOptions.EMPTY, Scene.SceneAnimations.EMPTY, SceneType.ON_PLAYER);
   public static final PacketCodec<RegistryByteBuf, Scene> PACKET_CODEC = PacketCodec.tuple(
      PacketCodecs.STRING,
      Scene::displayName,
      PacketCodecs.VAR_INT,
      Scene::requiredRelationshipLevel,
      Scene.SceneOptions.PACKET_CODEC,
      Scene::options,
      Scene.SceneAnimations.PACKET_CODEC,
      Scene::animations,
      SceneType.PACKET_CODEC,
      Scene::sceneType,
      Scene::new
   );
   public static final Codec<Scene> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codec.STRING.fieldOf("displayName").forGetter(Scene::displayName),
               Codec.INT.fieldOf("requiredRelationshipLevel").forGetter(Scene::requiredRelationshipLevel),
               Scene.SceneOptions.CODEC.fieldOf("options").forGetter(Scene::options),
               Scene.SceneAnimations.CODEC.fieldOf("animations").forGetter(Scene::animations),
               SceneType.CODEC.fieldOf("sceneType").forGetter(Scene::sceneType)
            )
            .apply(instance, Scene::new)
   );

   private Scene(String displayName, int requiredRelationshipLevel, Scene.SceneOptions options, Scene.SceneAnimations animations, SceneType sceneType) {
      this.displayName = displayName;
      this.requiredRelationshipLevel = requiredRelationshipLevel;
      this.options = options;
      this.animations = animations;
      this.sceneType = sceneType;
   }

   public final String displayName() {
      return this.displayName;
   }

   public final int requiredRelationshipLevel() {
      return this.requiredRelationshipLevel;
   }

   public final List<String> introAnim() {
      return this.animations.introAnim;
   }

   public final List<String> slowAnim() {
      return this.animations.slowAnim;
   }

   public final List<String> fastAnim() {
      return this.animations.fastAnim;
   }

   public final String cumAnim() {
      return this.animations.cumAnim;
   }

   public final float cumThreshold() {
      return this.options.cumThreshold;
   }

   public final boolean needsToStrip() {
      return this.options.needsToStrip;
   }

   public final boolean hidePlayer() {
      return this.options.hidePlayer();
   }

   public final SceneType sceneType() {
      return this.sceneType;
   }

   public final boolean useKeyFrameEvents() {
      return this.options.useKeyFrameEvents;
   }

   public final float bedAlignmentOffset() {
      return this.options.bedAlignmentOffset;
   }

   public final String layOnBed() {
      return this.animations.layOnBed;
   }

   public final String bedIdle() {
      return this.animations.bedIdle;
   }

   private Scene.SceneAnimations animations() {
      return this.animations;
   }

   private Scene.SceneOptions options() {
      return this.options;
   }

   public List<String> stationaryIntroAnim() {
      return this.animations.stationaryIntroAnim;
   }

   public String stationaryLoopAnim() {
      return this.animations.stationaryLoopAnim;
   }

   public int amountOfLoops() {
      return this.options.amountOfLoops;
   }

   public static Scene onBed(
      String name,
      int requiredRelationshipLevel,
      List<String> introAnim,
      List<String> slowAnim,
      List<String> fastAnim,
      String cumAnim,
      float cumThreshold,
      boolean needsToStrip,
      boolean useKeyFrameEvents,
      float bedAlignmentOffset,
      String layOnBed,
      String bedIdle
   ) {
      return new Scene(
         name,
         requiredRelationshipLevel,
         Scene.SceneOptions.of(cumThreshold, needsToStrip, useKeyFrameEvents, bedAlignmentOffset),
         Scene.SceneAnimations.of(introAnim, slowAnim, fastAnim, cumAnim, layOnBed, bedIdle),
         SceneType.ON_BED
      );
   }

   public static Scene onPlayer(
      String name,
      int requiredRelationshipLevel,
      List<String> introAnim,
      List<String> slowAnim,
      List<String> fastAnim,
      String cumAnim,
      float cumThreshold,
      boolean needsToStrip,
      boolean useKeyFrameEvents
   ) {
      return new Scene(
         name,
         requiredRelationshipLevel,
         Scene.SceneOptions.of(cumThreshold, needsToStrip, useKeyFrameEvents),
         Scene.SceneAnimations.of(introAnim, slowAnim, fastAnim, cumAnim),
         SceneType.ON_PLAYER
      );
   }

   public static Scene stationaryContact(
      String name,
      int requiredRelationshipLevel,
      List<String> introAnim,
      List<String> slowAnim,
      List<String> fastAnim,
      String cumAnim,
      float cumThreshold,
      boolean needsToStrip,
      boolean useKeyFrameEvents,
      String layDown,
      String idle
   ) {
      return new Scene(
         name,
         requiredRelationshipLevel,
         Scene.SceneOptions.of(cumThreshold, needsToStrip, useKeyFrameEvents),
         Scene.SceneAnimations.of(introAnim, slowAnim, fastAnim, cumAnim, layDown, idle),
         SceneType.STATIONARY_CONTACT
      );
   }

   public static Scene stationaryIntro(
      String name, int requiredRelationshipLevel, List<String> stationaryIntroAnim, String anim, int amountOfLoops, boolean needsToStrip, boolean hidePlayer
   ) {
      return new Scene(
         name,
         requiredRelationshipLevel,
         Scene.SceneOptions.of(needsToStrip, hidePlayer, amountOfLoops),
         Scene.SceneAnimations.of(stationaryIntroAnim, anim),
         SceneType.STATIONARY_INTRO
      );
   }

   public static Scene stationary(String name, int requiredRelationshipLevel, String anim, int amountOfLoops, boolean needsToStrip, boolean hidePlayer) {
      return new Scene(
         name,
         requiredRelationshipLevel,
         Scene.SceneOptions.of(needsToStrip, hidePlayer, amountOfLoops),
         Scene.SceneAnimations.of(new ArrayList<>(), anim),
         SceneType.STATIONARY
      );
   }

   public static record SceneAnimations(
      List<String> introAnim,
      List<String> slowAnim,
      List<String> fastAnim,
      String cumAnim,
      String layOnBed,
      String bedIdle,
      List<String> stationaryIntroAnim,
      String stationaryLoopAnim
   ) {
      public static final Scene.SceneAnimations EMPTY = new Scene.SceneAnimations(
         new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "", "", "", new ArrayList<>(), ""
      );
      public static final Codec<Scene.SceneAnimations> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
                  Codec.STRING.listOf().fieldOf("introAnim").forGetter(Scene.SceneAnimations::introAnim),
                  Codec.STRING.listOf().fieldOf("slowAnim").forGetter(Scene.SceneAnimations::slowAnim),
                  Codec.STRING.listOf().fieldOf("fastAnim").forGetter(Scene.SceneAnimations::fastAnim),
                  Codec.STRING.fieldOf("cumAnim").forGetter(Scene.SceneAnimations::cumAnim),
                  Codec.STRING.fieldOf("layOnBed").forGetter(Scene.SceneAnimations::layOnBed),
                  Codec.STRING.fieldOf("bedIdle").forGetter(Scene.SceneAnimations::bedIdle),
                  Codec.STRING.listOf().fieldOf("stationaryIntroAnim").forGetter(Scene.SceneAnimations::stationaryIntroAnim),
                  Codec.STRING.fieldOf("stationaryLoopAnim").forGetter(Scene.SceneAnimations::stationaryLoopAnim)
               )
               .apply(instance, Scene.SceneAnimations::new)
      );
      public static final PacketCodec<RegistryByteBuf, Scene.SceneAnimations> PACKET_CODEC = new PacketCodec<RegistryByteBuf, Scene.SceneAnimations>() {
         public Scene.SceneAnimations decode(RegistryByteBuf buf) {
            List<String> introAnim = (List<String>)PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).decode(buf);
            List<String> slowAnim = (List<String>)PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).decode(buf);
            List<String> fastAnim = (List<String>)PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).decode(buf);
            String cumAnim = PacketCodecs.STRING.decode(buf);
            String layOnBed = PacketCodecs.STRING.decode(buf);
            String bedIdle = PacketCodecs.STRING.decode(buf);
            List<String> stationaryIntroAnim = (List<String>)PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).decode(buf);
            String stationaryLoopAnim = PacketCodecs.STRING.decode(buf);
            return new Scene.SceneAnimations(introAnim, slowAnim, fastAnim, cumAnim, layOnBed, bedIdle, stationaryIntroAnim, stationaryLoopAnim);
         }

         public void encode(RegistryByteBuf buf, Scene.SceneAnimations value) {
            PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).encode(buf, new ArrayList<>(value.introAnim()));
            PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).encode(buf, new ArrayList<>(value.slowAnim()));
            PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).encode(buf, new ArrayList<>(value.fastAnim()));
            PacketCodecs.STRING.encode(buf, value.cumAnim());
            PacketCodecs.STRING.encode(buf, value.layOnBed());
            PacketCodecs.STRING.encode(buf, value.bedIdle());
            PacketCodecs.collection(ArrayList::new, PacketCodecs.STRING).encode(buf, new ArrayList<>(value.stationaryIntroAnim()));
            PacketCodecs.STRING.encode(buf, value.stationaryLoopAnim());
         }
      };

      public static Scene.SceneAnimations of(List<String> stationaryIntroAnim, String stationaryLoopAnim) {
         return new Scene.SceneAnimations(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "", "", "", stationaryIntroAnim, stationaryLoopAnim);
      }

      public static Scene.SceneAnimations of(
         List<String> introAnim, List<String> slowAnim, List<String> fastAnim, String cumAnim, String layOnBed, String bedIdle
      ) {
         return new Scene.SceneAnimations(introAnim, slowAnim, fastAnim, cumAnim, layOnBed, bedIdle, new ArrayList<>(), "");
      }

      public static Scene.SceneAnimations of(List<String> introAnim, List<String> slowAnim, List<String> fastAnim, String cumAnim) {
         return new Scene.SceneAnimations(introAnim, slowAnim, fastAnim, cumAnim, "", "", new ArrayList<>(), "");
      }
   }

   public static record SceneOptions(
      float cumThreshold, boolean needsToStrip, boolean useKeyFrameEvents, boolean hidePlayer, float bedAlignmentOffset, int amountOfLoops
   ) {
      public static final Scene.SceneOptions EMPTY = new Scene.SceneOptions(0.0F, false, false, false, 0.0F, 0);
      public static final Codec<Scene.SceneOptions> CODEC = RecordCodecBuilder.create(
         instance -> instance.group(
                  Codec.FLOAT.fieldOf("cumThreshold").forGetter(Scene.SceneOptions::cumThreshold),
                  Codec.BOOL.fieldOf("needsToStrip").forGetter(Scene.SceneOptions::needsToStrip),
                  Codec.BOOL.fieldOf("useKeyFrameEvents").forGetter(Scene.SceneOptions::useKeyFrameEvents),
                  Codec.BOOL.fieldOf("hidePlayer").forGetter(Scene.SceneOptions::hidePlayer),
                  Codec.FLOAT.fieldOf("bedAlignmentOffset").forGetter(Scene.SceneOptions::bedAlignmentOffset),
                  Codec.INT.fieldOf("amountOfLoops").forGetter(Scene.SceneOptions::amountOfLoops)
               )
               .apply(instance, Scene.SceneOptions::new)
      );
      public static final PacketCodec<RegistryByteBuf, Scene.SceneOptions> PACKET_CODEC = new PacketCodec<RegistryByteBuf, Scene.SceneOptions>() {
         public Scene.SceneOptions decode(RegistryByteBuf buf) {
            float cumThreshold = buf.readFloat();
            boolean needsToStrip = buf.readBoolean();
            boolean useKeyFrameEvents = buf.readBoolean();
            boolean hidePlayer = buf.readBoolean();
            float bedAlignmentOffset = buf.readFloat();
            int amountOfLoops = buf.readVarInt();
            return new Scene.SceneOptions(cumThreshold, needsToStrip, useKeyFrameEvents, hidePlayer, bedAlignmentOffset, amountOfLoops);
         }

         public void encode(RegistryByteBuf buf, Scene.SceneOptions value) {
            buf.writeFloat(value.cumThreshold());
            buf.writeBoolean(value.needsToStrip());
            buf.writeBoolean(value.useKeyFrameEvents());
            buf.writeBoolean(value.hidePlayer());
            buf.writeFloat(value.bedAlignmentOffset());
            buf.writeVarInt(value.amountOfLoops());
         }
      };

      public static Scene.SceneOptions of(float cumThreshold, boolean needsToStrip, boolean useKeyFrameEvents, float bedAlignmentOffset) {
         return new Scene.SceneOptions(cumThreshold, needsToStrip, useKeyFrameEvents, false, bedAlignmentOffset, 0);
      }

      public static Scene.SceneOptions of(float cumThreshold, boolean needsToStrip, boolean useKeyFrameEvents) {
         return new Scene.SceneOptions(cumThreshold, needsToStrip, useKeyFrameEvents, false, 0.0F, 0);
      }

      public static Scene.SceneOptions of(boolean needsToStrip, boolean hidePlayer, int amountOfLoops) {
         return new Scene.SceneOptions(0.0F, needsToStrip, false, hidePlayer, 0.0F, amountOfLoops);
      }
   }
}
