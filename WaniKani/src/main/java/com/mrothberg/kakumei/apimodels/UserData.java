package com.mrothberg.kakumei.apimodels;

import java.io.Serializable;

import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.wkamodels.Storable;

public class UserData implements Serializable, Storable {

    public String username;
    public int level;
    public String started_at;
    public SubscriptionInformation subscription;

    public UserData(String username, int level, String started_at) {
        this.username = username;
        this.level = level;
        this.started_at = started_at;
    }

    public class SubscriptionInformation {
        public String active;
        public String type;
        public int max_level_granted;
    }

    @Override
    public void save() {
        DatabaseManager.saveUserV2(this);
    }
}
