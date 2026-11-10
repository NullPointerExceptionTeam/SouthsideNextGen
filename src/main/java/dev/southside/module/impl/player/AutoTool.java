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
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.hit.HitResult;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.MaceItem;

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
        if (AutoTool.mc.player == null || AutoTool.mc.world == null) {
            return;
        }
        if (AutoTool.mc.player.isOnGround()) {
            if (this.peakFallDistance > 0.0f) {
                ++this.groundTicks;
                if (this.groundTicks >= 2) {
                    int bestSlot;
                    this.peakFallDistance = 0.0f;
                    if (((Boolean)this.maceFalling.getValue()).booleanValue() && AutoTool.mc.player.getInventory().selectedSlot == this.getMaceSlot() && (bestSlot = this.getBestWeaponSlot()) != -1 && AutoTool.mc.player.getInventory().selectedSlot != bestSlot) {
                        AutoTool.mc.player.getInventory().selectedSlot = bestSlot;
                    }
                }
            }
        } else {
            this.groundTicks = 0;
            this.peakFallDistance = Math.max(this.peakFallDistance, AutoTool.mc.player.fallDistance);
        }
        if (((Boolean)this.shieldBreak.getValue()).booleanValue() && this.hasSwitched && this.hasAttacked) {
            if (this.originalSlot != -1) {
                AutoTool.mc.player.getInventory().selectedSlot = this.originalSlot;
                this.originalSlot = -1;
            }
            this.hasAttacked = false;
            this.hasSwitched = false;
        }
        if (AutoTool.mc.interactionManager != null && AutoTool.mc.interactionManager.isBreakingBlock()) {
            int bestToolSlot;
            BlockPos blockPos = this.getTargetedBlock();
            if (blockPos == null) {
                return;
            }
            if (!this.work) {
                this.originalSlot = AutoTool.mc.player.getInventory().selectedSlot;
                this.work = true;
                this.revertTicks = 0;
            }
            if ((bestToolSlot = this.getBestToolSlot(blockPos)) != -1 && bestToolSlot != this.currentBestSlot) {
                this.currentBestSlot = bestToolSlot;
                if (AutoTool.mc.player.getInventory().selectedSlot != bestToolSlot) {
                    AutoTool.mc.player.getInventory().selectedSlot = bestToolSlot;
                }
            }
        } else if (this.work) {
            ++this.revertTicks;
            if (this.revertTicks >= ((Number)this.returnDelay.getValue()).intValue()) {
                if (this.originalSlot != -1 && AutoTool.mc.player.getInventory().selectedSlot != this.originalSlot) {
                    AutoTool.mc.player.getInventory().selectedSlot = this.originalSlot;
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
        if (AutoTool.mc.player == null) {
            return;
        }
        Packet rawPacket = event.getPacket();
        if (rawPacket instanceof PlayerInteractEntityC2SPacket) {
            PlayerInteractEntityC2SPacket packet = (PlayerInteractEntityC2SPacket)rawPacket;
            if (packet.type.getType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK && AutoTool.mc.player.getMainHandStack() == this.getBestAxe() && ((Boolean)this.shieldBreak.getValue()).booleanValue()) {
                this.hasAttacked = true;
            }
        }
    }

    @EventHandler
    public void onHeldItem(UpdateHeldItemEvent event) {
        ItemStack originalItem;
        if (!this.ss$sneakAllowed()) {
            return;
        }
        if (((Boolean)this.spoofItem.getValue()).booleanValue() && event.getHand() == Hand.MAIN_HAND && this.work && this.originalSlot != -1 && !(originalItem = AutoTool.mc.player.getInventory().getStack(this.originalSlot)).isEmpty()) {
            event.setItem(originalItem);
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (!this.ss$sneakAllowed()) {
            return;
        }
        if (AutoTool.mc.player == null || AutoTool.mc.interactionManager == null) {
            return;
        }
        if (event.stage == Stage.PRE) {
            PlayerEntity cnm;
            Entity targetEntity;
            if (((Boolean)this.shieldBreak.getValue()).booleanValue() && (targetEntity = event.target) instanceof PlayerEntity && this.isBlockingWithShield(cnm = (PlayerEntity)targetEntity) && this.getBestAxeSlot() != -1) {
                this.currentBestSlot = this.getBestAxeSlot();
            }
            if (((Boolean)this.shieldBreak.getValue()).booleanValue() && this.currentBestSlot == this.getBestAxeSlot() && this.currentBestSlot != -1) {
                this.originalSlot = AutoTool.mc.player.getInventory().selectedSlot;
                AutoTool.mc.player.getInventory().selectedSlot = this.currentBestSlot;
            }
            if (((Boolean)this.shieldBreak.getValue()).booleanValue() && AutoTool.mc.player.getMainHandStack() == this.getBestAxe() && this.getBestAxeSlot() != -1) {
                event.setCancelled(true);
                for (int i = 0; i < 2; ++i) {
                    if (AutoTool.mc.attackCooldown > 0) {
                        return;
                    }
                    if (AutoTool.mc.crosshairTarget == null) {
                        if (AutoTool.mc.interactionManager.hasLimitedAttackSpeed()) {
                            AutoTool.mc.attackCooldown = 10;
                        }
                        return;
                    }
                    AutoTool.mc.interactionManager.syncSelectedSlot();
                    PacketUtils.sendPacket((Packet)PlayerInteractEntityC2SPacket.attack((Entity)event.target, (boolean)AutoTool.mc.player.isSneaking()));
                    AutoTool.mc.player.attack(event.target);
                    AutoTool.mc.player.resetLastAttackedTicks();
                    AutoTool.mc.player.swingHand(Hand.MAIN_HAND);
                    CPSManager.INSTANCE.click();
                    this.hasSwitched = true;
                }
            }
            if (event.target instanceof PlayerEntity) {
                this.equipBestWeapon();
            }
        }
    }

    public void equipBestWeapon() {
        int maceSlot;
        if (!this.ss$sneakAllowed()) {
            return;
        }
        if (!((Boolean)this.autoWeapon.getValue()).booleanValue() || AutoTool.mc.player == null) {
            return;
        }
        ItemStack heldItem = AutoTool.mc.player.getMainHandStack();
        if (this.isHeldGodItem(heldItem)) {
            return;
        }
        if (((Boolean)this.maceFalling.getValue()).booleanValue() && this.peakFallDistance > ((Number)this.maceFallDistance.getValue()).floatValue() && (maceSlot = this.getMaceSlot()) != -1) {
            if (AutoTool.mc.player.getInventory().selectedSlot != maceSlot) {
                AutoTool.mc.player.getInventory().selectedSlot = maceSlot;
            }
            return;
        }
        int bestSlot = this.getBestWeaponSlot();
        if (bestSlot != -1 && AutoTool.mc.player.getInventory().selectedSlot != bestSlot) {
            AutoTool.mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private void resetState() {
        this.originalSlot = -1;
        this.currentBestSlot = -1;
        this.revertTicks = 0;
        this.work = false;
    }

    private BlockPos getTargetedBlock() {
        HitResult ItemStackParticleEffect = AutoTool.mc.crosshairTarget;
        if (ItemStackParticleEffect instanceof BlockHitResult) {
            BlockHitResult blockHitResult = (BlockHitResult)ItemStackParticleEffect;
            return blockHitResult.getBlockPos();
        }
        return null;
    }

    private int getBestToolSlot(BlockPos pos) {
        ClientPlayerEntity player = AutoTool.mc.player;
        float bestSpeed = 0.0f;
        int bestSlot = -1;
        for (int i = 0; i < 9; ++i) {
            float speed;
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty() || ((Boolean)this.godAxe.getValue()).booleanValue() && InventoryUtils.isGodAxe((ItemStack)stack) || !((speed = stack.getMiningSpeedMultiplier(AutoTool.mc.world.getBlockState(pos))) > bestSpeed)) continue;
            bestSpeed = speed;
            bestSlot = i;
        }
        return bestSpeed > 1.0f ? bestSlot : -1;
    }

    private int getBestWeaponSlot() {
        ClientPlayerEntity player = AutoTool.mc.player;
        int bestSlot = -1;
        float bestDamage = -1.0f;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float damage = 0.0f;
            if (stack.getItem() instanceof SwordItem) {
                damage = InventoryUtils.getSwordDamage((ItemStack)stack);
            } else {
                if (!(stack.getItem() instanceof AxeItem) || InventoryUtils.isGodAxe((ItemStack)stack) || !InventoryUtils.isSharpnessAxe((ItemStack)stack)) continue;
                damage = InventoryUtils.getAxeDamage((ItemStack)stack);
            }
            if (!(damage > bestDamage)) continue;
            bestDamage = damage;
            bestSlot = i;
        }
        return bestSlot;
    }

    private int getMaceSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoTool.mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof MaceItem)) continue;
            return i;
        }
        return -1;
    }

    public int getBestAxeSlot() {
        if (AutoTool.mc.player == null) {
            return -1;
        }
        ItemStack bestItem = this.getBestAxe();
        if (bestItem == null) {
            return -1;
        }
        for (int i = 0; i <= 9; ++i) {
            ItemStack stack = AutoTool.mc.player.getInventory().getStack(i);
            if (stack != bestItem) continue;
            return i;
        }
        return -1;
    }

    public ItemStack getBestAxe() {
        if (AutoTool.mc.player == null) {
            return null;
        }
        ItemStack itemStack = null;
        for (int i = 0; i <= 9; ++i) {
            ItemStack stack = AutoTool.mc.player.getInventory().getStack(i);
            if (!(stack.getItem() instanceof AxeItem)) continue;
            itemStack = InventoryUtils.getAxeDamage(itemStack) > InventoryUtils.getAxeDamage((ItemStack)stack) ? itemStack : stack;
        }
        return itemStack;
    }

    public boolean isBlockingWithShield(PlayerEntity player) {
        return player.isBlocking() && this.isHoldingShield(player);
    }

    public boolean isHoldingShield(PlayerEntity player) {
        return player.getMainHandStack().getItem() instanceof ShieldItem || player.getOffHandStack().getItem() instanceof ShieldItem;
    }

    private boolean isHeldGodItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return InventoryUtils.isEndCrystal((ItemStack)stack) || InventoryUtils.isEnchantedGApple((ItemStack)stack) || InventoryUtils.isGodAxe((ItemStack)stack) || InventoryUtils.isKBBall((ItemStack)stack);
    }

    private boolean ss$sneakAllowed() {
        if (!((Boolean)this.sneakOnly.getValue()).booleanValue()) {
            return true;
        }
        if (AutoTool.mc.player != null) {
            return AutoTool.mc.player.isSneaking();
        }
        return false;
    }

    private void ss$finishSneak() {
        if (AutoTool.mc.player != null && this.originalSlot >= 0) {
            AutoTool.mc.player.getInventory().selectedSlot = this.originalSlot;
        }
        this.resetState();
        this.hasSwitched = false;
        this.hasAttacked = false;
        this.groundTicks = 0;
        this.peakFallDistance = 0.0f;
    }
}
