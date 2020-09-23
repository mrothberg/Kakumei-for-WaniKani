package com.mrothberg.kakumei.client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import com.mrothberg.kakumei.wkamodels.CriticalItemsList;
import com.mrothberg.kakumei.wkamodels.KanjiList;
import com.mrothberg.kakumei.wkamodels.LevelProgression;
import com.mrothberg.kakumei.wkamodels.RadicalsList;
import com.mrothberg.kakumei.wkamodels.RecentUnlocksList;
import com.mrothberg.kakumei.wkamodels.Request;
import com.mrothberg.kakumei.wkamodels.SRSDistribution;
import com.mrothberg.kakumei.wkamodels.StudyQueue;
import com.mrothberg.kakumei.wkamodels.User;
import com.mrothberg.kakumei.wkamodels.VocabularyList;

public interface WaniKaniService {

    @GET("{api_key}/user-information")
    Call<Request<User>> getUser(@Path("api_key") String api_key);

    @GET("{api_key}/study-queue")
    Call<Request<StudyQueue>> getStudyQueue(@Path("api_key") String api_key);

    @GET("{api_key}/level-progression")
    Call<Request<LevelProgression>> getLevelProgression(@Path("api_key") String api_key);

    @GET("{api_key}/srs-distribution")
    Call<Request<SRSDistribution>> getSRSDistribution(@Path("api_key") String api_key);

    @GET("{api_key}/recent-unlocks/{limit}")
    Call<Request<RecentUnlocksList>> getRecentUnlocksList(@Path("api_key") String api_key, @Path("limit") int limit);

    @GET("{api_key}/critical-items/{percentage}")
    Call<Request<CriticalItemsList>> getCriticalItemsList(@Path("api_key") String api_key, @Path("percentage") int percentage);

    @GET("{api_key}/radicals/{level}")
    Call<Request<RadicalsList>> getRadicalsList(@Path("api_key") String api_key, @Path("level") String level); // We use a string to handle the level argument as the API accepts comma-delimited level argument

    @GET("{api_key}/kanji/{level}")
    Call<Request<KanjiList>> getKanjiList(@Path("api_key") String api_key, @Path("level") String level); // We use a string to handle the level argument as the API accepts comma-delimited level argument

    @GET("{api_key}/vocabulary/{level}")
    Call<Request<VocabularyList>> getVocabularyList(@Path("api_key") String api_key, @Path("level") String level); // We use a string to handle the level argument as the API accepts comma-delimited level argument

}
