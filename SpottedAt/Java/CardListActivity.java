package com.wlu.android.khan_fark_project;

import android.support.v4.app.Fragment;
import android.util.Log;

public class CardListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        Log.d("main","CardListActivity");
        return new CardListFragment();
    }


}
