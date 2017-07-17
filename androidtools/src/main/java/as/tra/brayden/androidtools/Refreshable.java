package as.tra.brayden.androidtools;


/**
 * Created by brayden on 6/1/2017.
 *
 * For refreshable items
 */

public interface Refreshable {

    int REFRESH_MENU_ITEM_ID = 592;

    // triggers a refresh of data
    void refresh();

    // callback for when data is changed
    void onDataChanged();


}
