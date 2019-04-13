package com.eternitywall.ots.attestation;

import com.eternitywall.ots.Utils;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TestAttestation {
    private static final byte[] randomTag1 = getRandomBytes(TimeAttestation._TAG_SIZE);
    private static final byte[] randomTag2 = getRandomBytes(TimeAttestation._TAG_SIZE);
    private static final byte[] randomBytes1 = getRandomBytes(77);
    private static final byte[] randomBytes2 = getRandomBytes(77);

    private static byte[] getRandomBytes(int length) {
        try {
            return Utils.randBytes(length);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not create random bytes, this is weird", e);
        }
    }

    @Test
    public void testCompareTo() {
        assertEquals(0, new PendingAttestation(randomBytes1).compareTo(new PendingAttestation(randomBytes1)));
        assertNotEquals(0, new PendingAttestation(randomBytes1).compareTo(new PendingAttestation(randomBytes2)));
        assertNotEquals(0, new PendingAttestation(randomBytes1).compareTo(new UnknownAttestation(randomTag1, randomBytes1)));
        assertNotEquals(0, new PendingAttestation(randomBytes1).compareTo(new UnknownAttestation(PendingAttestation._TAG, randomBytes1)));
        assertNotEquals(0, new PendingAttestation(randomBytes1).compareTo(new BitcoinBlockHeaderAttestation(77)));
        assertNotEquals(0, new PendingAttestation(randomBytes1).compareTo(new EthereumBlockHeaderAttestation(78)));
        assertNotEquals(0, new PendingAttestation(randomBytes1).compareTo(new LitecoinBlockHeaderAttestation(79)));

        assertEquals(0, new BitcoinBlockHeaderAttestation(77).compareTo(new BitcoinBlockHeaderAttestation(77)));
        assertEquals(1, new BitcoinBlockHeaderAttestation(78).compareTo(new BitcoinBlockHeaderAttestation(77)));
        assertEquals(-1, new BitcoinBlockHeaderAttestation(77).compareTo(new BitcoinBlockHeaderAttestation(78)));
        assertNotEquals(0, new BitcoinBlockHeaderAttestation(77).compareTo(new PendingAttestation(randomBytes1)));
        assertNotEquals(0, new BitcoinBlockHeaderAttestation(77).compareTo(new UnknownAttestation(randomTag1, randomBytes1)));
        assertNotEquals(0, new BitcoinBlockHeaderAttestation(77).compareTo(new UnknownAttestation(BitcoinBlockHeaderAttestation._TAG, randomBytes1)));
        assertNotEquals(0, new BitcoinBlockHeaderAttestation(77).compareTo(new EthereumBlockHeaderAttestation(77)));
        assertNotEquals(0, new BitcoinBlockHeaderAttestation(77).compareTo(new LitecoinBlockHeaderAttestation(77)));

        assertEquals(0, new EthereumBlockHeaderAttestation(78).compareTo(new EthereumBlockHeaderAttestation(78)));
        assertEquals(1, new EthereumBlockHeaderAttestation(79).compareTo(new EthereumBlockHeaderAttestation(78)));
        assertEquals(-1, new EthereumBlockHeaderAttestation(78).compareTo(new EthereumBlockHeaderAttestation(79)));
        assertNotEquals(0, new EthereumBlockHeaderAttestation(78).compareTo(new PendingAttestation(randomBytes1)));
        assertNotEquals(0, new EthereumBlockHeaderAttestation(78).compareTo(new UnknownAttestation(randomTag1, randomBytes1)));
        assertNotEquals(0, new EthereumBlockHeaderAttestation(78).compareTo(new UnknownAttestation(EthereumBlockHeaderAttestation._TAG, randomBytes1)));
        assertNotEquals(0, new EthereumBlockHeaderAttestation(78).compareTo(new BitcoinBlockHeaderAttestation(78)));
        assertNotEquals(0, new EthereumBlockHeaderAttestation(78).compareTo(new LitecoinBlockHeaderAttestation(78)));

        assertEquals(0, new LitecoinBlockHeaderAttestation(79).compareTo(new LitecoinBlockHeaderAttestation(79)));
        assertEquals(1, new LitecoinBlockHeaderAttestation(80).compareTo(new LitecoinBlockHeaderAttestation(79)));
        assertEquals(-1, new LitecoinBlockHeaderAttestation(79).compareTo(new LitecoinBlockHeaderAttestation(80)));
        assertNotEquals(0, new LitecoinBlockHeaderAttestation(79).compareTo(new PendingAttestation(randomBytes1)));
        assertNotEquals(0, new LitecoinBlockHeaderAttestation(79).compareTo(new UnknownAttestation(randomTag1, randomBytes1)));
        assertNotEquals(0, new LitecoinBlockHeaderAttestation(79).compareTo(new UnknownAttestation(LitecoinBlockHeaderAttestation._TAG, randomBytes1)));
        assertNotEquals(0, new LitecoinBlockHeaderAttestation(79).compareTo(new BitcoinBlockHeaderAttestation(79)));
        assertNotEquals(0, new LitecoinBlockHeaderAttestation(79).compareTo(new EthereumBlockHeaderAttestation(79)));

        assertEquals(0, new UnknownAttestation(randomTag1, randomBytes1).compareTo(new UnknownAttestation(randomTag1, randomBytes1)));
        assertNotEquals(0, new UnknownAttestation(randomTag1, randomBytes1).compareTo(new UnknownAttestation(randomTag2, randomBytes1)));
        assertNotEquals(0, new UnknownAttestation(randomTag1, randomBytes1).compareTo(new UnknownAttestation(randomTag1, randomBytes2)));
        assertNotEquals(0, new UnknownAttestation(randomTag1, randomBytes2).compareTo(new UnknownAttestation(randomTag1, randomBytes1)));
        assertNotEquals(0, new UnknownAttestation(randomTag1, randomBytes1).compareTo(new PendingAttestation(randomBytes1)));
        assertNotEquals(0, new UnknownAttestation(randomTag1, randomBytes1).compareTo(new BitcoinBlockHeaderAttestation(77)));
        assertNotEquals(0, new UnknownAttestation(randomTag1, randomBytes1).compareTo(new EthereumBlockHeaderAttestation(78)));
        assertNotEquals(0, new UnknownAttestation(randomTag1, randomBytes1).compareTo(new LitecoinBlockHeaderAttestation(79)));
    }

    @Test
    public void testEquals() {
        assertEquals(new PendingAttestation(randomBytes1), new PendingAttestation(randomBytes1));
        assertNotEquals(new PendingAttestation(randomBytes1), new PendingAttestation(randomBytes2));
        assertNotEquals(new PendingAttestation(randomBytes1), new UnknownAttestation(randomTag1, randomBytes1));
        assertNotEquals(new PendingAttestation(randomBytes1), new BitcoinBlockHeaderAttestation(77));
        assertNotEquals(new PendingAttestation(randomBytes1), new EthereumBlockHeaderAttestation(78));
        assertNotEquals(new PendingAttestation(randomBytes1), new LitecoinBlockHeaderAttestation(79));

        assertEquals(new BitcoinBlockHeaderAttestation(77), new BitcoinBlockHeaderAttestation(77));
        assertNotEquals(new BitcoinBlockHeaderAttestation(77 + 1), new BitcoinBlockHeaderAttestation(77));
        assertNotEquals(new BitcoinBlockHeaderAttestation(77), new BitcoinBlockHeaderAttestation(77 + 1));
        assertNotEquals(new BitcoinBlockHeaderAttestation(77), new PendingAttestation(randomBytes1));
        assertNotEquals(new BitcoinBlockHeaderAttestation(77), new UnknownAttestation(randomTag1, randomBytes1));
        assertNotEquals(new BitcoinBlockHeaderAttestation(77), new EthereumBlockHeaderAttestation(77));
        assertNotEquals(new BitcoinBlockHeaderAttestation(77), new LitecoinBlockHeaderAttestation(77));

        assertEquals(new EthereumBlockHeaderAttestation(78), new EthereumBlockHeaderAttestation(78));
        assertNotEquals(new EthereumBlockHeaderAttestation(78 + 1), new EthereumBlockHeaderAttestation(78));
        assertNotEquals(new EthereumBlockHeaderAttestation(78), new EthereumBlockHeaderAttestation(78 + 1));
        assertNotEquals(new EthereumBlockHeaderAttestation(78), new PendingAttestation(randomBytes1));
        assertNotEquals(new EthereumBlockHeaderAttestation(78), new UnknownAttestation(randomTag1, randomBytes1));
        assertNotEquals(new EthereumBlockHeaderAttestation(78), new BitcoinBlockHeaderAttestation(78));
        assertNotEquals(new EthereumBlockHeaderAttestation(78), new LitecoinBlockHeaderAttestation(78));

        assertEquals(new LitecoinBlockHeaderAttestation(79), new LitecoinBlockHeaderAttestation(79));
        assertNotEquals(new LitecoinBlockHeaderAttestation(79 + 1), new LitecoinBlockHeaderAttestation(79));
        assertNotEquals(new LitecoinBlockHeaderAttestation(79), new LitecoinBlockHeaderAttestation(79 + 1));
        assertNotEquals(new LitecoinBlockHeaderAttestation(79), new PendingAttestation(randomBytes1));
        assertNotEquals(new LitecoinBlockHeaderAttestation(79), new UnknownAttestation(randomTag1, randomBytes1));
        assertNotEquals(new LitecoinBlockHeaderAttestation(79), new BitcoinBlockHeaderAttestation(79));
        assertNotEquals(new LitecoinBlockHeaderAttestation(79), new EthereumBlockHeaderAttestation(79));

        assertEquals(new UnknownAttestation(randomTag1, randomBytes1), new UnknownAttestation(randomTag1, randomBytes1));
        assertNotEquals(new UnknownAttestation(randomTag1, randomBytes1), new UnknownAttestation(randomTag2, randomBytes1));
        assertNotEquals(new UnknownAttestation(randomTag1, randomBytes1), new UnknownAttestation(randomTag1, randomBytes2));
        assertNotEquals(new UnknownAttestation(randomTag1, randomBytes2), new UnknownAttestation(randomTag1, randomBytes1));
        assertNotEquals(new UnknownAttestation(randomTag1, randomBytes1), new PendingAttestation(randomBytes1));
        assertNotEquals(new UnknownAttestation(randomTag1, randomBytes1), new BitcoinBlockHeaderAttestation(77));
        assertNotEquals(new UnknownAttestation(randomTag1, randomBytes1), new EthereumBlockHeaderAttestation(78));
        assertNotEquals(new UnknownAttestation(randomTag1, randomBytes1), new LitecoinBlockHeaderAttestation(79));
    }
}
