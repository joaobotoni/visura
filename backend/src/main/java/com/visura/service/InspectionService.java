package com.visura.service;

import com.visura.database.entity.Inspection;
import com.visura.database.entity.Property;
import com.visura.database.entity.User;
import com.visura.database.repository.InspectionProgress;
import com.visura.database.repository.InspectionRepository;
import com.visura.dto.request.InspectionEditRequest;
import com.visura.dto.request.InspectionQuery;
import com.visura.dto.request.InspectionRequest;
import com.visura.dto.request.PageQuery;
import com.visura.dto.response.InspectionDetailResponse;
import com.visura.dto.response.InspectionResponse;
import com.visura.dto.response.PageResponse;
import com.visura.dto.response.ProgressResponse;
import com.visura.exception.custom.InspectionException;
import com.visura.exception.enums.problem.InspectionProblem;
import com.visura.mapper.InspectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Vistorias do usuário responsável. Vistoria de outro usuário, ou removida, responde 404. As regras de estado
 * (finalizada não muda, entrada válida, uma em andamento por imóvel) ficam no banco.
 */
@Service
@RequiredArgsConstructor
public class InspectionService {

    private static final Sort NEWEST = Sort.by(Sort.Direction.DESC, "id");

    private final InspectionRepository inspectionRepository;
    private final PropertyService propertyService;
    private final UserService userService;
    private final InspectionMapper inspectionMapper;

    @Transactional
    public InspectionResponse start(UUID user, InspectionRequest request) {
        User responsible = responsible(user);
        Property property = property(user, request.property());
        Inspection entry = entry(user, request.entry());
        Inspection inspection = save(responsible, property, entry, request);
        copy(inspection);
        return show(inspection);
    }

    @Transactional(readOnly = true)
    public InspectionDetailResponse view(UUID user, UUID inspection) {
        Inspection found = owned(user, inspection);
        return detail(found);
    }

    @Transactional(readOnly = true)
    public PageResponse<InspectionResponse> list(UUID user, InspectionQuery query, PageQuery page) {
        Page<Inspection> found = page(user, query, page);
        Map<Integer, ProgressResponse> progress = progress(found.getContent());
        return results(found, progress);
    }

    @Transactional
    public InspectionResponse edit(UUID user, UUID inspection, InspectionEditRequest request) {
        Inspection found = owned(user, inspection);
        amend(found, request);
        store(found);
        return show(found);
    }

    @Transactional
    public InspectionResponse finish(UUID user, UUID inspection) {
        Inspection found = owned(user, inspection);
        conclude(found);
        Inspection finished = owned(user, inspection);
        return show(finished);
    }

    @Transactional
    public void remove(UUID user, UUID inspection) {
        Inspection found = owned(user, inspection);
        discard(found);
    }

    @Transactional(readOnly = true)
    public Inspection find(UUID user, UUID inspection) {
        return owned(user, inspection);
    }

    private User responsible(UUID user) {
        return userService.find(user);
    }

    private Property property(UUID user, UUID property) {
        return propertyService.find(user, property);
    }

    private Inspection save(User user, Property property, Inspection entry, InspectionRequest request) {
        return inspectionRepository.save(create(user, property, entry, request));
    }

    private Inspection create(User user, Property property, Inspection entry, InspectionRequest request) {
        return inspectionMapper.entity(user, property, entry, request);
    }

    private void copy(Inspection inspection) {
        inspectionRepository.copy(inspection.getId());
    }

    private InspectionResponse show(Inspection inspection) {
        return inspectionMapper.response(inspection, measure(inspection));
    }

    private ProgressResponse measure(Inspection inspection) {
        return tally(List.of(inspection)).getOrDefault(inspection.getId(), ProgressResponse.NONE);
    }

    private InspectionDetailResponse detail(Inspection inspection) {
        return inspectionMapper.detail(inspection);
    }

    private Page<Inspection> page(UUID user, InspectionQuery query, PageQuery page) {
        return inspectionRepository.findAllOwned(user, query.property(), query.state(), pageable(page));
    }

    private static Pageable pageable(PageQuery page) {
        return PageRequest.of(page.page(), page.size(), NEWEST);
    }

    private PageResponse<InspectionResponse> results(Page<Inspection> page, Map<Integer, ProgressResponse> progress) {
        return PageResponse.of(page.map(inspection -> summary(inspection, progress)));
    }

    private InspectionResponse summary(Inspection inspection, Map<Integer, ProgressResponse> progress) {
        return inspectionMapper.response(inspection, progress.getOrDefault(inspection.getId(), ProgressResponse.NONE));
    }

    private Map<Integer, ProgressResponse> tally(List<Inspection> inspections) {
        return inspectionRepository.progress(ids(inspections)).stream()
                .collect(Collectors.toMap(InspectionProgress::getInspection, inspectionMapper::progress));
    }

    private static List<Integer> ids(List<Inspection> inspections) {
        return inspections.stream().map(Inspection::getId).toList();
    }

    private void amend(Inspection inspection, InspectionEditRequest request) {
        inspectionMapper.amend(request, inspection);
    }

    private void store(Inspection inspection) {
        inspectionRepository.saveAndFlush(inspection);
    }

    private void conclude(Inspection inspection) {
        inspectionRepository.finish(inspection.getId());
    }

    private void discard(Inspection inspection) {
        inspectionRepository.remove(inspection.getId());
    }

    private Map<Integer, ProgressResponse> progress(List<Inspection> inspections) {
        if (inspections.isEmpty()) {
            return Map.of();
        }
        return tally(inspections);
    }

    private Inspection entry(UUID user, UUID entry) {
        if (entry == null) {
            return null;
        }
        return inspectionRepository.findOwned(user, entry)
                .orElseThrow(() -> new InspectionException(InspectionProblem.ENTRY_NOT_FOUND));
    }

    private Inspection owned(UUID user, UUID inspection) {
        return inspectionRepository.findOwned(user, inspection)
                .orElseThrow(() -> new InspectionException(InspectionProblem.NOT_FOUND));
    }
}
