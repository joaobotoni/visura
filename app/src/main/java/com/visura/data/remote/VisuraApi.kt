package com.visura.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Endpoints da API. A base (BuildConfig.API_BASE_URL) já termina em /api/1/.
// Os que respondem 204 devolvem Response<Unit> para não tentar ler um corpo vazio.
interface VisuraApi {

    // ---- Autenticação e usuário ----

    @POST("auth/register")
    suspend fun registrar(@Body corpo: RegisterRequest): AuthenticationResponse

    @POST("auth/login")
    suspend fun entrar(@Body corpo: LoginRequest): AuthenticationResponse

    @POST("auth/google")
    suspend fun entrarComGoogle(@Body corpo: GoogleLoginRequest): AuthenticationResponse

    @POST("auth/password/forgot")
    suspend fun pedirCodigo(@Body corpo: RecoveryRequest): Response<Unit>

    @POST("auth/password/verify")
    suspend fun conferirCodigo(@Body corpo: RecoveryCodeRequest): Response<Unit>

    @POST("auth/password/reset")
    suspend fun redefinirSenha(@Body corpo: ResetPasswordRequest): Response<Unit>

    @POST("auth/logout")
    suspend fun sair(): Response<Unit>

    @GET("users/me")
    suspend fun usuarioAtual(): UserResponse

    // ---- Empresas ----

    @POST("companies")
    suspend fun cadastrarEmpresa(@Body corpo: CompanyAccountRequest): CompanyResponse

    @GET("companies")
    suspend fun empresas(): List<CompanyResponse>

    // ---- Imóveis ----

    @POST("properties")
    suspend fun cadastrarImovel(@Body corpo: PropertyRequest): PropertyResponse

    @GET("properties")
    suspend fun imoveis(@Query("page") page: Int = 0, @Query("size") size: Int = 50): PageResponse<PropertyResponse>

    @GET("properties/{id}")
    suspend fun imovel(@Path("id") id: String): PropertyResponse

    @GET("properties/{id}/owners")
    suspend fun proprietarios(@Path("id") id: String): List<PersonSummaryResponse>

    @POST("properties/{id}/owners")
    suspend fun vincularProprietario(@Path("id") id: String, @Body corpo: OwnerRequest): Response<Unit>

    // ---- Pessoas ----

    @POST("people/individuals")
    suspend fun cadastrarPessoaFisica(@Body corpo: IndividualRequest): PersonResponse

    @POST("people/companies")
    suspend fun cadastrarPessoaJuridica(@Body corpo: PersonCompanyRequest): PersonResponse

    @GET("people")
    suspend fun pessoas(@Query("page") page: Int = 0, @Query("size") size: Int = 50): PageResponse<PersonResponse>

    // ---- Vistorias ----

    @POST("inspections")
    suspend fun iniciarVistoria(@Body corpo: InspectionRequest): InspectionResponse

    @GET("inspections")
    suspend fun vistorias(
        @Query("property") imovel: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): PageResponse<InspectionResponse>

    @GET("inspections/{id}")
    suspend fun vistoria(@Path("id") id: String): InspectionDetailResponse

    @POST("inspections/{id}/finish")
    suspend fun finalizarVistoria(@Path("id") id: String): InspectionResponse

    @GET("inspections/{id}/people")
    suspend fun pessoasDaVistoria(@Path("id") id: String): List<ParticipantResponse>

    @POST("inspections/{id}/people")
    suspend fun vincularPessoa(@Path("id") id: String, @Body corpo: ParticipantRequest): Response<Unit>

    @POST("inspections/{id}/rooms")
    suspend fun adicionarAmbiente(@Path("id") id: String, @Body corpo: RoomRequest): RoomResponse

    // ---- Ambientes, itens e evidências ----

    @POST("rooms/{id}/items")
    suspend fun adicionarItem(@Path("id") ambienteId: String, @Body corpo: ItemRequest): ItemResponse

    @PATCH("items/{id}")
    suspend fun alterarItem(@Path("id") id: String, @Body corpo: ItemRequest): ItemResponse

    @DELETE("rooms/{id}")
    suspend fun removerAmbiente(@Path("id") id: String): Response<Unit>

    @DELETE("items/{id}")
    suspend fun removerItem(@Path("id") id: String): Response<Unit>

    @Multipart
    @POST("rooms/{id}/evidences")
    suspend fun enviarEvidencia(
        @Path("id") ambienteId: String,
        @Part arquivo: MultipartBody.Part,
        @Part("item") item: RequestBody?
    ): EvidenceResponse

    @GET("evidences/{id}")
    suspend fun evidencia(@Path("id") id: String): ResponseBody

    // ---- Catálogo ----

    @GET("catalog/rooms")
    suspend fun tiposDeAmbiente(): List<RoomTypeResponse>
}
