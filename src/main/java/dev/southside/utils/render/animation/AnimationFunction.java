package dev.southside.utils.render.animation;

@FunctionalInterface
public interface AnimationFunction<T> {
   T apply(float var1, T var2);
}
