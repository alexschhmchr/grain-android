package com.swpt.grain;

import android.content.res.AssetManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ModelLoader {
    public static final String MODEL_FOLDER_NAME = "models";

    public static final String HOG_MODEL = "hog.yml";
    private static final String[] MODELS = {HOG_MODEL};

    private AssetManager assetManager;

    public ModelLoader(AssetManager assetManager) {
        this.assetManager = assetManager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveModelToStorage(String modelName, String fileDir) throws IOException {
        InputStream inputStream = assetManager.open(modelName);
        System.out.println(inputStream.available());
        Path path = Paths.get(fileDir, modelName);
        System.out.println(path);
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

    }
}
