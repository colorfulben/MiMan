package com.flintsoft.miman;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.flintsoft.miman.dao.Favorites;
import com.flintsoft.miman.dao.FavoritesDAO;
import com.flintsoft.miman.dao.LastReadPosition;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

@EActivity
public class BookDetailActivity extends Activity implements AdapterView.OnItemClickListener{

    private JSONObject book;
    private final String TAG = this.getClass().getSimpleName();
    private boolean isFavorite = false;
    private ArrayAdapter aa;
    private RequestQueue mQueue;
    private SQLiteDatabase db;
    private FavoritesDAO dbHelper;
    private String bookId;
    private ArrayList<HashMap<String, Integer>> chapters;

    @ViewById(R.id.imgDetailCover)
    ImageView imgDetailCover;

    @ViewById(R.id.txtDetailTitle)
    TextView txtDetailTitle;

    @ViewById(R.id.txtDetailAuthor)
    TextView txtDetailAuthor;

    @ViewById(R.id.txtDetailVisited)
    TextView txtDetailVisited;

    @ViewById(R.id.txtDetailRating)
    TextView txtDetailRating;

    @ViewById(R.id.btnDetailStart)
    Button btnDetailStart;

    @ViewById(R.id.btnDetailFavorite)
    Button btnDetailFavorite;

    @ViewById(R.id.txtDetailIntro)
    TextView txtDetailIntro;

    @ViewById(R.id.grdDetailChapters)
    GridView grdDetailChapters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (db == null){
            dbHelper = new FavoritesDAO(this);
            db = dbHelper.getWritableDatabase();
        }
        mQueue = Volley.newRequestQueue(getApplicationContext());
        setContentView(R.layout.activity_book_detail);
    }

    @Override
    protected void onStop() {
        if (db != null) {
            db.close();
        }
        super.onStop();
    }

    @AfterViews
    protected void InitializeView() {
        isFavorite = getIsFavorite();
        if (isFavorite) {
            btnDetailFavorite.setText("取消收藏");
        }
        Intent intent = getIntent();
        imgDetailCover.setImageBitmap((Bitmap) intent.getParcelableExtra("Cover"));
        grdDetailChapters.setOnItemClickListener(this);
        String id = intent.getStringExtra("Id");
        JsonObjectRequest jsReq = new JsonObjectRequest(
                LocalOnlyPrivateConstants.GetBookDetailsAPIUrl + id,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject data) {
                        try {
                            txtDetailTitle.setText(data.getString("Title"));
                            txtDetailAuthor.setText("作者: " + data.getString("Author"));
                            txtDetailVisited.setText("阅读次数: " + data.getString("Visited"));
                            txtDetailRating.setText("评分: " + data.getString("Rating"));
                            txtDetailIntro.setText(data.getString("Intro"));
                            bookId = data.getString("BookId");
                            JSONArray rawChapters = data.getJSONArray("Chapters");
                            JSONObject[] sortedChapters = new JSONObject[rawChapters.length()];
                            for (int i = 0; i < rawChapters.length(); i++) {
                                sortedChapters[i] = (JSONObject)rawChapters.opt(i);
                            }
                            Arrays.sort(sortedChapters, new Comparator<JSONObject>() {
                                @Override
                                public int compare(JSONObject lhs, JSONObject rhs) {
                                    try {
                                        boolean lisVolume = lhs.getBoolean("IsVolume");
                                        boolean risVolume = rhs.getBoolean("IsVolume");
                                        if (lisVolume && !risVolume) {
                                            return -1;
                                        } else if (!lisVolume && risVolume) {
                                            return 1;
                                        }
                                        return Integer.parseInt(lhs.getString("Index")) - Integer.parseInt(rhs.getString("Index"));
                                    }  catch (NumberFormatException|JSONException e) {
                                        Log.e(TAG, e.toString(), e);
                                    }
                                    return 0;
                                }
                            });

                            chapters = new ArrayList<HashMap<String, Integer>>(sortedChapters.length);
                            String[] chapterNames = new String[sortedChapters.length];
                            for (int i = 0; i < sortedChapters.length; i++) {
                                JSONObject chapter = (JSONObject)sortedChapters[i];
                                String chapterName = chapter.getString("DisplayName");
                                if (chapterName == null || chapterName == "null"){
                                    chapterName = "";
                                    if(chapter.getBoolean("IsVolume")){
                                        chapterName = "卷";
                                    }
                                    chapterName += chapter.getString("Index");
                                }
                                chapterNames[i] = chapterName;
                                HashMap<String, Integer> map = new HashMap<String, Integer>();
                                map.put(chapterName, chapter.getInt("TotalPageNum"));
                                chapters.add(map);
                            }
                            aa = new ArrayAdapter(getApplication(),
                                    R.layout.chapter_item,
                                    chapterNames);
                            grdDetailChapters.setAdapter(aa);
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString(), e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, error.toString(), error);
                    }
                });

        jsReq.setShouldCache(true);
        mQueue.add(jsReq);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String chapterName = (String) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, PageViewerActivity_.class);
        intent.putExtra("bookId", bookId);
        int chapterIndex = 0;
        for (HashMap<String, Integer> map: chapters){
            if (map.containsKey(chapterName)){
                chapterIndex = chapters.indexOf(map);
            }
        }
        intent.putExtra("chapterIndex", chapterIndex);
        intent.putExtra("chapterList", chapters);
        this.startActivity(intent);
    }

    public void beginRead(View view){
        Intent intent = new Intent(this, PageViewerActivity_.class);
        intent.putExtra("bookId", bookId);
        intent.putExtra("chapterList", chapters);
        this.startActivity(intent);
    }

    public void doFavorite(View view){
        if (isFavorite){
            db.delete(Favorites.TABLE, Favorites.Column.ID + "=?", new String[]{ bookId});
            isFavorite = false;
            this.btnDetailFavorite.setText("加入收藏");
        }else{
            ContentValues values = new ContentValues();
            values.put(Favorites.Column.ID, bookId);
            db.insertWithOnConflict(Favorites.TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            isFavorite = true;
            btnDetailFavorite.setText("取消收藏");
        }
    }

    private boolean getIsFavorite(){
        Cursor cur = db.query(Favorites.TABLE, null, LastReadPosition.Column.ID + "=?", new String[]{ bookId},null, null,null);
        if (cur != null && cur.moveToFirst()) {
            cur.close();
            return true;
        }else{
            return false;
        }
    }
}
