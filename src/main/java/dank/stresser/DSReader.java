package dank.stresser;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: DankWise
 * Date: 17-02-2015
 * Time: 21:38
 * Created by Dogerina.
 * Copyright under GPL license by Dogerina.
 */
public class DSReader {
    private static final int MAGIC = 0xCAFEBABE;
    private final DataInputStream dis;

    private DSReader(final String file) throws FileNotFoundException {
        this.dis = new DataInputStream(new FileInputStream(file));
    }

    public static void main(String... args) {
        System.out.println(keys("./hooks.ds"));
    }

    public static List<String> keys(final String file) {
        final List<String> keys = new ArrayList<>();
        try {
            final DSReader ds2 = new DSReader(file);
            final int magic = ds2.dis.readInt();
            if (magic != MAGIC) throw new RuntimeException("bad_patch");
            final int manifets = ds2.dis.readInt(); //gamepack manifest hashcode
            final int revision = ds2.dis.readInt();
            final int build = ds2.dis.readInt(); //updater build number
            final short classes = ds2.dis.readShort();
            for (int i = 0; i < classes; i++) {
                final byte type = ds2.dis.readByte();
                if (type != Type.CLASS) throw new RuntimeException("Unhandled type operand!");
                final String internal_class = ds2.dis.readUTF();
                final String defined_class = ds2.dis.readUTF();
                final short members = ds2.dis.readShort();
                for (int j = 0; j < members; j++) {
                    final byte member = ds2.dis.readByte();
                    switch (member) {
                        case Type.FIELD:
                            break;
                        case Type.METHOD:
                            break;
                        case Type.BROKEN:
                            break;
                        default:
                            System.out.println("We fucked up!");
                    }
                    final String owner = ds2.dis.readUTF();
                    final String name = ds2.dis.readUTF();
                    final String desc = ds2.dis.readUTF();
                    final String definedHookName = ds2.dis.readUTF();
                    final long multiplier = ds2.dis.readLong();
                    final boolean isStatic = ds2.dis.readBoolean();
                    if (defined_class.contains("BROKE") || definedHookName.contains("BROKE")) continue;
                    keys.add(defined_class + "" + definedHookName);
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    private interface Type {
        int CLASS = 0;
        int FIELD = 1;
        int METHOD = 2;
        int BROKEN = 9;
    }
}
