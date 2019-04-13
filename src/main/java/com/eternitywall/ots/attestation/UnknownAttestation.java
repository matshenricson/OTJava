package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Placeholder for attestations that don't support
 *
 * @see TimeAttestation
 */
public class UnknownAttestation extends TimeAttestation {

    byte[] payload;
    public byte[] _TAG;

    public byte[] _TAG() {
        return _TAG;
    }

    UnknownAttestation(byte[] tag, byte[] payload) {
        this._TAG = tag;
        this.payload = payload;
    }

    @Override
    public void serializePayload(StreamSerializationContext ctx) {
        ctx.writeBytes(this.payload);
    }

    public static UnknownAttestation deserialize(StreamDeserializationContext ctxPayload, byte[] tag) {
        byte[] payload = ctxPayload.readVarbytes(_MAX_PAYLOAD_SIZE);

        return new UnknownAttestation(tag, payload);
    }

    public String toString() {
        return "UnknownAttestation " + Utils.bytesToHex(this._TAG()) + ' ' + Utils.bytesToHex(this.payload);
    }

    public int compareTo(TimeAttestation other) {
        int deltaTag = Utils.compare(this._TAG(), other._TAG());

        if (deltaTag != 0) {
            return deltaTag;
        }

        if (!(other instanceof UnknownAttestation)) {
            // This is very unlikely, but possible, since UnknownAttestation can have any tag
            return this.getClass().getName().compareTo(other.getClass().getName());   // This makes the comparison symmetric
        }

        UnknownAttestation that = (UnknownAttestation) other;

        return Utils.compare(this.payload, that.payload);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof UnknownAttestation)) {
            return false;
        }

        UnknownAttestation that = (UnknownAttestation) other;

        if (!Arrays.equals(this._TAG(), that._TAG())) {
            return false;
        }

        return Arrays.equals(this.payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this._TAG()) ^ Arrays.hashCode(this.payload);
    }
}
