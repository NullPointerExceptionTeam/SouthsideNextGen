package dev.southside.utils.render.jello.animation;

import java.util.function.Function;
import lombok.Generated;

public enum Easing {
    LINEAR(x -> x),
    EASE_IN_QUAD(x -> x * x),
    EASE_OUT_QUAD(x -> x * (2.0 - x)),
    EASE_IN_OUT_QUAD(x -> x < 0.5 ? 2.0 * x * x : -1.0 + (4.0 - 2.0 * x) * x),
    EASE_IN_CUBIC(x -> x * x * x),
    EASE_OUT_CUBIC(x -> {
        x = x - 1.0;
        return x * x * x + 1.0;
    }),
    EASE_IN_OUT_CUBIC(x -> x < 0.5 ? 4.0 * x * x * x : (x - 1.0) * (2.0 * x - 2.0) * (2.0 * x - 2.0) + 1.0),
    EASE_IN_QUART(x -> x * x * x * x),
    EASE_OUT_QUART(x -> {
        x = x - 1.0;
        return 1.0 - x * x * x * x;
    }),
    EASE_IN_OUT_QUART(x -> {
        double d;
        if (x < 0.5) {
            d = 8.0 * x * x * x * x;
        } else {
            x = x - 1.0;
            d = 1.0 - 8.0 * x * x * x * x;
        }
        return d;
    }),
    EASE_IN_QUINT(x -> x * x * x * x * x),
    EASE_OUT_QUINT(x -> {
        x = x - 1.0;
        return 1.0 + x * x * x * x * x;
    }),
    EASE_IN_OUT_QUINT(x -> {
        double d;
        if (x < 0.5) {
            d = 16.0 * x * x * x * x * x;
        } else {
            x = x - 1.0;
            d = 1.0 + 16.0 * x * x * x * x * x;
        }
        return d;
    }),
    EASE_IN_SINE(x -> 1.0 - Math.cos(x * Math.PI / 2.0)),
    EASE_OUT_SINE(x -> Math.sin(x * Math.PI / 2.0)),
    EASE_IN_OUT_SINE(x -> 1.0 - Math.cos(Math.PI * x / 2.0)),
    EASE_IN_EXPO(x -> x == 0.0 ? 0.0 : Math.pow(2.0, 10.0 * x - 10.0)),
    EASE_OUT_EXPO(x -> x == 1.0 ? 1.0 : 1.0 - Math.pow(2.0, -10.0 * x)),
    EASE_IN_OUT_EXPO(x -> x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : (x < 0.5 ? Math.pow(2.0, 20.0 * x - 10.0) / 2.0 : (2.0 - Math.pow(2.0, -20.0 * x + 10.0)) / 2.0))),
    EASE_IN_CIRC(x -> 1.0 - Math.sqrt(1.0 - x * x)),
    EASE_OUT_CIRC(x -> {
        x = x - 1.0;
        return Math.sqrt(1.0 - x * x);
    }),
    EASE_IN_OUT_CIRC(x -> x < 0.5 ? (1.0 - Math.sqrt(1.0 - 4.0 * x * x)) / 2.0 : (Math.sqrt(1.0 - 4.0 * (x - 1.0) * x) + 1.0) / 2.0),
    SIGMOID(x -> 1.0 / (1.0 + Math.exp(-x.doubleValue()))),
    EASE_OUT_ELASTIC(x -> x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0 - 0.75) * 2.0943951023931953) * 0.5 + 1.0)),
    EASE_IN_BACK(x -> 2.70158 * x * x * x - 1.70158 * x * x),
    EASE_OUT_BACK(x -> 1.0 + 2.70158 * Math.pow(x - 1.0, 3.0) + 1.70158 * Math.pow(x - 1.0, 2.0)),
    EASE_IN_OUT_ELASTIC(x -> {
        double c5 = 1.3962634015954636;
        double c6 = Math.sin((20.0 * x - 11.125) * c5);
        return x == 0.0 ? 0.0 : (x == 1.0 ? 1.0 : (x < 0.5 ? -(Math.pow(2.0, 20.0 * x - 10.0) * c6) / 2.0 : Math.pow(2.0, -20.0 * x + 10.0) * c6 / 2.0 + 1.0));
    }),
    EASE_OUT_BOUNCE(x -> {
        double n1 = 7.5625;
        double d1 = 2.75;
        if (x < 1.0 / d1) {
            return n1 * x * x;
        }
        if (x < 2.0 / d1) {
            x = x - 1.5 / d1;
            return n1 * x * x + 0.75;
        }
        if (x < 2.5 / d1) {
            x = x - 2.25 / d1;
            return n1 * x * x + 0.9375;
        }
        x = x - 2.625 / d1;
        return n1 * x * x + 0.984375;
    }),
    EASE_IN_OUT_BOUNCE(x -> x < 0.5 ? (1.0 - EASE_OUT_BOUNCE.getFunction().apply(1.0 - 2.0 * x)) / 2.0 : (1.0 + EASE_OUT_BOUNCE.getFunction().apply(2.0 * x - 1.0)) / 2.0),
    EASE_IN_OUT_BACK(x -> {
        double c1 = 1.70158;
        double c2 = c1 * 1.525;
        return x < 0.5 ? Math.pow(2.0 * x, 2.0) * ((c2 + 1.0) * 2.0 * x - c2) / 2.0 : (Math.pow(2.0 * x - 2.0, 2.0) * ((c2 + 1.0) * (x * 2.0 - 2.0) + c2) + 2.0) / 2.0;
    });

    private final Function<Double, Double> function;

    private Easing(Function<Double, Double> function) {
        this.function = function;
    }

    @Generated
    public Function<Double, Double> getFunction() {
        return this.function;
    }
}

