package io.github.sefiraat.networks.utils;

import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.Pair;

import java.lang.reflect.Field;

public class ReflectUtils {
    public static Pair<Field,Class> getDeclaredFieldsRecursively(Class clazz, String fieldName){
        try{
            Field field=clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return new Pair(field,clazz);
        }catch (Throwable e){
            clazz=clazz.getSuperclass();
            if(clazz==null){
                return null;
            }else{
                return getDeclaredFieldsRecursively(clazz,fieldName);
            }
        }
    }
}
