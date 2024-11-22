package io.github.sefiraat.networks.commands;

import javax.annotation.Nullable;
import java.util.List;

public interface SimpleCommandInputStream {
    String nextArg();
    @Nullable
    List<String> getTabComplete();
}