package com.eternitywall.ots;

import com.eternitywall.http.Request;
import com.eternitywall.http.Response;
import com.eternitywall.ots.exceptions.CommitmentNotFoundException;
import com.eternitywall.ots.exceptions.ExceededSizeException;
import com.eternitywall.ots.exceptions.UrlException;
import org.bitcoinj.core.ECKey;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing remote calendar server interface.
 */
public class Calendar {

    private String url;
    private ECKey key;

    /**
     * Create a RemoteCalendar.
     *
     * @param url The server url.
     */
    public Calendar(String url) {
        this.url = url;
    }

    /**
     * Set private key.
     *
     * @param key The private key.
     */
    public void setKey(ECKey key) {
        this.key = key;
    }

    /**
     * Get private key.
     *
     * @return The private key.
     */
    public ECKey getKey() {
        return this.key;
    }

    /**
     * Get calendar url.
     *
     * @return The calendar url.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Submitting a digest to remote calendar. Returns a com.eternitywall.ots.Timestamp committing to that digest.
     *
     * @param digest The digest hash to send.
     * @return the Timestamp received from the calendar.
     * @throws UrlException if url is not reachable.
     */
    public Timestamp submit(byte[] digest) throws UrlException {
        String submitUrl = url + "/digest";

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/vnd.opentimestamps.v1");
            headers.put("User-Agent", "java-opentimestamps");
            headers.put("Content-Type", "application/x-www-form-urlencoded");

            if (key != null) {
                String signature = key.signMessage(Utils.bytesToHex(digest).toLowerCase());
                headers.put("x-signature", signature);
            }

            URL obj = new URL(submitUrl);
            Request task = new Request(obj);
            task.setData(digest);
            task.setHeaders(headers);
            Response response = task.call();
            byte[] body = response.getBytes();

            if (body.length > 10000) {
                throw new ExceededSizeException("Calendar response exceeded size limit 10000 bytes");
            }

            StreamDeserializationContext ctx = new StreamDeserializationContext(body);

            return Timestamp.deserialize(ctx, digest);
        } catch (Exception e) {
            throw new UrlException("Could not submit digest to remote calendar at URL: " + submitUrl, e);
        }
    }

    /**
     * Get a timestamp for a given commitment.
     *
     * @param commitment The digest hash to send.
     * @return the Timestamp from the calendar server (with blockchain information if already written).
     * @throws UrlException if url is not reachable.
     */
    public Timestamp getTimestamp(byte[] commitment) throws UrlException {
        String timestampUrl = url + "/timestamp/" + Utils.bytesToHex(commitment).toLowerCase();

        try {
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept", "application/vnd.opentimestamps.v1");
            headers.put("User-Agent", "java-opentimestamps");
            headers.put("Content-Type", "application/x-www-form-urlencoded");

            URL obj = new URL(timestampUrl);
            Request task = new Request(obj);
            task.setHeaders(headers);
            Response response = task.call();
            byte[] body = response.getBytes();

            if (body.length > 10000) {
                throw new ExceededSizeException("Calendar response exceeded size limit 10000 bytes");
            }

            if (!response.isOk()) {
                throw new CommitmentNotFoundException("Calendar response != 200: " + response.getStatus());
            }

            StreamDeserializationContext ctx = new StreamDeserializationContext(body);

            return Timestamp.deserialize(ctx, commitment);
        } catch (Exception e) {
            throw new UrlException("Could not get timestamp from remote calendar at URL: " + timestampUrl, e);
        }
    }
}
