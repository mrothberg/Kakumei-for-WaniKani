package com.mrothberg.kakumei.client;

import com.mrothberg.kakumei.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WaniKaniServiceV2Builder implements WaniKaniServiceV2BuilderInterface {

    private final String API_HOST = "https://api.wanikani.com/v2/";
    private final String apiKey;

    public WaniKaniServiceV2Builder(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public WaniKaniServiceV2 buildService() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            clientBuilder.addInterceptor(httpLoggingInterceptor);
        }

        Interceptor bearerAuthInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                okhttp3.Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + apiKey)
                        .build();
                return chain.proceed(newRequest);
            }
        };
        clientBuilder.addInterceptor(bearerAuthInterceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_HOST)
                .build();

        return retrofit.create(WaniKaniServiceV2.class);
    }
}
