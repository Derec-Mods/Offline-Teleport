package io.github.derexxd.offlineTeleport.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

final class Nbt {

    static final int END = 0;
    static final int BYTE = 1;
    static final int SHORT = 2;
    static final int INT = 3;
    static final int LONG = 4;
    static final int FLOAT = 5;
    static final int DOUBLE = 6;
    static final int BYTE_ARRAY = 7;
    static final int STRING = 8;
    static final int LIST = 9;
    static final int COMPOUND = 10;
    static final int INT_ARRAY = 11;
    static final int LONG_ARRAY = 12;

    private Nbt() {
    }

    record FileData(String name, Compound root) {
    }

    sealed interface Tag permits Compound, ListTag, ByteTag, ShortTag, IntTag, LongTag, FloatTag, DoubleTag, StringTag, ByteArrayTag, IntArrayTag, LongArrayTag {
    }

    record Compound(Map<String, Tag> tags) implements Tag {
        Compound {
            tags = tags instanceof LinkedHashMap<String, Tag> map ? map : new LinkedHashMap<>(tags);
        }

        void put(String name, Tag tag) {
            tags.put(name, tag);
        }
    }

    record ListTag(int type, List<Tag> values) implements Tag {
        ListTag {
            values = values instanceof ArrayList<Tag> list ? list : new ArrayList<>(values);
        }
    }

    record ByteTag(byte value) implements Tag {
    }

    record ShortTag(short value) implements Tag {
    }

    record IntTag(int value) implements Tag {
    }

    record LongTag(long value) implements Tag {
    }

    record FloatTag(float value) implements Tag {
    }

    record DoubleTag(double value) implements Tag {
    }

    record StringTag(String value) implements Tag {
    }

    record ByteArrayTag(byte[] value) implements Tag {
    }

    record IntArrayTag(int[] value) implements Tag {
    }

    record LongArrayTag(long[] value) implements Tag {
    }

    static FileData readCompressed(Path path) throws IOException {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(path))))) {
            int type = in.readUnsignedByte();
            if (type != COMPOUND) {
                throw new IOException("Root NBT tag is not a compound");
            }
            return new FileData(in.readUTF(), (Compound) readPayload(COMPOUND, in));
        }
    }

    static void writeCompressed(Path path, FileData data) throws IOException {
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(Files.newOutputStream(temporary))))) {
            out.writeByte(COMPOUND);
            out.writeUTF(data.name());
            writePayload(data.root(), out);
        }
        try {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Tag readPayload(int type, DataInput in) throws IOException {
        return switch (type) {
            case BYTE -> new ByteTag(in.readByte());
            case SHORT -> new ShortTag(in.readShort());
            case INT -> new IntTag(in.readInt());
            case LONG -> new LongTag(in.readLong());
            case FLOAT -> new FloatTag(in.readFloat());
            case DOUBLE -> new DoubleTag(in.readDouble());
            case BYTE_ARRAY -> {
                byte[] data = new byte[in.readInt()];
                in.readFully(data);
                yield new ByteArrayTag(data);
            }
            case STRING -> new StringTag(in.readUTF());
            case LIST -> {
                int elementType = in.readUnsignedByte();
                int length = in.readInt();
                if (length < 0) {
                    throw new IOException("Negative NBT list length");
                }
                List<Tag> values = new ArrayList<>(length);
                for (int i = 0; i < length; i++) {
                    values.add(readPayload(elementType, in));
                }
                yield new ListTag(elementType, values);
            }
            case COMPOUND -> {
                Map<String, Tag> tags = new LinkedHashMap<>();
                while (true) {
                    int childType = in.readUnsignedByte();
                    if (childType == END) {
                        break;
                    }
                    tags.put(in.readUTF(), readPayload(childType, in));
                }
                yield new Compound(tags);
            }
            case INT_ARRAY -> {
                int[] data = new int[in.readInt()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = in.readInt();
                }
                yield new IntArrayTag(data);
            }
            case LONG_ARRAY -> {
                long[] data = new long[in.readInt()];
                for (int i = 0; i < data.length; i++) {
                    data[i] = in.readLong();
                }
                yield new LongArrayTag(data);
            }
            default -> throw new IOException("Unknown NBT tag type: " + type);
        };
    }

    private static void writePayload(Tag tag, DataOutput out) throws IOException {
        switch (tag) {
            case ByteTag(byte value) -> out.writeByte(value);
            case ShortTag(short value) -> out.writeShort(value);
            case IntTag(int value) -> out.writeInt(value);
            case LongTag(long value) -> out.writeLong(value);
            case FloatTag(float value) -> out.writeFloat(value);
            case DoubleTag(double value) -> out.writeDouble(value);
            case ByteArrayTag(byte[] value) -> {
                out.writeInt(value.length);
                out.write(value);
            }
            case StringTag(String value) -> out.writeUTF(value);
            case ListTag(int elementType, List<Tag> values) -> {
                out.writeByte(elementType);
                out.writeInt(values.size());
                for (Tag value : values) {
                    writePayload(value, out);
                }
            }
            case Compound(Map<String, Tag> tags) -> {
                for (Map.Entry<String, Tag> entry : tags.entrySet()) {
                    out.writeByte(typeOf(entry.getValue()));
                    out.writeUTF(entry.getKey());
                    writePayload(entry.getValue(), out);
                }
                out.writeByte(END);
            }
            case IntArrayTag(int[] value) -> {
                out.writeInt(value.length);
                for (int item : value) {
                    out.writeInt(item);
                }
            }
            case LongArrayTag(long[] value) -> {
                out.writeInt(value.length);
                for (long item : value) {
                    out.writeLong(item);
                }
            }
        }
    }

    private static int typeOf(Tag tag) {
        return switch (tag) {
            case ByteTag ignored -> BYTE;
            case ShortTag ignored -> SHORT;
            case IntTag ignored -> INT;
            case LongTag ignored -> LONG;
            case FloatTag ignored -> FLOAT;
            case DoubleTag ignored -> DOUBLE;
            case ByteArrayTag ignored -> BYTE_ARRAY;
            case StringTag ignored -> STRING;
            case ListTag ignored -> LIST;
            case Compound ignored -> COMPOUND;
            case IntArrayTag ignored -> INT_ARRAY;
            case LongArrayTag ignored -> LONG_ARRAY;
        };
    }
}
