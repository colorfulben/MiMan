package com.flintsoft.miman;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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
 * Created by Xin on 2016/1/10.
 */
public class WebBookLoader implements IBookLoader {
    private String TAG;
    private boolean loading = true;
    private boolean noMore = false;
    private Context appContext;
    private SimpleAdapter adapter;
    private ArrayList<HashMap<String, Object>> booksList = new ArrayList<HashMap<String, Object>>();
    private UrlProvider urlProvider;
    private RequestQueue mQueue;

    public WebBookLoader(UrlProvider urlProvider, Context appContext, String tag){
        this.urlProvider = urlProvider;
        this.appContext = appContext;
        this.mQueue = Volley.newRequestQueue(appContext);
        this.TAG = tag;
    }

    @Override
    public void setAdapter(SimpleAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public void setBookList(ArrayList<HashMap<String, Object>> booksList) {
        this.booksList = booksList;
    }

    @Override
    public void loadBooks() {
        if (!loading && !noMore) {
            loading = true;
            String url = urlProvider.getUrl();
            if (url != null) {
                JsonArrayRequest jsReq = new JsonArrayRequest(
                        url,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                processResponse(response);
                                loading = false;
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, error.toString(), error);
                                loading = false;
                            }
                        });
                jsReq.setShouldCache(true);
                mQueue.add(jsReq);
            }
        }
    }

    private void processResponse(JSONArray loadedBooks) {
        if (loadedBooks != null && loadedBooks.length() > 0) {
            for (int i = 0; i < loadedBooks.length(); i++) {
                JSONObject jsObj = (JSONObject) loadedBooks.opt(i);
                HashMap<String, Object> map = new HashMap<String, Object>();
                try {
                    map.put("Title", jsObj.getString("Title"));
                    byte[] data = Base64.decode(jsObj.getString("Cover"), Base64.NO_WRAP);
                    Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                    map.put("Cover", bm);
                    map.put("Id",jsObj.getString("Id"));
                } catch (JSONException e) {
                    Log.e(TAG, e.toString(), e);
                }
                booksList.add(map);
            }
            adapter.notifyDataSetChanged();
        } else {
            noMore = true;
        }
    }
}
