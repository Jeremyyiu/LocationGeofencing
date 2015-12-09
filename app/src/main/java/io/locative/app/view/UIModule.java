package io.locative.app.view;

import dagger.Module;

/**
 * Created by chris on 08.12.15.
 */

@Module(
        injects = {
                BaseActivity.class,
                SettingsActivity.class,
                SignupActivity.class,
                AddEditGeofenceActivity.class,
                GeofencesActivity.class
        },
        library = true,
        complete = false
)
public class UIModule {
}
