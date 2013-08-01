package org.sa.rainbow.management.ports;

public interface ITransmittableConverter {

    Object toTransmittable (Object o, Object context);

    Object fromTransmittable (Object o, Object context);

}
