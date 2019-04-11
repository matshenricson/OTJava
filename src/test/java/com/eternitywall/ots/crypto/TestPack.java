package com.eternitywall.ots.crypto;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestPack {

    private static final short A_SHORT = (short) 77;
    private static final int AN_INT = 78;
    private static final int ANOTHER_INT = 79;
    private static final long A_LONG = 80L;
    private static final long ANOTHER_LONG = 81L;

    private static final int[] MANY_INTS = {AN_INT, ANOTHER_INT};
    private static final long[] MANY_LONGS = {A_LONG, ANOTHER_LONG};

    @Test
    public void testShortToLittleEndianAndBack() {
        byte[] b = Pack.shortToLittleEndian(A_SHORT);
        int s = Pack.littleEndianToShort(b, 0);
        assertEquals(A_SHORT, s);
    }

    @Test
    public void testIntToBigEndianAndBack() {
        byte[] b = Pack.intToBigEndian(AN_INT);
        int i = Pack.bigEndianToInt(b, 0);
        assertEquals(AN_INT, i);
    }

    @Test
    public void testManyIntToBigEndianAndBack() {
        int[] newInts = new int[MANY_INTS.length];
        byte[] bytes = new byte[MANY_INTS.length * 4];
        Pack.intToBigEndian(MANY_INTS, bytes, 0);
        Pack.bigEndianToInt(bytes, 0, newInts);
        assertArrayEquals(MANY_INTS, newInts);
    }

    @Test
    public void testManyIntToLittleEndianAndBack() {
        int[] newInts = new int[MANY_INTS.length];
        byte[] bytes = new byte[MANY_INTS.length * 4];
        Pack.intToLittleEndian(MANY_INTS, bytes, 0);
        Pack.littleEndianToInt(bytes, 0, newInts);
        assertArrayEquals(MANY_INTS, newInts);
    }

    @Test
    public void testIntToLittleEndianAndBack() {
        byte[] b = Pack.intToLittleEndian(AN_INT);
        int i = Pack.littleEndianToInt(b, 0);
        assertEquals(AN_INT, i);
    }

    @Test
    public void testLongToBigEndianAndBack() {
        byte[] b = Pack.longToBigEndian(A_LONG);
        long l = Pack.bigEndianToLong(b, 0);
        assertEquals(A_LONG, l);
    }

    @Test
    public void testManyLongToBigEndianAndBack() {
        long[] newLongs = new long[MANY_LONGS.length];
        byte[] bytes = new byte[MANY_LONGS.length * 8];
        Pack.longToBigEndian(MANY_LONGS, bytes, 0);
        Pack.bigEndianToLong(bytes, 0, newLongs);
        assertArrayEquals(MANY_LONGS, newLongs);
    }

    @Test
    public void testLongToLittleEndianAndBack() {
        byte[] b = Pack.longToLittleEndian(A_LONG);
        long l = Pack.littleEndianToLong(b, 0);
        assertEquals(A_LONG, l);
    }

    @Test
    public void testManyLongToLittleEndianAndBack() {
        long[] newLongs = new long[MANY_LONGS.length];
        byte[] bytes = new byte[MANY_LONGS.length * 8];
        Pack.longToLittleEndian(MANY_LONGS, bytes, 0);
        Pack.littleEndianToLong(bytes, 0, newLongs);
        assertArrayEquals(MANY_LONGS, newLongs);
    }
}
