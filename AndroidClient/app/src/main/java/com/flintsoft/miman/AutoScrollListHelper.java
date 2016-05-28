package com.flintsoft.miman;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xin on 2015/11/28.
 */
public class AutoScrollListHelper implements AbsListView.OnScrollListener {
    private boolean isLastItem = false;
    private Application app;
    private IBookLoader bookLoader;

    public SimpleAdapter adapter;
    public ArrayList<HashMap<String, Object>> booksList = new ArrayList<HashMap<String, Object>>();

    public AutoScrollListHelper(Application app, Context appContext, GridView grdBooks, IBookLoader bookLoader){
        this.app = app;
        this.bookLoader = bookLoader;
        adapter = new SimpleAdapter(appContext,
                booksList, R.layout.book_list_item,
                new String[]{"Title", "Cover"},
                new int[]{R.id.txtListTitle, R.id.imgListCover});

        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view instanceof ImageView && data instanceof Bitmap) {
                    ImageView iv = (ImageView) view;
                    iv.setImageBitmap((Bitmap) data);
                    return true;
                } else return false;
            }
        });
        grdBooks.setAdapter(adapter);
        grdBooks.setOnScrollListener(this);
        bookLoader.setAdapter(adapter);
        bookLoader.setBookList(booksList);
    }

    public void start(){
        Toast.makeText(app, "加载中...", Toast.LENGTH_SHORT).show();
        this.bookLoader.loadBooks();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && isLastItem) {
            Toast.makeText(app, "加载中...", Toast.LENGTH_SHORT).show();
            this.bookLoader.loadBooks();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        isLastItem = firstVisibleItem + visibleItemCount == booksList.size();
        if (isLastItem) {
            Toast.makeText(app, "加载中...", Toast.LENGTH_SHORT).show();
            this.bookLoader.loadBooks();
        }
    }
}
