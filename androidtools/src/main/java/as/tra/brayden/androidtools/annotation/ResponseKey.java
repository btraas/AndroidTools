package as.tra.brayden.androidtools.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.ACTION_BROADCAST;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.ACTION_BROADCAST_EXTRAS;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.ALERT;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.BROADCAST;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.BROADCAST_EXTRAS;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.CANCEL;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.DIALOG;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.ERROR;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.LAST_FETCH;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.LINK;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.LOGOUT;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.MESSAGE;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.RESPONSE;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.SOUND;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.STATUS;
import static com.devrygreenhouses.driverapp.poc.annotation.ResponseKey.SUCCESS;

/**
 * Created by brayden on 6/6/2017.
 *
 * Defines possible response JSONObject keys from the server
 *
 */



@StringDef({
        ERROR,
        MESSAGE,
        SUCCESS,
        ALERT,
        BROADCAST,
        BROADCAST_EXTRAS,
        LINK,
        ACTION_BROADCAST,
        ACTION_BROADCAST_EXTRAS,
        DIALOG,
        CANCEL,
        RESPONSE,
        STATUS,
        SOUND,
        LOGOUT,
        LAST_FETCH

})
@Retention(RetentionPolicy.SOURCE)
public @interface ResponseKey {

    String ERROR                    = "error";
    String MESSAGE                  = "message";
    String SUCCESS                  = "success";
    String ALERT                    = "alert";
    String BROADCAST                = "broadcast";
    String BROADCAST_EXTRAS         = "broadcast_extras";
    String LINK                     = "link";
    String ACTION_BROADCAST         = "action_broadcast";
    String ACTION_BROADCAST_EXTRAS  = "action_broadcast_extras";
    String CANCEL_BROADCAST         = "cancel_broadcast";
    String BUTTONTEXT               = "buttontext";
    String DESCRIPTION              = "description";
    String DIALOG                   = "dialog";
    String CANCEL                   = "cancel";
    String RESPONSE                 = "response";
    String STATUS                   = "status";
    String SOUND                    = "sound";
    String LOGOUT                   = "logout";
    String LAST_FETCH               = "last_fetch";

}
