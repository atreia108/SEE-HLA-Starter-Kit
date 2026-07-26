package org.see.skf.runtime.models;

import hla.rti1516_2025.encoding.DecoderException;
import hla.rti1516_2025.encoding.EncoderFactory;
import hla.rti1516_2025.encoding.HLAfixedArray;
import hla.rti1516_2025.encoding.HLAfloat64LE;
import org.see.skf.encoding.Coder;

public final class Vector3Coder implements Coder<Vector3> {

    public HLAfixedArray<HLAfloat64LE> vector;

    public Vector3Coder(EncoderFactory encoderFactory) {
        this.vector = encoderFactory.createHLAfixedArray(
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE(),
                encoderFactory.createHLAfloat64LE());
    }

    @Override
    public byte[] encode(Vector3 data) {
        this.vector.get(0).setValue(data.getX());
        this.vector.get(1).setValue(data.getY());
        this.vector.get(2).setValue(data.getZ());

        return vector.toByteArray();
    }

    @Override
    public Vector3 decode(byte[] data) throws DecoderException {
        vector.decode(data);

        return new Vector3(vector.get(0).getValue(),
                vector.get(1).getValue(),
                vector.get(2).getValue());
    }
}
