package com.mrothberg.kakumei.wkamodels;

import java.util.ArrayList;

import com.mrothberg.kakumei.database.DatabaseManager;

public class RecentUnlocksList extends ArrayList<UnlockItem> implements Storable {
    @Override
    public void save() {
        DatabaseManager.saveRecentUnlocks(this);
    }
}
