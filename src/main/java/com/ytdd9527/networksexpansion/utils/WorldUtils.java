package com.ytdd9527.networksexpansion.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import java.lang.reflect.Field;

@Deprecated
public class WorldUtils {
    protected static Class<?> craftBlockStateClass;
    protected static Field interfaceBlockDataField;
    protected static Field blockPositionField;
    protected static Field worldField;
    protected static Field weakWorldField;
    protected static boolean success = false;

    static {
        try {
            World sampleWorld = Bukkit.getWorlds().get(0);
            BlockState blockstate = sampleWorld.getBlockAt(0, 0, 0).getState();
            var result = ReflectionUtil.getDeclaredFieldsRecursively(blockstate.getClass(), "data");
            interfaceBlockDataField = result.getFirstValue();
            interfaceBlockDataField.setAccessible(true);
            craftBlockStateClass = result.getSecondValue();
            blockPositionField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "position").getFirstValue();
            blockPositionField.setAccessible(true);
            worldField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "world").getFirstValue();
            worldField.setAccessible(true);
            weakWorldField = ReflectionUtil.getDeclaredFieldsRecursively(craftBlockStateClass, "weakWorld").getFirstValue();
            weakWorldField.setAccessible(true);
            success = true;
        } catch (Throwable ignored) {

        }
    }

    public static boolean copyBlockState(BlockState fromBlockState, Block toBlock) {
        if (!success) {
            return false;
        }

        BlockState toState = toBlock.getState();
        if (!craftBlockStateClass.isInstance(toState) || !craftBlockStateClass.isInstance(fromBlockState)) {
            return false;
        }

        try {
            blockPositionField.set(fromBlockState, blockPositionField.get(toState));
            worldField.set(fromBlockState, worldField.get(toState));
            weakWorldField.set(fromBlockState, weakWorldField.get(toState));
            fromBlockState.update(true, false);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}