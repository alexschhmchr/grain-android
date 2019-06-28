package com.swpt.grain;

import android.content.res.AssetManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ModelLoader {
    public static final String MODEL_FOLDER_NAME = "models";

    private static final String HOG_MODEL = "hog.yml";
    private static final String[] MODELS = {HOG_MODEL};

    private AssetManager assetManager;

    public ModelLoader(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public boolean isModelOnStorageUpdated() {
        for(String model : MODELS) {

        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveModelToStorage(String fileDir) throws IOException {
        InputStream inputStream = assetManager.open(HOG_MODEL);
        System.out.println(inputStream.available());
        Path path = Paths.get(fileDir, HOG_MODEL);
        System.out.println(path);
        Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

    }
}