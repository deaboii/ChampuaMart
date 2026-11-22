
package com.champua.mart;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.champua.mart.ui.home.HomeFragment;
import com.champua.mart.ui.category.CategoryFragment;
import com.champua.mart.ui.profile.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment fragment = null;

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();

            } else if (id == R.id.nav_category) {
                fragment = new CategoryFragment();

            } else if (id == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
