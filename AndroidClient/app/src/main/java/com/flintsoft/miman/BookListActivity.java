package com.flintsoft.miman;

import android.app.Activity;
import android.content.Intent;
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


@EActivity(R.layout.activity_book_list)
public class BookListActivity extends Activity implements UrlProvider,AdapterView.OnItemClickListener{
    private final String TAG = this.getClass().getSimpleName();
    private final int pageSize = 8;
    private String categoryId;
    private AutoScrollListHelper scrollListHelper;
    private static String BookListBaseUrl = null;

    @ViewById(R.id.grdBooks)
    GridView grdBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @AfterViews
    protected void InitializeView() {
        if(BookListBaseUrl == null){
            byte[] asBytes = Base64.decode(LocalOnlyPrivateConstants.BookListAPIUrl, Base64.DEFAULT);
            try{
                BookListBaseUrl = new String(asBytes, "UTF-8");
            }catch (UnsupportedEncodingException e){
                Log.e(TAG, e.toString());
            }
        }
        categoryId = getIntent().getStringExtra("Id");
        IBookLoader bookLoader = new WebBookLoader(this,getApplicationContext(), TAG);
        scrollListHelper = new AutoScrollListHelper(getApplication(), getApplicationContext(),this.grdBooks, bookLoader);
        grdBooks.setOnItemClickListener(this);
        scrollListHelper.start();
    }

    public String getUrl( ){
       return scrollListHelper.booksList.size() > 0 ? String.format(BookListBaseUrl + "?startIndex=%d", categoryId, scrollListHelper.booksList.size()) : String.format(BookListBaseUrl + "?resultSize=" + Integer.toString(pageSize), categoryId);
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
