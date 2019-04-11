package com.eternitywall.ots.op;

import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.bitcoinj.core.Utils.toBytes;
import static org.junit.Assert.*;

public class TestOp {

    @Test
    public void testAppendOperation() {
        assertArrayEquals(toBytes("msg" + "suffix", "UTF-8"),
                          new OpAppend(toBytes("suffix", "UTF-8")).call(toBytes("msg", "UTF-8")));
    }

    @Test
    public void testPrependOperation() {
        assertArrayEquals(toBytes("prefix" + "msg", "UTF-8"),
                          new OpPrepend(toBytes("prefix", "UTF-8")).call(toBytes("msg", "UTF-8")));
    }

    @Test
    public void testSha256Operation() {
        OpSHA256 opSHA256 = new OpSHA256();
        byte[] emptyMsg = {};
        assertArrayEquals(DatatypeConverter.parseHexBinary("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"),
                          opSHA256.call(emptyMsg));
    }

    @Test
    public void testRIPEMD160Operation() {
        OpRIPEMD160 opRIPEMD160 = new OpRIPEMD160();
        byte[] emptyMsg = {};
        assertArrayEquals(DatatypeConverter.parseHexBinary("9c1185a5c5e9fc54612808977ee8f548b2258d31"),
                          opRIPEMD160.call(emptyMsg));
    }

    @Test
    public void testOperationEquality() {
        assertEquals(new OpAppend(toBytes("foo", "UTF-8")), new OpAppend(toBytes("foo", "UTF-8")));
        assertEquals(new OpPrepend(toBytes("foo", "UTF-8")), new OpPrepend(toBytes("foo", "UTF-8")));
        assertNotEquals(new OpAppend(toBytes("foo", "UTF-8")), new OpAppend(toBytes("bar", "UTF-8")));
        assertNotEquals(new OpAppend(toBytes("foo", "UTF-8")), new OpPrepend(toBytes("foo", "UTF-8")));
        assertNotEquals(new OpPrepend(toBytes("foo", "UTF-8")), new OpAppend(toBytes("foo", "UTF-8")));

        assertEquals(new OpSHA1(), new OpSHA1());
        assertNotEquals(new OpSHA1(), new OpSHA256());
        assertNotEquals(new OpSHA1(), new OpRIPEMD160());
        assertNotEquals(new OpSHA1(), new OpKECCAK256());

        assertEquals(new OpSHA256(), new OpSHA256());
        assertNotEquals(new OpSHA256(), new OpSHA1());
        assertNotEquals(new OpSHA256(), new OpRIPEMD160());
        assertNotEquals(new OpSHA256(), new OpKECCAK256());

        assertEquals(new OpRIPEMD160(), new OpRIPEMD160());
        assertNotEquals(new OpRIPEMD160(), new OpSHA256());
        assertNotEquals(new OpRIPEMD160(), new OpSHA1());
        assertNotEquals(new OpRIPEMD160(), new OpKECCAK256());

        assertEquals(new OpKECCAK256(), new OpKECCAK256());
        assertNotEquals(new OpKECCAK256(), new OpSHA256());
        assertNotEquals(new OpKECCAK256(), new OpRIPEMD160());
        assertNotEquals(new OpKECCAK256(), new OpSHA1());
    }

    @Test
    public void testOperationOrdering() {
        assertTrue((new OpSHA1()).compareTo(new OpRIPEMD160()) < 0);

        OpSHA1 op1 = new OpSHA1();
        OpSHA1 op2 = new OpSHA1();
        assertFalse(op1.compareTo(op2) < 0);
        assertFalse(op1.compareTo(op2) > 0);

        OpAppend op3 = new OpAppend(toBytes("00", "UTF-8"));
        OpAppend op4 = new OpAppend(toBytes("01", "UTF-8"));
        assertTrue(op3.compareTo(op4) < 0);
        assertFalse(op3.compareTo(op4) > 0);

        OpAppend op5 = new OpAppend(toBytes("01", "UTF-8"));
        OpAppend op6 = new OpAppend(toBytes("00", "UTF-8"));
        assertFalse(op5.compareTo(op6) < 0);
        assertTrue(op5.compareTo(op6) > 0);
    }
}
