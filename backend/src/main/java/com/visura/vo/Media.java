package com.visura.vo;

import com.visura.exception.custom.PhotoException;
import com.visura.exception.enums.problem.PhotoProblem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Arquivo de evidência: foto (JPEG ou PNG) ou narração em áudio (M4A, o MP4 só com som que o Android grava). O formato
 * vem do conteúdo, nunca do nome do arquivo nem do {@code Content-Type} enviados pelo cliente. No MP4, a API percorre
 * as caixas ({@code moov/trak/mdia/hdlr}) e só aceita o arquivo cujas faixas são todas de som: um vídeo é recusado.
 */
public record Media(byte[] content, String contentType, String extension) {

    public static final String JPEG_TYPE = "image/jpeg";
    public static final String PNG_TYPE = "image/png";
    public static final String AUDIO_TYPE = "audio/mp4";

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final String SHA_256 = "SHA-256";
    private static final String SOUND = "soun";
    /** Um MP4 de verdade tem poucas caixas por nível; o teto evita varrer um arquivo forjado com milhões delas. */
    private static final int MAX_BOXES = 4096;

    public Media {
        content = content.clone();
    }

    public static Media of(byte[] content) {
        if (content == null || content.length == 0) {
            throw new PhotoException(PhotoProblem.EMPTY);
        }
        if (starts(content, JPEG)) {
            return new Media(content, JPEG_TYPE, ".jpg");
        }
        if (starts(content, PNG)) {
            return new Media(content, PNG_TYPE, ".png");
        }
        if (audio(content)) {
            return new Media(content, AUDIO_TYPE, ".m4a");
        }
        throw new PhotoException(PhotoProblem.UNSUPPORTED);
    }

    @Override
    public byte[] content() {
        return content.clone();
    }

    public int size() {
        return content.length;
    }

    /** SHA-256 do conteúdo, gravado na evidência e conferido a cada download. */
    public byte[] hash() {
        return digest(content);
    }

    public static byte[] digest(byte[] content) {
        try {
            return MessageDigest.getInstance(SHA_256).digest(content);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static boolean starts(byte[] content, byte[] signature) {
        return content.length >= signature.length
                && Arrays.equals(content, 0, signature.length, signature, 0, signature.length);
    }

    /** MP4 ({@code ftyp} no início) com ao menos uma faixa e todas de som. */
    private static boolean audio(byte[] content) {
        if (content.length < 12 || !"ftyp".equals(type(content, 4))) {
            return false;
        }
        List<String> handlers = handlers(content);
        return !handlers.isEmpty() && handlers.stream().allMatch(SOUND::equals);
    }

    /** Os tipos de faixa ({@code hdlr}) de cada {@code moov/trak/mdia}; lista vazia se a estrutura não fecha. */
    private static List<String> handlers(byte[] content) {
        List<String> handlers = new ArrayList<>();
        for (Box moov : children(content, 0, content.length, "moov")) {
            for (Box trak : children(content, moov.start(), moov.end(), "trak")) {
                for (Box mdia : children(content, trak.start(), trak.end(), "mdia")) {
                    children(content, mdia.start(), mdia.end(), "hdlr").forEach(hdlr -> handlers.add(handler(content, hdlr)));
                }
            }
        }
        return handlers;
    }

    /** O tipo de faixa da caixa {@code hdlr}: depois de versão e flags (4 bytes) e de {@code pre_defined} (4 bytes). */
    private static String handler(byte[] content, Box hdlr) {
        return hdlr.end() - hdlr.start() >= 12 ? type(content, hdlr.start() + 8) : "";
    }

    /** As caixas do tipo pedido entre {@code from} e {@code to}; uma caixa com tamanho inválido encerra a leitura. */
    private static List<Box> children(byte[] content, int from, int to, String wanted) {
        List<Box> found = new ArrayList<>();
        int offset = from;
        for (int count = 0; count < MAX_BOXES && offset + 8 <= to; count++) {
            long size = Integer.toUnsignedLong(integer(content, offset));
            int header = 8;
            if (size == 1 && offset + 16 <= to) {
                size = wide(content, offset + 8);
                header = 16;
            } else if (size == 0) {
                size = to - offset;
            }
            if (size < header || offset + size > to) {
                break;
            }
            if (wanted.equals(type(content, offset + 4))) {
                found.add(new Box(offset + header, (int) (offset + size)));
            }
            offset += (int) size;
        }
        return found;
    }

    private static int integer(byte[] content, int offset) {
        return ((content[offset] & 0xFF) << 24) | ((content[offset + 1] & 0xFF) << 16)
                | ((content[offset + 2] & 0xFF) << 8) | (content[offset + 3] & 0xFF);
    }

    private static long wide(byte[] content, int offset) {
        return (Integer.toUnsignedLong(integer(content, offset)) << 32) | Integer.toUnsignedLong(integer(content, offset + 4));
    }

    private static String type(byte[] content, int offset) {
        return new String(content, offset, 4, StandardCharsets.ISO_8859_1);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Media media && Arrays.equals(content, media.content);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(content);
    }

    @Override
    public String toString() {
        return "Media[" + contentType + ", " + content.length + " bytes]";
    }

    /** O conteúdo de uma caixa MP4: de {@code start} (depois do cabeçalho) até {@code end}. */
    private record Box(int start, int end) {
    }
}
