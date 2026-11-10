package dev.southside.render;

import dev.southside.render.pipeline.Mesh;
import dev.southside.render.pipeline.Pipelines;
import java.awt.Color;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.client.util.math.MatrixStack;

public class Render3D {
   public static void begin(MatrixStack mat) {
      triangles().enable3d();
      lines().enable3d();
      triangles().mat(mat);
      lines().mat(mat);
      triangles().begin();
      lines().begin();
   }

   public static void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
      line(x1, y1, z1, x2, y2, z2, color, color);
   }

   public static void line(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      line(x1, y1, z1, x2, y2, z2, color, color);
   }

   public static void line(double x1, double y1, double z1, double x2, double y2, double z2, Color color1, Color color2) {
      lines().line(lines().vec3(x1, y1, z1).color(color1).next(), lines().vec3(x2, y2, z2).color(color2).next());
   }

   public static void line(double x1, double y1, double z1, double x2, double y2, double z2, int color1, int color2) {
      lines().line(lines().vec3(x1, y1, z1).color(color1).next(), lines().vec3(x2, y2, z2).color(color2).next());
   }

   public static void quad(
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      Color topLeft,
      Color topRight,
      Color bottomRight,
      Color bottomLeft
   ) {
      triangles()
         .quad(
            triangles().vec3(x1, y1, z1).color(bottomLeft).next(),
            triangles().vec3(x2, y2, z2).color(topLeft).next(),
            triangles().vec3(x3, y3, z3).color(topRight).next(),
            triangles().vec3(x4, y4, z4).color(bottomRight).next()
         );
   }

   public static void quad(
      double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color color
   ) {
      quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color, color, color, color);
   }

   public static void boxLines(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
      int blb = lines().vec3(x1, y1, z1).color(color).next();
      int blf = lines().vec3(x1, y1, z2).color(color).next();
      int brb = lines().vec3(x2, y1, z1).color(color).next();
      int brf = lines().vec3(x2, y1, z2).color(color).next();
      int tlb = lines().vec3(x1, y2, z1).color(color).next();
      int tlf = lines().vec3(x1, y2, z2).color(color).next();
      int trb = lines().vec3(x2, y2, z1).color(color).next();
      int trf = lines().vec3(x2, y2, z2).color(color).next();
      lines().line(blb, tlb);
      lines().line(blf, tlf);
      lines().line(brb, trb);
      lines().line(brf, trf);
      lines().line(blb, blf);
      lines().line(brb, brf);
      lines().line(blb, brb);
      lines().line(blf, brf);
      lines().line(tlb, tlf);
      lines().line(trb, trf);
      lines().line(tlb, trb);
      lines().line(tlf, trf);
   }

   public static void blockLines(int x, int y, int z, Color color) {
      boxLines(x, y, z, x + 1, y + 1, z + 1, color);
   }

   public static void blockLines(BlockPos pos, Color color) {
      blockLines(pos.getX(), pos.getY(), pos.getZ(), color);
   }

   public static void boxSides(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
      int blb = triangles().vec3(x1, y1, z1).color(color).next();
      int blf = triangles().vec3(x1, y1, z2).color(color).next();
      int brb = triangles().vec3(x2, y1, z1).color(color).next();
      int brf = triangles().vec3(x2, y1, z2).color(color).next();
      int tlb = triangles().vec3(x1, y2, z1).color(color).next();
      int tlf = triangles().vec3(x1, y2, z2).color(color).next();
      int trb = triangles().vec3(x2, y2, z1).color(color).next();
      int trf = triangles().vec3(x2, y2, z2).color(color).next();
      triangles().quad(blb, blf, tlf, tlb);
      triangles().quad(brb, trb, trf, brf);
      triangles().quad(blb, tlb, trb, brb);
      triangles().quad(blf, brf, trf, tlf);
      triangles().quad(blb, brb, brf, blf);
      triangles().quad(tlb, tlf, trf, trb);
   }

   public static void blockSides(int x, int y, int z, Color color) {
      boxSides(x, y, z, x + 1, y + 1, z + 1, color);
   }

   public static void blockSides(BlockPos pos, Color color) {
      blockSides(pos.getX(), pos.getY(), pos.getZ(), color);
   }

   public static void boxSides(Box box, Color color) {
      boxSides(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   public static void boxLines(Box box, Color color) {
      boxLines(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   public static void sideLines(
      double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color color
   ) {
      int i1 = lines().vec3(x1, y1, z1).color(color).next();
      int i2 = lines().vec3(x2, y2, z2).color(color).next();
      int i3 = lines().vec3(x3, y3, z3).color(color).next();
      int i4 = lines().vec3(x4, y4, z4).color(color).next();
      lines().line(i1, i2);
      lines().line(i2, i3);
      lines().line(i3, i4);
      lines().line(i4, i1);
   }

   public static void sideQuad(
      double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3, double x4, double y4, double z4, Color color
   ) {
      quad(x1, y1, z1, x2, y2, z2, x3, y3, z3, x4, y4, z4, color);
   }

   public static void sideVerticalLines(double x1, double y1, double z1, double x2, double y2, double z2, Color col) {
      sideLines(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, col);
   }

   public static void sideHorizontalLines(double x1, double y, double z1, double x2, double z2, Color col) {
      sideLines(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, col);
   }

   public static void sideVerticalQuad(double x1, double y1, double z1, double x2, double y2, double z2, Color col) {
      sideQuad(x1, y1, z1, x1, y2, z1, x2, y2, z2, x2, y1, z2, col);
   }

   public static void sideHorizontalQuad(double x1, double y, double z1, double x2, double z2, Color col) {
      sideQuad(x1, y, z1, x1, y, z2, x2, y, z2, x2, y, z1, col);
   }

   public static Mesh lines() {
      return Pipelines.POS_COLOR_3D_LINES;
   }

   public static Mesh triangles() {
      return Pipelines.POS_COLOR_3D;
   }

   public static void end() {
      triangles().draw();
      lines().draw();
      triangles().disable3d();
      lines().disable3d();
   }
}
