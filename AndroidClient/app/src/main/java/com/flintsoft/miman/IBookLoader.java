package com.flintsoft.miman;


import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Xin on 2016/1/10.
 */
public interface IBookLoader {
    public void setAdapter(SimpleAdapter adapter);
    public void setBookList(ArrayList<HashMap<String, Object>> booksList);
    public void loadBooks();
}
