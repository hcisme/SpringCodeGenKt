package io.github.hcisme.springcodegenkt.bean

/**
 * 字段信息类
 */
data class FieldInfo(
    /**
     * 字段名称
     */
    var fieldName: String,

    /**
     * bean属性名称
     */
    var propertyName: String,

    /**
     * SQL类型
     */
    var sqlType: String,

    /**
     * 字段类型
     */
    var ktType: String,

    /**
     * 字段备注
     */
    var comment: String? = null,

    /**
     * 字段是否是自增长
     */
    var autoIncrement: Boolean = false
)
