package com.dank.translate;

import java.math.BigInteger;

public class Buffer{

    public static int[] fieldI = new int[256];
    static int fieldBa;
    static int[][][] fieldO;
    public int caret;
    public byte[] payload;


    static {
        for (int var1 = 0; var1 < 256; ++var1) {
            int var0 = var1;

            for (int var2 = 0; var2 < 8; ++var2) {
                if (1 == (var0 & 1)) {
                    var0 = var0 >>> 1 ^ -306674912;
                } else {
                    var0 >>>= 1;
                }
            }

            fieldI[var1] = var0;
        }

    }

    public Buffer(int var1) {
        this.payload = new byte[var1];
        this.caret = 0;
    }

    public void o(int var1, byte var2) {

            if (var1 >= 0 && var1 < 128) {
                if (var2 == 4) {
                    this.a(var1, -73130070);
                }
            } else if (var1 >= 0 && var1 < '\u8000') {
                if (var2 != 4) {
                    throw new IllegalStateException();
                } else {
                    this.writeShort('\u8000' + var1, 1185922173);
                }
            } else {
                throw new IllegalArgumentException();
            }

    }

    public void a(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;

    }

    public void decodeXTEA(int[] keys, int src, int dest, int var4) {

            int var5 = this.caret * 651432265;
            this.caret = -99503879 * src;
            int var6 = (dest - src) / 8;
            for (int var7 = 0; var7 < var6; ++var7) {
                int var8 = this.readInt((byte) -10);
                int var9 = this.readInt((byte) 23);
                int var10 = -957401312;
                int var11 = -1640531527;
                for (int var12 = 32; var12-- > 0; var8 -= var9 + (var9 << 4 ^ var9 >>> 5) ^ var10 + keys[var10 & 3]) {
                    var9 -= (var8 << 4 ^ var8 >>> 5) + var8 ^ var10 + keys[var10 >>> 11 & 3];
                    var10 -= var11;
                }
                this.caret -= -796031032;
                this.writeInt(var8, 999573322);
                this.writeInt(var9, 81645829);
            }
            this.caret = -99503879 * var5;

    }

    public void writeInt(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 24);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 16);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;

    }

    public void l(long var1) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) (var1 >> 56));
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) (var1 >> 48));
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) (var1 >> 40));
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) (var1 >> 32));
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) (var1 >> 24));
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) (var1 >> 16));
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) (var1 >> 8));
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) ((int) var1);

    }

    public void w(String var1, byte var2) {
        int var3 = var1.indexOf((char)0);
        if (var3 < 0) {
            this.caret += BufferUtil.getSerializedSize(var1, 0, var1.length(), this.payload, this.caret * 651432265, -1058997203) * -99503879;
            this.payload[(this.caret += -99503879) * 651432265 - 1] = 0;
        }

    }

    public void s(CharSequence var1, int var2) {

            int var4 = var1.length();
            int var5 = 0;

            for (int var6 = 0; var6 < var4; ++var6) {
                if (var2 <= 1354039107) {
                    throw new IllegalStateException();
                }

                char var7 = var1.charAt(var6);
                if (var7 <= 127) {
                    if (var2 <= 1354039107) {
                        return;
                    }

                    ++var5;
                } else if (var7 <= 2047) {
                    if (var2 <= 1354039107) {
                        throw new IllegalStateException();
                    }

                    var5 += 2;
                } else {
                    var5 += 3;
                }
            }

            this.payload[(this.caret += -99503879) * 651432265 - 1] = 0;
            this.q(var5, -335923327);
            this.caret += BufferUtil.method132(this.payload, this.caret * 651432265, var1, (byte) 67) * -99503879;

    }

    public void d(int var1, int var2) {

            this.payload[this.caret * 651432265 - var1 - 2] = (byte) (var1 >> 8);
            this.payload[this.caret * 651432265 - var1 - 1] = (byte) var1;

    }

    public void q(int var1, int var2) {

            if ((var1 & -128) != 0) {
                if (var2 >= 1714288368) {
                    throw new IllegalStateException();
                }

                if ((var1 & -16384) != 0) {
                    if ((var1 & -2097152) != 0) {
                        if ((var1 & -268435456) != 0) {
                            this.a(var1 >>> 28 | 128, 1667883517);
                        }

                        this.a(var1 >>> 21 | 128, 1554594597);
                    }

                    this.a(var1 >>> 14 | 128, 598142604);
                }

                this.a(var1 >>> 7 | 128, 623569023);
            }

            this.a(var1 & 127, 1192746364);

    }

    public byte readByte(int var1) {

            return this.payload[(this.caret += -99503879) * 651432265 - 1];

    }

    public void k(byte[] var1, int var2, int var3, int var4) {

            for (int var5 = var2; var5 < var2 + var3; ++var5) {
                if (var4 <= -1890510567) {
                    return;
                }

                this.payload[(this.caret += -99503879) * 651432265 - 1] = var1[var5];
            }


    }

    public int j(byte var1) {

            this.caret += -199007758;
            int var2 = (this.payload[this.caret * 651432265 - 1] & 255) + ((this.payload[this.caret * 651432265 - 2] & 255) << 8);
            if (var2 > 32767) {
                if (var1 != 1) {
                    throw new IllegalStateException();
                }

                var2 -= 65536;
            }

            return var2;

    }

    public int readInt(byte var1) {

            this.caret += -398015516;
            return (this.payload[this.caret * 651432265 - 1] & 255) + ((this.payload[this.caret * 651432265 - 3] & 255) << 16) + ((this.payload[this.caret * 651432265 - 4] & 255) << 24) + ((this.payload[this.caret * 651432265 - 2] & 255) << 8);

    }

    public long readLong(int var1) {

            long var2 = (long) this.readInt((byte) -33) & 4294967295L;
            long var4 = (long) this.readInt((byte) 38) & 4294967295L;
            return var4 + (var2 << 32);

    }

    public String r(short var1) {

            if (0 == this.payload[this.caret * 651432265]) {
                if (var1 == 402) {
                    throw new IllegalStateException();
                } else {
                    this.caret += -99503879;
                    return null;
                }
            } else {
                return this.readString(-132095391);
            }

    }

    public String readString(int var1) {

            int var2 = this.caret * 651432265;

            while (this.payload[(this.caret += -99503879) * 651432265 - 1] != 0) {
                ;
            }

            int var3 = this.caret * 651432265 - var2 - 1;
            return var3 == 0 ? "" : BufferUtil.method37(this.payload, var2, var3, (byte) 23);

    }

    public String c(int var1) {

            byte var2 = this.payload[(this.caret += -99503879) * 651432265 - 1];
            if (0 != var2) {
                throw new IllegalStateException("");
            } else {
                int var3 = this.caret * 651432265;

                do {
                    if (this.payload[(this.caret += -99503879) * 651432265 - 1] == 0) {
                        int var4 = this.caret * 651432265 - var3 - 1;
                        if (0 == var4) {
                            return "";
                        }

                        return BufferUtil.method37(this.payload, var3, var4, (byte) -3);
                    }
                } while (var1 == 1400990560);

                throw new IllegalStateException();
            }

    }

    public String an(int var1) {

            byte var2 = this.payload[(this.caret += -99503879) * 651432265 - 1];
            if (0 != var2) {
                if (var1 >= 1625359820) {
                    throw new IllegalStateException();
                } else {
                    throw new IllegalStateException("");
                }
            } else {
                int var3 = this.av((short) 857);
                if (this.caret * 651432265 + var3 > this.payload.length) {
                    if (var1 >= 1625359820) {
                        throw new IllegalStateException();
                    } else {
                        throw new IllegalStateException("");
                    }
                } else {
                    String var4 = BufferUtil.method11(this.payload, this.caret * 651432265, var3, -1717716777);
                    this.caret += var3 * -99503879;
                    return var4;
                }
            }

    }

    public void read(byte[] dest, int pos, int len, byte DUMMY) {

            for (int var5 = pos; var5 < pos + len; ++var5) {

                dest[var5] = this.payload[(this.caret += -99503879) * 651432265 - 1];
            }


    }

    public int readImmediateSmart(int var1) {

            int var2 = this.payload[this.caret * 651432265] & 0xff;
            return var2 < 128 ? this.readUByte(-1334522422) : this.readUShort(829603351) - '\u8000';

    }

    public int ae(int var1) {

            byte woah = this.payload[this.caret * 651432265];
            return woah < 0 ? this.readInt((byte) -13) & Integer.MAX_VALUE : this.readUShort(829603351);


    }

    public int av(short var1) {

            byte var2 = this.payload[(this.caret += -99503879) * 651432265 - 1];
            int var3;
            for (var3 = 0; var2 < 0; var2 = this.payload[(this.caret += -99503879) * 651432265 - 1]) {
                var3 = (var3 | var2 & 0x7f) << 7;
            }
            return var3 | var2;

    }

    public void encXTEA(int[] keys, int src, int dest, byte var4) {

            int var5 = this.caret * 651432265;
            this.caret = -99503879 * src;
            int var6 = (dest - src) / 8;
            for (int var7 = 0; var7 < var6; ++var7) {
                int var8 = this.readInt((byte) 46);
                int var9 = this.readInt((byte) 49);
                int var10 = 0;
                int var11 = -1640531527;
                for (int var12 = 32; var12-- > 0; var9 += (var8 << 4 ^ var8 >>> 5) + var8 ^ keys[var10 >>> 11 & 3] + var10) {
                    var8 += var9 + (var9 << 4 ^ var9 >>> 5) ^ var10 + keys[var10 & 3];
                    var10 += var11;
                }
                this.caret -= -796031032;
                this.writeInt(var8, 1906168111);
                this.writeInt(var9, 1684608592);
            }
            this.caret = var5 * -99503879;

    }

    public void applyRSA(BigInteger exp, BigInteger mod, int var3) {

            int var4 = this.caret * 651432265;
            this.caret = 0;
            byte[] var5 = new byte[var4];
            this.read(var5, 0, var4, (byte) 26);
            BigInteger var6 = new BigInteger(var5);
            BigInteger var7 = var6.modPow(exp, mod);
            byte[] var8 = var7.toByteArray();
            this.caret = 0;
            this.writeShort(var8.length, 900899610);
            this.k(var8, 0, var8.length, 778436199);

    }

    public int writeCrc(int caret, int var2) {

            byte[] var4 = this.payload;
            int var5 = this.caret * 651432265;
            int var6 = -1;
            for (int var7 = caret; var7 < var5; ++var7) {
                var6 = var6 >>> 8 ^ fieldI[(var6 ^ var4[var7]) & 255];
            }
            var6 = ~var6;
            this.writeInt(var6, -2028267446);
            return var6;

    }

    public boolean matchCrcs(short var1) {

            this.caret -= -398015516;
            byte[] var3 = this.payload;
            int var4 = this.caret * 651432265;
            int var5 = -1;
            int var6;
            for (var6 = 0; var6 < var4; ++var6) {
                var5 = var5 >>> 8 ^ fieldI[(var5 ^ var3[var6]) & 255];
            }
            var5 = ~var5;
            var6 = this.readInt((byte) 90);
            return var5 == var6;


    }

    public void ai(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (128 + var1);

    }

    public void aa(int var1, byte var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (0 - var1);


    }

    public void writeInvertedByte(int var1, byte var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (128 - var1);

    }

    public int ao(byte var1) {

            return 0 - this.payload[(this.caret += -99503879) * 651432265 - 1] & 255;

    }

    public int readInvertedUByte(byte var1) {

            return 128 - this.payload[(this.caret += -99503879) * 651432265 - 1] & 255;

    }

    public byte readInvertedByte(byte var1) {

            return (byte) (128 - this.payload[(this.caret += -99503879) * 651432265 - 1]);

    }

    public void aj(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (128 + var1);

    }

    public int ab(int var1) {

            this.caret += -199007758;
            return ((this.payload[this.caret * 651432265 - 1] & 0xff) << 8) + (this.payload[this.caret * 651432265 - 2] & 0xff);

    }

    public int ac(byte var1) {

            this.caret += -199007758;
            return ((this.payload[this.caret * 651432265 - 2] & 0xff) << 8)
                    + (this.payload[this.caret * 651432265 - 1] - 128 & 0xff);


    }

    public void f(int var1, byte var2) {

            this.payload[this.caret * 651432265 - var1 - 4] = (byte) (var1 >> 24);
            this.payload[this.caret * 651432265 - var1 - 3] = (byte) (var1 >> 16);
            this.payload[this.caret * 651432265 - var1 - 2] = (byte) (var1 >> 8);
            this.payload[this.caret * 651432265 - var1 - 1] = (byte) var1;

    }

    public int ay(byte var1) {

            this.caret += -199007758;
            int var2 = (this.payload[this.caret * 651432265 - 1] - 128 & 255) + ((this.payload[this.caret * 651432265 - 2] & 255) << 8);
            if (var2 > 32767) {
                var2 -= 65536;
            }

            return var2;

    }

    public void bu(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 16);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;

    }

    public void bi(int var1, byte var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 24);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 16);

    }

    public void bs(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 16);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 24);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);

    }

    public int bl(int var1) {

            this.caret += -398015516;
            return ((this.payload[this.caret * 651432265 - 1] & 0xff) << 24)
                    + ((this.payload[this.caret * 651432265 - 2] & 0xff) << 16)
                    + ((this.payload[this.caret * 651432265 - 3] & 0xff) << 8)
                    + (this.payload[this.caret * 651432265 - 4] & 0xff);

    }

    public int bw(int var1) {

            this.caret += -398015516;
            return (this.payload[this.caret * 651432265 - 2] & 0xff)
                    + ((this.payload[this.caret * 651432265 - 1] & 0xff) << 8)
                    + ((this.payload[this.caret * 651432265 - 3] & 0xff) << 24)
                    + ((this.payload[this.caret * 651432265 - 4] & 0xff) << 16);

    }

    public void read(byte[] var1, int var2, int var3, int var4) {

            for (int var5 = var2; var5 < var3 + var2; ++var5) {
                var1[var5] = (byte) (this.payload[(this.caret += -99503879) * 651432265 - 1] - 128);
            }


    }



    public void writeShort(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;

    }

    public int readUByte(int var1) {

            return this.payload[(this.caret += -99503879) * 651432265 - 1] & 0xff;

    }

    public void x(int var1, byte var2) {

            this.payload[this.caret * 651432265 - var1 - 1] = (byte) var1;


    }

    public int g(int var1) {

            this.caret += -298511637;
            return (this.payload[this.caret * 651432265 - 1] & 255) + ((this.payload[this.caret * 651432265 - 2] & 255) << 8) + ((this.payload[this.caret * 651432265 - 3] & 255) << 16);

    }

    public void ak(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);

    }

    public int ah(int var1) {

            int var2 = this.payload[this.caret * 651432265] & 255;
            if (var2 < 128) {
                if (var1 <= -425154278) {
                    throw new IllegalStateException();
                } else {
                    return this.readUByte(-1821389322) - 64;
                }
            } else {
                return this.readUShort(829603351) - '\uc000';
            }

    }

    public void bo(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 16);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 24);

    }

    public int aw(byte var1) {

            return this.payload[(this.caret += -99503879) * 651432265 - 1] - 128 & 255;

    }

    public void i(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 16);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) var1;

    }

    public byte ad(int var1) {

            return (byte) (0 - this.payload[(this.caret += -99503879) * 651432265 - 1]);

    }

    public int readUShort(int var1) {

            this.caret += -199007758;
            return ((this.payload[this.caret * 651432265 - 2] & 255) << 8) + (this.payload[this.caret * 651432265 - 1] & 255);

    }

    public void af(int var1, int var2) {

            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 + 128);
            this.payload[(this.caret += -99503879) * 651432265 - 1] = (byte) (var1 >> 8);

    }

    public void n(String var1, int var2) {

            int var3 = var1.indexOf(0);
            if (var3 >= 0) {
                throw new IllegalArgumentException("");
            } else {
                this.payload[(this.caret += -99503879) * 651432265 - 1] = 0;
                this.caret += BufferUtil.getSerializedSize(var1, 0, var1.length(), this.payload, this.caret * 651432265, -813083808) * -99503879;
                this.payload[(this.caret += -99503879) * 651432265 - 1] = 0;
            }

    }

    public int ap(int var1) {

            this.caret += -199007758;
            return ((this.payload[this.caret * 651432265 - 1] & 255) << 8) + (this.payload[this.caret * 651432265 - 2] - 128 & 255);

    }

    public int bp(int var1) {

            this.caret += -398015516;
            return ((this.payload[this.caret * 651432265 - 1] & 255) << 16) + ((this.payload[this.caret * 651432265 - 2] & 255) << 24) + ((this.payload[this.caret * 651432265 - 4] & 255) << 8) + (this.payload[this.caret * 651432265 - 3] & 255);

    }
}
