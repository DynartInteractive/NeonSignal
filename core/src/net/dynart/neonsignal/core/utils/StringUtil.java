package net.dynart.neonsignal.core.utils;

public class StringUtil {

    public static String camelize(String str, boolean first) {
        String input = str.toLowerCase();
        StringBuilder sb = new StringBuilder();
        final char delim = '_';
        char value;
        boolean capitalize = first;
        for (int i = 0; i < input.length(); ++i) {
            value = input.charAt(i);
            if (capitalize) {
                sb.append(Character.toUpperCase(value));
                capitalize = false;
            } else if (value == delim) {
                capitalize = true;
            } else {
                sb.append(value);
            }
        }
        return sb.toString();

    }

    public static String camelize(String str) {
        return camelize(str, true);
    }

    public static String capitalizeFirstChar(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
