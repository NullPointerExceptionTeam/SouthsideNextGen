package dev.southside.module.impl.misc;

import dev.southside.Southside;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.RemoveEntityEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.WorldEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class KillSay extends Module {
   private static final long ATTACK_TTL_MS = 15000L;
   private static final File KILLSAY_FILE = Southside.FOLDER.toPath().resolve("killsay.txt").toFile();
   private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
   private static final Pattern RANDOM_PATTERN = Pattern.compile("\\{random(?::(\\d+))?}");
   private final Map<Integer, Long> attackedEntities = new ConcurrentHashMap<>();
   private final Random random = new Random();
   private List<String> messages = new ArrayList<>();

   public KillSay() {
      super(Category.Misc, "KillSay", "击杀玩家后发送自定义消息");
      this.ensureFileExists();
   }

   @Override
   public void onEnable() {
      this.loadMessages();
      this.attackedEntities.clear();
   }

   @Override
   public void onDisable() {
      this.attackedEntities.clear();
   }

   @EventHandler
   public void onWorld(WorldEvent event) {
      this.attackedEntities.clear();
   }

   @EventHandler
   public void onAttack(AttackEvent event) {
      if (event.stage == Stage.POST) {
         if (event.target != null && event.target instanceof PlayerEntity) {
            if (event.target != mc.player) {
               if (!AntiBot.isBot(event.target)) {
                  this.attackedEntities.put(event.target.getId(), System.currentTimeMillis());
               }
            }
         }
      }
   }

   @EventHandler
   public void onRemoveEntity(RemoveEntityEvent event) {
      if (this.shouldTrigger(event.entity)) {
         this.sendKillMessage(event.entity);
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof EntityStatusS2CPacket packet) {
         if (mc.world != null && mc.player != null) {
            if (packet.getStatus() == 3) {
               Entity entity = packet.getEntity(mc.world);
               if (this.shouldTrigger(entity)) {
                  this.sendKillMessage(entity);
               }
            }
         }
      }
   }

   @EventHandler
   public void onTick(TickEvent event) {
      if (event.getState() == Stage.POST) {
         if (!this.attackedEntities.isEmpty()) {
            long now = System.currentTimeMillis();
            this.attackedEntities.entrySet().removeIf(e -> now - e.getValue() > 15000L);
         }
      }
   }

   private boolean shouldTrigger(Entity entity) {
      if (entity == null || mc.world == null || mc.player == null) {
         return false;
      } else if (!(entity instanceof PlayerEntity)) {
         return false;
      } else if (entity == mc.player) {
         return false;
      } else {
         return AntiBot.isBot(entity) ? false : this.attackedEntities.containsKey(entity.getId());
      }
   }

   private void sendKillMessage(Entity entity) {
      if (this.attackedEntities.containsKey(entity.getId())) {
         this.attackedEntities.remove(entity.getId());
         if (mc.getNetworkHandler() != null) {
            if (!this.messages.isEmpty()) {
               String name = entity.getName().getString();
               if (entity instanceof PlayerEntity player && player.getGameProfile() != null) {
                  name = player.getGameProfile().getName();
               }

               String msg = this.messages.get(this.random.nextInt(this.messages.size()));
               msg = msg.replace("{name}", name);
               msg = this.replaceRandom(msg);
               mc.getNetworkHandler().sendChatMessage(msg);
            }
         }
      }
   }

   private String replaceRandom(String msg) {
      Matcher matcher = RANDOM_PATTERN.matcher(msg);
      StringBuilder sb = new StringBuilder();

      while (matcher.find()) {
         String lenStr = matcher.group(1);
         int len = lenStr != null && !lenStr.isEmpty() ? Integer.parseInt(lenStr) : 5;
         len = Math.max(1, Math.min(len, 50));
         StringBuilder rand = new StringBuilder(len);

         for (int i = 0; i < len; i++) {
            rand.append(
               "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                  .charAt(this.random.nextInt("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".length()))
            );
         }

         matcher.appendReplacement(sb, rand.toString());
      }

      matcher.appendTail(sb);
      return sb.toString();
   }

   private void loadMessages() {
      this.messages.clear();

      try {
         if (!KILLSAY_FILE.exists()) {
            return;
         }

         for (String line : Files.readAllLines(KILLSAY_FILE.toPath(), StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
               this.messages.add(trimmed);
            }
         }
      } catch (IOException var5) {
         Southside.LOGGER.warn("Failed to load killsay.txt", var5);
      }
   }

   private void ensureFileExists() {
      try {
         if (!KILLSAY_FILE.exists()) {
            this.createDefaultFile();
         }
      } catch (IOException var2) {
         Southside.LOGGER.warn("Failed to create killsay.txt", var2);
      }
   }

   private void createDefaultFile() throws IOException {
      if (!KILLSAY_FILE.getParentFile().exists()) {
         KILLSAY_FILE.getParentFile().mkdirs();
      }

      try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(KILLSAY_FILE), StandardCharsets.UTF_8))) {
         writer.write("# KillSay新建文本文档。{name} 是击杀玩家名称占位符, {random} 随机5个字符, {random:10} 随机10个字符");
         writer.newLine();
         writer.write("Ｌ {name}!");
         writer.newLine();
         writer.write("ＦＷ {name}");
         writer.newLine();
         writer.write("{name} 不要忘记每天朗读《欣欣茎》！");
         writer.newLine();
         writer.write("{name} 一破！卧龙出山 你已被 southside 客户端击毙");
         writer.newLine();
         writer.write("{name} 双连！一战成名 你已被 southside 客户端击毙");
         writer.newLine();
         writer.write("{name} 三连！举世皆惊 你已被 southside 客户端击毙");
         writer.newLine();
         writer.write("{name} 四连！天下无敌 你已被 southside 客户端击毙");
         writer.newLine();
         writer.write("{name} 五连！诛天灭地 你已被 southside 客户端击毙");
         writer.newLine();
      }
   }
}
