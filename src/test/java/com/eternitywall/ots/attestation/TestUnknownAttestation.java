package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import static com.eternitywall.ots.Utils.hexToBytes;
import static com.eternitywall.ots.Utils.bytesToHex;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestUnknownAttestation {

    @Test
    public void testToString() {
        UnknownAttestation a = new UnknownAttestation(hexToBytes("0102030405060708"),
                                                      toBytes("Hello World!", "UTF-8"));

        String expectedString = "UnknownAttestation " + bytesToHex(a._TAG()) + ' ' + bytesToHex(a.payload);
        assertEquals(expectedString, a.toString());
    }

    @Test
    public void testSerializationDeserializationOfUnknownAttestations() throws IOException {
        byte[] tagBytes = hexToBytes("0102030405060708");
        byte[] helloWorldBytes = toBytes("Hello World!", "UTF-8");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(tagBytes);
        baos.write(0x0c);
        baos.write(helloWorldBytes);
        byte[] expectedSerialized = baos.toByteArray();

        StreamDeserializationContext ctx = new StreamDeserializationContext(baos.toByteArray());
        UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
        assertArrayEquals(tagBytes, a._TAG());
        assertArrayEquals(helloWorldBytes, a.payload);

        StreamSerializationContext ctx1 = new StreamSerializationContext();
        a.serialize(ctx1);
        assertArrayEquals(expectedSerialized, ctx1.getOutput());
    }

    @Test
    public void testDeserializationTooLong() throws IOException {
        // Deserialization of attestations with oversized payloads
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        baos1.write(hexToBytes("0102030405060708"));
        baos1.write(0x81);
        baos1.write(0x40);
        baos1.write('x' * 8193);
        StreamDeserializationContext ctx = new StreamDeserializationContext(baos1.toByteArray());
        UnknownAttestation a = (UnknownAttestation) TimeAttestation.deserialize(ctx);
        // TODO: exception

        // Pending attestation
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        baos2.write(hexToBytes("83dfe30d2ef90c8e"));
        baos2.write(0x81);
        baos2.write(0x40);
        baos2.write('x' * 8193);
        StreamDeserializationContext ctx1 = new StreamDeserializationContext(baos2.toByteArray());
        UnknownAttestation a1 = (UnknownAttestation) TimeAttestation.deserialize(ctx1);
        // TODO: exception
    }
}
