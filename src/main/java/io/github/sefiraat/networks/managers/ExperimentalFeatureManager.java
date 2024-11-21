package io.github.sefiraat.networks.managers;

import io.github.sefiraat.networks.Networks;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

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
    //已经使用
    @Getter
    private boolean enableParallelLineOperation=true;
    //已经使用
    @Getter
    private boolean enableAsyncSafeNetworkRoot=true;

    @Getter
    //需要进一步查看
    private boolean enableRootGetItemStackAsync=false;

    @Getter
    //已经使用
    private boolean enableRootAddItemStackAsync=true;

//    @Getter
//    //已经停用
//    private boolean enableNetworkStorageBlacklist=false;
    @Getter
    //已经使用
    private boolean enableLineGrabberParallel=true;

    @Getter
    //已经使用
    private boolean enableLineGrabberAsync=true;

    @Getter
    private boolean enableControllerPreviewItems=false;
    @Getter
    private boolean enableControllerPreviewItemsAsync=false;

    @Getter
    private boolean enableBreakPoint1=false;

    @Getter
    private boolean enableBreakPoint2=false;

    @Getter
    private boolean enableBreakPoint3=false;


    @Getter
    //已经启用
    private boolean enableSnapShotOptimize=true;

//    @Getter
//    private boolean enableMetaDirectlyCompare=true;
    //todo list
    //add NetworksPusher slotAccess parallel
    //check the safety of BlockMenuSnapShot
    //check NetworkImport parallel execution
    //check AdvancedImport parallel execution
    //check SnapShot of AdvancedExport
    public class Profiler{
        boolean enabled=true;
        public Profiler(boolean enabled ){
            //what is this for?
            this.enabled=enabled;
        }
    }
    @Getter
    @Setter
    private boolean enableGlobalDebugFlag =false;

    long lastTimeStamp=0L;
    boolean isTiming=false;
    public void startGlobalProfiler(){
        if(enableGlobalDebugFlag &&!isTiming){
            isTiming=true;
            lastTimeStamp=System.nanoTime();
        }
    }
    public void endGlobalProfiler(Supplier<String> message){
        if(enableGlobalDebugFlag &&isTiming){
            long time=System.nanoTime();
            isTiming=false;
            sendTimings(lastTimeStamp,time,message);
            this.lastTimeStamp=0L;
        }
    }

    public void sendTimings(long start, long end, Supplier<String> string){
        if(enableGlobalDebugFlag){
            Networks.getInstance().getLogger().info( String.format( string.get(),String.valueOf(end-start)));
        }
    }

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
