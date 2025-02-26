package com.mrothberg.kakumei.client;

import android.util.Log;
import android.util.Pair;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import java9.util.concurrent.CompletableFuture;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.mrothberg.kakumei.apimodels.Assignments.Assignment;
import com.mrothberg.kakumei.apimodels.Assignments.Assignment.AssignmentData;
import com.mrothberg.kakumei.apimodels.ReviewStatistics.ReviewStatistic;
import com.mrothberg.kakumei.apimodels.ReviewStatistics.ReviewStatistic.ReviewStatisticData;
import com.mrothberg.kakumei.apimodels.Subjects.SubjectItem;
import com.mrothberg.kakumei.apimodels.Subjects.SubjectItem.SubjectItemData;
import com.mrothberg.kakumei.apimodels.Subjects.SubjectItem.SubjectItemData.CharacterImage;
import com.mrothberg.kakumei.apimodels.SummaryRequest.SummaryData;
import com.mrothberg.kakumei.apimodels.SummaryRequest.SummaryData.Reviews;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.wkamodels.BaseItem;
import com.mrothberg.kakumei.wkamodels.KanjiList;
import com.mrothberg.kakumei.wkamodels.LevelProgression;
import com.mrothberg.kakumei.wkamodels.RadicalsList;
import com.mrothberg.kakumei.wkamodels.SRSDistribution;
import com.mrothberg.kakumei.wkamodels.StudyQueue;
import com.mrothberg.kakumei.wkamodels.VocabularyList;
import com.mrothberg.kakumei.apimodels.Assignments;
import com.mrothberg.kakumei.apimodels.ReviewStatistics;
import com.mrothberg.kakumei.apimodels.Subjects;
import com.mrothberg.kakumei.apimodels.SummaryRequest;
import com.mrothberg.kakumei.apimodels.UserRequest;

public class WaniKaniApiV2 implements WaniKaniAPIV1Interface {

    private static final String TAG = "WaniKaniApiV2";

    private static WaniKaniServiceV2 service;

    private static final DateFormat iso8601Parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);

    private static final String RADICAL = "radical";
    private static final String KANJI = "kanji";
    private static final String VOCABULARY = "vocabulary";

    public WaniKaniApiV2(WaniKaniServiceV2BuilderInterface serviceBuilder) {
        iso8601Parser.setTimeZone(TimeZone.getTimeZone("UTC"));
        service = serviceBuilder.buildService();
    }

    public CompletableFuture<UserRequest> getUser() {
        CompletableFuture<UserRequest> future = new CompletableFuture<>();
        service.getUser().enqueue(new Callback<UserRequest>() {
            @Override
            public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    onFailure(call, new Throwable("Failure retrieving user information from api"));
                    return;
                }
                response.body().data.save();
                future.complete(response.body());
            }

            @Override
            public void onFailure(Call<UserRequest> call, Throwable t) {
                Log.e(TAG, "Failed getting user data", t);
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<KanjiList> getKanjiList(String level) {
        return getSubjectItems(level, KANJI)
                .thenCompose(subjects -> getAssignments(level, KANJI)
                        .thenCombine(getReviewStatistics(level, getSubjectIDList(subjects)), (assignments, reviewStatistics) -> {
                            KanjiList kanjiList = new KanjiList();
                            kanjiList.addAll(getItemList(subjects, assignments, reviewStatistics));
                            kanjiList.save();
                            return kanjiList;
                        }));
    }

    private String getSubjectIDList(Subjects subjects) {
        return subjects.data.stream()
                .map(subjectItem -> String.valueOf(subjectItem.id))
                .collect(Collectors.joining(","));
    }

    @Override
    public CompletableFuture<RadicalsList> getRadicalsList(String level) {
        return getSubjectItems(level, RADICAL)
                .thenCompose(subjects -> getAssignments(level, RADICAL)
                    .thenCombine(getReviewStatistics(level, getSubjectIDList(subjects)), (assignments, reviewStatistics) -> {
                        RadicalsList radicalsList = new RadicalsList();
                        radicalsList.addAll(getItemList(subjects, assignments, reviewStatistics));
                        radicalsList.save();
                        return radicalsList;
                    }));
    }

    @Override
    public CompletableFuture<VocabularyList> getVocabularyList(String level) {
        return getSubjectItems(level, VOCABULARY)
                .thenCompose(subjects -> getAssignments(level, VOCABULARY)
                        .thenCombine(getReviewStatistics(level, getSubjectIDList(subjects)), (assignments, reviewStatistics) -> {
                            VocabularyList vocabularyList = new VocabularyList();
                            vocabularyList.addAll(getItemList(subjects, assignments, reviewStatistics));
                            vocabularyList.save();
                            return vocabularyList;
                        }));
    }

    private List<BaseItem> getItemList(Subjects subjects, Assignments assignments, ReviewStatistics reviewStatistics) {
        List<SubjectItem> subjectItems = subjects.data;
        Map<Integer, AssignmentData> subjectToAssignmentDataMap = new HashMap<>();
        Map<Integer, ReviewStatisticData> subjectToReviewStatsMap = new HashMap<>();

        for (Assignment assignment : assignments.data) {
            subjectToAssignmentDataMap.put(assignment.data.subject_id, assignment.data);
        }

        for (ReviewStatistic reviewStatData : reviewStatistics.data) {
            //TODO: account for getting no review statistics (null ref)
            subjectToReviewStatsMap.put(reviewStatData.data.subject_id, reviewStatData.data);
        }

        List<BaseItem> baseItemsList = new ArrayList<>();
        for(SubjectItem subjectItem : subjectItems) {
            SubjectItemData subjectItemData = subjectItem.data;
            AssignmentData assignmentData = subjectToAssignmentDataMap.get(subjectItem.id);
            ReviewStatisticData reviewStatData = subjectToReviewStatsMap.get(subjectItem.id);

            String character = subjectItemData.characters;
            String onyomi = getKanjiReadings(subjectItem, "onyomi");
            String kunyomi = getKanjiReadings(subjectItem, "kunyomi");
            String meaning = getPrimaryMeaning(subjectItem);
            String importantReading = getPrimaryReadingType(subjectItem);
            String image = getOriginalImageUrl(subjectItem);
            String kana = getPrimaryReading(subjectItem);
            int itemLevel = subjectItem.data.level;
            boolean burned = false;
            long burnedDate = 0;
            long nextAvailableDate = 0;
            String srs = "";
            long unlockDate = 0;
            if (assignmentData != null) {
                burned = assignmentData.burned_at != null;
                srs = getSrsStageName(assignmentData.srs_stage);
                try {
                    unlockDate = iso8601Parser.parse(assignmentData.unlocked_at).getTime() / 1000;
                    if (assignmentData.available_at != null) {
                        nextAvailableDate = iso8601Parser.parse(assignmentData.available_at).getTime() / 1000;
                    }
                    if (assignmentData.burned_at != null) {
                        burned = true;
                        burnedDate = iso8601Parser.parse(assignmentData.burned_at).getTime() / 1000;
                    } else burned = false;
                } catch (ParseException e) {
                    Log.e(TAG, "Error parsing unlock date", e);
                }
            }

            int meaning_correct = 0;
            int meaning_incorrect = 0;
            int meaning_max_streak = 0;
            int meaning_current_streak = 0;
            int reading_correct = 0;
            int reading_incorrect = 0;
            int reading_max_streak = 0;
            int reading_current_streak = 0;
            if(reviewStatData != null) {
                meaning_correct = reviewStatData.meaning_correct;
                meaning_incorrect = reviewStatData.meaning_incorrect;
                meaning_max_streak = reviewStatData.meaning_max_streak;
                meaning_current_streak = reviewStatData.meaning_current_streak;
                reading_correct = reviewStatData.reading_correct;
                reading_incorrect = reviewStatData.reading_incorrect;
                reading_max_streak = reviewStatData.reading_max_streak;
                reading_current_streak = reviewStatData.reading_current_streak;
            }
            BaseItem baseItem = new BaseItem(0, character, kana, meaning, image, onyomi,
                    kunyomi, importantReading, itemLevel, "", srs,
                    unlockDate, nextAvailableDate, burned, burnedDate,
                    meaning_correct, meaning_incorrect, meaning_max_streak,
                    meaning_current_streak, reading_correct, reading_incorrect,
                    reading_max_streak, reading_current_streak, null,
                    null, null);
            baseItemsList.add(baseItem);
        }
        return baseItemsList;
    }

    private String getOriginalImageUrl(Subjects.SubjectItem subjectItem) {
        if (subjectItem.data.character_images == null) return null;
        for (CharacterImage ci : subjectItem.data.character_images) {
            if (ci.metadata != null && ci.metadata.style_name != null && ci.metadata.style_name.equals("original")) {
                return ci.url;
            }
        }
        return null;
    }

    private String getPrimaryReadingType(SubjectItem subjectItem) {
        if (subjectItem.data.readings == null) return "";
        return subjectItem.data.readings.stream()
                .filter(readings -> readings.primary)
                .map(readings -> readings.type)
                .collect(Collectors.joining());
    }

    private String getPrimaryReading(SubjectItem subjectItem) {
        if (subjectItem.data.readings == null) return "";
        return subjectItem.data.readings.stream()
                .filter(readings -> readings.primary)
                .map(readings -> readings.reading)
                .collect(Collectors.joining());
    }

    private String getPrimaryMeaning(SubjectItem subjectItem) {
        if (subjectItem.data.meanings == null) return "";
        return subjectItem.data.meanings.stream()
                .filter(meanings -> meanings.primary)
                .map(meanings -> meanings.meaning)
                .collect(Collectors.joining());
    }

    private String getKanjiReadings(SubjectItem subjectItem, String reading_type) {
        if (subjectItem.data.readings == null) return "";
        return subjectItem.data.readings.stream()
                .filter(readings -> readings.type != null && readings.type.equals(reading_type))
                .map(readings -> readings.reading)
                .collect(Collectors.joining(","));
    }

    private CompletableFuture<Subjects> getSubjectItems(String level, String type) {
        CompletableFuture<Subjects> future = new CompletableFuture<>();
        service.getSubjectItems(level, type).enqueue(new Callback<Subjects>() {
            @Override
            public void onResponse(Call<Subjects> call, Response<Subjects> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    onFailure(call, new Throwable("Failure retrieving subjects"));
                    return;
                }
                future.complete(response.body());
            }

            @Override
            public void onFailure(Call<Subjects> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private CompletableFuture<Assignments> getAssignments(String level, String type) {
        CompletableFuture<Assignments> future = new CompletableFuture<>();
        service.getAssignments(level, type).enqueue(new Callback<Assignments>() {
            @Override
            public void onResponse(Call<Assignments> call, Response<Assignments> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    onFailure(call, new Throwable("Failure retrieving assignments"));
                    return;
                }
                future.complete(response.body());
            }

            @Override
            public void onFailure(Call<Assignments> call, Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    private CompletableFuture<ReviewStatistics> getReviewStatistics(String level, String subjectIDList) {
        CompletableFuture<ReviewStatistics> future = new CompletableFuture<>();
        service.getReviewStatistics(subjectIDList).enqueue(new Callback<ReviewStatistics>() {
            @Override
            public void onResponse(Call<ReviewStatistics> call, Response<ReviewStatistics> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    onFailure(call, new Throwable("Failure retrieving review statistics"));
                    return;
                }
                future.complete(response.body());
            }

            @Override
            public void onFailure(Call<ReviewStatistics> call, Throwable t) {
                Log.e(TAG, "getReviewStatistics()", t);
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    @Override
    public CompletableFuture<StudyQueue> getStudyQueue() {
        CompletableFuture<StudyQueue> summaryCompletableFuture = new CompletableFuture<>();
        service.getSummary().enqueue(new Callback<SummaryRequest>() {
            @Override
            public void onResponse(Call<SummaryRequest> call, Response<SummaryRequest> response) {
                //TODO check api response status
                // if (response.isSuccessful() && response.body().user_information != null && response.body().requested_information != null) {
                try {
                    SummaryRequest summaryRequest = response.body();
                    SummaryData data = summaryRequest.data;
                    List<Reviews> reviews = data.reviews;

                    int lessonsAvailableNowCount = data.lessons.get(0).subject_ids.size();
                    int reviewsAvailableNowCount = reviews.get(0).subject_ids.size();
                    int reviewsAvailableNextHourCount = reviews.get(1).subject_ids.size();
                    int reviewsAvailableNextDayCount = reviews.stream().skip(2).map(review -> review.subject_ids.size()).reduce(0, Integer::sum);

                    long date = 0;
                    if (data.next_reviews_at != null) {
                        date = iso8601Parser.parse(data.next_reviews_at).getTime() / 1000;
                    }

                    StudyQueue studyQueue = new StudyQueue(0,
                            lessonsAvailableNowCount,
                            reviewsAvailableNowCount,
                            reviewsAvailableNextHourCount,
                            reviewsAvailableNextDayCount,
                            date);
                    //TODO handle persisting to database elsewhere
                    DatabaseManager.saveStudyQueue(studyQueue);
                    summaryCompletableFuture.complete(studyQueue);
                } catch (ParseException e) {
                    onFailure(call, e);
                    Log.e(TAG, "Parse exception", e);
                }
            }

            @Override
            public void onFailure(Call<SummaryRequest> call, Throwable t) {
                summaryCompletableFuture.completeExceptionally(t);
            }
        });
        return summaryCompletableFuture;
    }

    @Override
    public CompletableFuture<SRSDistribution> getSRSDistribution() {
        CompletableFuture<Assignments> assignmentsFuture = new CompletableFuture<>();
        service.getAssignments().enqueue(new Callback<Assignments>() {
            @Override
            public void onResponse(Call<Assignments> call, Response<Assignments> response) {
                if (!response.isSuccessful() || response.body().data == null) {
                    onFailure(call, new Throwable("Failed to get assignments"));
                    return;
                }
                Assignments assignments = response.body();
                assignmentsFuture.complete(assignments);
            }

            @Override
            public void onFailure(Call<Assignments> call, Throwable t) {
                assignmentsFuture.completeExceptionally(t);
            }
        });

        return assignmentsFuture.thenCompose(assignments -> getCompleteAssignmentList(assignments, new ArrayList<>()))
                .thenApply(this::createSRSDistribution);
    }

    private SRSDistribution createSRSDistribution(List<Assignments.Assignment> totalAssignmentList) {
        int apprenticeRadicals = 0;
        int apprenticeKanji = 0;
        int apprenticeVocabulary = 0;
        int guruRadicals = 0;
        int guruKanji = 0;
        int guruVocabulary = 0;
        int masterRadicals = 0;
        int masterKanji = 0;
        int masterVocabulary = 0;
        int enlightenedRadicals = 0;
        int enlightenedKanji = 0;
        int enlightenedVocabulary = 0;
        int burnedRadicals = 0;
        int burnedKanji = 0;
        int burnedVocabulary = 0;

        for (Assignment assignment : totalAssignmentList) {
            AssignmentData currentAssignment = assignment.data;

            //TODO: WaniKani is getting rid of srs stage names so reevaluate this
            //TODO Think of a cleaner way to do this
            int srsStage = currentAssignment.srs_stage;
            if (srsStage >= 1 && srsStage <= 4) {
                //apprentice
                switch (currentAssignment.subject_type) {
                    case "radical":
                        apprenticeRadicals++;
                        break;
                    case "kanji":
                        apprenticeKanji++;
                        break;
                    case "vocabulary":
                        apprenticeVocabulary++;
                        break;
                }
            } else if (srsStage >= 5 && srsStage <= 6) {
                // guru
                switch (currentAssignment.subject_type) {
                    case "radical":
                        guruRadicals++;
                        break;
                    case "kanji":
                        guruKanji++;
                        break;
                    case "vocabulary":
                        guruVocabulary++;
                        break;
                }
            } else if (srsStage == 7) {
                //master
                switch (currentAssignment.subject_type) {
                    case "radical":
                        masterRadicals++;
                        break;
                    case "kanji":
                        masterKanji++;
                        break;
                    case "vocabulary":
                        masterVocabulary++;
                        break;
                }
            } else if (srsStage == 8) {
                //enlightened
                switch (currentAssignment.subject_type) {
                    case "radical":
                        enlightenedRadicals++;
                        break;
                    case "kanji":
                        enlightenedKanji++;
                        break;
                    case "vocabulary":
                        enlightenedVocabulary++;
                        break;
                }
            } else if (srsStage == 9) {
                //burned
                switch (currentAssignment.subject_type) {
                    case "radical":
                        burnedRadicals++;
                        break;
                    case "kanji":
                        burnedKanji++;
                        break;
                    case "vocabulary":
                        burnedVocabulary++;
                        break;
                }
            }
        }

        return new SRSDistribution(0,
                apprenticeRadicals,
                apprenticeKanji,
                apprenticeVocabulary,
                guruRadicals,
                guruKanji,
                guruVocabulary,
                masterRadicals,
                masterKanji,
                masterVocabulary,
                enlightenedRadicals,
                enlightenedKanji,
                enlightenedVocabulary,
                burnedRadicals,
                burnedKanji,
                burnedVocabulary);
    }

    //TODO: Define these as constants somewhere - they're currently used in many diff places
    private String getSrsStageName(int srsStage) {
        if (srsStage >= 1 && srsStage <= 4) {
            return "apprentice";
        } else if (srsStage >= 5 && srsStage <= 6) {
            return "guru";
        } else if (srsStage == 7) {
            return "master";
        } else if (srsStage == 8) {
            return "enlighten";
        } else if (srsStage == 9) {
            return "burned";
        }
        return "";
    }

    private CompletableFuture<List<Assignment>> getCompleteAssignmentList(Assignments assignments, List<Assignment> partialResult) {
        List<Assignment> partialResultCopy = new ArrayList<>(partialResult); // defensive copy
        partialResultCopy.addAll(assignments.data);

        final String url = assignments.pages.next_url;
        if (url == null) {
            return CompletableFuture.completedFuture(partialResultCopy);
        }

        //TODO Reevaluate this parsing logic
        int nextPageStartId = Integer.parseInt(url.substring(url.indexOf("=") + 1));
        CompletableFuture<Assignments> nextAssignmentsPage = new CompletableFuture<>();
        service.getNextAssignmentsPage(nextPageStartId).enqueue(new Callback<Assignments>() {
            @Override
            public void onResponse(Call<Assignments> call, Response<Assignments> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    onFailure(call, new Throwable("Failed to get next assignment page"));
                    return;
                }
                nextAssignmentsPage.complete(response.body());
            }

            @Override
            public void onFailure(Call<Assignments> call, Throwable t) {
                nextAssignmentsPage.completeExceptionally(t);
            }
        });
        return nextAssignmentsPage.thenCompose(nextAssignmentPage -> getCompleteAssignmentList(nextAssignmentPage, partialResultCopy));
    }

    @Override
    public CompletableFuture<LevelProgression> getCurrentLevelProgression() {
        CompletableFuture<Integer> userLevelFuture = getCurrentUserLevel();
        Pair<CompletableFuture<Pair<Integer, Integer>>, CompletableFuture<Pair<Integer, Integer>>> progressFuturePair = getProgressCardFutures(userLevelFuture);

        return progressFuturePair.first.thenCombine(progressFuturePair.second, (progressPair, totalPair) -> {
            int totalRadicals = totalPair.first;
            int totalKanjis = totalPair.second;
            int passedRadicals = progressPair.first;
            int passedKanjis = progressPair.second;

            return new LevelProgression(0, passedRadicals, totalRadicals, passedKanjis, totalKanjis);
        });
    }

    private CompletableFuture<Integer> getCurrentUserLevel() {
        CompletableFuture<Integer> userLevelFuture = new CompletableFuture<>();
        service.getUser().enqueue(new Callback<UserRequest>() {
            @Override
            public void onResponse(Call<UserRequest> call, Response<UserRequest> response) {
                if (response.body() == null || response.body().data == null) {
                    onFailure(call, new Throwable("User Request failed"));
                    return;
                }
                userLevelFuture.complete(response.body().data.level);
            }

            @Override
            public void onFailure(Call<UserRequest> call, Throwable t) {
                userLevelFuture.completeExceptionally(t);
            }
        });
        return userLevelFuture;
    }

    private Pair<CompletableFuture<Pair<Integer, Integer>>, CompletableFuture<Pair<Integer, Integer>>> getProgressCardFutures(CompletableFuture<Integer> userLevelFuture) {
        CompletableFuture<Pair<Integer, Integer>> radicalKanjiTotalFuture = new CompletableFuture<>();
        CompletableFuture<Pair<Integer, Integer>> radicalKanjiProgressFuture = new CompletableFuture<>();
        userLevelFuture.thenAccept(level -> {
            service.getRadicalAndKanjiSubjects(level).enqueue(new RadicalKanjiSubjectsFutureCompleter(radicalKanjiTotalFuture));
            service.getRadicalAndKanjiAssignments(level).enqueue(new RadicalKanjiAssignmentsFutureCompleter(radicalKanjiProgressFuture));
        });
        return new Pair<>(radicalKanjiProgressFuture, radicalKanjiTotalFuture);
    }

    private static class RadicalKanjiSubjectsFutureCompleter implements Callback<Subjects> {
        private final CompletableFuture<Pair<Integer, Integer>> radicalKanjiTotalFuture;

        public RadicalKanjiSubjectsFutureCompleter(CompletableFuture<Pair<Integer, Integer>> radicalKanjiTotalFuture) {
            this.radicalKanjiTotalFuture = radicalKanjiTotalFuture;
        }

        @Override
        public void onResponse(Call<Subjects> call, Response<Subjects> response) {
            if (response.body() == null || response.body().data == null) {
                onFailure(call, new Throwable("getRadicalAndKanjiSubjects() call failed"));
                return;
            }
            completeRadicalKanjiTotalFuture(call, response);
        }

        private void completeRadicalKanjiTotalFuture(Call<Subjects> call, Response<Subjects> response) {
            List<SubjectItem> subjects = response.body().data;
            radicalKanjiTotalFuture.complete(subjects.stream().reduce(
                    new Pair<>(0, 0),
                    (acc, curr) -> {
                        if (curr.object.equals("radical")) {
                            return new Pair<>(acc.first + 1, acc.second);
                        } else if (curr.object.equals("kanji")) {
                            return new Pair<>(acc.first, acc.second + 1);
                        } else return new Pair<>(acc.first, acc.second);
                    },
                    (pair1, pair2) -> new Pair<>(pair1.first + pair2.first, pair1.second + pair2.second)));
        }

        @Override
        public void onFailure(Call<Subjects> call, Throwable t) {
            Log.e(TAG, "subjectCallObservable onfailure()", t);
            radicalKanjiTotalFuture.completeExceptionally(t);
        }
    }

    private static class RadicalKanjiAssignmentsFutureCompleter implements Callback<Assignments> {
        private final CompletableFuture<Pair<Integer, Integer>> radicalKanjiProgressFuture;

        public RadicalKanjiAssignmentsFutureCompleter(CompletableFuture<Pair<Integer, Integer>> radicalKanjiProgressFuture) {
            this.radicalKanjiProgressFuture = radicalKanjiProgressFuture;
        }

        @Override
        public void onResponse(Call<Assignments> call, Response<Assignments> response) {
            if (response.body() == null || response.body().data == null) {
                onFailure(call, new Throwable("getRadicalAndKanjiAssignments() call failed"));
                return;
            }
            completeRadicalKanjiProgressFuture(response);
        }

        private void completeRadicalKanjiProgressFuture(Response<Assignments> response) {
            List<Assignment> assignments = response.body().data;
            radicalKanjiProgressFuture.complete(assignments.stream().reduce(
                    new Pair<>(0, 0),
                    (acc, curr) -> {
                        if (curr.data.passed_at != null) {
                            if (curr.data.subject_type.equals("radical")) {
                                return new Pair<>(acc.first + 1, acc.second);
                            }
                            if (curr.data.subject_type.equals("kanji")) {
                                return new Pair<>(acc.first, acc.second + 1);
                            }
                        }
                        return new Pair<>(acc.first, acc.second);
                    },
                    (pair1, pair2) -> new Pair<>(pair1.first + pair2.first, pair1.second + pair2.second)));
        }

        @Override
        public void onFailure(Call<Assignments> call, Throwable t) {
            Log.e(TAG, "assignmentCall onfailure()", t);
            radicalKanjiProgressFuture.completeExceptionally(t);
        }
    }
}
