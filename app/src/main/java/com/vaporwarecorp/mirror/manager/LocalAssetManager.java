package com.vaporwarecorp.mirror.manager;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import org.apache.commons.io.FileUtils;
import timber.log.Timber;

import java.io.*;
import java.util.*;

public class LocalAssetManager {
// ------------------------------ FIELDS ------------------------------

    private static final String LOCAL_ASSETS_DIR = "local_assets";

    private AssetManager mAssetManager;
    private Map<String, String> mAssetPaths;
    private File mExternalDir;

// --------------------------- CONSTRUCTORS ---------------------------

    public LocalAssetManager(Context context) throws IOException {
        File appDir = context.getExternalFilesDir(null);
        if (null == appDir)
            throw new IOException("cannot get external files dir, external storage state is " +
                    Environment.getExternalStorageState());
        mExternalDir = new File(appDir, LOCAL_ASSETS_DIR);
        mAssetManager = context.getAssets();
        mAssetPaths = new HashMap<>();
        syncAssets();
    }

// -------------------------- OTHER METHODS --------------------------

    public String getLocalAssetPath(String assetPath) {
        return mAssetPaths.get(assetPath);
    }

    public File getLocalAssetsDir() {
        return mExternalDir;
    }

    /**
     * Copies raw asset resource to external storage of the device.
     *
     * @param asset path of the asset to copy
     * @throws IOException if an I/O error occurs
     */
    private File copy(String asset) throws IOException {
        InputStream source = mAssetManager.open(asset);
        File destinationFile = new File(mExternalDir, asset);
        destinationFile.getParentFile().mkdirs();
        OutputStream destination = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[1024];
        int nread;

        while ((nread = source.read(buffer)) != -1) {
            if (nread == 0) {
                nread = source.read();
                if (nread < 0)
                    break;
                destination.write(nread);
                continue;
            }
            destination.write(buffer, 0, nread);
        }
        destination.close();
        return destinationFile;
    }

    /**
     * In case you want to create more smart sync implementation, this method
     * returns the list of items which must be synchronized.
     */
    private Collection<String> getItemsToCopy(String path) throws IOException {
        Collection<String> items = new ArrayList<>();
        Queue<String> queue = new ArrayDeque<>();
        queue.offer(path);

        while (!queue.isEmpty()) {
            path = queue.poll();
            String[] list = mAssetManager.list(path);
            for (String nested : list) {
                if (!"".equals(path)) {
                    queue.offer(path + "/" + nested);
                } else {
                    queue.offer(nested);
                }
            }
            if (list.length == 0) {
                Timber.i("adding " + path);
                items.add(path);
            }
        }

        return items;
    }

    private void syncAssets() throws IOException {
        FileUtils.deleteDirectory(mExternalDir);

        Collection<String> assetsPath = getItemsToCopy("");
        for (String assetPath : assetsPath) {
            String localAssetPath = copy(assetPath).getAbsolutePath();
            mAssetPaths.put(assetPath, localAssetPath);
            Timber.i("localAssetPath " + localAssetPath);
        }
    }
}
