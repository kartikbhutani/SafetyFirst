package com.vikas.dtu.safetyfirst2.mDiscussion.fragment;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.vikas.dtu.safetyfirst2.R;

public class MyPostsFragment extends PostListFragment {

    private TextView myPosts;
    private LinearLayout lltext;
    private ProgressBar progressBar;

    public MyPostsFragment() {
    }

//    @Override
//    public Query getQuery(DatabaseReference databaseReference) {
//        // All my posts
//        myPosts = (TextView) getView().findViewById(R.id.tv_my_posts);
//        myPosts.setVisibility(View.INVISIBLE);
//        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
//        Query tempQuery= databaseReference.child("user-posts").child(getUid());
//        Log.d("TAG111",tempQuery+"");
//        if(tempQuery.toString()==null){
//            {
//                progressBar.setVisibility(View.INVISIBLE);
//                myPosts.setVisibility(View.VISIBLE);
//            }
//
//        }
//        /////////////////////////////////////////
//        return tempQuery;
//
//    }
}
