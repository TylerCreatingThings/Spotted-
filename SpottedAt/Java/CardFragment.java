package com.wlu.android.khan_fark_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class CardFragment extends Fragment {

    private static final String ARG_card_ID = "card_id";
    public static final String EXTRA_MESSAGE = "link";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;

    private Card mCard;
    private TextView mTitleField;
    private TextView mDescriptionField;
    private TextView mCommentsField;
    private ImageView mImageField;
    private String commentList="";
    private StorageReference mStorageRef;
    public static String urls;
    private String m_Text = "";

    public View v;

    public static CardFragment newInstance(UUID cardId) {
        Log.d("main","CardFragment");
        Log.d("test","Got the card, card id is "+cardId);
        Bundle args = new Bundle();
        args.putSerializable(ARG_card_ID, cardId);

        CardFragment fragment = new CardFragment();
        fragment.setArguments(args);


        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("main","CardFragment 2");

        super.onCreate(savedInstanceState);
        UUID cardId = (UUID) getArguments().getSerializable(ARG_card_ID);
        mCard = Deck.get(getActivity()).getCard(cardId);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.card_fragment, container, false);

        mTitleField = (TextView) v.findViewById(R.id.title);
        mTitleField.setText(mCard.getTitle());
        mDescriptionField =(TextView) v.findViewById(R.id.description);
        mDescriptionField.setText(mCard.getDescription());
        Log.d("main","Title is: "+mCard.getTitle());


        Resources res = getResources();
        if(mCard.getImage()!=null) {
            int resourceId = res.getIdentifier(mCard.getImage(), "drawable", "com.wlu.android.khan_fark_project");
            ImageView image = (ImageView) v.findViewById(R.id.cardFragmentImage);
            image.setImageResource(resourceId);
        }




        final Button addCommentButton = (Button) v.findViewById(R.id.addcomment);

        addCommentButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Title");

// Set up the input
                final EditText input = new EditText(getActivity());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        Log.d("main","The input text is "+m_Text);
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference comments = database.getReference("COMMENTS");
                        comments.child("pid"+mCard.getImage()).push().setValue(m_Text);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });






        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("COMMENTS/"+"pid"+mCard.getImage());




        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mCommentsField=(TextView) v.findViewById(R.id.comments);

                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Object snapz=dataSnapshot.getValue();
                String[] comments;
                Log.d("main","Vals are: "+snapz);
                String[] splitter=snapz.toString().split("-L-");
                for(int i=1;i<splitter.length;i++){
                    String commentSplit=splitter[i];
                    String commentPull=commentSplit.substring(18,commentSplit.length()-2);


                    Random rand = new Random();

                    int  n = rand.nextInt(10000) + 1;


                    if(!(commentPull==null || commentPull.contains("Empty post here #1242"))) {
                        if(!commentList.contains(commentPull)) {
                            commentList = commentList + "\nAnonymous" + +n+":\n    "+commentPull+"\n";
                        }
                    }
                    Log.d("main","Comment is: "+commentPull);
                }
                Log.d("main","Comment list is: "+commentList);
                mCommentsField.setText(commentList);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });








        mImageField= (ImageView) v.findViewById(R.id.cardFragmentImage);


        mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child(mCard.getImage()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                urls=uri.toString();
                Log.d("main","Urls is: "+uri.toString());
                Picasso.with(getActivity().getApplicationContext())
                        .load(urls)
                        .error(R.drawable.spottedmarker)
                        .into(mImageField);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                Picasso.with(getActivity().getApplicationContext())
                        .load(urls)
                        .error(R.drawable.spottedmarker)
                        .into(mImageField);
            }
        });
        Picasso.with(getActivity().getApplicationContext())
                .load(mCard.getImage())
                .error(R.drawable.spottedmarker)
                .into(mImageField);

        return v;
    }





    @Override
    public void onPause() {
        super.onPause();

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            Log.d("main","Returning question is"+mCard.getQuestion());

            return;
        }


    }

}
