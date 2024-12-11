package me.rainma22.Raymond;

import net.dv8tion.jda.api.entities.Guild;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;

public class GuildOptions extends HashMap<String, JSONObject> {
    private static GuildOptions guildOptions;
    private static final Path OPTION_PATH = Path.of(".","settings","serverSettings.json");
    private static final JSONObject BLANK_JSON = new JSONObject();
    static{
        BLANK_JSON.put("volume", 1f);
    }

    public static GuildOptions getGuildOptions() {
        if(guildOptions == null){
            guildOptions = new GuildOptions(OPTION_PATH);
        }
        return guildOptions;
    }

    private GuildOptions(Path filePath){
        try {
            loadFromFile(filePath);
        } catch (FileNotFoundException e) {
            try {
                saveToFile(filePath);
            } catch (IOException ex) {
                System.err.printf("Unable to create File %s\n", filePath);
            }
        }
    }

    public JSONObject get(Guild guild) {
        return super.getOrDefault(guild.getId(),BLANK_JSON);
    }

    @Override
    public JSONObject get(Object key) {
        return super.getOrDefault(key,BLANK_JSON);
    }

    public float getVolumeOf(Guild guild){
        return get(guild).getFloat("volume");
    }

    public void setVolumeOf(Guild guild, float volume){
        JSONObject settings = get(guild);
        settings.put("volume", volume);
    }

    private void saveToFile(Path filePath) throws IOException {
        filePath.getParent().toFile().mkdirs();
        File out = filePath.toFile();
        if (out.exists() && !out.isFile()) throw new IOException("given path is not a file!");
        JSONObject settings = new JSONObject();

        forEach((key, val) -> {
            if(val.getFloat("volume") != 1f) settings.put(key, val);
        });
        settings.put("default", BLANK_JSON);
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(out));
        settings.write(writer);
        writer.flush();
        writer.close();
    }

    private void loadFromFile(Path filePath) throws FileNotFoundException {
        File in = filePath.toFile();
        if(!in.exists()) throw new FileNotFoundException("File does not Exist!");
        JSONTokener tokener = new JSONTokener(new FileInputStream(in));
        JSONObject settings = new JSONObject(tokener);
        for (String key: settings.keySet()){
            put(key, settings.getJSONObject(key));
        }
    }

    public void save() throws IOException {
        saveToFile(OPTION_PATH);
    }
}