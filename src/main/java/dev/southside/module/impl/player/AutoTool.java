package dev.southside.module.impl.player;

import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.event.Stage;
import dev.southside.event.events.AttackEvent;
import dev.southside.event.events.PacketEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.event.events.UpdateHeldItemEvent;
import dev.southside.manager.CPSManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.orbit.EventHandler;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.InventoryUtils;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1819;
import net.minecraft.class_1829;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_2596;
import net.minecraft.class_2824;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_9362;

public class AutoTool
extends Module {
    public final Switch sneakOnly;
    public final Switch autoWeapon = new Switch("AutoWeapon", false);
    public final Switch shieldBreak = new Switch("ShieldBreak", false);
    public final Switch maceFalling = new Switch("Mace on Fall", true);
    public final Slider maceFallDistance = new Slider("Mace Fall Distance", 2, 1, 10, 1);
    public final Switch godAxe = new Switch("Check Golden Axe", true);
    public final Switch spoofItem = new Switch("Spoof Item", true);
    public final Slider returnDelay = new Slider("Return Delay", 2, 0, 10, 1);
    private int originalSlot = -1;
    private int currentBestSlot = -1;
    private int revertTicks = 0;
    private int groundTicks = 0;
    public boolean hasSwitched;
    public boolean hasAttacked;
    private boolean work = false;
    private float peakFallDistance = 0.0f;

    public AutoTool() {
        super(Category.Player, "AutoTool", "\u81ea\u52a8\u6253\u80f6");
        this.sneakOnly = new Switch("Sneak Only", true);
    }

    public void initPostRunnable() {
        super.initPostRunnable();
        this.hideIf(this.maceFalling.getLabel(), () -> (Boolean)this.autoWeapon.getValue() == false);
        this.hideIf(this.maceFallDistance.getLabel(), () -> (Boolean)this.autoWeapon.getValue() == false || (Boolean)this.maceFalling.getValue() == false);
    }

    @EventHandler
    public void onTick(TickEvent tickEvent) {
        if (!this.ss$sneakAllowed()) {
            this.ss$finishSneak();
            return;
        }
        if (AutoTool.mc.field_1724 == null || AutoTool.mc.field_1687 == null) {
            return;
        }
        if (AutoTool.mc.field_1724.method_24828()) {
            if (this.peakFallDistance > 0.0f) {
                ++this.groundTicks;
                if (this.groundTicks >= 2) {
                    int bestSlot;
                    this.peakFallDistance = 0.0f;
                    if (((Boolean)this.maceFalling.getValue()).booleanValue() && AutoTool.mc.field_1724.method_31548().field_7545 == this.getMaceSlot() && (bestSlot = this.getBestWeaponSlot()) != -1 && AutoTool.mc.field_1724.method_31548().field_7545 != bestSlot) {
                        AutoTool.mc.field_1724.method_31548().field_7545 = bestSlot;
                    }
                }
            }
        } else {
            this.groundTicks = 0;
            this.peakFallDistance = Math.max(this.peakFallDistance, AutoTool.mc.field_1724.field_6017);
        }
        if (((Boolean)this.shieldBreak.getValue()).booleanValue() && this.hasSwitched && this.hasAttacked) {
            if (this.originalSlot != -1) {
                AutoTool.mc.field_1724.method_31548().field_7545 = this.originalSlot;
                this.originalSlot = -1;
            }
            this.hasAttacked = false;
            this.hasSwitched = false;
        }
        if (AutoTool.mc.field_1761 != null && AutoTool.mc.field_1761.method_2923()) {
            int bestToolSlot;
            class_2338 blockPos = this.getTargetedBlock();
            if (blockPos == null) {
                return;
            }
            if (!this.work) {
                this.originalSlot = AutoTool.mc.field_1724.method_31548().field_7545;
                this.work = true;
                this.revertTicks = 0;
            }
            if ((bestToolSlot = this.getBestToolSlot(blockPos)) != -1 && bestToolSlot != this.currentBestSlot) {
                this.currentBestSlot = bestToolSlot;
                if (AutoTool.mc.field_1724.method_31548().field_7545 != bestToolSlot) {
                    AutoTool.mc.field_1724.method_31548().field_7545 = bestToolSlot;
                }
            }
        } else if (this.work) {
            ++this.revertTicks;
            if (this.revertTicks >= ((Number)this.returnDelay.getValue()).intValue()) {
                if (this.originalSlot != -1 && AutoTool.mc.field_1724.method_31548().field_7545 != this.originalSlot) {
                    AutoTool.mc.field_1724.method_31548().field_7545 = this.originalSlot;
                }
                this.resetState();
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!this.ss$sneakAllowed()) {
            return;
        }
        if (AutoTool.mc.field_1724 == null) {
            return;
        }
        class_2596 class_25962 = event.getPacket();
        if (class_25962 instanceof class_2824) {
            class_2824 packet = (class_2824)class_25962;
            if (packet.field_12871.method_34211() == class_2824.class_5907.field_29172 && AutoTool.mc.field_1724.method_6047() == this.getBestAxe() && ((Boolean)this.shieldBreak.getValue()).booleanValue()) {
                this.hasAttacked = true;
            }
        }
    }

    @EventHandler
    public void onHeldItem(UpdateHeldItemEvent event) {
        class_1799 originalItem;
        if (!this.ss$sneakAllowed()) {
            return;
        }
        if (((Boolean)this.spoofItem.getValue()).booleanValue() && event.getHand() == class_1268.field_5808 && this.work && this.originalSlot != -1 && !(originalItem = AutoTool.mc.field_1724.method_31548().method_5438(this.originalSlot)).method_7960()) {
            event.setItem(originalItem);
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (!this.ss$sneakAllowed()) {
            return;
        }
        if (AutoTool.mc.field_1724 == null || AutoTool.mc.field_1761 == null) {
            return;
        }
        if (event.stage == Stage.PRE) {
            class_1657 cnm;
            class_1297 class_12972;
            if (((Boolean)this.shieldBreak.getValue()).booleanValue() && (class_12972 = event.target) instanceof class_1657 && this.isBlockingWithShield(cnm = (class_1657)class_12972) && this.getBestAxeSlot() != -1) {
                this.currentBestSlot = this.getBestAxeSlot();
            }
            if (((Boolean)this.shieldBreak.getValue()).booleanValue() && this.currentBestSlot == this.getBestAxeSlot() && this.currentBestSlot != -1) {
                this.originalSlot = AutoTool.mc.field_1724.method_31548().field_7545;
                AutoTool.mc.field_1724.method_31548().field_7545 = this.currentBestSlot;
            }
            if (((Boolean)this.shieldBreak.getValue()).booleanValue() && AutoTool.mc.field_1724.method_6047() == this.getBestAxe() && this.getBestAxeSlot() != -1) {
                event.setCancelled(true);
                for (int i = 0; i < 2; ++i) {
                    if (AutoTool.mc.field_1771 > 0) {
                        return;
                    }
                    if (AutoTool.mc.field_1765 == null) {
                        if (AutoTool.mc.field_1761.method_2924()) {
                            AutoTool.mc.field_1771 = 10;
                        }
                        return;
                    }
                    AutoTool.mc.field_1761.method_2911();
                    PacketUtils.sendPacket((class_2596)class_2824.method_34206((class_1297)event.target, (boolean)AutoTool.mc.field_1724.method_5715()));
                    AutoTool.mc.field_1724.method_7324(event.target);
                    AutoTool.mc.field_1724.method_7350();
                    AutoTool.mc.field_1724.method_6104(class_1268.field_5808);
                    CPSManager.INSTANCE.click();
                    this.hasSwitched = true;
                }
            }
            if (event.target instanceof class_1657) {
                this.equipBestWeapon();
            }
        }
    }

    public void equipBestWeapon() {
        int maceSlot;
        if (!this.ss$sneakAllowed()) {
            return;
        }
        if (!((Boolean)this.autoWeapon.getValue()).booleanValue() || AutoTool.mc.field_1724 == null) {
            return;
        }
        class_1799 heldItem = AutoTool.mc.field_1724.method_6047();
        if (this.isHeldGodItem(heldItem)) {
            return;
        }
        if (((Boolean)this.maceFalling.getValue()).booleanValue() && this.peakFallDistance > ((Number)this.maceFallDistance.getValue()).floatValue() && (maceSlot = this.getMaceSlot()) != -1) {
            if (AutoTool.mc.field_1724.method_31548().field_7545 != maceSlot) {
                AutoTool.mc.field_1724.method_31548().field_7545 = maceSlot;
            }
            return;
        }
        int bestSlot = this.getBestWeaponSlot();
        if (bestSlot != -1 && AutoTool.mc.field_1724.method_31548().field_7545 != bestSlot) {
            AutoTool.mc.field_1724.method_31548().field_7545 = bestSlot;
        }
    }

    private void resetState() {
        this.originalSlot = -1;
        this.currentBestSlot = -1;
        this.revertTicks = 0;
        this.work = false;
    }

    private class_2338 getTargetedBlock() {
        class_239 class_2392 = AutoTool.mc.field_1765;
        if (class_2392 instanceof class_3965) {
            class_3965 blockHitResult = (class_3965)class_2392;
            return blockHitResult.method_17777();
        }
        return null;
    }

    private int getBestToolSlot(class_2338 pos) {
        class_746 player = AutoTool.mc.field_1724;
        float bestSpeed = 0.0f;
        int bestSlot = -1;
        for (int i = 0; i < 9; ++i) {
            float speed;
            class_1799 stack = player.method_31548().method_5438(i);
            if (stack.method_7960() || ((Boolean)this.godAxe.getValue()).booleanValue() && InventoryUtils.isGodAxe((class_1799)stack) || !((speed = stack.method_7924(AutoTool.mc.field_1687.method_8320(pos))) > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = i;
        }
        return bestSpeed > 1.0f ? bestSlot : -1;
    }

    private int getBestWeaponSlot() {
        class_746 player = AutoTool.mc.field_1724;
        int bestSlot = -1;
        float bestDamage = -1.0f;
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = player.method_31548().method_5438(i);
            if (stack.method_7960()) continue;
            float damage = 0.0f;
            if (stack.method_7909() instanceof class_1829) {
                damage = InventoryUtils.getSwordDamage((class_1799)stack);
            } else {
                if (!(stack.method_7909() instanceof class_1743) || InventoryUtils.isGodAxe((class_1799)stack) || !InventoryUtils.isSharpnessAxe((class_1799)stack)) continue;
                damage = InventoryUtils.getAxeDamage((class_1799)stack);
            }
            if (!(damage > bestDamage)) continue;
            bestDamage = damage;
            bestSlot = i;
        }
        return bestSlot;
    }

    private int getMaceSlot() {
        for (int i = 0; i < 9; ++i) {
            class_1799 stack = AutoTool.mc.field_1724.method_31548().method_5438(i);
            if (stack.method_7960() || !(stack.method_7909() instanceof class_9362)) continue;
            return i;
        }
        return -1;
    }

    public int getBestAxeSlot() {
        if (AutoTool.mc.field_1724 == null) {
            return -1;
        }
        class_1799 bestItem = this.getBestAxe();
        if (bestItem == null) {
            return -1;
        }
        for (int i = 0; i <= 9; ++i) {
            class_1799 stack = AutoTool.mc.field_1724.method_31548().method_5438(i);
            if (stack != bestItem) continue;
            return i;
        }
        return -1;
    }

    public class_1799 getBestAxe() {
        if (AutoTool.mc.field_1724 == null) {
            return null;
        }
        class_1799 itemStack = null;
        for (int i = 0; i <= 9; ++i) {
            class_1799 stack = AutoTool.mc.field_1724.method_31548().method_5438(i);
            if (!(stack.method_7909() instanceof class_1743)) continue;
            itemStack = InventoryUtils.getAxeDamage(itemStack) > InventoryUtils.getAxeDamage((class_1799)stack) ? itemStack : stack;
        }
        return itemStack;
    }

    public boolean isBlockingWithShield(class_1657 player) {
        return player.method_6039() && this.isHoldingShield(player);
    }

    public boolean isHoldingShield(class_1657 player) {
        return player.method_6047().method_7909() instanceof class_1819 || player.method_6079().method_7909() instanceof class_1819;
    }

    private boolean isHeldGodItem(class_1799 stack) {
        if (stack.method_7960()) {
            return false;
        }
        return InventoryUtils.isEndCrystal((class_1799)stack) || InventoryUtils.isEnchantedGApple((class_1799)stack) || InventoryUtils.isGodAxe((class_1799)stack) || InventoryUtils.isKBBall((class_1799)stack);
    }

    private boolean ss$sneakAllowed() {
        if (!((Boolean)this.sneakOnly.getValue()).booleanValue()) {
            return true;
        }
        if (AutoTool.mc.field_1724 != null) {
            return AutoTool.mc.field_1724.method_5715();
        }
        return false;
    }

    private void ss$finishSneak() {
        if (AutoTool.mc.field_1724 != null && this.originalSlot >= 0) {
            AutoTool.mc.field_1724.method_31548().field_7545 = this.originalSlot;
        }
        this.resetState();
        this.hasSwitched = false;
        this.hasAttacked = false;
        this.groundTicks = 0;
        this.peakFallDistance = 0.0f;
    }
}
