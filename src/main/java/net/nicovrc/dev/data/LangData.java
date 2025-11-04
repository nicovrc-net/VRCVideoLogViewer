package net.nicovrc.dev.data;

import java.util.HashMap;

public class LangData {

    HashMap<String, String> langData = new HashMap<>();

    public void add(String o_text, String t_text){
        langData.put(o_text, t_text);
    }

    public String get(String o_text){
        return langData.get(o_text);
    }

}
