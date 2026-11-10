package dev.southside.module.impl.combat;

import cc.polyfrost.oneconfig.config.core.OneColor;
import cc.polyfrost.oneconfig.config.options.impl.Dropdown;
import cc.polyfrost.oneconfig.config.options.impl.Slider;
import cc.polyfrost.oneconfig.config.options.impl.Switch;
import dev.southside.Southside;
import dev.southside.event.events.PlayerUpdateEvent;
import dev.southside.event.events.Render3DEvent;
import dev.southside.event.events.RotationAppliedEvent;
import dev.southside.event.events.RotationEvent;
import dev.southside.event.events.TickEvent;
import dev.southside.manager.CPSManager;
import dev.southside.module.Category;
import dev.southside.module.Module;
import dev.southside.module.impl.combat.AutoRod;
import dev.southside.module.impl.combat.Criticals;
import dev.southside.module.impl.combat.KBController;
import dev.southside.module.impl.combat.Velocity;
import dev.southside.module.impl.movement.NoSlowTest;
import dev.southside.module.impl.player.AutoTool;
import dev.southside.module.impl.render.OldHitting;
import dev.southside.module.impl.world.Scaffold;
import dev.southside.orbit.EventHandler;
import dev.southside.render.Render3D;
import dev.southside.utils.math.AimMath;
import dev.southside.utils.math.RandomUtils;
import dev.southside.utils.misc.ClientUtils;
import dev.southside.utils.misc.MSTimer;
import dev.southside.utils.network.PacketUtils;
import dev.southside.utils.player.BoundBoxUtils;
import dev.southside.utils.player.PlayerUtils;
import dev.southside.utils.player.TargetUtils;
import dev.southside.utils.raytrace.ClientRayTraceUtil;
import dev.southside.utils.render.ColorUtils;
import dev.southside.utils.render.MarkDrawer;
import dev.southside.utils.render.animation.TimerUtil;
import dev.southside.utils.rotation.RotationUtils;
import dev.southside.utils.rotation.MoveFixMode;
import dev.southside.utils.types.Rotation;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.util.Hand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.Box;

public class KillAura
extends Module {
    public static KillAura INSTANCE;
    public Slider range = new Slider("Range", (Number)3.0, (Number)3.0, (Number)8.0, 0.01);
    public Slider discoverOffset = new Slider("Discover Offset", (Number)1.0, (Number)1.0, (Number)3.0, 0.01);
    public Slider minCPS = new Slider("Min CPS", 8, 0, 20, 1);
    public Slider maxCps = new Slider("Max CPS", 10, 0, 20, 1);
    public Dropdown targetMode = new Dropdown("Target Mode", "Switch", "Switch", "Single", "Multi");
    public Slider switchDelay = new Slider("Switch delay", (Number)500.0, (Number)0.0, (Number)1000.0, 10.0);
    public Dropdown rotationMode = new Dropdown("Rotation Mode", "Normal", "Normal", "Legit", "Better", "Smart", "None");
    public Dropdown sortingMode = new Dropdown("Sorted by", "Closest Distance", "Closest Distance", "Lowest Armor", "Lowest Health");
    public Switch fixRotation = new Switch("Fix Rotation", true);
    public Switch attackCooldown = new Switch("Attack Cooldown", false);
    public Switch checkRod = new Switch("Check Auto FishRod", true);
    public Switch rayCast = new Switch("RayCast", true);
    public Switch rotOnlyRay = new Switch("Rotation Only RayCast", true);
    public Switch noise = new Switch("Noise", false);
    public static final Switch keepSprint;
    private final Switch auto_disable = new Switch("Auto Disable", false);
    public Dropdown markMode = new Dropdown("Mark Mode", "NurikZapen", "NurikZapen", "Circle", "ThunderHack2", "Box", "None");
    public Switch blockingAnimation = new Switch("Blocking Animation", true);
    private final Switch healthCol = new Switch("Health Color", false);
    private final cc.polyfrost.oneconfig.config.options.impl.Color sideColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Side Color", true, new OneColor(255, 10, 10, 70));
    private final cc.polyfrost.oneconfig.config.options.impl.Color lineColor = new cc.polyfrost.oneconfig.config.options.impl.Color("Line Color", true, new OneColor(255, 10, 10, 150));
    public static List<Entity> targets;
    public static Map<Entity, Rotation> attackableTargets;
    private final TimerUtil switchTimer = new TimerUtil();
    public static final MSTimer msTimer;
    public static Rotation rotation;
    public boolean waitAttack = false;
    private boolean isBlocking = false;
    private boolean bypassKS = true;
    public static boolean lastAttackSprint;
    private double cps;
    public static Entity target;
    public int index = 0;

    public KillAura() {
        super(Category.Combat, "KillAura", "Automatically attacks entities", 82);
        INSTANCE = this;
    }

    public static boolean isBlocking() {
        return INSTANCE.isEnabled() && KillAura.INSTANCE.isBlocking && (Boolean)KillAura.INSTANCE.blockingAnimation.getValue() != false;
    }

    @Override
    public void initPostRunnable() {
        super.initPostRunnable();
        this.hideIf(this.switchDelay.getLabel(), () -> !this.targetMode.getMode().equals("Switch"));
        this.hideIf(this.blockingAnimation.getLabel(), () -> !Southside.INSTANCE.getModuleManager().get(OldHitting.class).isEnabled());
        this.hideIf(this.healthCol.getLabel(), () -> !this.markMode.isMode("Box"));
        this.hideIf(this.sideColor.getLabel(), () -> !this.markMode.isMode("Box"));
        this.hideIf(this.lineColor.getLabel(), () -> !this.markMode.isMode("Box"));
        this.hideIf(this.noise.getLabel(), () -> !this.rotationMode.isMode("Normal"));
        this.addRangedValueRestrict(this.minCPS, this.maxCps);
    }

    @Override
    public void onEnable() {
        this.isBlocking = false;
        this.waitAttack = false;
        targets.clear();
        this.switchTimer.reset();
        target = null;
        this.bypassKS = true;
        attackableTargets.clear();
        this.index = 0;
        this.cps = RandomUtils.nextDouble(((Number)this.minCPS.getValue()).intValue(), ((Number)this.maxCps.getValue()).intValue());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.isBlocking = false;
        target = null;
        this.bypassKS = true;
        attackableTargets.clear();
        targets.clear();
        this.index = 0;
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (target != null) {
            float f = ((LivingEntity)target).getHealth();
            Objects.requireNonNull((LivingEntity)target);
            Color healthColor = ColorUtils.getHealthColor(f, 20.0f);
            double moveX = target.getX() - KillAura.target.prevX;
            double moveY = target.getY() - KillAura.target.prevY;
            double moveZ = target.getZ() - KillAura.target.prevZ;
            float tickDelta = event.getTickCounter();
            switch (this.markMode.getMode()) {
                case "None": {
                    break;
                }
                case "NurikZapen": {
                    Render3D.end();
                    MarkDrawer.renderNurikZapen(target);
                    Render3D.begin(event.getMatrixStack());
                    break;
                }
                case "Circle": {
                    Render3D.end();
                    MarkDrawer.drawCircleEsp(event.getMatrixStack(), target);
                    Render3D.begin(event.getMatrixStack());
                    break;
                }
                case "ThunderHack2": {
                    Render3D.end();
                    MarkDrawer.renderTH(14, 8, 1.8f, 3.0f, target);
                    Render3D.begin(event.getMatrixStack());
                    break;
                }
                case "Box": {
                    Box box = target.getBoundingBox().expand(0.1, 0.2, 0.1).offset(-moveX + moveX * (double)tickDelta, -moveY + moveY * (double)tickDelta, -moveZ + moveZ * (double)tickDelta);
                    Color color1 = (Boolean)this.healthCol.getValue() != false ? new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 50) : ((OneColor)this.sideColor.getValue()).toJavaColor();
                    Color color2 = (Boolean)this.healthCol.getValue() != false ? new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 90) : ((OneColor)this.lineColor.getValue()).toJavaColor();
                    Render3D.boxSides(box, color1);
                    Render3D.boxLines(box, color2);
                }
            }
        }
    }

    @EventHandler(priority=-100)
    public void onRotation(RotationEvent event) {
        MarkDrawer.tick();
        boolean bl = this.isBlocking = target != null;
        if (ClientUtils.nullCheck()) {
            return;
        }
        if (this.isHoldingEndCrystal()) {
            target = null;
            this.bypassKS = true;
            attackableTargets.clear();
            this.waitAttack = false;
            return;
        }
        this.updateTargets();
        if (targets.isEmpty()) {
            return;
        }
        this.updateTarget();
        if (target == null || !target.isAlive() || !KillAura.mc.player.canSee(target)) {
            return;
        }
        this.getRotation();
        this.waitAttack = true;
    }

    @EventHandler(priority=-200)
    public void onRotationApplied(RotationAppliedEvent event) {
        if (ClientUtils.nullCheck()) {
            return;
        }
        if (this.isHoldingEndCrystal()) {
            return;
        }
        if (target == null || !target.isAlive() || !KillAura.mc.player.canSee(target) || ((Boolean)this.rayCast.getValue()).booleanValue() && !this.inRaycast(target) && !this.targetMode.isMode("Multi") || KillAura.mc.player.isUsingItem()) {
            return;
        }
        KBController kbController = Southside.INSTANCE.getModuleManager().get(KBController.class);
        if (kbController.isBuffering()) {
            return;
        }
        if (this.waitAttack) {
            this.checkAndAttack();
            this.waitAttack = false;
        }
    }

    @EventHandler
    public void onUpdate(PlayerUpdateEvent event) {
        this.setSuffix(this.targetMode.getMode());
    }

    public void getRotation() {
        if (target == null || KillAura.mc.player == null) {
            return;
        }
        Rotation rotation = null;
        if (this.targetMode.isMode("Multi")) {
            Rotation targetRotation;
            if (attackableTargets.isEmpty()) {
                return;
            }
            KillAura.rotation = targetRotation = attackableTargets.get(target);
            RotationUtils.setRotation(targetRotation, this.rotationPriority(), MoveFixMode.Silent);
            return;
        }
        switch (this.rotationMode.getMode()) {
            case "Normal": {
                rotation = RotationUtils.getRotationByVector(KillAura.mc.player.getEyePos(), BoundBoxUtils.getMinDistanceVec3dByBox(KillAura.mc.player.getEyePos(), target.getBoundingBox()), (Boolean)this.noise.getValue());
                break;
            }
            case "Legit": {
                rotation = RotationUtils.calculate(target, ((Number)this.range.getValue()).doubleValue(), 0.0, 1.0f, 0.0f);
                break;
            }
            case "Better": {
                rotation = RotationUtils.getBetterRotation(target, ((Number)this.range.getValue()).doubleValue());
                break;
            }
            case "Smart": {
                rotation = AimMath.toRotation(AimMath.computeBestAimToEntity(KillAura.mc.player, target, mc.getRenderTickCounter().getTickDelta(false)));
            }
        }
        if (((Boolean)this.fixRotation.getValue()).booleanValue() && rotation != null) {
            rotation.fixedSensitivity(((Double)KillAura.mc.options.getMouseSensitivity().getValue()).floatValue());
        }
        KillAura.rotation = rotation;
        if (((Boolean)this.rotOnlyRay.getValue()).booleanValue() && rotation != null && !ClientRayTraceUtil.didHitEntity(rotation.yaw, rotation.pitch, ((Number)this.range.getValue()).floatValue(), target)) {
            return;
        }
        RotationUtils.setRotation(rotation, this.rotationPriority(), MoveFixMode.Silent);
    }

    public boolean inRaycast(Entity entity) {
        if (entity == null) {
            return false;
        }
        return ClientRayTraceUtil.didHitEntity(RotationUtils.getRotationSafely().yaw, RotationUtils.getRotationSafely().pitch, this.range.getDouble(), entity) && KillAura.mc.currentScreen == null;
    }

    public void checkAndAttack() {
        if (KillAura.mc.player == null || KillAura.mc.player.isUsingItem()) {
            return;
        }
        if (this.isHoldingEndCrystal()) {
            return;
        }
        if (KillAura.mc.player.getMainHandStack().getItem() == Items.ENDER_PEARL) {
            return;
        }
        if (((Boolean)this.checkRod.getValue()).booleanValue() && Southside.INSTANCE.getModuleManager().get(AutoRod.class).isRodActive) {
            return;
        }
        if (((Boolean)this.attackCooldown.getValue()).booleanValue() && KillAura.mc.player.getAttackCooldownProgress(0.0f) < 1.0f) {
            return;
        }
        if (target == null || !target.isAlive()) {
            return;
        }
        AutoTool autoTool = Southside.INSTANCE.getModuleManager().get(AutoTool.class);
        if (autoTool.isEnabled()) {
            autoTool.equipBestWeapon();
        }
        if (msTimer.hasTimePassed((long)(1000.0 / this.cps))) {
            this.cps = RandomUtils.nextDouble(((Number)this.minCPS.getValue()).intValue(), ((Number)this.maxCps.getValue()).intValue());
            if (this.targetMode.isMode("Multi") && !attackableTargets.isEmpty()) {
                attackableTargets.forEach((entity, targetRotation) -> {
                    PacketUtils.spoofRotation(targetRotation);
                    this.attack((Entity)entity);
                });
                msTimer.reset();
                return;
            }
            if (!(!((Boolean)keepSprint.getValue()).booleanValue() || this.bypassKS || !KillAura.mc.player.isSprinting() || Velocity.attack > 0 || Velocity.gotKB || KillAura.mc.player.isOnGround() && KillAura.mc.options.jumpKey.isPressed())) {
                PlayerUtils.stopSprint();
                return;
            }
            this.attack(target);
            msTimer.reset();
            lastAttackSprint = KillAura.mc.player.lastSprinting;
        } else if (!((Boolean)keepSprint.getValue()).booleanValue() && Southside.INSTANCE.getModuleManager().isEnabled(Criticals.class) && Criticals.mode.isMode("Packet") && !Velocity.gotKB && lastAttackSprint && KillAura.mc.player.lastSprinting) {
            PlayerUtils.stopSprint();
        }
    }

    public void attack(Entity targetEntity) {
        if (!CPSManager.INSTANCE.cancelAttack(20)) {
            KillAura.mc.interactionManager.attackEntity((PlayerEntity)KillAura.mc.player, targetEntity);
            KillAura.mc.player.swingHand(Hand.MAIN_HAND);
            CPSManager.INSTANCE.click();
        }
        this.bypassKS = false;
    }

    public void updateTargets() {
        targets.clear();
        if (!this.isExclude()) {
            for (Entity entity : KillAura.mc.world.getEntities()) {
                if (!TargetUtils.isTarget(entity, true, true) || !(BoundBoxUtils.getMinDistanceVec3dByBox(KillAura.mc.player.getEyePos(), entity.getBoundingBox()).distanceTo(KillAura.mc.player.getEyePos()) <= ((Number)this.range.getValue()).doubleValue() + ((Number)this.discoverOffset.getValue()).doubleValue())) continue;
                targets.add(entity);
                if (this.targetMode.isMode("Multi")) {
                    Rotation targetRotation = AimMath.toRotation(AimMath.computeBestAimToEntity(KillAura.mc.player, entity, mc.getRenderTickCounter().getTickDelta(false)));
                    if (!ClientRayTraceUtil.didHitEntity(targetRotation.yaw, targetRotation.pitch, ((Number)this.range.getValue()).doubleValue(), entity)) continue;
                    attackableTargets.put(entity, targetRotation);
                    continue;
                }
                if (attackableTargets.isEmpty()) continue;
                attackableTargets.clear();
            }
        }
        if (targets.isEmpty()) {
            target = null;
            this.bypassKS = true;
            attackableTargets.clear();
        } else {
            targets.sort(Comparator.comparingDouble(this::getEntitySortValue));
        }
    }

    public boolean isExclude() {
        return Southside.INSTANCE.getModuleManager().isEnabled(Scaffold.class) || NoSlowTest.shouldEnable;
    }

    public void updateTarget() {
        if (targets.isEmpty()) {
            return;
        }
        if (this.index >= targets.size()) {
            this.index = 0;
        }
        target = BoundBoxUtils.getMinDistanceVec3dByBox(KillAura.mc.player.getEyePos(), targets.get(this.index).getBoundingBox()).distanceTo(KillAura.mc.player.getEyePos()) <= ((Number)this.range.getValue()).doubleValue() ? targets.get(this.index) : targets.getFirst();
        if (targets.size() > 1 && this.targetMode.getMode().equals("Switch") && this.switchTimer.hasReached(((Number)this.switchDelay.getValue()).doubleValue())) {
            ++this.index;
            this.switchTimer.reset();
        }
        if (targets.size() > 1 && this.targetMode.getMode().equals("Single")) {
            if (BoundBoxUtils.getMinDistanceVec3dByBox(KillAura.mc.player.getEyePos(), target.getBoundingBox()).distanceTo(KillAura.mc.player.getEyePos()) > ((Number)this.range.getValue()).doubleValue() + ((Number)this.discoverOffset.getValue()).doubleValue()) {
                ++this.index;
            } else if (!target.isAlive()) {
                ++this.index;
            }
        }
    }

    private double getEntitySortValue(Entity entity) {
        if (KillAura.mc.player == null) {
            return Double.MAX_VALUE;
        }
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return switch (this.sortingMode.getMode()) {
                case "Lowest Health" -> livingEntity.getHealth();
                case "Lowest Armor" -> livingEntity.getArmor();
                case "Closest Distance" -> KillAura.mc.player.distanceTo(entity);
                default -> KillAura.mc.player.distanceTo(entity);
            };
        }
        return Double.MAX_VALUE;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (KillAura.mc.player == null || KillAura.mc.world == null) {
            return;
        }
        if (this.isHoldingEndCrystal()) {
            target = null;
            this.bypassKS = true;
            attackableTargets.clear();
            this.waitAttack = false;
        }
        if (((Boolean)this.auto_disable.getValue()).booleanValue() && KillAura.mc.world != null) {
            if (!KillAura.mc.player.isAlive()) {
                this.setEnable(false);
                return;
            }
            if (KillAura.mc.player.age <= 1) {
                this.setEnable(false);
            }
        }
    }

    @Override
    public int rotationPriority() {
        return 1;
    }

    private boolean isHoldingEndCrystal() {
        return KillAura.mc.player != null && (KillAura.mc.player.getMainHandStack().isOf(Items.END_CRYSTAL) || KillAura.mc.player.getOffHandStack().isOf(Items.END_CRYSTAL));
    }

    static {
        keepSprint = new Switch("Keep Sprint", false);
        targets = new ArrayList<Entity>();
        attackableTargets = new HashMap<Entity, Rotation>();
        msTimer = new MSTimer();
        rotation = new Rotation(0.0f, 0.0f);
        lastAttackSprint = false;
    }
}

