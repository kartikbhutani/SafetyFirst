package com.vikas.dtu.safetyfirst2.mDiscussion.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ValueEventListener;
import com.vikas.dtu.safetyfirst2.R;
import com.vikas.dtu.safetyfirst2.mData.Post;
import com.vikas.dtu.safetyfirst2.mDiscussion.PostDetailActivity;
import com.vikas.dtu.safetyfirst2.mRecycler.PostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.vikas.dtu.safetyfirst2.mUser.UpdateProfile;
import com.vikas.dtu.safetyfirst2.mUser.UserProfileActivity;

import static com.vikas.dtu.safetyfirst2.mUtils.FirebaseUtil.getCurrentUserId;

public abstract class PostListFragment extends Fragment {

    private static final String TAG = "NewsListFragment";

    private TextView myPosts;
    private ProgressBar progressBar;

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private ProgressBar mProgressBar;

    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public PostListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_posts, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery(mDatabase);
        Log.d("TAG111","p: " + postsQuery+"");
        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(Post.class, R.layout.item_post,
                PostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostViewHolder viewHolder, final Post model, final int position) {

                mProgressBar.setVisibility(ProgressBar.INVISIBLE);

                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                if (model.stars.containsKey(getUid())) {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_24);
                } else {
                    viewHolder.starView.setImageResource(R.drawable.ic_toggle_star_outline_24);
                }

                //Set User Photo
                if (model.getPhotoUrl() == null) {
                    viewHolder.authorImage.setImageDrawable(ContextCompat.getDrawable(getContext(),
                            R.drawable.ic_action_account_circle_40));
                } else {
                    Glide.with(getContext())
                            .load(model.getPhotoUrl())
                            .into(viewHolder.authorImage);
                }

                if (model.getImage() != null) {
                    Glide.with(getContext())
                            .load(model.getImage())
                            .into(viewHolder.postImage);
                    viewHolder.postImage.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.postImage.setVisibility(View.GONE);
                }

                // Bind Post to ViewHolder, setting OnClickListener for the star round_blue_dark
                viewHolder.bindToPost(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalPostRef = mDatabase.child("posts").child(postRef.getKey());
                        DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        // Run two transactions
                        onStarClicked(globalPostRef);
                        onStarClicked(userPostRef);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View authorView) {
                        Intent userDetailIntent = new Intent(getContext(), UserProfileActivity.class);
                        userDetailIntent.putExtra(UserProfileActivity.USER_ID_EXTRA_NAME,
                                model.getPostAuthorUID());

                        startActivity(userDetailIntent);

                    }
                });
            }
        };

        Log.d("TAG111","mAdapter: " + mAdapter+"");
        mRecycler.setAdapter(mAdapter);
    }

    // [START post_stars_transaction]
    private void onStarClicked(DatabaseReference postRef) {
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.stars.containsKey(getUid())) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1;
                    p.stars.remove(getUid());
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1;
                    p.stars.put(getUid(), true);
                }

                // Set value and report transaction success
                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }
    // [END post_stars_transaction]

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    //    public abstract Query getQuery(DatabaseReference databaseReference);
    public Query getQuery(DatabaseReference databaseReference){
        myPosts = (TextView) getView().findViewById(R.id.tv_my_posts);
        myPosts.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
        Query tempQuery= databaseReference.child("user-posts").child(getUid());
        tempQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(Post.class)==null){
                    progressBar.setVisibility(View.INVISIBLE);
                    myPosts.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Log.d("TAG111","t: "+tempQuery+"");
        if(tempQuery.toString()==null){
            {
                progressBar.setVisibility(View.INVISIBLE);
                myPosts.setVisibility(View.VISIBLE);
            }

        }
        /////////////////////////////////////////
        return tempQuery;
    }
}
