package com.eternitywall.http;

import com.eternitywall.ots.OpenTimestamps;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestRequestResponse {

    @Test
    public void testHttp() throws Exception {
        Request request = new Request(new URL("http://httpbin.org/status/418"));
        Response response = request.call();
        assertEquals(418, response.getStatus().intValue());
    }

    @Test
    public void testHttps() throws Exception {
        Request request = new Request(new URL(OpenTimestamps.FINNEY_URL));
        Response response = request.call();
        assertTrue(response.isOk());
        assertNotNull(response.getString());
    }
}
