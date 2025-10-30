package com.visura.domain.exceptions.property

sealed class PropertyException(message: String, cause: Throwable? = null) :
    Exception(message, cause) {


    class IsBlankPropertyException(cause: Throwable? = null) :
        PropertyException("Por favor, selecione o tipo de imóvel", cause)

    class IsBlankCategoryPropertyException(cause: Throwable? = null) :
        PropertyException("Por favor, selecione a categoria de residência", cause)


}