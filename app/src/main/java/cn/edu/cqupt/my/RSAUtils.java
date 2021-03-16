package cn.edu.cqupt.my;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class RSAUtils {

    /**
     * Computes a^b mod c
     */

    private static int modpow(long a, long b, int c)
    {
        long res = 1;
        while(b > 0)
        {
            /* Need long multiplication else this will overflow... */
            if((b & 1)==1)
            {
                res = (res * a) % c;
            }
            b = b >> 1;
            a = (a * a) % c; /* Same deal here */
        }
        return (int)(res);
    }

    /**
     * Compute gcd(a, b)
     */
    int gcd(int a, int b)
    {
        int temp;
        while(b != 0)
        {
            temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * Compute n^-1 mod m by extended euclidian method
     */
    int inverse(int n, int modulus)
    {
        int a = n, b = modulus;
        int x = 0, y = 1, x0 = 1, y0 = 0, q, temp;
        while(b != 0)
        {
            q = a / b;
            temp = a % b;
            a = b;
            b = temp;
            temp = x;
            x = x0 - q * x;
            x0 = temp;
            temp = y;
            y = y0 - q * y;
            y0 = temp;
        }
        if(x0 < 0) x0 += modulus;
        return x0;
    }

    /**
     * Encode the message m using public exponent and modulus, c = m^e mod n
     */
    private int encode(int m, int e, int n)
    {
        return modpow(m, e, n);
    }

    /**
     * Decode cryptogram c using private exponent and public modulus, m = c^d mod n
     */
    private static int decode(int c, int d, int n)
    {
        return modpow(c, d, n);
    }

    /**
     * Encode the message of given length, using the public key (exponent, modulus)
     * The resulting array will be of size len/bytes, each index being the encryption
     * of "bytes" consecutive characters, given by m = (m1 + m2*128 + m3*128^2 + ..),
     * outCrypto = m^exponent mod modulus
     */
    public static void encodeMessage(int len, int bytes, char[] message,int[] outCrypto, int exponent, int modulus)
    {
        int x, i, j;
        for(i = 0; i < len; i += bytes)
        {
            x = 0;
            for(j = 0; j < bytes; j++) x += message[i + j] * (1 << (7 * j));
            outCrypto[i/bytes] = decode(x, exponent, modulus);
        }
        return ;
    }

    public static String getString(ByteBuffer buffer)
    {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try
        {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }
    /**
     * Decode the cryptogram of given length, using the private key (exponent, modulus)
     * Each encrypted packet should represent "bytes" characters as per encodeMessage.
     * The returned message will be of size len * bytes.
     */
    public static void decodeMessage(int len, int bytes, int[] cryptogram,char[] outSource, int exponent, int modulus)
    {
        int x, i, j;
        for(i = 0; i < len; i++)
        {
            x = decode(cryptogram[i], exponent, modulus);
            for(j = 0; j < bytes; j++)
            {
                outSource[i*bytes + j] = (char)((x >> (7 * j)) % 128);
            }
        }
        return ;
    }


}
