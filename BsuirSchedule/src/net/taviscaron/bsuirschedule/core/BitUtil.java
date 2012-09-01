package net.taviscaron.bsuirschedule.core;

public class BitUtil {
    public static int encode(int[] bitIndexes) {
        int result = 0;
        for (int i = 0; i < bitIndexes.length; i++) {
            result |= 1 << bitIndexes[i];
        }
        return result;
    }
    
    public static Integer[] decode(int bits) {
        int count = 0;
        Integer[] buff = new Integer[32];
        for (int i = 0; i < buff.length; i++) {
            if ((bits & (1 << i)) > 0) {
                buff[count++] = i;
            }
        }
        
        Integer[] result = new Integer[count];
        System.arraycopy(buff, 0, result, 0, count);
        return result;
    }
}
