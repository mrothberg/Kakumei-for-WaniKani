package tr.xip.wanikani.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tr.xip.wanikani.BuildConfig;
import tr.xip.wanikani.client.task.callback.ThroughDbCallback;
import tr.xip.wanikani.managers.PrefManager;
import tr.xip.wanikani.wkamodels.CriticalItemsList;
import tr.xip.wanikani.wkamodels.KanjiList;
import tr.xip.wanikani.wkamodels.LevelProgression;
import tr.xip.wanikani.wkamodels.RadicalsList;
import tr.xip.wanikani.wkamodels.RecentUnlocksList;
import tr.xip.wanikani.wkamodels.Request;
import tr.xip.wanikani.wkamodels.SRSDistribution;
import tr.xip.wanikani.wkamodels.StudyQueue;
import tr.xip.wanikani.wkamodels.User;
import tr.xip.wanikani.wkamodels.VocabularyList;

public
class WaniKaniApi implements WaniKaniAPIV1Interface {
    private static final String API_HOST = "https://www.wanikani.com/api/user/";

    private static WaniKaniService service;
    private static String API_KEY;

    static {
        init();
    }

    public static void init() {
        API_KEY = PrefManager.getApiKey();
        setupService();
    }

    private static void setupService() {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(httpLoggingInterceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(API_HOST)
                .build();

        service = retrofit.create(WaniKaniService.class);
    }

//    public static Call<Request<User>> getUser() {
//        return service.getUser(API_KEY);
//    }

    public static Call<Request<User>> getUser(String apiKey) {
        return service.getUser(apiKey);
    }

    @Override
    public CompletableFuture<LevelProgression> getCurrentLevelProgression() {
        CompletableFuture<LevelProgression> future = new CompletableFuture<>();

        service.getLevelProgression(API_KEY).enqueue(new ThroughDbCallback<Request<LevelProgression>, LevelProgression>() {
            @Override
            public void onResponse(Call<Request<LevelProgression>> call, Response<Request<LevelProgression>> response) {
                super.onResponse(call, response);
                if(!response.isSuccessful() || response.body().requested_information == null) {
                    onFailure(call, new Throwable("Bad response from getLevelProgression()"));
                    return;
                }
                LevelProgression levelProgression = response.body().requested_information;
                future.complete(levelProgression);
            }

            @Override
            public void onFailure(Call<Request<LevelProgression>> call, Throwable t) {
                super.onFailure(call, t);
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    public CompletableFuture<SRSDistribution> getSRSDistribution() {
        CompletableFuture<SRSDistribution> srsDistributionFuture = new CompletableFuture<>();
        service.getSRSDistribution(API_KEY).enqueue(new ThroughDbCallback<Request<SRSDistribution>, SRSDistribution>() {
            @Override
            public void onResponse(Call<Request<SRSDistribution>> call, Response<Request<SRSDistribution>> response) {
                super.onResponse(call, response);
                if(!response.isSuccessful() || response.body().requested_information == null) {
                    onFailure(call, new Throwable("getSRSDistribution() failed"));
                    return;
                }

                srsDistributionFuture.complete(response.body().requested_information);
            }

            @Override
            public void onFailure(Call<Request<SRSDistribution>> call, Throwable t) {
                super.onFailure(call, t);
                srsDistributionFuture.completeExceptionally(t);
            }
        });

        return srsDistributionFuture;
    }

    public static Call<Request<RecentUnlocksList>> getRecentUnlocksList(int limit) {
        return service.getRecentUnlocksList(API_KEY, limit);
    }

    public static Call<Request<CriticalItemsList>> getCriticalItemsList(int percentage) {
        return service.getCriticalItemsList(API_KEY, percentage);
    }

    public static Call<Request<RadicalsList>> getRadicalsListOld(String level) {
        return service.getRadicalsList(API_KEY, level);
    }

    public static Call<Request<KanjiList>> getKanjiListOld(String level) {
        return service.getKanjiList(API_KEY, level);
    }

    public static Call<Request<VocabularyList>> getVocabularyListOld(String level) {
        return service.getVocabularyList(API_KEY, level);
    }

    @Override
    public CompletableFuture<KanjiList> getKanjiList(String level) {
        CompletableFuture<KanjiList> kanjiListFuture = new CompletableFuture<>();
        service.getKanjiList(API_KEY, level).enqueue(new ThroughDbCallback<Request<KanjiList>, KanjiList>() {
            @Override
            public void onResponse(Call<Request<KanjiList>> call, Response<Request<KanjiList>> response) {
                super.onResponse(call, response);
                if(!response.isSuccessful() || response.body().requested_information == null) {
                    onFailure(call, new Throwable("getKanjiList() request failed"));
                    return;
                }

                KanjiList kanjiList = response.body().requested_information;
                kanjiListFuture.complete(kanjiList);
            }

            @Override
            public void onFailure(Call<Request<KanjiList>> call, Throwable t) {
                super.onFailure(call, t);
                kanjiListFuture.completeExceptionally(t);
            }
        });
        return kanjiListFuture;
    }

    @Override
    public CompletableFuture<RadicalsList> getRadicalsList(String level) {
        CompletableFuture<RadicalsList> radicalsListFuture = new CompletableFuture<>();
        service.getRadicalsList(API_KEY, level).enqueue(new ThroughDbCallback<Request<RadicalsList>, RadicalsList>() {
            @Override
            public void onResponse(Call<Request<RadicalsList>> call, Response<Request<RadicalsList>> response) {
                super.onResponse(call, response);
                if(!response.isSuccessful() || response.body().requested_information == null) {
                    onFailure(call, new Throwable("getRadicalsList() request failed"));
                    return;
                }
                radicalsListFuture.complete(response.body().requested_information);
            }

            @Override
            public void onFailure(Call<Request<RadicalsList>> call, Throwable t) {
                super.onFailure(call, t);
                radicalsListFuture.completeExceptionally(t);
            }
        });
        return radicalsListFuture;
    }

    @Override
    public CompletableFuture<VocabularyList> getVocabularyList(String level) {
        CompletableFuture<VocabularyList> vocabListFuture = new CompletableFuture<>();
        service.getVocabularyList(API_KEY, level).enqueue(new ThroughDbCallback<Request<VocabularyList>, VocabularyList>() {
            @Override
            public void onResponse(Call<Request<VocabularyList>> call, Response<Request<VocabularyList>> response) {
                super.onResponse(call, response);
                if(!response.isSuccessful() || response.body().requested_information == null) {
                    onFailure(call, new Throwable("getVocabularyList() request failed"));
                    return;
                }
                vocabListFuture.complete(response.body().requested_information);
            }

            @Override
            public void onFailure(Call<Request<VocabularyList>> call, Throwable t) {
                super.onFailure(call, t);
                vocabListFuture.completeExceptionally(t);
            }
        });
        return vocabListFuture;
    }

    @Override
    public CompletableFuture<StudyQueue> getStudyQueue() {
        CompletableFuture<StudyQueue> summaryCompletableFuture = new CompletableFuture<>();

        service.getStudyQueue(API_KEY).enqueue(new ThroughDbCallback<Request<StudyQueue>, StudyQueue>() {
            @Override
            public void onResponse(Call<Request<StudyQueue>> call, Response<Request<StudyQueue>> response) {
                super.onResponse(call, response);
                if(!response.isSuccessful() || response.body().requested_information == null) {
                    onFailure(call, new Throwable("Bad response from getStudyQueue()"));
                    return;
                }

                StudyQueue studyQueue = response.body().requested_information;
                summaryCompletableFuture.complete(studyQueue);
            }

            @Override
            public void onFailure(Call<Request<StudyQueue>> call, Throwable t) {
                super.onFailure(call, t);
                summaryCompletableFuture.completeExceptionally(t);
            }
        });

        return summaryCompletableFuture;
    }
}

