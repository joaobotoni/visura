package com.visura.service;

import com.visura.database.enums.InspectionType;
import com.visura.database.repository.InspectionRepository;
import com.visura.dto.request.InspectionRequest;
import com.visura.exception.custom.InspectionException;
import com.visura.exception.enums.problem.InspectionProblem;
import com.visura.mapper.InspectionMapper;
import com.visura.support.Controle;
import com.visura.support.Users;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Vistorias")
class InspectionServiceTest {

    private final InspectionRepository inspectionRepository = mock(InspectionRepository.class);
    private final InspectionService inspectionService = new InspectionService(inspectionRepository, mock(PropertyService.class),
            mock(UserService.class), mock(InspectionMapper.class));

    @Test
    @Controle
    @DisplayName("saída apontando a entrada de outro usuário é recusada antes de gravar")
    void foreignEntryIsRejected() {
        UUID entry = UUID.randomUUID();
        when(inspectionRepository.findOwned(Users.UUID, entry)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionService.start(Users.UUID,
                new InspectionRequest(UUID.randomUUID(), InspectionType.EXIT, entry, null)))
                .isInstanceOf(InspectionException.class)
                .extracting("problem").isEqualTo(InspectionProblem.ENTRY_NOT_FOUND);

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    @Controle
    @DisplayName("vistoria de outro usuário não é finalizada nem removida")
    void foreignInspectionIsUntouched() {
        UUID inspection = UUID.randomUUID();
        when(inspectionRepository.findOwned(Users.UUID, inspection)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionService.finish(Users.UUID, inspection)).isInstanceOf(InspectionException.class);
        assertThatThrownBy(() -> inspectionService.remove(Users.UUID, inspection)).isInstanceOf(InspectionException.class);

        verify(inspectionRepository, never()).finish(any());
        verify(inspectionRepository, never()).remove(any());
    }
}
