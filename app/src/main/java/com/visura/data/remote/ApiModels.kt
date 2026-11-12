package com.visura.data.remote

// Formatos JSON da API (backend/). Os nomes dos campos seguem os DTOs do Spring, em inglês.
// Campos de resposta são anuláveis porque o Gson não respeita a nulabilidade do Kotlin.

// ---- Autenticação ----

data class DeviceRequest(
    val identifier: String,
    val platform: String,
    val manufacturer: String,
    val model: String,
    val osVersion: String
)

data class LoginRequest(val email: String, val password: String, val device: DeviceRequest)

data class RegisterRequest(
    val name: String,
    val cpf: String,
    val email: String,
    val password: String,
    val device: DeviceRequest
)

data class RefreshRequest(val refreshToken: String)

data class UserResponse(val id: String?, val name: String?, val email: String?)

data class TokenResponse(val accessToken: String?, val refreshToken: String?, val expiresIn: Long?)

data class AuthenticationResponse(val user: UserResponse?, val token: TokenResponse?)

// ---- Listas paginadas ----

data class PageResponse<T>(val items: List<T>?, val page: Int?, val size: Int?, val total: Long?)

// ---- Empresas ----

data class CompanyAccountRequest(val name: String, val tradeName: String, val cnpj: String, val role: String)

data class CompanyResponse(val id: String?, val name: String?, val tradeName: String?, val cnpj: String?, val role: String?)

// ---- Imóveis ----

data class AddressRequest(
    val cep: String,
    val state: String,
    val city: String,
    val district: String,
    val street: String,
    val number: String,
    val complement: String?
)

data class PropertyRequest(
    val description: String?,
    val type: String,
    val category: String,
    val address: AddressRequest
)

data class AddressResponse(
    val cep: String?,
    val state: String?,
    val city: String?,
    val district: String?,
    val street: String?,
    val number: String?,
    val complement: String?
)

data class PropertyResponse(
    val id: String?,
    val description: String?,
    val type: String?,
    val category: String?,
    val address: AddressResponse?,
    val company: String?
)

data class OwnerRequest(val person: String)

// ---- Pessoas ----

data class IndividualRequest(val name: String, val cpf: String, val phone: String?, val email: String?)

data class PersonCompanyRequest(
    val name: String,
    val tradeName: String,
    val cnpj: String,
    val phone: String?,
    val email: String?
)

data class PersonResponse(
    val id: String?,
    val type: String?,
    val name: String?,
    val tradeName: String?,
    val document: String?,
    val phone: String?,
    val email: String?
)

data class PersonSummaryResponse(val id: String?, val type: String?, val name: String?, val document: String?)

// ---- Vistorias ----

data class InspectionRequest(val property: String, val type: String, val entry: String?, val notes: String?)

data class InspectionResponse(
    val id: String?,
    val property: String?,
    val type: String?,
    val status: String?,
    val entry: String?,
    val notes: String?,
    val startedAt: String?,
    val finishedAt: String?
)

data class InspectionDetailResponse(
    val id: String?,
    val property: String?,
    val type: String?,
    val status: String?,
    val entry: String?,
    val notes: String?,
    val startedAt: String?,
    val finishedAt: String?,
    val rooms: List<RoomResponse>?
)

data class ParticipantRequest(val person: String, val role: String)

data class ParticipantResponse(val person: PersonSummaryResponse?, val role: String?)

// ---- Ambientes, itens e evidências ----

data class RoomRequest(val name: String, val type: String?, val notes: String?)

data class RoomResponse(
    val id: String?,
    val name: String?,
    val type: String?,
    val notes: String?,
    val origin: String?,
    val items: List<ItemResponse>?,
    val evidences: List<EvidenceResponse>?
)

data class ItemRequest(
    val name: String,
    val type: String? = null,
    val description: String? = null,
    val condition: String? = null,
    val material: String? = null,
    val notes: String? = null
)

data class ItemResponse(
    val id: String?,
    val name: String?,
    val type: String?,
    val description: String?,
    val condition: String?,
    val material: String?,
    val notes: String?,
    val origin: String?
)

data class EvidenceResponse(
    val id: String?,
    val item: String?,
    val contentType: String?,
    val size: Int?,
    val description: String?,
    val receivedAt: String?
)

// ---- Catálogo ----

data class ItemTypeResponse(val id: String?, val name: String?, val standard: Boolean?)

data class RoomTypeResponse(val id: String?, val name: String?, val standard: Boolean?, val items: List<ItemTypeResponse>?)

// ---- Erros (RFC 9457) ----

data class FieldError(val field: String?, val message: String?)

data class ProblemResponse(
    val title: String?,
    val status: Int?,
    val detail: String?,
    val code: String?,
    val errors: List<FieldError>?
)
