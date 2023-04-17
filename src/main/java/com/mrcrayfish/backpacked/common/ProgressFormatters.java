package com.mrcrayfish.backpacked.common;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.BiFunction;

/**
 * Author: MrCrayfish
 */
public class ProgressFormatters
{
    public static final BiFunction<Integer, Integer, ITextComponent> COLLECT_X_OF_X = (count, maxCount) -> {
        count = MathHelper.clamp(count, 0, maxCount);
        return new TranslationTextComponent("backpacked.formatter.collected_x_of_x", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> FED_X_OF_X = (count, maxCount) -> {
        count = MathHelper.clamp(count, 0, maxCount);
        return new TranslationTextComponent("backpacked.formatter.fed_x_of_x", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> FOUND_X_OF_X = (count, maxCount) -> {
        count = MathHelper.clamp(count, 0, maxCount);
        return new TranslationTextComponent("backpacked.formatter.found_x_of_x", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> USED_X_TIMES = (count, unused) -> {
        return new TranslationTextComponent("backpacked.formatter.used_x_times", count);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> BRED_X_OF_X = (count, maxCount) -> {
        return new TranslationTextComponent("backpacked.formatter.bred_x_of_x", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> CUT_X_OF_X = (count, maxCount) -> {
        return new TranslationTextComponent("backpacked.formatter.cut_x_of_x", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> SHEARED_X_SHEEP = (count, maxCount) -> {
        return new TranslationTextComponent("backpacked.formatter.shear_x_sheep", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> INT_PERCENT = (numerator, denominator) -> {
        int percent = (int) (100 * (double) numerator / (double) denominator);
        return new TranslationTextComponent("backpacked.formatter.int_percent", percent, "%");
    };

    public static final BiFunction<Integer, Integer, ITextComponent> CRAFT_X_OF_X = (count, maxCount) -> {
        count = MathHelper.clamp(count, 0, maxCount);
        return new TranslationTextComponent("backpacked.formatter.craft_x_of_x", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> INCOMPLETE_COMPLETE = (count, maxCount) -> {
        if(count < maxCount) return new TranslationTextComponent("backpacked.formatter.incomplete");
        return new TranslationTextComponent("backpacked.formatter.complete");
    };

    public static final BiFunction<Integer, Integer, ITextComponent> TRADED_X_OF_X = (count, maxCount) -> {
        count = MathHelper.clamp(count, 0, maxCount);
        return new TranslationTextComponent("backpacked.formatter.traded_x_of_x", count, maxCount);
    };

    public static final BiFunction<Integer, Integer, ITextComponent> EXPLORED_X_OF_X = (count, maxCount) -> {
        count = MathHelper.clamp(count, 0, maxCount);
        return new TranslationTextComponent("backpacked.formatter.explored_x_of_x", count, maxCount);
    };
}
