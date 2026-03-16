package com.example.expensetracker;

import android.os.Bundle;
import java.util.List;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ExpenseViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        BottomNavigationView nav = findViewById(R.id.bottom_navigation);

        // По умолчанию открываем список (HomeFragment)
        loadFragment(new HomeFragment());

        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) loadFragment(new HomeFragment());
            else if (item.getItemId() == R.id.nav_stats) loadFragment(new StatsFragment());
            return true;
        });

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            AddExpenseSheet sheet = new AddExpenseSheet((title, amount, category, description) -> {
                viewModel.insert(new Expense(title, amount, category, description, System.currentTimeMillis()));
            });
            sheet.show(getSupportFragmentManager(), "add_expense");
        });
    }

    private void loadFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, f)
                .commit();
    }
}