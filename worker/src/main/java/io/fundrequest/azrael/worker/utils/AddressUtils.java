package io.fundrequest.azrael.worker.utils;

public final class AddressUtils {

    public static String prettify(final String address) {
        if (!address.startsWith("0x")) {
            return String.format("0x%s", address);
        } else {
            return address;
        }
    }
}
