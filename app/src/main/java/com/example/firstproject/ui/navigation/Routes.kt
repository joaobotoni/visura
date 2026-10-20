package com.example.firstproject.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val FORGOT_PASSWORD_EMAIL = "forgot_password_email"
    const val FORGOT_PASSWORD_CODE = "forgot_password_code"
    const val FORGOT_PASSWORD_NEW = "forgot_password_new"
    const val CREATE_ACCOUNT_INFO = "create_account_info"
    const val CREATE_ACCOUNT_DOCS = "create_account_docs"
    const val GOOGLE_ACCOUNT_PICKER = "google_account_picker"
    const val HOME = "home"
    const val IMOVEIS = "imoveis"
    const val IMOVEL_DETALHE = "imovel_detalhe/{imovelId}"
    const val NOVO_IMOVEL = "novo_imovel"
    const val NOVO_IMOVEL_TIPO = "novo_imovel_tipo"
    const val NOVA_VISTORIA = "nova_vistoria/{imovelId}"
    const val PESSOAS_VINCULADAS = "pessoas_vinculadas/{imovelId}"

    fun imovelDetalhe(imovelId: String) = "imovel_detalhe/$imovelId"
    fun novaVistoria(imovelId: String) = "nova_vistoria/$imovelId"
    fun pessoasVinculadas(imovelId: String) = "pessoas_vinculadas/$imovelId"
}