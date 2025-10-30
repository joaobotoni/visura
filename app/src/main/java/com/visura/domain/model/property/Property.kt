    package com.visura.domain.model.property

    import com.visura.domain.exceptions.property.PropertyException
    enum class PropertyType(val displayName: String) {
        RESIDENTIAL("Residencial"),
        NON_RESIDENTIAL("Não Residencial"),
        COMMERCIAL("Comercial")
    }

    @JvmInline
    value class Property(val type: PropertyType) {
        companion object {
            fun access(type: PropertyType?): Result<Property> = runCatching {
                type?.let { Property(it) }
                    ?: throw PropertyException.IsBlankCategoryPropertyException()
            }
        }
    }