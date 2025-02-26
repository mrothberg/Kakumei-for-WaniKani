package com.mrothberg.kakumei;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.mrothberg.kakumei.apimodels.Assignments;
import com.mrothberg.kakumei.apimodels.ReviewStatistics;
import com.mrothberg.kakumei.apimodels.Subjects;
import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.apimodels.UserRequest;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.client.WaniKaniApiV2;
import com.mrothberg.kakumei.client.WaniKaniServiceV2BuilderInterface;
import com.mrothberg.kakumei.client.WaniKaniServiceV2;
import com.mrothberg.kakumei.database.DatabaseHelper;
import com.mrothberg.kakumei.database.DatabaseManager;
import com.mrothberg.kakumei.wkamodels.KanjiList;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import java9.util.concurrent.CompletableFuture;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WaniKaniAPIV2Tests {

    private WaniKaniAPIV1Interface waniKaniAPIV1Interface;

    @Mock
    WaniKaniServiceV2 mockAPIService;

    @Mock
    WaniKaniServiceV2BuilderInterface mockServiceBuilder;

    @Mock
    Context mockContext;

    @Mock
    DatabaseHelper mockDatabaseHelper;

    @Mock
    SQLiteDatabase mockDatabase;

    Request mockRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockRequest = new Request.Builder().url("http://example.com").build();

        setupDatabase();
        when(mockServiceBuilder.buildService()).thenReturn(mockAPIService);
        waniKaniAPIV1Interface = new WaniKaniApiV2(mockServiceBuilder);
    }

    private void setupDatabase() {
        DatabaseHelper.setInstance(mockDatabaseHelper);
        when(mockDatabaseHelper.getWritableDatabase()).thenReturn(mockDatabase);
        DatabaseManager.init(mockContext);
    }

    @Test
    public void getUser_savesUserDataToDatabase() throws ExecutionException, InterruptedException {
        Call<UserRequest> mockCall = mock(Call.class);
        UserRequest res = mock(UserRequest.class);
        UserData data = mock(UserData.class);
        res.data = data;

        when(mockAPIService.getUser()).thenReturn(mockCall);

        doAnswer(invocation -> {
            Callback<UserRequest> callback = invocation.getArgument(0);

            callback.onResponse(mockCall, Response.success(res));

            return null;
        }).when(mockCall).enqueue(any(Callback.class));

        CompletableFuture<UserRequest> fut = waniKaniAPIV1Interface.getUser();
        verify(data).save();
    }

    @Test
    public void getUserRequestFails_completesExceptionally() {
        Call<UserRequest> mockCall = mock(Call.class);

        //TODO: This is required because the Response that gets passed into callback.onResponse()
        //      cannot be mocked bc retrofit2.Response is a final class - try Mockito exentions
        //      that allows mocking final classes
        ResponseBody mockResponseBody = ResponseBody.create("", MediaType.parse("application/json"));
        okhttp3.Response.Builder builder = new okhttp3.Response.Builder();
        okhttp3.Response mockResponse = builder
                .code(401)
                .request(mockRequest)
                .protocol(Protocol.HTTP_2)
                .message("")
                .body(mockResponseBody).build();

        when(mockAPIService.getUser()).thenReturn(mockCall);
        doAnswer(invocation -> {
            Callback<UserRequest> callback = invocation.getArgument(0);
            callback.onResponse(mockCall, Response.error(mockResponseBody, mockResponse));
            return null;
        }).when(mockCall).enqueue(any(Callback.class));

        CompletableFuture<UserRequest> fut = waniKaniAPIV1Interface.getUser();
        Exception exception = assertThrows(ExecutionException.class, fut::get);
        assertEquals("java.lang.Throwable: Failure retrieving user information from api",
                              exception.getMessage());
    }

    @Test
    public void testGetKanjiListWithMocks() throws ExecutionException, InterruptedException {
        String level = "1";
        Call<Subjects> mockSubjectsCall = mock(Call.class);
        Subjects mockSubjects = mock(Subjects.class);
        Subjects.SubjectItem mockSubjectItem = mock(Subjects.SubjectItem.class);
        mockSubjects.data = new ArrayList<>();
        mockSubjects.data.add(mockSubjectItem);
        when(mockAPIService.getSubjectItems(level, "kanji")).thenReturn(mockSubjectsCall);
        doAnswer(invocation -> {
            Callback<Subjects> callback = invocation.getArgument(0);
            callback.onResponse(mockSubjectsCall, Response.success(mockSubjects));
            return null;
        }).when(mockSubjectsCall).enqueue(any(Callback.class));
        CompletableFuture<KanjiList> fut = waniKaniAPIV1Interface.getKanjiList(level);
        //TODO: Verifications
    }

    @Test
    public void testGetKanjiListRealData() throws ExecutionException, InterruptedException {
        String level = "1";
        Gson gson = new Gson();
        Subjects subjects = gson.fromJson(JsonResponses.level1KanjiSubjects, Subjects.class);

        Call<Subjects> mockSubjectsCall = mock(Call.class);
        when(mockAPIService.getSubjectItems(level, "kanji")).thenReturn(mockSubjectsCall);
        doAnswer(invocation -> {
            Callback<Subjects> callback = invocation.getArgument(0);
            callback.onResponse(mockSubjectsCall, Response.success(subjects));
            return null;
        }).when(mockSubjectsCall).enqueue(any(Callback.class));

        Assignments assignments = gson.fromJson(JsonResponses.assignments, Assignments.class);
        Call<Assignments> mockAssignmentsCall = mock(Call.class);
        when(mockAPIService.getAssignments(level, "kanji")).thenReturn(mockAssignmentsCall);
        doAnswer(invocation -> {
            Callback<Assignments> callback = invocation.getArgument(0);
            callback.onResponse(mockAssignmentsCall, Response.success(assignments));
            return null;
        }).when(mockAssignmentsCall).enqueue(any(Callback.class));

        ReviewStatistics reviewStatistics = gson.fromJson(JsonResponses.reviewStatistics, ReviewStatistics.class);
        Call<ReviewStatistics> mockReviewStatsCall = mock(Call.class);
        when(mockAPIService.getReviewStatistics(any(String.class))).thenReturn(mockReviewStatsCall);
        doAnswer(invocation -> {
            Callback<ReviewStatistics> callback = invocation.getArgument(0);
            callback.onResponse(mockReviewStatsCall, Response.success(reviewStatistics));
            return null;
        }).when(mockReviewStatsCall).enqueue(any(Callback.class));

        CompletableFuture<KanjiList> fut = waniKaniAPIV1Interface.getKanjiList(level);
        KanjiList kanjiList = fut.get();
        //TODO: Verifications
    }

    @Test
    @Ignore
    public void testRetrofitCallFails() {
        //TODO: Use retrofit mocks library to test call failure scenarios
//        Call<UserRequest> mockCall = Calls.response();
    }
}