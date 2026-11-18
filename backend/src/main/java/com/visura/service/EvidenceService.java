package com.visura.service;

import com.visura.database.entity.Evidence;
import com.visura.database.entity.Item;
import com.visura.database.entity.Room;
import com.visura.database.repository.EvidenceRepository;
import com.visura.dto.request.EvidenceRequest;
import com.visura.dto.response.EvidenceFile;
import com.visura.dto.response.EvidenceResponse;
import com.visura.exception.custom.InspectionException;
import com.visura.exception.custom.PhotoException;
import com.visura.exception.custom.StorageException;
import com.visura.exception.enums.problem.InspectionProblem;
import com.visura.exception.enums.problem.PhotoProblem;
import com.visura.exception.enums.problem.StorageProblem;
import com.visura.mapper.EvidenceMapper;
import com.visura.vo.Media;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.UUID;

/**
 * Evidências (fotos e narrações em áudio) dos ambientes das vistorias do usuário. O acesso ao ambiente é conferido antes de tocar no disco;
 * o arquivo é gravado fora de transação (sem prender conexão do banco durante a escrita) e o registro, numa transação
 * curta. Se o registro for recusado (vistoria finalizada, item de outro ambiente), o arquivo é apagado. O download
 * confere o SHA-256 gravado no envio.
 */
@Service
@RequiredArgsConstructor
public class EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final RoomService roomService;
    private final ItemService itemService;
    private final StorageService storageService;
    private final EvidenceMapper evidenceMapper;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public EvidenceResponse upload(UUID user, UUID room, EvidenceRequest request) {
        Room found = room(user, room);
        Item item = item(user, request.item());
        Media media = media(request.file());
        String path = path(media);
        return record(found, item, path, media, request.description());
    }

    @Transactional(readOnly = true)
    public EvidenceFile download(UUID user, UUID evidence) {
        Evidence found = owned(user, evidence);
        byte[] content = load(found);
        intact(found, content);
        return file(found, content);
    }

    @Transactional
    public void remove(UUID user, UUID evidence) {
        Evidence found = owned(user, evidence);
        discard(found);
    }

    private Room room(UUID user, UUID room) {
        return roomService.find(user, room);
    }

    private String path(Media media) {
        return storageService.save(media);
    }

    private EvidenceResponse save(Room room, Item item, String path, Media media, String description) {
        return show(evidenceRepository.saveAndFlush(create(room, item, path, media, description)));
    }

    private Evidence create(Room room, Item item, String path, Media media, String description) {
        return evidenceMapper.entity(room, item, path, media, clock.instant(), description);
    }

    private EvidenceResponse show(Evidence evidence) {
        return evidenceMapper.response(evidence);
    }

    private byte[] load(Evidence evidence) {
        return storageService.load(evidence.getPath());
    }

    private static EvidenceFile file(Evidence evidence, byte[] content) {
        return new EvidenceFile(evidence.getContentType(), content);
    }

    private void discard(Evidence evidence) {
        evidenceRepository.remove(evidence.getId());
    }

    private Item item(UUID user, UUID item) {
        if (item == null) {
            return null;
        }
        return itemService.find(user, item);
    }

    private static Media media(MultipartFile file) {
        try {
            return Media.of(file.getBytes());
        } catch (IOException exception) {
            throw new PhotoException(PhotoProblem.UNREADABLE, exception);
        }
    }

    /** Grava o registro numa transação curta; recusado, apaga o arquivo já gravado. */
    private EvidenceResponse record(Room room, Item item, String path, Media media, String description) {
        try {
            return transactionTemplate.execute(status -> save(room, item, path, media, description));
        } catch (RuntimeException exception) {
            storageService.delete(path);
            throw exception;
        }
    }

    /** Evidências antigas, sem hash gravado, não são conferidas. */
    private static void intact(Evidence evidence, byte[] content) {
        if (evidence.getHash() == null) {
            return;
        }
        if (!MessageDigest.isEqual(evidence.getHash(), Media.digest(content))) {
            throw new StorageException(StorageProblem.INTEGRITY);
        }
    }

    private Evidence owned(UUID user, UUID evidence) {
        return evidenceRepository.findOwned(user, evidence)
                .orElseThrow(() -> new InspectionException(InspectionProblem.EVIDENCE_NOT_FOUND));
    }
}
