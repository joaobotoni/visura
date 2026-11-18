package com.visura.vo;

import com.visura.exception.custom.PhotoException;
import com.visura.exception.enums.problem.PhotoProblem;
import com.visura.support.Controle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.visura.support.Mp4.box;
import static com.visura.support.Mp4.moov;
import static com.visura.support.Mp4.mp4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Arquivo de evidência")
class MediaTest {

    @Test
    @Controle
    @DisplayName("reconhece JPEG e PNG pela assinatura dos bytes")
    void formatComesFromSignature() {
        Media jpeg = Media.of(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xDB});
        Media png = Media.of(new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0});

        assertThat(jpeg.contentType()).isEqualTo("image/jpeg");
        assertThat(jpeg.extension()).isEqualTo(".jpg");
        assertThat(png.contentType()).isEqualTo("image/png");
        assertThat(png.size()).isEqualTo(9);
    }

    @Test
    @Controle
    @DisplayName("reconhece a narração em M4A: MP4 só com faixa de som, com o moov no fim como o Android grava")
    void audioIsAccepted() {
        Media audio = Media.of(mp4(box("mdat", new byte[64]), moov("soun")));

        assertThat(audio.contentType()).isEqualTo("audio/mp4");
        assertThat(audio.extension()).isEqualTo(".m4a");
    }

    @Test
    @Controle
    @DisplayName("recusa MP4 com vídeo, MP4 sem faixa e MP4 com caixa de tamanho forjado")
    void otherMp4AreRejected() {
        assertUnsupported(mp4(moov("vide", "soun")));
        assertUnsupported(mp4(box("moov", new byte[0])));
        byte[] forged = mp4(moov("soun"));
        ByteBuffer.wrap(forged).putInt(24, Integer.MAX_VALUE); // tamanho do moov, logo depois do ftyp de 24 bytes
        assertUnsupported(forged);
        assertUnsupported("....ftypM4A ".getBytes(StandardCharsets.ISO_8859_1));
    }

    @Test
    @Controle
    @DisplayName("recusa HTML, SVG, GIF e assinaturas cortadas")
    void otherFormatsAreRejected() {
        for (String content : new String[]{"<html></html>", "<svg xmlns=\"http://www.w3.org/2000/svg\"/>", "GIF89a"}) {
            assertUnsupported(content.getBytes(StandardCharsets.UTF_8));
        }
        assertUnsupported(new byte[]{(byte) 0xFF, (byte) 0xD8});
    }

    @Test
    @Controle
    @DisplayName("recusa arquivo vazio")
    void emptyIsRejected() {
        assertThatThrownBy(() -> Media.of(new byte[0]))
                .isInstanceOf(PhotoException.class)
                .extracting("problem").isEqualTo(PhotoProblem.EMPTY);
    }

    @Test
    @Controle
    @DisplayName("o conteúdo não muda por fora depois de criado")
    void contentIsCopied() {
        byte[] bytes = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        Media media = Media.of(bytes);
        bytes[0] = 0;
        media.content()[1] = 0;

        assertThat(media.content()).containsExactly((byte) 0xFF, (byte) 0xD8, (byte) 0xFF);
    }

    private static void assertUnsupported(byte[] content) {
        assertThatThrownBy(() -> Media.of(content))
                .isInstanceOf(PhotoException.class)
                .extracting("problem").isEqualTo(PhotoProblem.UNSUPPORTED);
    }
}
