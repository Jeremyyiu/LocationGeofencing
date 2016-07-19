package io.locative.app.view;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.Toast;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.locative.app.R;
import javax.inject.Inject;

import io.locative.app.LocativeApplication;
import io.locative.app.network.SessionManager;

public abstract class BaseActivity extends AppCompatActivity {
  @BindView(R.id.toolbar_actionbar) protected Toolbar mToolbar;

  @BindString(R.string.error_permission_refused) String error_permission_refused;

  @Inject protected SessionManager mSessionManager;
  @Inject protected SharedPreferences mPrefs;

  private static final int REQUEST_FINE_LOCATION = 1;

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getLayoutResourceId() != 0) {
      setContentView(getLayoutResourceId());
    }

    ButterKnife.bind(this);

    ((LocativeApplication) getApplication()).inject(this);

    if (mToolbar != null) {
      setSupportActionBar(mToolbar);
      mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24px);
      mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          onBackPressed();
        }
      });
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(this,
            new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQUEST_FINE_LOCATION);
      }
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (mToolbar != null) {
      if (getToolbarTitle() != null) {
        setTitle(getToolbarTitle());
      }
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    if (getMenuResourceId() != 0) {
      getMenuInflater().inflate(getMenuResourceId(), menu);
      return true;
    }
    return false;
  }

  @Override public void onRequestPermissionsResult(int requestCode, String permissions[],
      int[] grantResults) {
    switch (requestCode) {
      case REQUEST_FINE_LOCATION: {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
          Toast.makeText(this, error_permission_refused, Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  /**
   * Return the resourceId for layout.
   *
   * @return 0 or resourceId.
   */
  protected abstract int getLayoutResourceId();

  /**
   * Return the string for toolbar title.
   *
   * @return null or string.
   */
  @Nullable
  protected abstract String getToolbarTitle();

  /**
   * Return the resourceId for menu layout.
   *
   * @return 0 or resourceId.
   */
  protected abstract int getMenuResourceId();
}