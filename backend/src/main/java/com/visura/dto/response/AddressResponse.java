package com.visura.dto.response;

import com.visura.vo.Cep;

public record AddressResponse(
        Cep cep,
        String state,
        String city,
        String district,
        String street,
        String number,
        String complement
) {
}
