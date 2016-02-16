package com.winthier.minilink.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSON {
    public static Object button(String chat, String command, String... tooltip)
    {
        Map<String, Object> map = new HashMap<>();
        map.put("text", Msg.format(chat));
        Map<String, Object> map2 = new HashMap<>();
        map.put("clickEvent", map2);
        map2.put("action", "run_command");
        map2.put("value", command);
        map2 = new HashMap<>();
        map.put("hoverEvent", map2);
        map2.put("action", "show_text");
        List<String> lines = new ArrayList<>();
        for (String line : tooltip) {
            if (!lines.isEmpty()) lines.add("\n");
            lines.add(Msg.format(line));
        }
        map2.put("value", lines);
        return map;
    }
}
