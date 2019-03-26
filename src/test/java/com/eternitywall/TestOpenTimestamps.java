package com.eternitywall;

import com.eternitywall.ots.DetachedTimestampFile;
import com.eternitywall.ots.Hash;
import com.eternitywall.ots.OpenTimestamps;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Timestamp;
import com.eternitywall.ots.Utils;
import com.eternitywall.ots.VerifyResult;
import com.eternitywall.ots.attestation.TimeAttestation;
import com.eternitywall.ots.exceptions.VerificationException;
import com.eternitywall.ots.op.OpSHA256;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TestOpenTimestamps {

    private static final byte[] incomplete                 = getByteArrayFromLocalFile("examples/incomplete.txt");
    private static final byte[] incompleteOts              = getByteArrayFromLocalFile("examples/incomplete.txt.ots");
    private static final String incompleteOtsInfo          = getStringFromLocalFile(   "examples/incomplete.txt.ots.info");
    private static final byte[] helloWorld                 = getByteArrayFromLocalFile("examples/hello-world.txt");
    private static final byte[] helloWorldOts              = getByteArrayFromLocalFile("examples/hello-world.txt.ots");
    private static final byte[] merkle1Ots                 = getByteArrayFromLocalFile("examples/merkle1.txt.ots");
    private static final byte[] merkle2Ots                 = getByteArrayFromLocalFile("examples/merkle2.txt.ots");
    private static final String merkle2OtsInfo             = getStringFromLocalFile(   "examples/merkle2.txt.ots.info");
    private static final byte[] merkle3Ots                 = getByteArrayFromLocalFile("examples/merkle3.txt.ots");
    private static final byte[] differentBlockchainOts     = getByteArrayFromLocalFile("examples/different-blockchains.txt.ots");
    private static final String differentBlockchainOtsInfo = getStringFromLocalFile(   "examples/different-blockchains.txt.ots.info");

    private static byte[] getByteArrayFromLocalFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load local file, really weird: " + e);
        }
    }

    private static String getStringFromLocalFile(String path) {
        return new String(getByteArrayFromLocalFile(path), StandardCharsets.UTF_8);
    }

    @Test
    public void info() {
        String result1 = OpenTimestamps.info(DetachedTimestampFile.deserialize(incompleteOts));
        assertNotNull(result1);
        assertNotNull(incompleteOtsInfo);
        assertEquals(incompleteOtsInfo, result1);

        String result2 = OpenTimestamps.info(DetachedTimestampFile.deserialize(merkle2Ots));
        assertNotNull(result2);
        assertNotNull(merkle2OtsInfo);
        assertEquals(merkle2OtsInfo, result2);

        String result3 = OpenTimestamps.info(DetachedTimestampFile.deserialize(differentBlockchainOts));
        assertNotNull(result3);
        assertNotNull(differentBlockchainOtsInfo);
        assertEquals(differentBlockchainOtsInfo, result3);
    }

    @Test
    public void stamp() throws NoSuchAlgorithmException, IOException {
        {
            byte[] bytes = Utils.randBytes(32);
            DetachedTimestampFile detached = DetachedTimestampFile.from(new Hash(bytes, OpSHA256._TAG));
            Timestamp stamp = OpenTimestamps.stamp(detached);
            byte[] digest = detached.fileDigest();
            assertArrayEquals(digest, bytes);
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(helloWorld, OpSHA256._TAG));
            Timestamp stamp = OpenTimestamps.stamp(detached);
            byte[] digest = detached.fileDigest();
            final String helloWorldHashHex = "03ba204e50d126e4674c005e04d82e84c21366780af1f43bd54a37816b6ab340";

            assertArrayEquals(digest, Utils.hexToBytes(helloWorldHashHex));
        }
    }

    @Test
    public void merkle() throws NoSuchAlgorithmException, IOException {
        List<byte[]> files = new ArrayList<>();
        files.add(helloWorld);
        files.add(merkle2Ots);
        files.add(incomplete);
        List<DetachedTimestampFile> fileTimestamps = new ArrayList<>();

        for (byte[] file : files) {      // TODO: file is never used !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            InputStream is = new ByteArrayInputStream(helloWorld);
            DetachedTimestampFile detachedTimestampFile = DetachedTimestampFile.from(new OpSHA256(), is);
            fileTimestamps.add(detachedTimestampFile);
        }

        Timestamp merkleTip = OpenTimestamps.makeMerkleTree(fileTimestamps);

        // For each fileTimestamps check the tip
        for (DetachedTimestampFile fileTimestamp : fileTimestamps) {
            Set<byte[]> tips = fileTimestamp.getTimestamp().allTips();

            for (byte[] tip : tips) {
                assertArrayEquals(tip, merkleTip.getDigest());
            }
        }
    }

    @Test(expected = Exception.class)
    public void verify2() throws Exception {
        DetachedTimestampFile helloOts = DetachedTimestampFile.deserialize(helloWorldOts);
        DetachedTimestampFile differentOts = DetachedTimestampFile.deserialize(differentBlockchainOts);
        helloOts.getTimestamp().attestations = differentOts.getTimestamp().attestations;

        helloOts.getTimestamp().ops = differentOts.getTimestamp().ops;

        DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(helloWorld, OpSHA256._TAG));
        helloOts = DetachedTimestampFile.deserialize(helloOts.serialize());

        System.out.println(OpenTimestamps.verify(helloOts, detached).toString()); // returns the timestamp of the second file
    }

    @Test
    public void verify() throws NoSuchAlgorithmException, IOException {
        {
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(helloWorldOts);
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(helloWorld, OpSHA256._TAG));

            try {
                HashMap<VerifyResult.Chains, VerifyResult> results = OpenTimestamps.verify(detachedOts, detached);
                assertTrue(results.size() > 0);
                assertTrue(results.containsKey(VerifyResult.Chains.BITCOIN));
                assertEquals(1432827678L, results.get(VerifyResult.Chains.BITCOIN).timestamp.longValue());
            } catch (Exception e) {
                fail("Exception during test: " + e);
            }
        }

        // verify on python call upgrade
        {
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(incompleteOts);
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(incomplete, OpSHA256._TAG));

            try {
                HashMap<VerifyResult.Chains, VerifyResult> results = OpenTimestamps.verify(detachedOts, detached);
                assertEquals(results.size(), 0);
            } catch (Exception e) {
                fail("Exception during test: " + e);
            }
        }
    }

    @Test(expected = VerificationException.class)
    public void verifyCheckForFileManipulation() throws Exception {
        DetachedTimestampFile helloOts = DetachedTimestampFile.deserialize(helloWorldOts);
        DetachedTimestampFile differentOts = DetachedTimestampFile.deserialize(differentBlockchainOts);

        helloOts.getTimestamp().ops = differentOts.getTimestamp().ops;

        DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(helloWorld, OpSHA256._TAG));
        helloOts = DetachedTimestampFile.deserialize(helloOts.serialize());
        OpenTimestamps.verify(helloOts, detached);
    }

    @Test
    public void upgrade() {
        try {
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(incomplete, OpSHA256._TAG));
            DetachedTimestampFile detachedOts = DetachedTimestampFile.deserialize(incompleteOts);
            boolean changed = OpenTimestamps.upgrade(detachedOts);
            assertTrue(changed);

            HashMap<VerifyResult.Chains, VerifyResult> results = OpenTimestamps.verify(detachedOts, detached);
            assertTrue(results.size() > 0);
            assertTrue(results.containsKey(VerifyResult.Chains.BITCOIN));
            assertEquals(1473227803L, results.get(VerifyResult.Chains.BITCOIN).timestamp.longValue());
        } catch (Exception e) {
            fail("Exception during test: " + e);
        }

        try {
            byte[] hashBytes = Utils.randBytes(32);
            DetachedTimestampFile detached = DetachedTimestampFile.from(Hash.from(hashBytes, OpSHA256._TAG));
            Timestamp stamp = OpenTimestamps.stamp(detached);
            boolean changed = OpenTimestamps.upgrade(stamp);
            assertFalse(changed);
        } catch (Exception e) {
            fail("Exception during test: " + e);
        }
    }

    @Test
    public void test() {
        byte[] ots = Utils.hexToBytes("F0105C3F2B3F8524A32854E07AD8ADDE9C1908F10458D95A36F008088D287213A8B9880083DFE30D2EF90C8E2C2B68747470733A2F2F626F622E6274632E63616C656E6461722E6F70656E74696D657374616D70732E6F7267");
        byte[] digest = Utils.hexToBytes("7aa9273d2a50dbe0cc5a6ccc444a5ca90c9491dd2ac91849e45195ae46f64fe352c3a63ba02775642c96131df39b5b85");

        StreamDeserializationContext streamDeserializationContext = new StreamDeserializationContext(ots);
        Timestamp timestamp = Timestamp.deserialize(streamDeserializationContext, digest);

        StreamSerializationContext streamSerializationContext = new StreamSerializationContext();
        timestamp.serialize(streamSerializationContext);
        byte[] otsBefore = streamSerializationContext.getOutput();

        try {
            boolean changed = OpenTimestamps.upgrade(timestamp);
            assertTrue(changed);
        } catch (Exception e) {
            fail("Exception during test: " + e);
        }
    }

    @Test
    public void shrink() throws Exception {
        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(helloWorldOts);
            Timestamp timestamp = detached.getTimestamp();
            assertEquals(1, timestamp.getAttestations().size());

            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(1, timestamp.getAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestation));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(incompleteOts);
            Timestamp timestamp = detached.getTimestamp();
            assertEquals(1, timestamp.getAttestations().size());

            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(1, timestamp.getAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(2, timestamp.allAttestations().size());

            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(2, timestamp.allAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(merkle1Ots);
            Timestamp timestamp = detached.getTimestamp();
            assertEquals(2, timestamp.getAttestations().size());

            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(2, timestamp.getAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(4, timestamp.allAttestations().size());

            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(2, timestamp.allAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(merkle2Ots);
            Timestamp timestamp = detached.getTimestamp();
            assertEquals(2, timestamp.getAttestations().size());

            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(2, timestamp.getAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(4, timestamp.allAttestations().size());

            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(2, timestamp.getAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));
        }

        {
            DetachedTimestampFile detached = DetachedTimestampFile.deserialize(merkle3Ots);
            Timestamp timestamp = detached.getTimestamp();
            assertEquals(2, timestamp.getAttestations().size());

            TimeAttestation resultAttestation = timestamp.shrink();
            assertEquals(2, timestamp.getAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestation));

            OpenTimestamps.upgrade(detached);
            assertEquals(4, timestamp.allAttestations().size());

            TimeAttestation resultAttestationBitcoin = timestamp.shrink();
            assertEquals(2, timestamp.allAttestations().size());
            assertTrue(timestamp.getAttestations().contains(resultAttestationBitcoin));
        }
    }
}
