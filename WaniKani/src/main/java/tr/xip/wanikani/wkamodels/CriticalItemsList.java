package tr.xip.wanikani.wkamodels;

import java.util.ArrayList;

import tr.xip.wanikani.database.DatabaseManager;

public class CriticalItemsList extends ArrayList<CriticalItem> implements Storable {
    @Override
    public void save() {
        DatabaseManager.saveCriticalItems(this);
    }
}