package me.rainma22.Raymond;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Path;

public class GlobalOptions extends JSONObject{
    private static GlobalOptions globalOptions;
    public static final Path OPTION_PATH = Path.of(".","settings","botSettings.json");
    private static final JSONObject BLANK_JSON = new JSONObject();
    static{
        BLANK_JSON.put("api_key", "");
        BLANK_JSON.put("ffmpeg_path", "ffmpeg");
    }

    public static GlobalOptions getGlobalOptions() {
        if(globalOptions == null){
            globalOptions = new GlobalOptions(OPTION_PATH);
        }
        return globalOptions;
    }

    public String getFFmpegPath(){
        return getString("ffmpeg_path");
    }

    public String getAPIKey(){
        return getString("api_key");
    }

    private GlobalOptions(Path filePath){
        super();
        for (String key: BLANK_JSON.keySet()){
            put(key, BLANK_JSON.getString(key));
        }
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

    private void saveToFile(Path filePath) throws IOException {
        filePath.getParent().toFile().mkdirs();
        File out = filePath.toFile();
        if (out.exists() && !out.isFile()) throw new IOException("given path is not a file!");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(out));
        write(writer);
        writer.flush();
        writer.close();
    }

    private void loadFromFile(Path filePath) throws FileNotFoundException {
        File in = filePath.toFile();
        if(!in.exists()) throw new FileNotFoundException("File does not Exist!");
        JSONTokener tokener = new JSONTokener(new FileInputStream(in));
        JSONObject settings = new JSONObject(tokener);
        for (String key: settings.keySet()){
            put(key, settings.getString(key));
        }
    }

    public void save() throws IOException {
        saveToFile(OPTION_PATH);
    }
}