package com.eternitywall.ots;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class TestCalendar {
    private static Logger log = Utils.getLogger(TestCalendar.class.getName());

    @Test
    public void testSingle() throws Exception {
        byte[] digest = Utils.randBytes(32);
        Calendar calendar = new Calendar(OpenTimestamps.FINNEY_URL);
        Timestamp timestamp = calendar.submit(digest);
        assertNotNull(timestamp);
        assertArrayEquals(digest, timestamp.getDigest());
    }

    @Test
    public void testPrivate() throws Exception {
        // key.wif it's a file of properties with the format
        // <calendar url> = <private key in wif format>
        // auth.calendar.eternitywall.com = KwT2r9sL........
        Map<String, String> privateUrls = getPrivateUrlsMap("key.wif");
        byte[] digest = Utils.randBytes(32);

        for (Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://" + entry.getKey();
            String wifKey = entry.getValue();
            ECKey key;

            try {
                BigInteger privKey = new BigInteger(wifKey);
                key = ECKey.fromPrivate(privKey);
            } catch (Exception e) {
                DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(NetworkParameters.prodNet(), wifKey);
                key = dumpedPrivateKey.getKey();
            }

            Calendar calendar = new Calendar(calendarUrl);
            calendar.setKey(key);
            Timestamp timestamp = calendar.submit(digest);
            assertNotNull(timestamp);
            assertArrayEquals(digest, timestamp.getDigest());
        }
    }

    @Test
    public void testPrivateWif() throws Exception {
        // key.wif it's a file of properties with the format
        // <calendar url> = <private key in wif format>
        // auth.calendar.eternitywall.com = KwT2r9sL........
        Map<String, String> privateUrls = getPrivateUrlsMap("key.wif");
        byte[] digest = Utils.randBytes(32);

        for (Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://" + entry.getKey();
            String wifKey = entry.getValue();
            DumpedPrivateKey dumpedPrivateKey = new DumpedPrivateKey(NetworkParameters.prodNet(), wifKey);
            ECKey key = dumpedPrivateKey.getKey();
            Calendar calendar = new Calendar(calendarUrl);
            calendar.setKey(key);
            Timestamp timestamp = calendar.submit(digest);
            assertNotNull(timestamp);
            assertArrayEquals(digest, timestamp.getDigest());
        }
    }

    private static Map<String, String> getPrivateUrlsMap(String fileName) throws IOException {
        Path path = Paths.get(fileName);

        if (!Files.exists(path)) {
            return Collections.emptyMap();    // This will make the resulting test a NOP
        }

        Properties properties = new Properties();
        properties.load(new FileInputStream(path.toString()));

        HashMap<String, String> privateUrls = new HashMap<>();

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            privateUrls.put(key, value);
        }

        assertFalse(privateUrls.isEmpty());   // If the properties file exist, then it shall not be empty

        return privateUrls;
    }

    @Test
    public void testSingleAsync() throws Exception {
        byte[] digest = Utils.randBytes(32);
        CalendarAsyncSubmit task = new CalendarAsyncSubmit(OpenTimestamps.FINNEY_URL, digest);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(1);
        task.setQueue(queue);
        task.call();
        Optional<Timestamp> timestamp = queue.take();
        assertTrue(timestamp.isPresent());
        assertNotNull(timestamp.get());
        assertArrayEquals(digest, timestamp.get().getDigest());
    }

    @Test
    public void testSingleAsyncPrivate() throws Exception {
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(1);
        Map<String, String> privateUrls = getPrivateUrlsMap("signature.key");
        byte[] digest = Utils.randBytes(32);

        for (Map.Entry<String, String> entry : privateUrls.entrySet()) {
            String calendarUrl = "https://" + entry.getKey();
            String signature = entry.getValue();
            BigInteger privKey = new BigInteger(signature);
            ECKey key = ECKey.fromPrivate(privKey);
            CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
            task.setKey(key);
            task.setQueue(queue);
            task.call();
            Optional<Timestamp> timestamp = queue.take();
            assertTrue(timestamp.isPresent());
            assertNotNull(timestamp.get());
            assertArrayEquals(digest, timestamp.get().getDigest());
        }
    }

    @Test
    public void testMulti() throws Exception {
        List<String> calendarsUrl = Arrays.asList(OpenTimestamps.ALICE_URL, OpenTimestamps.BOB_URL, OpenTimestamps.FINNEY_URL);
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(calendarsUrl.size());
        ExecutorService executor = Executors.newFixedThreadPool(calendarsUrl.size());
        int m = calendarsUrl.size();

        for (String calendarUrl : calendarsUrl) {
            try {
                CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
                task.setQueue(queue);
                executor.submit(task);
            } catch (Exception e) {
                log.warning("Exception while submitting executor tasks: " + e);
            }
        }

        int count = 0;

        for (String calendarUrl : calendarsUrl) {
            try {
                Optional<Timestamp> stamp = queue.take();
                //timestamp.merge(stamp);

                if (stamp.isPresent()) {
                    count++;
                }

                if (count >= m) {
                    break;
                }
            } catch (InterruptedException e) {
                log.warning("Interrupted while creating timestamp: " + e);
            }
        }

        if (count < m) {
            log.severe("Failed to create timestamp: requested " + String.valueOf(m) + " attestation" + ((m > 1) ? "s" : "") + " but received only " + String.valueOf(count));
        }

        assertFalse(count < m);

        executor.shutdown();
    }

    @Test
    public void testMultiWithInvalidCalendar() throws Exception {
        List<String> calendarsUrl = Arrays.asList(OpenTimestamps.ALICE_URL, OpenTimestamps.BOB_URL, "");
        byte[] digest = Utils.randBytes(32);
        ArrayBlockingQueue<Optional<Timestamp>> queue = new ArrayBlockingQueue<>(calendarsUrl.size());
        ExecutorService executor = Executors.newFixedThreadPool(calendarsUrl.size());
        int m = 2;

        for (String calendarUrl : calendarsUrl) {
            try {
                CalendarAsyncSubmit task = new CalendarAsyncSubmit(calendarUrl, digest);
                task.setQueue(queue);
                executor.submit(task);
            } catch (Exception e) {
                log.warning("Exception while submitting executor tasks: " + e);
            }
        }

        int count = 0;

        for (String calendarUrl : calendarsUrl) {
            try {
                Optional<Timestamp> stamp = queue.take();
                //timestamp.merge(stamp);

                if (stamp.isPresent()) {
                    count++;
                }

                if (count >= m) {
                    break;
                }
            } catch (InterruptedException e) {
                log.warning("Interrupted while creating timestamp: " + e);
            }
        }

        if (count < m) {
            log.severe("Failed to create timestamp: requested " + String.valueOf(m) + " attestation" + ((m > 1) ? "s" : "") + " but received only " + String.valueOf(count));
        }

        assertFalse(count < m);

        executor.shutdown();
    }

    @Test
    public void rfc6979() {
        BigInteger privateKey = new BigInteger("235236247357325473457345");
        ECKey ecKey = ECKey.fromPrivate(privateKey);
        String a = ecKey.signMessage("a");
        assertEquals("IBY7a75Ygps/o1BqTQ0OpFL+a8WHfd9jNO/8820ST0gyQ0SAuIWKm8/M90aG1G40oJvjrlcoiKngKAYYsJS6I0s=", a);
    }
}
