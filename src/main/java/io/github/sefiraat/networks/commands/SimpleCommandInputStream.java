package me.testserver.Debugger.utils.commandClass;

import javax.annotation.Nullable;
import java.util.List;

public interface SimpleCommandInputStream {
    String nextArg();
    @Nullable
    List<String> getTabComplete();
}