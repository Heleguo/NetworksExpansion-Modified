package io.github.sefiraat.networks.managers;

import lombok.Getter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ExperimentalFeatureManager implements TabExecutor {
    @Getter
    private static ExperimentalFeatureManager instance=new ExperimentalFeatureManager();
    private HashMap<String, Field> features=new HashMap<>();
    public ExperimentalFeatureManager() {
        instance = this;
        init();
    }
    private void init() {
        for (Field field : ExperimentalFeatureManager.class.getDeclaredFields()) {
            if(field.getType()==boolean.class&&field.getName().startsWith("enable")){
                field.setAccessible(true);
                this.features.put(field.getName().substring("enable".length()), field);
            }
        }
    }
    @Getter
    private boolean enableParallelLineOperation=false;
    @Getter
    private boolean enableAsyncSafeNetworkRoot=false;
    @Getter
    private boolean enableRootGetItemStackAsync=false;
    @Getter
    private boolean enableRootAddItemStackSync=false;

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length<2){
            sender.sendMessage("Usage: /experimental <feature> true|false");
            return true;
        }
        Field feature=features.get(args[0]);
        if(feature==null){
            sender.sendMessage("Feature "+args[0]+" not found");
            return true;
        }else {
            try{
                if("true".equals(args[1])){
                    feature.set(this,true);
                }else if("false".equals(args[1])){
                    feature.set(this,false);
                }else {
                    sender.sendMessage("String "+args[1]+" is not a valid boolean");
                }
            }catch(IllegalAccessException e){
                e.printStackTrace();
                sender.sendMessage("An error occurred while setting enable flag");
            }
            return true;
        }
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(strings.length==1){
            return features.keySet().stream().toList();
        }
        if(strings.length==2&& features.containsKey(strings[0])){
            return List.of("true","false");
        }
        return List.of();
    }
}
