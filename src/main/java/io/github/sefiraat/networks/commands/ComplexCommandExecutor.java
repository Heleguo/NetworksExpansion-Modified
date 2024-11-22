package me.testserver.Debugger.utils.commandClass;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public interface ComplexCommandExecutor extends TabExecutor, SubCommand.SubCommandCaller {
    public SubCommand getMainCommand();
    public SubCommand getSubCommand(String name);
    default List<String> onTabComplete(CommandSender var1, Command var2, String var3, String[] var4){
        var re=getMainCommand().parseInput(var4);
        if(re.getSecondValue().length==0){
            List<String> provider=re.getFirstValue().getTabComplete();
            return provider==null?new ArrayList<>():provider;
        }else{
            SubCommand subCommand= getSubCommand(re.getFirstValue().nextArg());
            if(subCommand!=null){
                String[] elseArg=re.getSecondValue();
                List<String> tab= subCommand.parseInput(elseArg).getFirstValue().getTabComplete();
                if(tab!=null){
                    return tab;
                }
            }
        }
        return new ArrayList<>();
    }
}
