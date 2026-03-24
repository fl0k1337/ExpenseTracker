package com.example.expensetracker;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(Expense expense);

    @Delete
    void delete(Expense expense);

    @Update
    void update(Expense expense);

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    LiveData<List<Expense>> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE title LIKE :query OR category LIKE :query ORDER BY date DESC")
    LiveData<List<Expense>> searchExpenses(String query);

    @Query("SELECT * FROM expenses WHERE date >= :startDate ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesFromDate(long startDate);

    @Query("SELECT * FROM expenses WHERE isRecurring = 1")
    LiveData<List<Expense>> getRecurringExpenses();

}