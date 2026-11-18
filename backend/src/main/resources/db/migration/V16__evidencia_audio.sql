-- A evidência passa a aceitar a narração do vistoriador em áudio (M4A: MP4 só com faixas de som), além das fotos.
-- O formato é conferido pela API nos bytes do arquivo; aqui fica só a lista de tipos aceitos.
alter table vistorias.evidencia
    drop constraint ck_evidencia_tipo_arquivo,
    add constraint ck_evidencia_tipo_arquivo check (tipo_arquivo in ('image/jpeg', 'image/png', 'audio/mp4'));
