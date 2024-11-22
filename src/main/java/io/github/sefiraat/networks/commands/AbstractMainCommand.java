package io.github.sefiraat.networks.commands;

import io.github.thebusybiscuit.slimefun4.libraries.commons.lang.Validate;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractMainCommand implements ComplexCommandExecutor {
    @Getter
    private LinkedHashSet<SubCommand> subCommands = new LinkedHashSet<>();
    private SubCommand mainInternal;
    private boolean registered = false;
    private Logger Debug;
    @Getter
    private Plugin plugin;
    void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    public <T extends AbstractMainCommand> T registerCommand(Plugin plugin){
        Validate.isTrue(!registered, "Command have already been registered!");
        this.plugin = plugin;
        this.Debug = plugin.getLogger();
        plugin.getServer().getPluginCommand(getMainName()).setExecutor(this);
        plugin.getServer().getPluginCommand(getMainName()).setTabCompleter(this);
        this.registered=true;
        return (T)this;
    }
    public <T extends AbstractMainCommand> T unregisterCommand(){
        Validate.isTrue(registered, "Command functional havem't been unregistered!");
        plugin.getServer().getPluginCommand(getMainName()).setExecutor(null);
        plugin.getServer().getPluginCommand(getMainName()).setTabCompleter(null);
        this.registered=false;
        return (T)this;
    }
    public SubCommand getSubCommand(String name) {
        for(SubCommand command:subCommands){
            if(command.getName().equalsIgnoreCase(name)){
                return command;
            }
        }return null;
    }
    @Override
    public void registerSub(SubCommand command) {
        this.subCommands.add(command);
    }
    private SubCommand getMainInternal() {
        if(mainInternal == null){
            try{
                Field field=this.getClass().getDeclaredField("mainCommand");
                field.setAccessible(true);
                mainInternal=(SubCommand)field.get(this);
            }catch (Throwable e){
                Debug.info("Error in "+this.getClass().getName()+": main Command Not Found");
                e.printStackTrace();
            }
        }
        return mainInternal;
    }
    public SubCommand getMainCommand() {
        return getMainInternal();
    }
    public String getMainName(){
        return getMainInternal().getName();
    }
    public boolean onCommand(CommandSender var1, Command var2, String var3, String[] var4){
        if(permissionRequired()==null|| var1.hasPermission(permissionRequired())){
            if(var4.length>=1){
                SubCommand command=getSubCommand(var4[0]);
                if(command!=null){
                    String[] elseArg= Arrays.copyOfRange(var4,1,var4.length);
                    return command.getExecutor().onCommand(var1,var2,var3,elseArg);
                }
            }
            showHelpCommand(var1);
        }else{
            noPermission(var1);
        }
        return true;
    }
    public void noPermission(CommandSender var1){
        sendMessage(var1,"&c你没有权限使用该指令!");
    }
    public abstract String permissionRequired();

    void showHelpCommand(CommandSender sender){
        sendMessage(sender,"&a/%s 全部指令大全".formatted(getMainName()));
        for(SubCommand cmd:subCommands){
            for (String help:cmd.getHelp()){
                sendMessage(sender,"&a"+help);
            }
        }
    }
    public List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4){
        var re=getMainCommand().parseInput(var4);
        if(re.getSecondValue().length==0){
            List<String> provider=re.getFirstValue().getTabComplete();
            return provider==null?new ArrayList<>():provider;
        }else{
            SubCommand subCommand= getSubCommand(re.getFirstValue().nextArg());
            if(subCommand!=null){
                String[] elseArg=re.getSecondValue();
               return subCommand.onTabComplete(var1,var2,var3,elseArg);
            }
        }
        return new ArrayList<>();
    }
    public Player isPlayer(CommandSender sender, boolean sendMessage){
        if(sender instanceof Player player){
            return player;
        }else {
            if(sendMessage){
                sendMessage(sender,"&c该指令只能在游戏内执行!");
            }
            return null;
        }
    }
}
