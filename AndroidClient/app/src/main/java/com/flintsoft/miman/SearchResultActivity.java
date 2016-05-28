package com.flintsoft.miman;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;


@EActivity
public class SearchResultActivity extends Activity implements UrlProvider,AdapterView.OnItemClickListener{
    private final String TAG = this.getClass().getSimpleName();
    private final int pageSize = 8;
    private static String SearchResultBaseUrl = null;
    private AutoScrollListHelper scrollListHelper;
    private String keywords;

    @ViewById(R.id.grdBooks)
    GridView grdBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);
    }

    @AfterViews

    protected void InitializeView(){
        keywords = getIntent().getStringExtra("keywords");
        IBookLoader bookLoader = new WebBookLoader(this,getApplicationContext(), TAG);
        scrollListHelper = new AutoScrollListHelper(getApplication(), getApplicationContext(),this.grdBooks, bookLoader);
        grdBooks.setOnItemClickListener(this);
        scrollListHelper.start();
    }

    public String getUrl( ){
        if(SearchResultBaseUrl == null){
            byte[] asBytes = Base64.decode(LocalOnlyPrivateConstants.SearchAPIUrl, Base64.DEFAULT);
            try{
                SearchResultBaseUrl = new String(asBytes, "UTF-8");
            }catch (UnsupportedEncodingException e){
                Log.e(TAG, e.toString());
                return null;
            }
        }
        return scrollListHelper.booksList.size() > 0 ?
                // Load remaining items
                Uri.parse(SearchResultBaseUrl).buildUpon()
                        .appendQueryParameter("keywords", keywords)
                        .appendQueryParameter("startIndex", Integer.toString(scrollListHelper.booksList.size()))
                        .build().toString()
                // Load first result page
                :Uri.parse(SearchResultBaseUrl).buildUpon()
                        .appendQueryParameter("keywords", keywords)
                        .appendQueryParameter("startIndex", Integer.toString(0))
                        .appendQueryParameter("resultSize", Integer.toString(pageSize))
                        .build().toString();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, BookDetailActivity_.class);
        intent.putExtra("Cover", (Parcelable)item.get("Cover"));
        intent.putExtra("Id", item.get("Id").toString());
        this.startActivity(intent);
    }
}
