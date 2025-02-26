package com.mrothberg.kakumei.wkamodels;

import java.util.ArrayList;

import com.mrothberg.kakumei.database.DatabaseManager;

public class CriticalItemsList extends ArrayList<CriticalItem> implements Storable {
    @Override
    public void save() {
        DatabaseManager.saveCriticalItems(this);
    }
}