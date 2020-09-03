package tr.xip.wanikani.client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import tr.xip.wanikani.apimodels.Assignments;
import tr.xip.wanikani.apimodels.ReviewStatistics;
import tr.xip.wanikani.apimodels.Subjects;
import tr.xip.wanikani.apimodels.SummaryRequest;
import tr.xip.wanikani.apimodels.UserRequest;

public interface WaniKaniServiceV2 {

    @GET("user")
    Call<UserRequest> getUser();

    @GET("summary")
    Call<SummaryRequest> getSummary();

    //TODO Can just use this method and pass in subject types list
    @GET("subjects")
    Call<Subjects> getSubjectItems(@Query("levels") String level, @Query("types") String types); // We use a string to handle the level argument as the API accepts comma-delimited level argument);

    @GET("subjects?types=radical,kanji")
    Call<Subjects> getRadicalAndKanjiSubjects(@Query("levels") int level);

    @GET("assignments?subject_types=radical,kanji")
    Call<Assignments> getRadicalAndKanjiAssignments(@Query("levels") int level);

    //TODO figure out optional query parameters
    @GET("assignments")
    Call<Assignments> getAssignments();

    @GET("assignments")
    Call<Assignments> getAssignments(@Query("levels") String levels, @Query("subject_types") String types);

    @GET("assignments")
    Call<Assignments> getNextAssignmentsPage(@Query("page_after_id") int id);

    @GET("review_statistics")
    Call<ReviewStatistics> getReviewStatistics(@Query("subject_ids") String subjectIDs);
}
