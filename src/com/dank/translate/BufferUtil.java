package com.dank.translate;

/**
 * Created by Jamie on 4/15/2015.
 */
public class BufferUtil {

    static final char[] fieldA = new char[]{'\u20ac', '\u0000', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\u0000', '\u017d', '\u0000', '\u0000', '\u2018', '\u2019', '\u201c', '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\u0000', '\u017e', '\u0178'};

    public static String method11(byte[] var0, int var1, int var2, int var3) {

            char[] var4 = new char[var2];
            int var5 = 0;
            int var6 = var1;

            int var9;
            for (int var7 = var1 + var2; var6 < var7; var4[var5++] = (char) var9) {
                if (var3 >= '\uff00') {
                    throw new IllegalStateException();
                }

                int var8 = var0[var6++] & 255;
                if (var8 < 128) {
                    if (0 == var8) {
                        if (var3 >= '\uff00') {
                            throw new IllegalStateException();
                        }

                        var9 = '\ufffd';
                    } else {
                        var9 = var8;
                    }
                } else if (var8 < 192) {
                    if (var3 >= '\uff00') {
                        throw new IllegalStateException();
                    }

                    var9 = '\ufffd';
                } else if (var8 < 224) {
                    if (var3 >= '\uff00') {
                        throw new IllegalStateException();
                    }

                    if (var6 < var7 && (var0[var6] & 192) == 128) {
                        if (var3 >= '\uff00') {
                            throw new IllegalStateException();
                        }

                        var9 = (var8 & 31) << 6 | var0[var6++] & 63;
                        if (var9 < 128) {
                            if (var3 >= '\uff00') {
                                throw new IllegalStateException();
                            }

                            var9 = '\ufffd';
                        }
                    } else {
                        var9 = '\ufffd';
                    }
                } else if (var8 < 240) {
                    if (1 + var6 < var7) {
                        if (var3 >= '\uff00') {
                            throw new IllegalStateException();
                        }

                        if (128 == (var0[var6] & 192)) {
                            if (var3 >= '\uff00') {
                                throw new IllegalStateException();
                            }

                            if (128 == (var0[var6 + 1] & 192)) {
                                if (var3 >= '\uff00') {
                                    throw new IllegalStateException();
                                }

                                var9 = (var8 & 15) << 12 | (var0[var6++] & 63) << 6 | var0[var6++] & 63;
                                if (var9 < 2048) {
                                    var9 = '\ufffd';
                                }
                                continue;
                            }
                        }
                    }

                    var9 = '\ufffd';
                } else if (var8 < 248) {
                    if (var3 >= '\uff00') {
                        throw new IllegalStateException();
                    }

                    if (2 + var6 < var7) {
                        if (var3 >= '\uff00') {
                            throw new IllegalStateException();
                        }

                        if (128 == (var0[var6] & 192)) {
                            if (var3 >= '\uff00') {
                                throw new IllegalStateException();
                            }

                            if (128 == (var0[var6 + 1] & 192)) {
                                if (var3 >= '\uff00') {
                                    throw new IllegalStateException();
                                }

                                if ((var0[2 + var6] & 192) == 128) {
                                    if (var3 >= '\uff00') {
                                        throw new IllegalStateException();
                                    }

                                    var9 = (var8 & 7) << 18 | (var0[var6++] & 63) << 12 | (var0[var6++] & 63) << 6 | var0[var6++] & 63;
                                    if (var9 >= 65536) {
                                        if (var3 >= '\uff00') {
                                            throw new IllegalStateException();
                                        }

                                        if (var9 <= 1114111) {
                                            var9 = '\ufffd';
                                            continue;
                                        }

                                        if (var3 >= '\uff00') {
                                            throw new IllegalStateException();
                                        }
                                    }

                                    var9 = '\ufffd';
                                    continue;
                                }
                            }
                        }
                    }

                    var9 = '\ufffd';
                } else {
                    var9 = '\ufffd';
                }
            }

            return new String(var4, 0, var5);

    }

    public static String method37(byte[] var0, int var1, int var2, byte var3) {

            char[] var4 = new char[var2];
            int var5 = 0;

            for (int var6 = 0; var6 < var2; ++var6) {
                int var7 = var0[var6 + var1] & 255;
                if (var7 != 0) {
                    if (var7 >= 128 && var7 < 160) {
                        char var8 = fieldA[var7 - 128];
                        if (var8 == 0) {
                            var8 = 63;
                        }
                        var7 = var8;
                    }
                    var4[var5++] = (char) var7;
                }
            }
            return new String(var4, 0, var5);

    }

    public static int method132(byte[] var0, int var1, CharSequence var2, byte var3) {

            int var4 = var2.length();
            int var5 = var1;

            for (int var6 = 0; var6 < var4; ++var6) {
                char var7 = var2.charAt(var6);
                if (var7 <= 127) {
                    if (var3 >= 122) {
                        throw new IllegalStateException();
                    }

                    var0[var5++] = (byte) var7;
                } else if (var7 <= 2047) {
                    if (var3 >= 122) {
                        throw new IllegalStateException();
                    }

                    var0[var5++] = (byte) (192 | var7 >> 6);
                    var0[var5++] = (byte) (128 | var7 & 63);
                } else {
                    var0[var5++] = (byte) (224 | var7 >> 12);
                    var0[var5++] = (byte) (128 | var7 >> 6 & 63);
                    var0[var5++] = (byte) (128 | var7 & 63);
                }
            }

            return var5 - var1;

    }

    public static int getSerializedSize(CharSequence var0, int var1, int var2, byte[] var3, int var4, int var5) {

            int var6 = var2 - var1;

            for (int var7 = 0; var7 < var6; ++var7) {
                if (var5 >= -222863657) {
                    throw new IllegalStateException();
                }

                char var8;
                label224:
                {
                    var8 = var0.charAt(var1 + var7);
                    if (var8 > 0) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        if (var8 < 128) {
                            break label224;
                        }

                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }
                    }

                    if (var8 >= 160) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        if (var8 <= 255) {
                            if (var5 >= -222863657) {
                                throw new IllegalStateException();
                            }
                            break label224;
                        }
                    }

                    if (8364 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -128;
                    } else if (8218 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var7 + var4] = -126;
                    } else if (var8 == 402) {
                        var3[var4 + var7] = -125;
                    } else if (8222 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -124;
                    } else if (var8 == 8230) {
                        var3[var7 + var4] = -123;
                    } else if (var8 == 8224) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -122;
                    } else if (var8 == 8225) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var7 + var4] = -121;
                    } else if (var8 == 710) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var7 + var4] = -120;
                    } else if (8240 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -119;
                    } else if (var8 == 352) {
                        var3[var7 + var4] = -118;
                    } else if (var8 == 8249) {
                        var3[var4 + var7] = -117;
                    } else if (var8 == 338) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -116;
                    } else if (381 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -114;
                    } else if (var8 == 8216) {
                        var3[var4 + var7] = -111;
                    } else if (8217 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -110;
                    } else if (8220 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var7 + var4] = -109;
                    } else if (8221 == var8) {
                        var3[var4 + var7] = -108;
                    } else if (8226 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var7 + var4] = -107;
                    } else if (var8 == 8211) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var7 + var4] = -106;
                    } else if (var8 == 8212) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -105;
                    } else if (var8 == 732) {
                        var3[var7 + var4] = -104;
                    } else if (var8 == 8482) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -103;
                    } else if (353 == var8) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -102;
                    } else if (8250 == var8) {
                        var3[var7 + var4] = -101;
                    } else if (var8 == 339) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -100;
                    } else if (var8 == 382) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var4 + var7] = -98;
                    } else if (var8 == 376) {
                        if (var5 >= -222863657) {
                            throw new IllegalStateException();
                        }

                        var3[var7 + var4] = -97;
                    } else {
                        var3[var7 + var4] = 63;
                    }
                    continue;
                }

                var3[var4 + var7] = (byte) var8;
            }

            return var6;

    }

}
