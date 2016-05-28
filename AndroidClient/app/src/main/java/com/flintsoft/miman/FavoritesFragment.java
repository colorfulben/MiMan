package com.flintsoft.miman;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.flintsoft.miman.dao.Favorites;
import com.flintsoft.miman.dao.FavoritesDAO;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

@EFragment
public class FavoritesFragment extends Fragment implements UrlProvider, AdapterView.OnItemClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private String urlPrefix;
    private AutoScrollListHelper scrollListHelper;
    private SQLiteDatabase db;
    private ArrayList<Integer> ids = new ArrayList<>();
    private int currentPageIndex = 0;
    private final int pageSize = 8;

    @ViewById(R.id.grdBooks)
    GridView grdBooks;

    @AfterViews
    public void InitializeView(){
        setBaseUrl();
        FavoritesDAO dbHelper = new FavoritesDAO(getActivity());
        db = dbHelper.getReadableDatabase();
        Cursor cur = db.query(Favorites.TABLE,null,null,null,null, null,null);
        if (cur != null && cur.moveToFirst()) {
            do{
                ids.add(cur.getInt(cur.getColumnIndex(Favorites.Column.ID)));
            }while(cur.moveToNext());
        }
    }

    @Override
    public void onStop() {
        if (db != null) {
            db.close();
        }
        super.onStop();
    }

    @Override
    public String getUrl() {
        String url = null;
        int startIndex = currentPageIndex * pageSize;
        if(ids.size() > startIndex){
            Uri.Builder uriBuilder = Uri.parse(urlPrefix).buildUpon();
            for (int i = 0; i < pageSize && startIndex + i < ids.size(); i++){
                uriBuilder.appendQueryParameter("ids", Integer.toString(ids.get(startIndex + i)));
            }
            url = uriBuilder.build().toString();
        }

        return url;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), BookDetailActivity_.class);
        intent.putExtra("Cover", (Parcelable)item.get("Cover"));
        intent.putExtra("Id", item.get("Id").toString());
        this.startActivity(intent);
    }

    private boolean setBaseUrl() {
        byte[] asBytes = Base64.decode(LocalOnlyPrivateConstants.FavoritesAPIUrl, Base64.DEFAULT);
        try{
            urlPrefix = new String(asBytes, "UTF-8");
        }catch (UnsupportedEncodingException e){
            Log.e(TAG, e.toString());
            return false;
        }
        return true;
    }
//    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FavoritesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FavoritesFragment newInstance(String param1, String param2) {
        FavoritesFragment fragment = new FavoritesFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_book_list, container, false);
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        public void onFragmentInteraction(Uri uri);
//    }

}
