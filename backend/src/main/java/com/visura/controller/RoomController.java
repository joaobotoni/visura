package com.visura.controller;

import com.visura.dto.request.EvidenceRequest;
import com.visura.dto.request.ItemRequest;
import com.visura.dto.request.RoomEditRequest;
import com.visura.dto.response.EvidenceResponse;
import com.visura.dto.response.ItemResponse;
import com.visura.dto.response.RoomResponse;
import com.visura.principal.Principal;
import com.visura.service.EvidenceService;
import com.visura.service.ItemService;
import com.visura.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(path = "/rooms", version = "1")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final ItemService itemService;
    private final EvidenceService evidenceService;

    @PatchMapping("/{id}")
    public ResponseEntity<RoomResponse> edit(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                             @RequestBody @Valid RoomEditRequest request) {
        return ResponseEntity.ok(roomService.edit(principal.user(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Principal principal, @PathVariable UUID id) {
        roomService.remove(principal.user(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/evidences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceResponse> upload(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                                   @Valid EvidenceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(evidenceService.upload(principal.user(), id, request));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ItemResponse> add(@AuthenticationPrincipal Principal principal, @PathVariable UUID id,
                                            @RequestBody @Valid ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.add(principal.user(), id, request));
    }
}
