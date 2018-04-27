package com.oghbaei.bakingapp;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.oghbaei.bakingapp.Utils.Utils;
import com.oghbaei.bakingapp.queryModel.Recipe;
import com.oghbaei.bakingapp.queryModel.RecipesApi;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeActivity extends AppCompatActivity implements RecipeFragment.OnRecipeFragmentInteractionListener {

    public static final String RECIPE_KEY_RECIPE_ACT_TO_DETAIL_ACT = "RECIPE_KEY_RECIPE_ACT_TO_DETAIL_ACT";

    @BindView(R.id.tv_error_msg_display) TextView mErrorTextView;
    @BindView(R.id.pb_loading_indicator) ProgressBar mLoadingIndicator;
    @BindView(R.id.btn_search_again) Button mRetryButton;
    @BindView(R.id.fl_recipe) FrameLayout mRecipeFragmentFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        ButterKnife.bind(this);

        // Check if it is online.
        boolean isOnline = Utils.isOnline(this);
        if (isOnline) {
            queryDataFromWeb();
        } else {
            showHideErrorMessage(getString(R.string.no_internet), true);
        }
    }

    private void showHideErrorMessage(String msg, boolean show) {
        if (show) {
            mRecipeFragmentFrameLayout.setVisibility(View.INVISIBLE);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mRetryButton.setVisibility(View.VISIBLE);
            mErrorTextView.setVisibility(View.VISIBLE);
            mErrorTextView.setText(msg);
        } else {
            mRecipeFragmentFrameLayout.setVisibility(View.VISIBLE);
            mRetryButton.setVisibility(View.INVISIBLE);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            mErrorTextView.setVisibility(View.INVISIBLE);
        }
    }

    private void queryDataFromWeb() {
        mLoadingIndicator.setVisibility(View.VISIBLE);
        mRetryButton.setVisibility(View.INVISIBLE);
        RecipesApi recipesApi = RecipesApi.retrofit.create(RecipesApi.class);
        Call<ArrayList<Recipe>> call = recipesApi.getRecipes();
        call.enqueue(new Callback<ArrayList<Recipe>>() {
            @Override
            public void onResponse(@NonNull Call<ArrayList<Recipe>> call, @NonNull Response<ArrayList<Recipe>> response) {
                ArrayList<Recipe> recipes = response.body();
                if (recipes == null || recipes.isEmpty()) return;

                RecipeFragment recipeFragment = RecipeFragment.newInstance(recipes);
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(R.id.fl_recipe, recipeFragment)
                        .commitAllowingStateLoss();

                showHideErrorMessage(null, false);
            }

            @Override
            public void onFailure(@NonNull Call<ArrayList<Recipe>> call, @NonNull Throwable t) {
                showHideErrorMessage(getString(R.string.no_internet), true);
            }
        });
    }

    public void onRetryButtonClicked(View view) {
        if (view.getId() == R.id.btn_search_again) {
            queryDataFromWeb();
        }
    }

    @Override
    public void onRecipePassData(Recipe recipe) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(RECIPE_KEY_RECIPE_ACT_TO_DETAIL_ACT, recipe);
        startActivity(intent);

    }
}
