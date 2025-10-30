    package com.visura.domain.model.property

    import com.visura.domain.exceptions.property.PropertyException

    enum class PropertyCategoryType(val displayName: String) {
        HOME("Casa"),
        APARTMENT("Apartamento")
    }

    @JvmInline
    value class PropertyCategory(val type: PropertyCategoryType) {
        companion object {
            fun access(type: PropertyCategoryType?): Result<PropertyCategory> = runCatching {
                type?.let { PropertyCategory(it) }
                    ?: throw PropertyException.IsBlankPropertyException()
            }
        }
    }