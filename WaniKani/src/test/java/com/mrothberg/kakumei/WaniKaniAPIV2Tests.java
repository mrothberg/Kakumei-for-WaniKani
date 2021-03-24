package com.mrothberg.kakumei;

import com.mrothberg.kakumei.apimodels.UserData;
import com.mrothberg.kakumei.apimodels.UserRequest;
import com.mrothberg.kakumei.client.WaniKaniAPIV1Interface;
import com.mrothberg.kakumei.client.WaniKaniApiV2;
import com.mrothberg.kakumei.client.WaniKaniServiceV2BuilderInterface;
import com.mrothberg.kakumei.client.WaniKaniServiceV2;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    Request mockRequest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        mockRequest = new Request.Builder().url("http://example.com").build();

        when(mockServiceBuilder.buildService()).thenReturn(mockAPIService);
        waniKaniAPIV1Interface = new WaniKaniApiV2(mockServiceBuilder);
    }

    @Test
    public void testGetUser() throws ExecutionException, InterruptedException {
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

        CompletableFuture<UserRequest> test = waniKaniAPIV1Interface.getUser();
        verify(data).save();
    }

    @Test
    public void testUserThing() {
        Call<UserRequest> mockCall = mock(Call.class);

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

        CompletableFuture<UserRequest> test = waniKaniAPIV1Interface.getUser();
        Exception exception = assertThrows(ExecutionException.class, test::get);
        assertEquals("java.lang.Throwable: Failure retrieving user information from api",
                              exception.getMessage());
    }

    @Test
    @Ignore
    public void testRetrofitCallFails() {
        //TODO: use retrofit mocks to test call failure
//        Call<UserRequest> mockCall = Calls.response();
    }
}