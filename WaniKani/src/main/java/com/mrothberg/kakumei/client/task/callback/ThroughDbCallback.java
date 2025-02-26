package com.mrothberg.kakumei.client.task.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.mrothberg.kakumei.client.error.RetrofitErrorHandler;
import com.mrothberg.kakumei.wkamodels.Request;
import com.mrothberg.kakumei.wkamodels.Storable;
import com.mrothberg.kakumei.wkamodels.User;

public abstract class ThroughDbCallback<T extends Request<B>, B extends Storable> implements Callback<T> {
    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        T result = response.body();
        if (result == null) return;

        final User userInfo = result.user_information;
        final B requestedInfo = result.requested_information;
        //TODO: read https://developer.android.com/guide/background/threading#java
        if (result.error == null && (userInfo != null || requestedInfo != null)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (userInfo != null) {
                        userInfo.save();
                    }
                    if (requestedInfo != null) {
                        requestedInfo.save();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        RetrofitErrorHandler.handleError(t);
    }
}
