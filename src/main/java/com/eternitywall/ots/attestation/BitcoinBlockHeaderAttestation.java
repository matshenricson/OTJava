package com.eternitywall.ots.attestation;

import com.eternitywall.ots.BlockHeader;
import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;
import com.eternitywall.ots.exceptions.VerificationException;

import java.util.Arrays;

/**
 * Bitcoin Block Header Attestation.
 * The commitment digest will be the merkleroot of the blockheader.
 * The block height is recorded so that looking up the correct block header in
 * an external block header database doesn't require every header to be stored
 * locally (33MB and counting). (remember that a memory-constrained local
 * client can save an MMR that commits to all blocks, and use an external service to fill
 * in pruned details).
 * Otherwise no additional redundant data about the block header is recorded.
 * This is very intentional: since the attestation contains (nearly) the
 * absolute bare minimum amount of data, we encourage implementations to do
 * the correct thing and get the block header from a by-height index, check
 * that the merkleroots match, and then calculate the time from the header
 * information. Providing more data would encourage implementations to cheat.
 * Remember that the only thing that would invalidate the block height is a
 * reorg, but in the event of a reorg the merkleroot will be invalid anyway,
 * so there's no point to recording data in the attestation like the header
 * itself. At best that would just give us extra confirmation that a reorg
 * made the attestation invalid; reorgs deep enough to invalidate timestamps are
 * exceptionally rare events anyway, so better to just tell the user the timestamp
 * can't be verified rather than add almost-never tested code to handle that case
 * more gracefully.
 *
 * @see TimeAttestation
 */
public class BitcoinBlockHeaderAttestation extends TimeAttestation {

    public static final byte[] _TAG = {(byte) 0x05, (byte) 0x88, (byte) 0x96, (byte) 0x0d, (byte) 0x73, (byte) 0xd7, (byte) 0x19, (byte) 0x01};
    public static final String chain = "bitcoin";

    private int height;

    @Override
    public byte[] _TAG() {
        return BitcoinBlockHeaderAttestation._TAG;
    }

    public int getHeight() {
        return height;
    }

    public BitcoinBlockHeaderAttestation(int height) {
        super();
        this.height = height;
    }

    public static BitcoinBlockHeaderAttestation deserialize(StreamDeserializationContext ctxPayload) {
        int height = ctxPayload.readVaruint();

        return new BitcoinBlockHeaderAttestation(height);
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeVaruint(this.height);
    }

    public String toString() {
        return "BitcoinBlockHeaderAttestation(" + this.height + ")";
    }

    @Override
    public int compareTo(TimeAttestation other) {
        if (!(other instanceof BitcoinBlockHeaderAttestation)) {
            return this.getClass().getName().compareTo(other.getClass().getName());   // This makes the comparison symmetric
        }

        BitcoinBlockHeaderAttestation that = (BitcoinBlockHeaderAttestation) other;

        return this.height - that.height;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof BitcoinBlockHeaderAttestation)) {
            return false;
        }

        BitcoinBlockHeaderAttestation that = (BitcoinBlockHeaderAttestation) other;

        if (!Arrays.equals(this._TAG(), that._TAG())) {
            return false;
        }

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
