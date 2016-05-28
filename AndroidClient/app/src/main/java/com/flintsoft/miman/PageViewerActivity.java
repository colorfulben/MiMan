package com.flintsoft.miman;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.flintsoft.miman.dao.LastReadPosition;
import com.flintsoft.miman.dao.LastReadPositionDAO;
import com.jakewharton.disklrucache.DiskLruCache;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

@EActivity
public class PageViewerActivity extends Activity implements ViewSwitcher.ViewFactory, View.OnTouchListener {

    private final String TAG = this.getClass().getSimpleName();
    private final int DefaultPageStartIndex = 1;
    private float downX;
    private String bookId;
    private Integer mChapterIndex = -1;
    private Integer mPageIndex = -1;
    // ChapterList: key is chapter name, value is total page number.
    private ArrayList<HashMap<String, Integer>> chapterList;
    private static DiskLruCache diskLruCache;
    private static LruCache<String, Bitmap> mLruCache;
    private String urlPrefix;
    private RequestQueue mQueue;
    private Bitmap page404 = null;
    private LastReadPositionDAO dbHelper;
    private SQLiteDatabase db;
    private boolean indexSet = false;
    private boolean isLoading = false;
    final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();


    @ViewById(R.id.imgSwitcher)
    ImageSwitcher imgSwitcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!setBaseUrl()) return;
        super.onCreate(savedInstanceState);
        dbHelper = new LastReadPositionDAO(this);
        db = dbHelper.getWritableDatabase();
        mQueue = Volley.newRequestQueue(getApplicationContext());
        setContentView(R.layout.activity_page_viewer);
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.e(TAG, paramThrowable.getMessage());
                Log.e(TAG, paramThrowable.toString());
                System.exit(2);
            }
        });
    }

    @Override
    protected void onPause() {
        ContentValues values = new ContentValues();
        values.put(LastReadPosition.Column.ID, bookId);
        values.put(LastReadPosition.Column.ChapterIndex,mChapterIndex);
        values.put(LastReadPosition.Column.PageIndex, mPageIndex);
        db.insertWithOnConflict(LastReadPosition.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (db != null) {
            db.close();
        }
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(LastReadPosition.Column.ChapterIndex, mChapterIndex);
        outState.putInt(LastReadPosition.Column.PageIndex, mPageIndex);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null){
            mChapterIndex = savedInstanceState.getInt(LastReadPosition.Column.ChapterIndex);
            mPageIndex = savedInstanceState.getInt(LastReadPosition.Column.PageIndex);
            indexSet = true;
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        if (!indexSet){
            setChapterPageIndex(intent);
        }

        chapterList = (ArrayList<HashMap<String, Integer>>) intent.getSerializableExtra("chapterList");
        imgSwitcher.setFactory(this);
        imgSwitcher.setOnTouchListener(this);
        flipPage();
        super.onResume();
    }

    private boolean setBaseUrl() {
        byte[] asBytes = Base64.decode(LocalOnlyPrivateConstants.ImageAPIUrl, Base64.DEFAULT);
        try{
            urlPrefix = new String(asBytes, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }

    private void setChapterPageIndex(Intent intent){
        // If user clicked a specific chapter
        mChapterIndex = intent.getIntExtra("chapterIndex", -1);
        mPageIndex = DefaultPageStartIndex;

        // Otherwise load last read position
        if (mChapterIndex < 0){
            Cursor cur = db.query(LastReadPosition.TABLE, null, LastReadPosition.Column.ID + "=?", new String[]{ bookId},null, null,null);
            if (cur != null && cur.moveToFirst()) {
                mChapterIndex = cur.getInt(cur.getColumnIndex(LastReadPosition.Column.ChapterIndex));
                mPageIndex = cur.getInt(cur.getColumnIndex(LastReadPosition.Column.PageIndex));
                cur.close();
            }
        }

        // Start from the first page by default
        if (mChapterIndex < 0){
            mChapterIndex = 0;
            mPageIndex = DefaultPageStartIndex;
        }
    }

    @AfterViews
    protected void InitializeView() {
        int maxSize = (int) (Runtime.getRuntime().maxMemory() / 4);
        // 实例化LruCaceh对象
        mLruCache = new LruCache<String, Bitmap>(maxSize) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                Log.d(TAG, key + "is removed");
                oldValue.recycle();
                super.entryRemoved(evicted, key, oldValue, newValue);
            }

            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount();
            }
        };
        File cacheDir = Util.getDiskCacheDir(this);
        if (!cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                Log.e(TAG, "Unable to create cache directory.");
            }
        }

        try {
            diskLruCache = DiskLruCache.open(cacheDir, Util.getAppVersion(this), 1, 64 * 1024 * 1024);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    private void flipPage() {
        loadImage(false, bookId, getChapterNameByIndex(mChapterIndex), mPageIndex.toString());
        int[] nextPageIndexes = GetNextPage(mChapterIndex, mPageIndex);
        if (nextPageIndexes != null) {
            loadImage(true, bookId, getChapterNameByIndex(nextPageIndexes[0]), Integer.toString(nextPageIndexes[1]));
        }
    }

    private void loadImage(final boolean isPreload, String... params
            ){
        try {
            if (!isPreload){
                isLoading = true; // if it's reload, don't change current state,
            }
            Bitmap bm;
            final String[] params2 = params; // A work around for compilation error, otherwise compiler can't tell whether it's array or null.
            if (existInMemCache(params) || existInDiskCache(params)) {
                if (!isPreload) {
                    bm = loadFromCache(params);
                    imgSwitcher.setImageDrawable(new BitmapDrawable(getResources(), bm));
                    isLoading = false;
                }
            }
            else{
                // base + book id + chapter + page index
                String URL = String.format("%s/%s/%s/%s.jpg", urlPrefix, params[0], URLEncoder.encode(params[1], "UTF-8"), params[2]);
                Log.d(TAG, String.format("Downloading image from %s", URL));
                ImageRequest request = new ImageRequest(URL,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap bm) {
                                if (bm != null) {
                                    saveToCache(true, bm, params2);
                                    if (!isPreload) {
                                        imgSwitcher.setImageDrawable(new BitmapDrawable(getResources(), bm));
                                        isLoading = false;
                                    }
                                }else{
                                    handleImageLoadingFailure();
                                    isLoading = false;
                                }
                            }
                        }, 0, 0, Bitmap.Config.RGB_565,
                        new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                handleImageLoadingFailure();
                                isLoading = false;
                            }
                        });
                request.setShouldCache(false);
                mQueue.add(request);
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    private void handleImageLoadingFailure(){
        if (page404 == null || page404.isRecycled()) {
            Resources res = getResources();
            page404 = BitmapFactory.decodeResource(res, R.drawable.page404);
        }
        imgSwitcher.setImageDrawable(new BitmapDrawable(getResources(), page404));
    }

    @Nullable
    private int[] GetNextPage(int chapterIndex, int pageIndex) {
        int[] returnValue = new int[2];
        if (pageIndex == chapterList.get(chapterIndex).values().iterator().next()) {
            if (chapterIndex < chapterList.size() - 1) {
                chapterIndex++;
            } else {
                return null;
            }
            pageIndex = 1;
        } else {
            pageIndex++;
        }
        returnValue[0] = chapterIndex;
        returnValue[1] = pageIndex;
        return returnValue;
    }

    @Nullable
    private int[] GetPreviousPage(int chapterIndex, int pageIndex) {
        int[] returnValue = new int[2];
        if (pageIndex == 1) {
            if (chapterIndex > 0) {
                chapterIndex--;
                pageIndex = chapterList.get(chapterIndex).values().iterator().next();
            } else {
                return null;
            }
        } else {
            pageIndex--;
        }

        returnValue[0] = chapterIndex;
        returnValue[1] = pageIndex;
        return returnValue;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isLoading) return false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                break;
            }
            case MotionEvent.ACTION_UP: {
                float lastX = event.getX();
                if (lastX < downX) { // go forward
                    imgSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.slide_in_right));
                    imgSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), R.anim.slide_out_left));
                    int[] indexes = GetNextPage(mChapterIndex, mPageIndex);
                    if (indexes != null) {
                        mChapterIndex = indexes[0];
                        mPageIndex = indexes[1];
                        flipPage();
                    } else {
                        Toast.makeText(getApplication(), "已经是最后一页啦", Toast.LENGTH_SHORT).show();
                    }
                } else if (lastX > downX) { // go back
                    imgSwitcher.setInAnimation(AnimationUtils.loadAnimation(getApplication(), android.R.anim.slide_in_left));
                    imgSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getApplication(), android.R.anim.slide_out_right));
                    int[] indexes = GetPreviousPage(mChapterIndex, mPageIndex);
                    if (indexes != null) {
                        mChapterIndex = indexes[0];
                        mPageIndex = indexes[1];
                        flipPage();
                    }
                }
                break;
            }
        }
        return true;
    }

    @Override
    public View makeView() {
        final ImageView i = new ImageView(this);
        i.setBackgroundColor(0xff000000);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return i;
    }

    private Bitmap loadFromCache(String... params) {
        Bitmap bm = null;
        String cacheKey = getCacheItemKey(params);
        try {
            bm = mLruCache.get(cacheKey);
            if (bm == null) {
                DiskLruCache.Snapshot snapshot = diskLruCache.get(cacheKey);
                if (snapshot != null) {
                    InputStream in = snapshot.getInputStream(0);
                    bm = BitmapFactory.decodeStream(in, null, bitmapOptions);
                    in.close();
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }

        Log.i(TAG, String.format("looking for %s, %s in cache, %s, key: %s",
                params[1],
                params[2],
                bm == null ? "missed" : "hit",
                cacheKey));
        return bm;
    }

    private void saveToCache(boolean saveInMemCache, Bitmap bm, String... params) {
        try {
            String cacheKey = getCacheItemKey(params);
            if (saveInMemCache) {
                mLruCache.put(cacheKey, bm);
                Log.d(TAG, String.format("%s, %s saved to mem cache, key: %s", params[1], params[2], cacheKey));
            }
            DiskLruCache.Editor editor = diskLruCache.edit(cacheKey);
            if (editor != null) {
                OutputStream ost = editor.newOutputStream(0);
                if (bm.compress(Bitmap.CompressFormat.PNG, 100, ost)) {
                    editor.commit();
                    ost.close();
                    Log.d(TAG, String.format("%s, %s saved to disk cache, key: %s", params[1], params[2], cacheKey));
                } else {
                    editor.abort();
                }
                diskLruCache.flush();
            }
        } catch (Throwable e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    private boolean existInMemCache(String... params) {
        String cacheKey = getCacheItemKey(params);
        return mLruCache.get(cacheKey) != null;
    }

    private boolean existInDiskCache(String... params) {
        String cacheKey = getCacheItemKey(params);
        try {
            return diskLruCache.get(cacheKey) != null;
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }
        return false;
    }

    private String getCacheItemKey(String... params) {
        String rawKey = String.format("%s_%s_%s", params[0], params[1], params[2]);
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(rawKey.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(rawKey.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        if (bytes!=null) {
            for (byte b :bytes) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
        }
        return sb.toString();
    }

    private String getChapterNameByIndex(int index){
        if (index < chapterList.size()) {
            return chapterList.get(index).keySet().iterator().next();
        }else{
            return null;
        }
    }
}
