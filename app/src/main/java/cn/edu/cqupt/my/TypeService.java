package cn.edu.cqupt.my;

public class TypeService {

    /**
     * 整数转C++存放格式的字节数组
     *
     * @param v int
     * @return byte[]
     */
    public static byte[] int2Byte4C(int v) {
        byte[] b = new byte[4];
//注意,不是java中的顺序的 b[0],b[1],b[2],b[3]
        b[3] = (byte) ((v >>> 24) & 0xFF);
        b[2] = (byte) ((v >>> 16) & 0xFF);
        b[1] = (byte) ((v >>> 8) & 0xFF);
        b[0] = (byte) ((v >>> 0) & 0xFF);
        return b;
    }

    public static String byteToBit(byte b) {
        return ""
                + (byte) ((b >> 7) & 0x1) + (byte) ((b >> 6) & 0x1)
                + (byte) ((b >> 5) & 0x1) + (byte) ((b >> 4) & 0x1)
                + (byte) ((b >> 3) & 0x1) + (byte) ((b >> 2) & 0x1)
                + (byte) ((b >> 1) & 0x1) + (byte) ((b >> 0) & 0x1);
    }
    /**
     * C++存放格式的数组转整数
     *
     * @param b byte[]
     * @return int
     */
    public static int byte2Int4C(byte[] b) {
        return byte2Int(b, 0);

    }

    /**
     * C++存放格式的数组转整数
     *
     * @param b     byte[]
     * @param index int 指定的数组的起始索引位置
     * @return int
     */
    public static int byte2Int4C(byte[] b, int index) {
//注意,不是java中的顺序的 b[0],b[1],b[2],b[3]
        return (b[index + 3] & 0xff) << 24 |
                (b[index + 2] & 0xff) << 16 |
                (b[index + 1] & 0xff) << 8 |
                b[index + 0] & 0xff;

/**
 下面这个是错的 ，我被坑了 ， 当时照抄 的 java.io.DataInputStream.readInt();
 if ((ch1 | ch2 | ch3 | ch4) < 0) {
 return 0;
 }
 return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

 */

    }

    /**
     * 整数转java存放格式的字节数组
     *
     * @param v int
     * @return byte[]
     */
    public static byte[] int2Byte(int v) {
        byte[] b = new byte[4];
        b[0] = (byte) ((v >>> 24) & 0xFF);
        b[1] = (byte) ((v >>> 16) & 0xFF);
        b[2] = (byte) ((v >>> 8) & 0xFF);
        b[3] = (byte) ((v >>> 0) & 0xFF);
        return b;
    }

    /**
     * Java存放格式的数组转整数
     *
     * @param b byte[]
     * @return int
     */
    public static int byte2Int(byte[] b) {
        return byte2Int(b, 0);
    }

    /**
     * Java存放格式的数组转整数
     *
     * @param b     byte[]
     * @param index int 指定的数组的起始索引位置
     * @return int
     */
    public static int byte2Int(byte[] b, int index) {
        return (b[index + 0] & 0xff) << 24 |
                (b[index + 1] & 0xff) << 16 |
                (b[index + 2] & 0xff) << 8 |
                b[index + 3] & 0xff;
/**
 下面这个是错的 ，我被坑了 ， 当时照抄 的 java.io.DataInputStream.readInt();
 if ((ch1 | ch2 | ch3 | ch4) < 0) {
 return 0;
 }
 return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));

 */

    }

}