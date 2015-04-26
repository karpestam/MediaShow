package se.karpestam.mediashow.Media;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class BitmapDiskCache {
    private static final int DISK_CACHE_SIZE = 200 * 1024 * 1024;
    private static final String CACHE_FOLDER_SUFFIX = "bitmap";
    private File mCacheDir;
    private LruCache<String, FileMirror> mFileCache;

    public BitmapDiskCache(Context context) {
        mCacheDir = new File(context.getApplicationContext().getCacheDir().getAbsolutePath(), CACHE_FOLDER_SUFFIX);
        mCacheDir.mkdir();
        mFileCache = new LruCache<String, FileMirror>(DISK_CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, FileMirror value) {
                return value.mFileSize;
            }

            @Override
            protected void entryRemoved(boolean evicted, String key, FileMirror oldValue, FileMirror newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                File file = new File(oldValue.mFilePath);
                if (file.exists()) {
                    file.delete();
                }
            }
        };
        long start = System.currentTimeMillis();
        readCacheFromDisk(mCacheDir);
        Log.d("MATS", "readCacheFromDisk took " + (System.currentTimeMillis() - start));
    }

    public void add(String filePath, Bitmap bitmap) {
        File inFile = new File(filePath);
        File cacheDir = new File(mCacheDir.getAbsolutePath(), inFile.getParent());
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        if (cacheDir.exists()) {
            File outFile = null;
            try {
                outFile = new File(cacheDir.getAbsolutePath(), inFile.getName());
                outFile.setReadable(false);
                if (outFile.createNewFile()) {
                    FileOutputStream outputStream = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    outputStream.close();
                    mFileCache.put(outFile.getAbsolutePath(),
                            new FileMirror(outFile.getAbsolutePath(), (int)outFile.length()));
                }
            } catch (IOException e) {

            }
            finally {
                if (outFile != null) {
                    outFile.setReadable(true);
                }
            }
        }
    }

    public Bitmap get(String filePath) {
        File file = new File(mCacheDir.getAbsolutePath(), filePath);
        if (file.exists()) {
            /* We must get this object to update the usage. */
            FileMirror mirror = mFileCache.get(file.getAbsolutePath());
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }

    /**
     * Go through the cache dir, read the current size. Make Mirror objects.
     *
     * @param dir
     */
    private void readCacheFromDisk(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; ++i) {
                File file = files[i];
                if (file.isDirectory()) {
                    readCacheFromDisk(file);
                } else {
                    /* Put the mirror to the file cache in the lru-cache. */
                    mFileCache.put(file.getAbsolutePath(), new FileMirror(file.getAbsolutePath(), (int) file.length()));
                }
            }
        }
    }

    private class FileMirror {
        public String mFilePath;
        public int mFileSize;
        public FileMirror(String filePath, int fileSize) {
            mFilePath = filePath;
            mFileSize = fileSize;
        }
    }
}