package com.example.expensetracker;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import java.util.Calendar;
import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {
    private final ExpenseDao mDao;
    private final LiveData<List<Expense>> mAllExpenses;
    private final MutableLiveData<Long> selectedStartDate = new MutableLiveData<>(getStartOfWeek());
    private final LiveData<List<Expense>> expensesByPeriod;

    public ExpenseViewModel(Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        mDao = db.expenseDao();
        mAllExpenses = mDao.getAllExpenses();

        expensesByPeriod = Transformations.switchMap(selectedStartDate, startDate -> {
            if (startDate == 0) {
                return mDao.getAllExpenses(); // всё время
            } else {
                return mDao.getExpensesFromDate(startDate);
            }
        });
    }

    public LiveData<List<Expense>> getAllExpenses() {
        return mAllExpenses;
    }

    public LiveData<List<Expense>> getExpensesByPeriod() {
        return expensesByPeriod;
    }

    public void setPeriod(int days) {
        long startDate;
        if (days == 0) {
            startDate = 0; // всё время
        } else {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -days);
            startDate = cal.getTimeInMillis();
        }
        selectedStartDate.setValue(startDate);
    }

    public void insert(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> mDao.insert(expense));
    }

    public void delete(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> mDao.delete(expense));
    }

    public LiveData<List<Expense>> search(String text) {
        return mDao.searchExpenses("%" + text + "%");
    }

    private long getStartOfWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public void update(Expense expense) {
        AppDatabase.databaseWriteExecutor.execute(() -> mDao.update(expense));
    }
}