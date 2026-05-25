package com.example.desicionhelper;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    private static final String FILE_NAME = "choice_groups.json";

    public static void saveGroups(Context context, List<ChoiceGroup> groups) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(groups);

        try (FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ChoiceGroup> loadGroups(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = context.openFileInput(FILE_NAME);
             InputStreamReader reader = new InputStreamReader(fis)) {
            char[] buf = new char[1024];
            int len;
            while ((len = reader.read(buf)) != -1) {
                sb.append(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        if (sb.length() == 0) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<ChoiceGroup>>() {}.getType();
        return new Gson().fromJson(sb.toString(), type);
    }
}
