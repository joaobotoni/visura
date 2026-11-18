package com.visura.support;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Arquivos MP4 mínimos, montados caixa a caixa, para testar a narração em áudio sem guardar binários no repositório.
 */
public final class Mp4 {

    private Mp4() {
    }

    /** Um MP4 mínimo: {@code ftyp} e as caixas dadas, na ordem. */
    public static byte[] mp4(byte[]... boxes) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes(box("ftyp", "M4A \0\0\0\0isomM4A ".getBytes(StandardCharsets.ISO_8859_1)));
        for (byte[] box : boxes) {
            out.writeBytes(box);
        }
        return out.toByteArray();
    }

    /** {@code moov} com uma {@code trak/mdia/hdlr} por tipo de faixa. */
    public static byte[] moov(String... handlers) {
        ByteArrayOutputStream traks = new ByteArrayOutputStream();
        traks.writeBytes(box("mvhd", new byte[100]));
        for (String handler : handlers) {
            byte[] hdlr = ByteBuffer.allocate(24).putInt(0).putInt(0).put(handler.getBytes(StandardCharsets.ISO_8859_1)).array();
            traks.writeBytes(box("trak", box("mdia", box("mdhd", new byte[24]), box("hdlr", hdlr))));
        }
        return box("moov", traks.toByteArray());
    }

    public static byte[] box(String type, byte[]... contents) {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        for (byte[] content : contents) {
            body.writeBytes(content);
        }
        return ByteBuffer.allocate(8 + body.size()).putInt(8 + body.size())
                .put(type.getBytes(StandardCharsets.ISO_8859_1)).put(body.toByteArray()).array();
    }
}
