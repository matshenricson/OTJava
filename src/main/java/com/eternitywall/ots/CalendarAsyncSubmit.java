package com.eternitywall.ots;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import org.bitcoinj.core.ECKey;

import javax.xml.bind.DatatypeConverter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by luca on 08/03/2017.
 */
public class CalendarAsyncSubmit implements Callable<Optional<Timestamp>> {

    private static Logger log = Logger.getLogger(CalendarAsyncSubmit.class.getName());

    private String url;
    private byte[] digest;
    private BlockingQueue<Optional<Timestamp>> queue;
    ECKey key;

    public CalendarAsyncSubmit(String url, byte[] digest) {
        this.url = url;
        this.digest=digest;
    }

    /**
     * Create a RemoteCalendar.
     * @param key The server key.
     */
    public void setKey(ECKey key) {
        this.key = key;
    }

    public void setQueue(BlockingQueue<Optional<Timestamp>> queue) {
        this.queue = queue;
    }

    @Override
    public Optional<Timestamp> call() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept","application/vnd.opentimestamps.v1");
        headers.put("User-Agent","java-opentimestamps");
        headers.put("Content-Type","application/x-www-form-urlencoded");

        if (key != null ) {
            String signature = key.signMessage(new String(digest, StandardCharsets.US_ASCII));
            headers.put("x-signature", signature);
        }

        URL obj = new URL(url + "/digest");
        Request task = new Request(obj);
        task.setData(digest);
        task.setHeaders(headers);
        Response response = task.call();
        if(response.isOk()) {
            byte[] body = response.getBytes();

            StreamDeserializationContext ctx = new StreamDeserializationContext(body);
            Timestamp timestamp = Timestamp.deserialize(ctx, digest);
            Optional<Timestamp> of = Optional.of(timestamp);
            queue.add(of);
            return of;
        }
        queue.add(Optional.<Timestamp>absent());
        return Optional.absent();
    }
}