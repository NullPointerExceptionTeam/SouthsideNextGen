package dev.southside.utils.player;

import dev.southside.MinecraftAccessor;
import dev.southside.mixin.accessors.ICreativeInventoryScreen;
import dev.southside.mixin.accessors.IHorseScreenHandler;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.BeaconScreenHandler;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.BrewingStandScreenHandler;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.Generic3x3ContainerScreenHandler;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.screen.LoomScreenHandler;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.TorchBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.block.CarpetBlock;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.block.BlockState;
import net.minecraft.world.EmptyBlockView;
import net.minecraft.screen.BlastFurnaceScreenHandler;
import net.minecraft.screen.SmokerScreenHandler;
import net.minecraft.block.ScaffoldingBlock;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.FurnaceScreenHandler;
import net.minecraft.screen.CartographyTableScreenHandler;
import net.minecraft.screen.LecternScreenHandler;
import net.minecraft.block.CampfireBlock;
import net.minecraft.screen.StonecutterScreenHandler;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;

public class SlotUtils implements MinecraftAccessor {
   public static final int HOTBAR_START = 0;
   public static final int HOTBAR_END = 8;
   public static final int OFFHAND = 45;
   public static final int MAIN_START = 9;
   public static final int MAIN_END = 35;
   public static final int ARMOR_START = 36;
   public static final int ARMOR_END = 39;

   public static int indexToId(int i) {
      if (mc.player == null) {
         return -1;
      } else {
         ScreenHandler handler = mc.player.currentScreenHandler;
         if (handler instanceof PlayerScreenHandler) {
            return survivalInventory(i);
         } else if (handler instanceof CreativeScreenHandler) {
            return creativeInventory(i);
         } else if (handler instanceof GenericContainerScreenHandler genericContainerScreenHandler) {
            return genericContainer(i, genericContainerScreenHandler.getRows());
         } else if (handler instanceof CraftingScreenHandler) {
            return craftingTable(i);
         } else if (handler instanceof FurnaceScreenHandler) {
            return furnace(i);
         } else if (handler instanceof BlastFurnaceScreenHandler) {
            return furnace(i);
         } else if (handler instanceof SmokerScreenHandler) {
            return furnace(i);
         } else if (handler instanceof Generic3x3ContainerScreenHandler) {
            return generic3x3(i);
         } else if (handler instanceof EnchantmentScreenHandler) {
            return enchantmentTable(i);
         } else if (handler instanceof BrewingStandScreenHandler) {
            return brewingStand(i);
         } else if (handler instanceof MerchantScreenHandler) {
            return villager(i);
         } else if (handler instanceof BeaconScreenHandler) {
            return beacon(i);
         } else if (handler instanceof AnvilScreenHandler) {
            return anvil(i);
         } else if (handler instanceof HopperScreenHandler) {
            return hopper(i);
         } else if (handler instanceof ShulkerBoxScreenHandler) {
            return genericContainer(i, 3);
         } else if (handler instanceof HorseScreenHandler) {
            return horse(handler, i);
         } else if (handler instanceof CartographyTableScreenHandler) {
            return cartographyTable(i);
         } else if (handler instanceof GrindstoneScreenHandler) {
            return grindstone(i);
         } else if (handler instanceof LecternScreenHandler) {
            return lectern();
         } else if (handler instanceof LoomScreenHandler) {
            return loom(i);
         } else {
            return handler instanceof StonecutterScreenHandler ? stonecutter(i) : -1;
         }
      }
   }

   private static int survivalInventory(int i) {
      if (isHotbar(i)) {
         return 36 + i;
      } else {
         return isArmor(i) ? 5 + (i - 36) : i;
      }
   }

   public static boolean isGoodForBridging(Block block) {
      BlockState state = block.getDefaultState();
      if (!(block instanceof FallingBlock)
         && !(block instanceof AirBlock)
         && !(block instanceof FluidBlock)
         && !(block instanceof ScaffoldingBlock)
         && !(block instanceof TorchBlock)
         && !(block instanceof CarpetBlock)
         && !(block instanceof DoorBlock)
         && !(block instanceof TrapdoorBlock)
         && !(block instanceof FenceBlock)
         && !(block instanceof WallBlock)
         && !(block instanceof PaneBlock)
         && !(block instanceof SlabBlock)
         && !(block instanceof StairsBlock)
         && !(block instanceof PlantBlock)
         && !(block instanceof SugarCaneBlock)
         && !(block instanceof TntBlock)
         && !(block instanceof CactusBlock)
         && !(block instanceof CampfireBlock)) {
         VoxelShape shape = state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
         return Block.isShapeFullCube(shape);
      } else {
         return false;
      }
   }

   public static boolean isGoodForBridging(Item item) {
      if (item instanceof BlockItem blockItem) {
         Block block = blockItem.getBlock();
         BlockState state = block.getDefaultState();
         if (!(block instanceof FallingBlock)
            && !(block instanceof AirBlock)
            && !(block instanceof FluidBlock)
            && !(block instanceof ScaffoldingBlock)
            && !(block instanceof TorchBlock)
            && !(block instanceof CarpetBlock)
            && !(block instanceof DoorBlock)
            && !(block instanceof TrapdoorBlock)
            && !(block instanceof FenceBlock)
            && !(block instanceof WallBlock)
            && !(block instanceof PaneBlock)
            && !(block instanceof SlabBlock)
            && !(block instanceof StairsBlock)
            && !(block instanceof PlantBlock)
            && !(block instanceof SugarCaneBlock)
            && !(block instanceof TntBlock)
            && !(block instanceof CactusBlock)
            && !(block instanceof CampfireBlock)) {
            VoxelShape shape = state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN);
            return Block.isShapeFullCube(shape);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static int creativeInventory(int i) {
      return mc.currentScreen instanceof CreativeInventoryScreen && ICreativeInventoryScreen.getSelectedTab() == Registries.ITEM_GROUP.get(ItemGroups.INVENTORY)
         ? survivalInventory(i)
         : -1;
   }

   private static int genericContainer(int i, int rows) {
      if (isHotbar(i)) {
         return (rows + 3) * 9 + i;
      } else {
         return isMain(i) ? rows * 9 + (i - 9) : -1;
      }
   }

   private static int craftingTable(int i) {
      if (isHotbar(i)) {
         return 37 + i;
      } else {
         return isMain(i) ? i + 1 : -1;
      }
   }

   private static int furnace(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int generic3x3(int i) {
      if (isHotbar(i)) {
         return 36 + i;
      } else {
         return isMain(i) ? i : -1;
      }
   }

   private static int enchantmentTable(int i) {
      if (isHotbar(i)) {
         return 29 + i;
      } else {
         return isMain(i) ? 2 + (i - 9) : -1;
      }
   }

   private static int brewingStand(int i) {
      if (isHotbar(i)) {
         return 32 + i;
      } else {
         return isMain(i) ? 5 + (i - 9) : -1;
      }
   }

   private static int villager(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int beacon(int i) {
      if (isHotbar(i)) {
         return 28 + i;
      } else {
         return isMain(i) ? 1 + (i - 9) : -1;
      }
   }

   private static int anvil(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int hopper(int i) {
      if (isHotbar(i)) {
         return 32 + i;
      } else {
         return isMain(i) ? 5 + (i - 9) : -1;
      }
   }

   private static int horse(ScreenHandler handler, int i) {
      AbstractHorseEntity entity = ((IHorseScreenHandler)handler).getEntity();
      if (entity instanceof LlamaEntity llamaEntity) {
         int strength = llamaEntity.getStrength();
         if (isHotbar(i)) {
            return 2 + 3 * strength + 28 + i;
         }

         if (isMain(i)) {
            return 2 + 3 * strength + 1 + (i - 9);
         }
      } else if (!(entity instanceof HorseEntity) && !(entity instanceof SkeletonHorseEntity) && !(entity instanceof ZombieHorseEntity)) {
         if (entity instanceof AbstractDonkeyEntity abstractDonkeyEntity) {
            boolean chest = abstractDonkeyEntity.hasChest();
            if (isHotbar(i)) {
               return (chest ? 44 : 29) + i;
            }

            if (isMain(i)) {
               return (chest ? 17 : 2) + (i - 9);
            }
         }
      } else {
         if (isHotbar(i)) {
            return 29 + i;
         }

         if (isMain(i)) {
            return 2 + (i - 9);
         }
      }

      return -1;
   }

   private static int cartographyTable(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int grindstone(int i) {
      if (isHotbar(i)) {
         return 30 + i;
      } else {
         return isMain(i) ? 3 + (i - 9) : -1;
      }
   }

   private static int lectern() {
      return -1;
   }

   private static int loom(int i) {
      if (isHotbar(i)) {
         return 31 + i;
      } else {
         return isMain(i) ? 4 + (i - 9) : -1;
      }
   }

   private static int stonecutter(int i) {
      if (isHotbar(i)) {
         return 29 + i;
      } else {
         return isMain(i) ? 2 + (i - 9) : -1;
      }
   }

   public static boolean isHotbar(int i) {
      return i >= 0 && i <= 8;
   }

   public static boolean isMain(int i) {
      return i >= 9 && i <= 35;
   }

   public static boolean isArmor(int i) {
      return i >= 36 && i <= 39;
   }
}
