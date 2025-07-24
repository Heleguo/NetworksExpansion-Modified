package com.balugaq.netex.api.gui;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class GUI {
    public final @NotNull List<String> gui;

    public GUI(String... gui) {
        this.gui = List.of(gui);
    }

    public int select1(@NotNull String c) {
        return select1(c.charAt(0));
    }

    public int select1(char c) {
        int[] r = select(c);
        if (r.length > 0) {
            return r[0];
        }
        return -1;
    }

    public int[] select(@NotNull String c) {
        return select(c.charAt(0));
    }

    public int[] select(char c) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < gui.size(); i++) {
            String row = gui.get(i);
            for (int j = 0; j < row.length(); j++) {
                if (row.charAt(j) == c) {
                    list.add(i * 9 + j);
                }
            }
        }

        return list.stream().mapToInt(i -> i).toArray();
    }
}
