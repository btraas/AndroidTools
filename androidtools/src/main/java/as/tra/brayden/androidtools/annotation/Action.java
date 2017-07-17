package as.tra.brayden.androidtools.annotation;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Iterator;

import static as.tra.brayden.androidtools.annotation.Action.ADD_CUST;
import static as.tra.brayden.androidtools.annotation.Action.HANDLE_FINISH_TOUR;
import static as.tra.brayden.androidtools.annotation.Action.HEARTBEAT_CALLBACK;
import static as.tra.brayden.androidtools.annotation.Action.LOCATION_CHANGED;
import static as.tra.brayden.androidtools.annotation.Action.LOCATION_PROVIDER_DISABLED;
import static as.tra.brayden.androidtools.annotation.Action.LOCATION_PROVIDER_ENABLED;
import static as.tra.brayden.androidtools.annotation.Action.LOCATION_STATUS_CHANGED;
import static as.tra.brayden.androidtools.annotation.Action.LOGOUT;
import static as.tra.brayden.androidtools.annotation.Action.MESSAGE;
import static as.tra.brayden.androidtools.annotation.Action.NEW_DATA;
import static as.tra.brayden.androidtools.annotation.Action.PAUSE_SCANNING;
import static as.tra.brayden.androidtools.annotation.Action.REFRESH;
import static as.tra.brayden.androidtools.annotation.Action.RESUME_SCANNING;
import static as.tra.brayden.androidtools.annotation.Action.UPDATE_UI;

/**
 * Created by brayden on 6/6/2017.
 *
 * Defines Actions that can be broadcasted.
 *
 */



@StringDef({
        MESSAGE,
        REFRESH,
        UPDATE_UI,
        HEARTBEAT_CALLBACK,
        LOCATION_CHANGED,
        LOCATION_STATUS_CHANGED,
        LOCATION_PROVIDER_ENABLED,
        LOCATION_PROVIDER_DISABLED,
        NEW_DATA,
        LOGOUT,
        ADD_CUST,
        PAUSE_SCANNING,
        RESUME_SCANNING,
        HANDLE_FINISH_TOUR
})
@Retention(RetentionPolicy.SOURCE)
public @interface Action {


    String MESSAGE = "action_message";
    String REFRESH = "action_refresh";
    String UPDATE_UI = "action_update_ui";
    String HEARTBEAT_CALLBACK = "action_heartbeat_callback";
    String LOCATION_CHANGED = "action_location_changed";
    String LOCATION_STATUS_CHANGED = "action_location_status_changed";
    String LOCATION_PROVIDER_ENABLED = "action_location_provider_enabled";
    String LOCATION_PROVIDER_DISABLED = "action_location_provider_disabled";
    String NEW_DATA = "action_new_data";
    String LOGOUT = "action_logout";
    String ADD_CUST = "action_add_cust";
    String PAUSE_SCANNING = "action_pause_scanning";
    String RESUME_SCANNING = "action_resume_scanning";
    String HANDLE_FINISH_TOUR = "action_handle_finish_tour";

    @StringDef({
            LOCATION_CHANGED,
            LOCATION_STATUS_CHANGED,
            LOCATION_PROVIDER_DISABLED,
            LOCATION_PROVIDER_ENABLED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Location  {}

    @StringDef({
            PAUSE_SCANNING,
            RESUME_SCANNING
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Scanning {}

    class Broadcast {

        private static final String TAG = Broadcast.class.getSimpleName();

        public Broadcast(@NonNull Context ctx, @Action String action) {
            this(ctx, action,new JSONObject());
        }

        public Broadcast(@NonNull Context ctx, @Action String action, @NonNull JSONObject extras) {

            String[] actions = action.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            if(actions.length > 1) {
                for(@Action String act : actions) {
                    new Broadcast(ctx, act, extras);
                }
                return;
            }

            Intent intent = new Intent();
            intent.setAction(action);

            Log.d(TAG, "Broadcasting "+action);

            Iterator<String> keys = extras.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                try {
                    intent.putExtra(key, extras.getString(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            ctx.sendBroadcast(intent);
        }
    }

}
