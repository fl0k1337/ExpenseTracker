package com.example.expensetracker;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {
    private final ExpenseDao mDao;
    private final LiveData<List<Expense>> mAllExpenses;

    public ExpenseViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        mDao = db.expenseDao();
        mAllExpenses = mDao.getAllExpenses();
    }

    LiveData<List<Expense>> getAllExpenses() { return mAllExpenses; }

    public void insert(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> mDao.insert(expense));
    }

    public void delete(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> mDao.delete(expense));
    }

    public LiveData<List<Expense>> search(String text) {
        return mDao.searchExpenses("%" + text + "%");
    }
}