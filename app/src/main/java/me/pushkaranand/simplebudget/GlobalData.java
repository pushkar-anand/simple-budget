package me.pushkaranand.simplebudget;

import android.app.Application;

import java.util.ArrayList;


public class GlobalData extends Application {
    private ArrayList<Integer> notifiedList = new ArrayList<>();

    public boolean isInNotifiedList(Integer x) {
        return notifiedList.contains(x);
    }

    public void addToNotifiedList(Integer x) {
        notifiedList.add(x);
    }
}
