package me.bright.damage;

import me.bright.entity.BrightEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

public record ConditionalDamage(@NotNull BiPredicate<BrightEntity, BrightEntity> condition,
                                @NotNull Damage damage) {
}
