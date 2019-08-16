package com.my.project.http.client;

import com.my.project.http.client.stub.ReceiveFileStub.Response;
import com.my.project.http.client.util.JsonUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HttpUploadClientTest extends BaseTest {

    private HttpUploadClient client = new HttpUploadClient(5000, null, null);

    /**
     * Exception
     */
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void testNullHttp() throws IOException {
        exception.expect(NullPointerException.class);
        response(client.upload(uploadUrlByHttp(), null, null));
    }

    @Test
    public void testNullHttps() throws IOException {
        exception.expect(NullPointerException.class);
        response(client.upload(uploadUrlByHttps(), null, null));
    }

    @Test
    public void testEmptyHttp() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("status code: 400, reason phrase: Bad Request");
        response(client.upload(uploadUrlByHttp(), Collections.emptyMap(), Collections.emptyMap()));
    }

    @Test
    public void testEmptyHttps() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("status code: 400, reason phrase: Bad Request");
        response(client.upload(uploadUrlByHttps(), Collections.emptyMap(), Collections.emptyMap()));
    }

    @Test
    public void testEmptyBodyHttp() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("status code: 400, reason phrase: Bad Request");
        response(client.upload(uploadUrlByHttp(), headers(), Collections.emptyMap()));
    }

    @Test
    public void testEmptyBodyHttps() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("status code: 400, reason phrase: Bad Request");
        response(client.upload(uploadUrlByHttps(), headers(), Collections.emptyMap()));
    }

    @Test
    public void testEmptyHeaderHttp() throws IOException {
        Response result = response(client.upload(uploadUrlByHttp(), Collections.emptyMap(), requestParam()));
        assertEquals("upload success", result.getErrorMessage());
        assertEquals("uploading", result.getStatus());
        assertEquals(new Integer(123456), result.getUploadSequenceId());
    }

    @Test
    public void testEmptyHeaderHttps() throws IOException {
        Response result = response(client.upload(uploadUrlByHttps(), Collections.emptyMap(), requestParam()));
        assertEquals("upload success", result.getErrorMessage());
        assertEquals("uploading", result.getStatus());
        assertEquals(new Integer(123456), result.getUploadSequenceId());
    }

    @Test
    public void testNoFileHttp() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("status code: 500, reason phrase: Request failed");
        Map<String, Object> request = requestParam();
        request.remove("file");
        response(client.upload(uploadUrlByHttp(), headers(), request));
    }

    @Test
    public void testNoFileHttps() throws IOException {
        exception.expect(IOException.class);
        exception.expectMessage("status code: 500, reason phrase: Request failed");
        Map<String, Object> request = requestParam();
        request.remove("file");
        response(client.upload(uploadUrlByHttps(), headers(), request));
    }

    @Test
    public void testUploadHttp() throws IOException {
        Response result = response(client.upload(uploadUrlByHttp(), headers(), requestParam()));
        assertEquals("upload success", result.getErrorMessage());
        assertEquals("uploading", result.getStatus());
        assertEquals(new Integer(123456), result.getUploadSequenceId());
    }

    @Test
    public void testUploadHttps() throws IOException {
        Response result = response(client.upload(uploadUrlByHttps(), headers(), requestParam()));
        assertEquals("upload success", result.getErrorMessage());
        assertEquals("uploading", result.getStatus());
        assertEquals(new Integer(123456), result.getUploadSequenceId());
    }

    private Map<String, Object> requestParam() {
        Map<String, String> request = new HashMap<String, String>();
        request.put("uid", "121212121212");
        request.put("email", "admin@example.com");
        request.put("token", "120a23b234cd23e132f0");
        request.put("uploadType", "config");

        File file = new File("pom.xml");

        Map<String, Object> requestParam = new HashMap<String, Object>();
        requestParam.put("file", file);
        requestParam.put("request", request);
        return requestParam;
    }

    private Map<String, String> headers() {
        return Collections.singletonMap("Accept-Language", "en");
    }

    private Response response(Map<String, Object> response) {
        return JsonUtils.jsonToObject(JsonUtils.toJsonString(response), Response.class);
    }

}