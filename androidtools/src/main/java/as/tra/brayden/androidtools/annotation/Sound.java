package as.tra.brayden.androidtools.annotation;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.media.ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;
import static android.media.ToneGenerator.TONE_CDMA_PIP;
import static com.devrygreenhouses.driverapp.poc.annotation.Sound.FAILURE;
import static com.devrygreenhouses.driverapp.poc.annotation.Sound.NONE;
import static com.devrygreenhouses.driverapp.poc.annotation.Sound.SUCCESS;
import static com.devrygreenhouses.driverapp.poc.annotation.Sound.UPDATE;


/**
 * Created by brayden on 6/6/2017.
 *
 * Defines sounds that can be played.
 *
 */

@StringDef({
        SUCCESS,
        FAILURE,
        NONE,
        UPDATE
})
@Retention(RetentionPolicy.SOURCE)
public @interface Sound {

    String SUCCESS  = "SUCCESS";
    String FAILURE  = "ERROR";
    String NONE     = "NONE";
    String UPDATE   = "UDPATE";



    @IntDef({
            Tone.SUCCESS,
            Tone.FAILURE,
            Tone.UPDATE
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Tone {
        int SUCCESS = TONE_CDMA_ALERT_CALL_GUARD;
        int FAILURE = TONE_CDMA_PIP;
        int UPDATE  = -1; //TONE_CDMA_ANSWER; // TODO make it sound more like an update??
    }
}

