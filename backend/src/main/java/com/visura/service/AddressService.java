package com.visura.service;

import com.visura.database.entity.Address;
import com.visura.database.repository.AddressRepository;
import com.visura.dto.request.AddressRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;

    @Transactional
    public Address locate(AddressRequest request) {
        Integer address = register(request);
        return reference(address);
    }

    private Integer register(AddressRequest request) {
        return addressRepository.register(request.state(), request.city(), request.district(), request.cep().value(),
                request.street(), request.number(), request.complement());
    }

    private Address reference(Integer address) {
        return addressRepository.getReferenceById(address);
    }
}
