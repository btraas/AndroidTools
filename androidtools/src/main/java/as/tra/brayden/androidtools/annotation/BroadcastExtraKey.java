package as.tra.brayden.androidtools.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.devrygreenhouses.driverapp.poc.annotation.BroadcastExtraKey.CUST_NO;


/**
 * Created by brayden on 6/6/2017.
 *
 * Defines sounds that can be played.
 *
 */

@StringDef({
        CUST_NO
})
@Retention(RetentionPolicy.SOURCE)
public @interface BroadcastExtraKey {
    String CUST_NO = "cust_no";
}

