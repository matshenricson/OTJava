package com.eternitywall.ots.crypto;

import com.eternitywall.ots.Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;

public class TestKeccakDigest {

    @Test
    public void testKeccakEmptyMsg() {
        KeccakDigest digest = new KeccakDigest(256);
        byte[] msg = new byte[0];
        digest.update(msg, 0, msg.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        assertEquals("c5d2460186f7233c927e7db2dcc703c0e500b653ca82273b7bfad8045d85a470", Utils.bytesToHex(hash).toLowerCase());
    }

    @Test
    public void testKeccakRealMsg() {
        KeccakDigest digest = new KeccakDigest(256);
        byte[] msg = Utils.hexToBytes("80");
        digest.update(msg, 0, msg.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        assertEquals("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421", Utils.bytesToHex(hash).toLowerCase());
    }

    @Test
    public void testReset() throws NoSuchAlgorithmException {
        KeccakDigest digest = new KeccakDigest(256);
        digest.update(Utils.randBytes(777), 0, 777);
        digest.reset();

        byte[] msg = Utils.hexToBytes("80");
        digest.update(msg, 0, msg.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        assertEquals("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421", Utils.bytesToHex(hash).toLowerCase());
    }
}
