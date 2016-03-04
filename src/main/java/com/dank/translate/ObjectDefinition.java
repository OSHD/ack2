package com.dank.translate;

public class ObjectDefinition {

    public int[] transformIds;
    public int[] fieldAc;
    public int fieldU = 491254494;
    public int id;
    public int hintIconId = -1980752465;
    public int sizeX = 185953465;
    public int sizeY = -1904935735;
    public boolean fieldAl = true;
    public boolean fieldJ = true;
    public int fieldG = -305171085;
    public boolean fieldR = false;
    public int wallThickness = 257546160;
    public String[] actions = new String[5];
    public int fieldAj = 0;
    public int animationId = 2118472049;
    public String name = "null";
    public boolean fieldAt = false;
    public int fieldAo = -2007857941;
    public int fieldAk = -1061618737;
    public int fieldAf = 0;
    public int fieldAb = 0;
    public int objectIcon = 1110127897;
    int[] fieldK;
    int[] fieldS;
    short[] fieldD;
    short[] fieldQ;
    short[] fieldX;
    short[] fieldO;
    int fieldAa = 0;
    int fieldY = 703996935;
    int fieldAx = 193203328;
    int fieldAz = 0;
    boolean fieldP = false;
    int fieldAn = 0;
    int fieldAs = 0;
    boolean fieldAv = false;
    int transVarpbit = -977731905;
    int fieldAm = 447958400;
    int fieldAi = 0;
    boolean fieldAw = false;
    int transVarp = -1597778363;
    int fieldAq = -1845417856;
    public boolean fieldA;


    void finalize(int var1) {


            if (this.fieldG * 243222597 == -1) {

                this.fieldG = 0;

                if (this.fieldS != null) {
                    label75:
                    {
                        if (null != this.fieldK) {
                            if (this.fieldK[0] != 10) {
                                break label75;
                            }
                        }
                        this.fieldG = 305171085; // = 1
                    }
                }

                for (int var2 = 0; var2 < 5; ++var2) {
                    if (null != this.actions[var2]) {
                        this.fieldG = 305171085; // = 1
                    }
                }

            }

            if (-1 == this.fieldAo * 2025716797) {
                byte var10001;
                if (0 != this.fieldU * 2025817487) {
                    var10001 = 1;
                } else {
                    var10001 = 0;
                }

                this.fieldAo = var10001 * 2007857941;
            }


    }


    void read(Buffer var1, int opcode, short DUMMY) {

            int var4;
            int var5;
            if (opcode == 1) {
                var4 = var1.readUByte(-1997388176);
                if (var4 > 0) {
                    if (null != this.fieldS) {
                        if (!fieldA) {
                            var1.caret += var4 * -298511637;
                            return;
                        }
                    }
                    this.fieldK = new int[var4];
                    this.fieldS = new int[var4];
                    for (var5 = 0; var5 < var4; ++var5) {
                        this.fieldS[var5] = var1.readUShort(829603351);
                        this.fieldK[var5] = var1.readUByte(-546628317);
                    }
                }
            } else if (opcode == 2) {
                this.name = var1.readString(1007067529);
            } else if (opcode == 5) {
                var4 = var1.readUByte(-1787316748);
                if (var4 > 0) {
                    if (null != this.fieldS) {
                        if (!fieldA) {
                            var1.caret += var4 * -199007758;
                            return;
                        }
                    }
                    this.fieldK = null;
                    this.fieldS = new int[var4];
                    for (var5 = 0; var5 < var4; ++var5) {
                        this.fieldS[var5] = var1.readUShort(829603351);
                    }
                }
            } else if (opcode == 14) {
                this.sizeX = var1.readUByte(-1436261962) * 185953465;
            } else if (opcode == 15) {
                this.sizeY = var1.readUByte(-754453220) * -1904935735;
            } else if (opcode == 17) {
                this.fieldU = 0;
                this.fieldJ = false;
            } else if (18 == opcode) {
                this.fieldJ = false;
            } else if (opcode == 19) {
                this.fieldG = var1.readUByte(-1087007986) * 305171085;
            } else if (21 == opcode) {
                this.fieldY = 0;
            } else if (22 == opcode) {
                this.fieldP = true;
            } else if (23 == opcode) {
                this.fieldR = true;
            } else if (opcode == 24) {
                this.animationId = var1.readUShort(829603351) * -2118472049;
                if (this.animationId * 1563734127 == '\uffff') {
                    this.animationId = 2118472049;
                }
            } else if (opcode == 27) {
                this.fieldU = -1901856401;
            } else if (opcode == 28) {
                this.wallThickness = var1.readUByte(-586672174) * -1862951557;
            } else if (opcode == 29) {
                this.fieldAn = var1.readByte(250344736) * 1980664045;
            } else if (39 == opcode) {
                this.fieldAs = var1.readByte(250344736) * -674849363;
            } else if (opcode >= 30 && opcode < 35) {
                this.actions[opcode - 30] = var1.readString(-323863011);
                if (this.actions[opcode - 30].equalsIgnoreCase("Hidden")) {
                    this.actions[opcode - 30] = null;
                }
            } else if (40 == opcode) {
                var4 = var1.readUByte(-588887670);
                this.fieldD = new short[var4];
                this.fieldX = new short[var4];
                for (var5 = 0; var5 < var4; ++var5) {
                    this.fieldD[var5] = (short) var1.readUShort(829603351);
                    this.fieldX[var5] = (short) var1.readUShort(829603351);
                }
            } else if (41 == opcode) {
                var4 = var1.readUByte(577119050);
                this.fieldO = new short[var4];
                this.fieldQ = new short[var4];
                for (var5 = 0; var5 < var4; ++var5) {
                    this.fieldO[var5] = (short) var1.readUShort(829603351);
                    this.fieldQ[var5] = (short) var1.readUShort(829603351);
                }
            } else if (60 == opcode) {
                this.hintIconId = var1.readUShort(829603351) * 1980752465;
            } else if (62 == opcode) {
                this.fieldAv = true;
            } else if (opcode == 64) {
                this.fieldAl = false;
            } else if (opcode == 65) {
                this.fieldAq = var1.readUShort(829603351) * 656671313;
            } else if (66 == opcode) {
                this.fieldAm = var1.readUShort(829603351) * -1405786469;
            } else if (67 == opcode) {
                this.fieldAx = var1.readUShort(829603351) * -1810429927;
            } else if (opcode == 68) {
                this.objectIcon = var1.readUShort(829603351) * -1110127897;
            } else if (69 == opcode) {
                var1.readUByte(-2112979704);
            } else if (70 == opcode) {
                this.fieldAz = var1.j((byte) 1) * 982375195;
            } else if (opcode == 71) {
                this.fieldAi = var1.j((byte) 1) * 1986767173;
            } else if (72 == opcode) {
                this.fieldAa = var1.j((byte) 1) * -1562876439;
            } else if (opcode == 73) {
                this.fieldAt = true;
            } else if (opcode == 74) {
                this.fieldAw = true;
            } else if (opcode == 75) {
                this.fieldAo = var1.readUByte(425037362) * 2007857941;
            } else if (77 == opcode) {
                this.transVarpbit = var1.readUShort(829603351) * 977731905;
                if ('\uffff' == this.transVarpbit * -700019007) {
                    this.transVarpbit = -977731905;
                }
                this.transVarp = var1.readUShort(829603351) * 1597778363;
                if (this.transVarp * 2011902835 == '\uffff') {
                    this.transVarp = -1597778363;
                }

                var4 = var1.readUByte(184259764);
                this.transformIds = new int[var4 + 1];

                for (var5 = 0; var5 <= var4; ++var5) {
                    this.transformIds[var5] = var1.readUShort(829603351);
                    if (this.transformIds[var5] == '\uffff') {
                        this.transformIds[var5] = -1;
                    }
                }
            } else if (78 == opcode) {
                this.fieldAk = var1.readUShort(829603351) * 1061618737;
                this.fieldAj = var1.readUByte(635965400) * -2021113321;
            } else if (opcode == 79) {
                this.fieldAf = var1.readUShort(829603351) * 1020280521;
                this.fieldAb = var1.readUShort(829603351) * 438333831;
                this.fieldAj = var1.readUByte(-1821177953) * -2021113321;
                var4 = var1.readUByte(-992663997);
                this.fieldAc = new int[var4];
                for (var5 = 0; var5 < var4; ++var5) {
                    this.fieldAc[var5] = var1.readUShort(829603351);
                }
            } else if (opcode == 81) {
                this.fieldY = var1.readUByte(-317863526) * 165411072;
            } else {
                throw new InternalError("Unknown opcode:" + opcode);
            }


    }

    void read(Buffer var1, int var2) {

            while (true) {
                int var3 = var1.readUByte(-885783755);
                if (var3 == 0) {
                    return;
                }
                this.read(var1, var3, (short) 321);
            }

    }

}

