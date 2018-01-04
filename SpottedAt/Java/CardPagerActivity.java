package com.wlu.android.khan_fark_project;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;
import java.util.UUID;

public class CardPagerActivity extends AppCompatActivity {

    private static final String EXTRA_card_ID =
            "com.bignerdranch.android.khan_fark_project.card_id";
    private static final String MAPPER =
            "mapper";
    private static final String REALID=
            "REALONLY";


    private ViewPager mViewPager;
    private List<Card> mCards;
    public static boolean mapOrNot;
    public static UUID cardId;
    public static String realID;

    public static Intent newIntent(Context packageContext, UUID cardId, boolean mapOrNot, String cardRealID) {
        Intent intent = new Intent(packageContext, CardPagerActivity.class);
        intent.putExtra(EXTRA_card_ID, cardId);
        intent.putExtra(MAPPER, mapOrNot);
        intent.putExtra(REALID,cardRealID);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("main","CardPagerActivty");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_pager);

        cardId = (UUID) getIntent()
                .getSerializableExtra(EXTRA_card_ID);

        mapOrNot= (Boolean) getIntent().getSerializableExtra(MAPPER);

        realID= (String) getIntent().getSerializableExtra(REALID);

        Log.d("test","The id we got is: "+cardId);
        mViewPager = (ViewPager) findViewById(R.id.card_view_pager);


        mCards = Deck.get(this).getCards();

        FragmentManager fragmentManager = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                if(mapOrNot==true){

                    for(Card cardz: mCards){
                        Log.d("test","The two ids are \n"+cardz.getId());
                        Log.d("test","cardid is: "+cardId);

                        if(cardz.getImage().equals(realID)){
                            Log.d("test","ITS TRUE");
                            return CardFragment.newInstance(cardz.getId());
                        }
                    }
                }

                Log.d("main","CardPager");

                Card card = mCards.get(position);



                return CardFragment.newInstance(card.getId());
            }

            @Override
            public int getCount() {
                return mCards.size();
            }
        });

        for (int i = 0; i < mCards.size(); i++) {
            if (mCards.get(i).getId().equals(cardId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.d("main","We got to the add button at least");
        switch (item.getItemId()) {
            case R.id.new_post:
                Log.d("main", "add button pressed");
                Intent intent = new Intent(this, CreatePostActivity.class);
                int integerConstant=25;
                startActivityForResult(intent, integerConstant);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    } //onOptionsItemsSelected

}
