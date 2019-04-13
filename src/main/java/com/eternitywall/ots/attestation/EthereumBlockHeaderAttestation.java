package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Ethereum Block Header Attestation.
 *
 * @see TimeAttestation
 */
public class EthereumBlockHeaderAttestation extends TimeAttestation {

    public static final byte[] _TAG = {(byte) 0x30, (byte) 0xfe, (byte) 0x80, (byte) 0x87, (byte) 0xb5, (byte) 0xc7, (byte) 0xea, (byte) 0xd7};
    public static final String chain = "ethereum";

    private int height;

    public byte[] _TAG() {
        return EthereumBlockHeaderAttestation._TAG;
    }

    public int getHeight() {
        return height;
    }

    EthereumBlockHeaderAttestation(int height) {
        super();
        this.height = height;
    }

    public static EthereumBlockHeaderAttestation deserialize(StreamDeserializationContext ctxPayload) {
        int height = ctxPayload.readVaruint();

        return new EthereumBlockHeaderAttestation(height);
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeVaruint(this.height);
    }

    public String toString() {
        return "EthereumBlockHeaderAttestation(" + this.height + ")";
    }

    public int compareTo(TimeAttestation other) {
        int deltaTag = Utils.compare(this._TAG(), other._TAG());

        if (deltaTag != 0) {
            return deltaTag;
        }

        if (!(other instanceof EthereumBlockHeaderAttestation)) {
            // This is very unlikely, but possible, since UnknownAttestation can have any tag
            return this.getClass().getName().compareTo(other.getClass().getName());   // This makes the comparison symmetric
        }

        EthereumBlockHeaderAttestation that = (EthereumBlockHeaderAttestation) other;

        return this.height - that.height;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof EthereumBlockHeaderAttestation)) {
            return false;
        }

        EthereumBlockHeaderAttestation that = (EthereumBlockHeaderAttestation) other;

        return this.height == that.height;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this._TAG()) ^ this.height;
    }
}
