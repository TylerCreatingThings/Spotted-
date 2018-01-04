package com.wlu.android.khan_fark_project;

import android.content.Context;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Deck {
    private static Deck sDeck;
    private List<Card> cards;
    private static List<Card> checkingCards;
    public static boolean back=false;
    public static Deck get(Context context){
        if(sDeck == null) {
            Log.d("main","Recreated");
            sDeck = new Deck(context);
        }


        return sDeck;
    }


    private Deck(Context context){
        cards= new ArrayList<>();
    }

    public List<Card> getCards(){
        return cards;
    }

    public Card getCard(UUID id){
        for(Card card : cards){
            if(card.getId().equals(id)){
                return card;
            }
        }
        return null;
    }



    public void addCard(Card c) {
        cards.add(c);
    }





}
