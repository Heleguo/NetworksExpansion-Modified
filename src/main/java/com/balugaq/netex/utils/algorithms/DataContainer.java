package com.balugaq.netex.utils.algorithms;

import io.github.sefiraat.networks.network.stackcaches.ItemRequest;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;
import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ClickAction;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface DataContainer extends ChestMenu.MenuClickHandler {
    default int getInt(int val){
        return 0;
    }
    default void setInt(int val, int val2){

    }
    default String getString(int val){
        return "";
    }
    default void setString(int val, String val2){

    }
    default ItemStack getItemStack(int val){
        return null;
    }
    default void setItemStack(int val, ItemStack val2){

    }
    default ItemStack[] getItemStacks(int val){
        return null;
    }
    default void setItemStacks(int val1,ItemStack[] val){

    }
    default ItemRequest[] getItemRequests(int val){
        return null;
    }
    default void setItemRequests(int val1,ItemRequest[] val){

    }
    default Location getLocation(int val){
        return null;
    }
    default void setLocation(int val, Location val2){

    }
    default Object getObject(int val){
        return null;
    }
    default void setObject(int val,Object val2){

    }
    @Override
    default boolean onClick(Player player, int i, ItemStack itemStack, ClickAction clickAction) {
        return false;
    }
}
