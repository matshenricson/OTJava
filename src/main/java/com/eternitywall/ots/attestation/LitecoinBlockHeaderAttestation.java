package com.eternitywall.ots.attestation;

import com.eternitywall.ots.BlockHeader;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import com.eternitywall.ots.exceptions.VerificationException;

import java.util.Arrays;

/**
 * Litecoin Block Header Attestation.
 *
 * @see TimeAttestation
 */
public class LitecoinBlockHeaderAttestation extends TimeAttestation {

    public static final byte[] _TAG = {(byte) 0x06, (byte) 0x86, (byte) 0x9a, (byte) 0x0d, (byte) 0x73, (byte) 0xd7, (byte) 0x1b, (byte) 0x45};
    public static final String chain = "litecoin";

    private int height;

    public byte[] _TAG() {
        return LitecoinBlockHeaderAttestation._TAG;
    }

    public int getHeight() {
        return height;
    }

    public LitecoinBlockHeaderAttestation(int height) {
        super();
        this.height = height;
    }

    public static LitecoinBlockHeaderAttestation deserialize(StreamDeserializationContext ctxPayload) {
        int height = ctxPayload.readVaruint();

        return new LitecoinBlockHeaderAttestation(height);
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeVaruint(this.height);
    }

    public String toString() {
        return "LitecoinBlockHeaderAttestation(" + this.height + ")";
    }

    public int compareTo(TimeAttestation other) {
        int deltaTag = Utils.compare(this._TAG(), other._TAG());

        if (deltaTag != 0) {
            return deltaTag;
        }

        if (!(other instanceof LitecoinBlockHeaderAttestation)) {
            // This is very unlikely, but possible, since UnknownAttestation can have any tag
            return this.getClass().getName().compareTo(other.getClass().getName());   // This makes the comparison symmetric
        }

        LitecoinBlockHeaderAttestation that = (LitecoinBlockHeaderAttestation) other;

        return this.height - that.height;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LitecoinBlockHeaderAttestation)) {
            return false;
        }

        LitecoinBlockHeaderAttestation that = (LitecoinBlockHeaderAttestation) other;

        return this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this._TAG()) ^ this.height;
    }

    /**
     * Verify attestation against a block header.
     * Returns the block time on success; raises VerificationError on failure.
     */
    public Long verifyAgainstBlockheader(byte[] digest, BlockHeader block) throws VerificationException {
        if (digest.length != 32) {
            throw new VerificationException("Expected digest with length 32 bytes; got " + digest.length + " bytes");
        } else if (!Arrays.equals(digest, Utils.hexToBytes(block.getMerkleroot()))) {
            throw new VerificationException("Digest does not match merkleroot");
        }

        return block.getTime();
    }
}
