package com.example.expensetracker;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "finance_prefs";
    private static final String KEY_BUDGET = "monthly_budget";

    public static void setBudget(Context context, float budget) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putFloat(KEY_BUDGET, budget).apply();
    }

    public static float getBudget(Context context) {
        SharedPreferences pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getFloat(KEY_BUDGET, 50000f);
    }
}