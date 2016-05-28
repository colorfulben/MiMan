package com.flintsoft.miman;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

// Why use AppCompatActivity: This was supported on older versions through ActionBarActivity first which now has been deprecated and replaced with AppCompatActivity. Since, both of these classes extend FragmentActivity they support hosting Fragments as well.
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {
    private Category[] categories;
    private final String TAG = this.getClass().getSimpleName();
    private RequestQueue mQueue;

    @ViewById(R.id.grdCategories)
    GridView grdCategories;

    @ViewById(R.id.edit_search)
    EditText txtSearch;

    @ViewById(R.id.btnSearch)
    ImageButton btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mQueue = Volley.newRequestQueue(getApplicationContext());
    }

    @AfterViews
    protected void setUpUI(){
        loadCategory();
        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    @Click({R.id.btnSearch})
    void searchButtonClicked(){
        String keyword = txtSearch.getText().toString();
        if (keyword != null){
            search(keyword);
        }
    }

    @ItemClick({R.id.grdCategories})
    void selectCategory(HashMap<String, Object> item){
        Intent intent = new Intent(this, BookListActivity_.class);
        intent.putExtra("Id", item.get("Id").toString());
        this.startActivity(intent);
    }

    private void loadCategory(){
        JsonArrayRequest jsReq = new JsonArrayRequest(LocalOnlyPrivateConstants.CategoryAPIUrl,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    initializeCategoryGrid(response);
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

    private void initializeCategoryGrid(JSONArray response){
        ArrayList<HashMap<String, Object>> categoryList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < response.length(); i++) {
            JSONObject jsObj = (JSONObject)response.opt(i) ;
            HashMap<String, Object> map = new HashMap<String, Object>();
            try{
                map.put("Name", jsObj.getString("Name"));
                map.put("Id", jsObj.getInt("Id"));
            }catch (JSONException e){
                Log.e(TAG, e.toString(), e);
            }
            categoryList.add(map);
        }

        SimpleAdapter sa = new SimpleAdapter(this, categoryList,android.R.layout.simple_list_item_1, new String[]{"Name"}, new int[]{android.R.id.text1});
        this.grdCategories.setAdapter(sa);
    }

    private void search(String keyWords){
        Intent intent = new Intent(this, SearchResultActivity_.class);
        intent.putExtra("keywords", keyWords);
        this.startActivity(intent);
    }
}
