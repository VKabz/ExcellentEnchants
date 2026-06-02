package su.nightexpress.excellentenchants.api;


import java.util.function.BiFunction;

import org.jspecify.annotations.NullMarked;

@NullMarked
public enum ModifierAction {

    ADD(Double::sum),
    MULTIPLY((origin, target) -> origin * target);

    private final BiFunction<Double, Double, Double> function;

    ModifierAction(BiFunction<Double, Double, Double> function) {
        this.function = function;
    }

    public double math(double origin, double target) {
        return this.function.apply(origin, target);
    }
}
