package com.eternitywall.ots.attestation;

import com.eternitywall.ots.StreamDeserializationContext;
import com.eternitywall.ots.StreamSerializationContext;
import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Class representing {@link com.eternitywall.ots.Timestamp} signature verification
 */
public abstract class TimeAttestation implements Comparable<TimeAttestation> {

    public static final int _TAG_SIZE = 8;
    public static final int _MAX_PAYLOAD_SIZE = 8192;

    public byte[] _TAG;

    public byte[] _TAG() {
        return new byte[]{};
    }

    /**
     * Deserialize a general Time Attestation to the specific subclass Attestation.
     *
     * @param ctx The stream deserialization context.
     * @return The specific subclass Attestation.
     */
    public static TimeAttestation deserialize(StreamDeserializationContext ctx) {
        // console.log('attestation deserialize');

        byte[] tag = ctx.readBytes(_TAG_SIZE);
        // console.log('tag: ', com.eternitywall.ots.Utils.bytesToHex(tag));

        byte[] serializedAttestation = ctx.readVarbytes(_MAX_PAYLOAD_SIZE);
        // console.log('serializedAttestation: ', com.eternitywall.ots.Utils.bytesToHex(serializedAttestation));

        StreamDeserializationContext ctxPayload = new StreamDeserializationContext(serializedAttestation);

        /* eslint no-use-before-define: ["error", { "classes": false }] */
        if (Arrays.equals(tag, PendingAttestation._TAG)) {
            return PendingAttestation.deserialize(ctxPayload);
        } else if (Arrays.equals(tag, BitcoinBlockHeaderAttestation._TAG)) {
            return BitcoinBlockHeaderAttestation.deserialize(ctxPayload);
        } else if (Arrays.equals(tag, LitecoinBlockHeaderAttestation._TAG)) {
            return LitecoinBlockHeaderAttestation.deserialize(ctxPayload);
        } else if (Arrays.equals(tag, EthereumBlockHeaderAttestation._TAG)) {
            return EthereumBlockHeaderAttestation.deserialize(ctxPayload);
        }

        return new UnknownAttestation(tag, serializedAttestation);
    }

    /**
     * Serialize a a general Time Attestation to the specific subclass Attestation.
     *
     * @param ctx The output stream serialization context.
     */
    public void serialize(StreamSerializationContext ctx) {
        ctx.writeBytes(this._TAG());
        StreamSerializationContext ctxPayload = new StreamSerializationContext();
        serializePayload(ctxPayload);
        ctx.writeVarbytes(ctxPayload.getOutput());
    }

    public void serializePayload(StreamSerializationContext ctxPayload) {
        // TODO: Is this intentional?
    }

    @Override
    public int compareTo(TimeAttestation other) {
        int deltaTag = Utils.compare(this._TAG(), other._TAG());

        if (deltaTag == 0) {
            return this.compareTo(other);     // TODO: Wouldn't this be an infinite recursive call ???
        } else {
            return deltaTag;
        }
    }
}
