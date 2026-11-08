package dev.southside.module;

public enum Category {
   Combat(Category.Pages.COMBAT),
   Movement(Category.Pages.MOVEMENT),
   World(Category.Pages.WORLD),
   Player(Category.Pages.PLAYER),
   Misc(Category.Pages.MISC),
   Render(Category.Pages.RENDER),
   Client(Category.Pages.CLIENT);

   public final Category.Pages pages;

   private Category(Category.Pages pages) {
      this.pages = pages;
   }

   public Category.Pages[] getSubPages() {
      return Category.Pages.values();
   }

   public static enum Pages {
      COMBAT(true, false),
      MOVEMENT(true, false),
      RENDER(true, false),
      WORLD(true, false),
      PLAYER(true, false),
      MISC(true, false),
      HUD(true, true),
      CONFIGS(false, true),
      LUA(true, true),
      CLIENT(true, true);

      public final boolean module;
      public final boolean gradient;

      private Pages(boolean module, boolean gradient) {
         this.module = module;
         this.gradient = gradient;
      }
   }
}
