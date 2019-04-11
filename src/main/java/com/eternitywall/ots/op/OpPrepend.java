package com.eternitywall.ots.op;

import com.eternitywall.ots.Utils;

import java.util.Arrays;

/**
 * Prepend a prefix to a message.
 *
 * @see OpBinary
 */
public class OpPrepend extends OpBinary {

    public static final byte _TAG = (byte) 0xf1;

    @Override
    public byte _TAG() {
        return OpPrepend._TAG;
    }

    @Override
    public String _TAG_NAME() {
        return "prepend";
    }

    public OpPrepend() {
        super();
    }

    public OpPrepend(byte[] arg) {
        super(arg);
    }

    @Override
    public byte[] call(byte[] msg) {
        return Utils.arraysConcat(this.arg, msg);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OpPrepend)) {
            return false;
        }

        return Arrays.equals(this.arg, ((OpPrepend) other).arg);
    }
}
