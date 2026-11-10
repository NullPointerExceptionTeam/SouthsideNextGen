package dev.southside.module.impl.misc;

import cc.polyfrost.oneconfig.config.options.impl.Switch;
import cc.polyfrost.oneconfig.config.options.impl.Text;
import dev.southside.event.events.AddEntityEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.ChatUtils;
import java.util.ArrayList;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;

public class AntiStaff extends Module {
   private final ArrayList<String> staff_Ids = new ArrayList<>();
   public final Switch autoHub = new Switch("Auto Hub", false);
   public final Text hubCommand = new Text("Hub Command", "返回大厅的命令", false, false, "", 1, "/hub");

   public AntiStaff() {
      super(Category.Misc, "AntiStaff");
      this.staff_Ids.add("妖猫");
      this.staff_Ids.add("Andrewkrist");
      this.staff_Ids.add("BACs");
      this.staff_Ids.add("CandyApostle");
      this.staff_Ids.add("chunyi1");
      this.staff_Ids.add("CuteXiaoYY");
      this.staff_Ids.add("Davidsun");
      this.staff_Ids.add("叼口仙气就飘飘");
      this.staff_Ids.add("东海林皇");
      this.staff_Ids.add("抖音_awa马原");
      this.staff_Ids.add("斗战胜佛");
      this.staff_Ids.add("枫萧林然");
      this.staff_Ids.add("Fia9");
      this.staff_Ids.add("Fruit_Candy");
      this.staff_Ids.add("广告君A");
      this.staff_Ids.add("呱太");
      this.staff_Ids.add("Henglie");
      this.staff_Ids.add("LamMolincen_");
      this.staff_Ids.add("Jay__Chou");
      this.staff_Ids.add("Kiana__24");
      this.staff_Ids.add("枕上书、塑望月");
      this.staff_Ids.add("流影只会嘤嘤嘤");
      this.staff_Ids.add("绿豆奶");
      this.staff_Ids.add("Magician");
      this.staff_Ids.add("mc加载");
      this.staff_Ids.add("MnamLeo_");
      this.staff_Ids.add("Kuri_Turgut");
      this.staff_Ids.add("七鹤さん");
      this.staff_Ids.add("Qiiiiiii");
      this.staff_Ids.add("qxtmlc99");
      this.staff_Ids.add("圣上荣耀233");
      this.staff_Ids.add("神坑之逗");
      this.staff_Ids.add("霜月月OO");
      this.staff_Ids.add("Skyfoy");
      this.staff_Ids.add("Stellarcat");
      this.staff_Ids.add("倘若两散不如两难");
      this.staff_Ids.add("TempMono");
      this.staff_Ids.add("无量域雪");
      this.staff_Ids.add("小匪");
      this.staff_Ids.add("小H");
      this.staff_Ids.add("小军君、天使之翼");
      this.staff_Ids.add("xPir4te_");
      this.staff_Ids.add("虚空");
      this.staff_Ids.add("枕上书、傲寒");
      this.staff_Ids.add("枕上书、雪夜");
      this.staff_Ids.add("枕上书、中二少年DL");
   }

   private void onStaffDetected(String name) {
      ChatUtils.warning("Staff Detected! (" + name + ")");
      ChatUtils.warning("Staff Detected! (" + name + ")");
      ChatUtils.warning("Staff Detected! (" + name + ")");
      if (this.autoHub.getValue() && mc.player != null && mc.getNetworkHandler() != null) {
         String cmd = this.hubCommand.getValue().trim();
         if (!cmd.isEmpty()) {
            ChatUtils.warning("Auto Hub → " + cmd);
            if (cmd.startsWith("/")) {
               mc.getNetworkHandler().sendCommand(cmd.substring(1));
            } else {
               mc.getNetworkHandler().sendChatMessage(cmd);
            }
         }
      }
   }

   @EventHandler
   public void onPacket(PacketEvent event) {
      if (event.getPacket() instanceof TeamS2CPacket packet) {
         for (String name : packet.getPlayerNames()) {
            if (this.staff_Ids.contains(name)) {
               this.onStaffDetected(name);
            }
         }
      }
   }

   @EventHandler
   public void onAddE(AddEntityEvent event) {
      if (this.staff_Ids.contains(event.entity.getDisplayName().getString())) {
         this.onStaffDetected(event.entity.getDisplayName().getString());
      }
   }
}
