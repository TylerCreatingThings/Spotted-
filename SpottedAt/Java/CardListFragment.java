package com.wlu.android.khan_fark_project;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import static android.content.ContentValues.TAG;


@SuppressLint("ShowToast")
public class CardListFragment extends Fragment {

    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    private RecyclerView mcardRecyclerView;
    private cardAdapter mAdapter;
    private boolean mSubtitleVisible;
    private static List<Card> mCards;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private StorageReference mStorageRef;
    public static String urls;
    private static List<Card> newsCardSet= new ArrayList<Card>();

    public static Deck deck;
    public static boolean firstTime=true;
    String baseURL="http://www.cbc.ca/cmlink/rss-topstories/";
    String url;

    public static String location =""; //not used

    public static Bundle saved;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        saved=savedInstanceState;
        Log.d("main","CardListFragment");

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (!checkPermissions()) {
            startLocationPermissionRequest();
        }
    }

    @Override
    @TargetApi(11)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Deck.get(getActivity()).getCards().clear();

        View view = inflater.inflate(R.layout.fragment_card_list, container, false);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("POSTS");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                newsCardSet.clear();
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Card carz = postSnapshot.getValue(Card.class);
                    Log.d("main","Card title is: "+carz.getTitle());
                    newsCardSet.add(carz);
                    // here you can access to name property like university.name
                }
                Object value = dataSnapshot.getValue();
                Log.d("main","datasnapshot is:"+value.toString());
                if(value.toString().equals("NONE")){
                    return;
                }

                updateUI();



                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        mcardRecyclerView = (RecyclerView) view
                .findViewById(R.id.card_recycler_view);
        mcardRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));




        updateUI();

        return view;
    }







    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(getActivity(),
                new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }



    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.



    @Override
    public void onResume() {

        Log.d("main","Its been resumed");
        super.onResume();
        updateUI();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_card_list, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_post:
                Log.d("main", "add button pressed");
                Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                int integerConstant=25;
                startActivityForResult(intent, integerConstant);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("main", "Starting part");

        if (resultCode != Activity.RESULT_OK) {

            updateUI();
            Log.d("main", "location is:");
            return;
        }
    }



    private void updateUI() {
            newsCardSet=Deck.get(getActivity()).getCards();
            mAdapter = new cardAdapter(newsCardSet);
            mcardRecyclerView.setAdapter(mAdapter);

    }

    private class cardHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private Card mCard;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mImageView;

        public cardHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_card, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = (TextView) itemView.findViewById(R.id.card_title);
            mImageView = (ImageView) itemView.findViewById(R.id.theImage);
        }

        public void bind(Card card) {
            mCard = card;
            mTitleTextView.setText(card.getTitle());
            Resources res = getResources();


            if(mCard.getImage()!=null) {

                mStorageRef = FirebaseStorage.getInstance().getReference();
                Log.d("main","image is called: "+mCard.getImage());
                mStorageRef.child(mCard.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("main","made it here");
                        // Got the download URL for 'users/me/profile.png'
                        url=uri.toString();

                        Picasso.with(getActivity().getApplicationContext())
                                .load(url)
                                .error(R.drawable.fahamk)
                                .into(mImageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.d("main","made it here shiiit");
                        Log.d("main","Loading url "+url);
                        // Handle any errors
                        Picasso.with(getActivity().getApplicationContext())
                                .load(url)
                                .error(R.drawable.fahamk)
                                .into(mImageView);
                    }
                });




            }



        }

        @Override
        public void onClick(View view) {
            Intent intent = CardPagerActivity.newIntent(getActivity(), mCard.getId(),false, "");
            startActivity(intent);
        }
    }

    private class cardAdapter extends RecyclerView.Adapter<cardHolder> {


        public cardAdapter(List<Card> cards) {
            newsCardSet = cards;
        }

        @Override
        public cardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new cardHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(cardHolder holder, int position) {
            Card card = newsCardSet.get(position);
            holder.bind(card);
        }

        @Override
        public int getItemCount() {
            return newsCardSet.size();
        }

        public void setcards(List<Card> newsCardSet) {
            newsCardSet = newsCardSet;
        }
    }
     //onOptionsItemsSelected

}
