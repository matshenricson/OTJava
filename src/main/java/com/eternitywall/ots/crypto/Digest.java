package com.eternitywall.ots.crypto;

/**
 * Message digest interface
 */
public interface Digest {
    /**
     * Return the algorithm name
     *
     * @return the algorithm name
     */
    String getAlgorithmName();

    /**
     * Return the size, in bytes, of the digest produced by this message digest.
     *
     * @return the size, in bytes, of the digest produced by this message digest.
     */
    int getDigestSize();

    /**
     * Update the message digest with a single byte.
     *
     * @param in the input byte to be entered.
     */
    void update(byte in);

    /**
     * Update the message digest with a block of bytes.
     *
     * @param in    the byte array containing the data.
     * @param inOff the offset into the byte array where the data starts.
     * @param len   the length of the data.
     */
    void update(byte[] in, int inOff, int len);

    /**
     * Close the digest, producing the final digest value.
     * The doFinal call also resets the digest.
     *
     * @param out    the array the digest is to be copied into.
     * @param outOff the offset into the out array the digest is to start at.
     * @return       the length of the digest
     * @see #reset()
     */
    int doFinal(byte[] out, int outOff);

    /**
     * Reset the digest back to it's initial state.
     */
    void reset();
}
