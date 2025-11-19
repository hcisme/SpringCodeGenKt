package io.github.hcisme.springcodegenkt.bean

/**
 * 表信息类
 */
data class TableInfo(
    /**
     * 表名
     */
    var tableName: String,

    /**
     * bean名称
     */
    var beanName: String,

    /**
     * 参数名称
     */
    var beanQueryName: String,

    /**
     * 表注释
     */
    var comment: String? = null,

    /**
     * 字段信息
     */
    var fieldList: List<FieldInfo> = emptyList(),

    /**
     * 唯一索引集合
     */
    var keyIndexMap: MutableMap<String, MutableList<FieldInfo>> = mutableMapOf(),

    /**
     * 是否有date类型
     */
    var haveDate: Boolean = false,

    /**
     * 是否有时间类型
     */
    var haveDateTime: Boolean = false,

    /**
     * 是否有bigdecimal类型
     */
    var haveBigDecimal: Boolean = false
)
