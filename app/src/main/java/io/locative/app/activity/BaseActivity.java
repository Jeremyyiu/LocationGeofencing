package io.locative.app.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.locative.app.R;

/**
 * Created by chris on 28.11.15.
 */
public abstract class BaseActivity extends AppCompatActivity {


    @Bind(R.id.toolbar_actionbar)
    protected Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getLayoutResourceId() != 0) {
            setContentView(getLayoutResourceId());
        }
        ButterKnife.bind(this);

        // show background in android statusbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

        // setup toolbar
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getMenuResourceId() != 0) {
            getMenuInflater().inflate(getMenuResourceId(), menu);
            return true;
        }
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mToolbar != null) {
            if (getToolbarTitle() != null) {
                setTitle(getToolbarTitle().toUpperCase());
            }
        }
    }


    /**
     * Return the resoureId for layout
     *
     * @return 0 or resoureId
     */
    protected abstract int getLayoutResourceId();

    /**
     * Return the string for toolbar title
     *
     * @return null or string
     */
    protected abstract String getToolbarTitle();

    /**
     * Return the resoureId for menu layout
     *
     * @return 0 or resoureId
     */
    protected abstract int getMenuResourceId();

}
