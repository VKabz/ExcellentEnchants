package su.nightexpress.excellentenchants.tooltip;


import su.nightexpress.excellentenchants.api.tooltip.TooltipHandler;

import org.jspecify.annotations.NullMarked;

import su.nightexpress.excellentenchants.api.tooltip.TooltipController;

@FunctionalInterface
@NullMarked
public interface TooltipFactory {

    TooltipHandler create(TooltipController provider);
}
