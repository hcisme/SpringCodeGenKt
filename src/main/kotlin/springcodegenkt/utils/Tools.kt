package io.github.hcisme.springcodegenkt.utils

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object Tools {
    /**
     * 将 SQL 类型映射为 Kotlin 类型
     * @param sqlType SQL 数据类型
     * @param nullable 是否返回可空类型，默认为 true
     * @return 对应的 Kotlin 类型字符串
     */
    fun sqlTypeToKotlinType(sqlType: String, nullable: Boolean = true): String {
        val baseType = when (sqlType.uppercase()) {
            // 整数类型
            "SMALLINT", "SMALLINT UNSIGNED", "YEAR" -> "Short"
            "INT", "INT UNSIGNED", "INTEGER", "MEDIUMINT", "MEDIUMINT UNSIGNED", "TINYINT", "TINYINT UNSIGNED" -> "Int"
            "BIGINT", "BIGINT UNSIGNED" -> "Long"

            // 浮点类型
            "FLOAT", "FLOAT UNSIGNED" -> "Float"
            "DOUBLE", "DOUBLE UNSIGNED", "REAL" -> "Double"
            "DECIMAL", "NUMERIC" -> BigDecimal::class.java.simpleName

            // 字符串类型
            "CHAR", "VARCHAR", "TEXT", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT",
            "ENUM", "SET", "JSON" -> "String"

            // 二进制类型
            "BINARY", "VARBINARY", "BLOB", "TINYBLOB", "MEDIUMBLOB", "LONGBLOB" -> "ByteArray"

            // 日期时间类型
            "DATE" -> LocalDate::class.java.simpleName
            "TIME" -> LocalTime::class.java.simpleName
            "DATETIME", "TIMESTAMP" -> LocalDateTime::class.java.simpleName

            // 布尔类型
            "BIT", "BOOL", "BOOLEAN" -> "Boolean"
            else -> error("未知的 sql 类型: $sqlType")
        }

        // 添加可空标识
        return if (nullable && baseType != "ByteArray") "$baseType?" else baseType
    }

    /**
     * 检查 SQL 类型是否对应日期类型
     */
    fun isDateType(sqlType: String) = sqlType.uppercase() == "DATE"

    /**
     * 检查 SQL 类型是否对应日期时间类型
     */
    fun isDateTimeType(sqlType: String): Boolean {
        val upperType = sqlType.uppercase()
        return upperType == "DATETIME" || upperType == "TIMESTAMP"
    }

    /**
     * 检查 SQL 类型是否对应 BigDecimal 类型
     */
    fun isBigDecimalType(sqlType: String): Boolean {
        val upperType = sqlType.uppercase()
        return upperType == "DECIMAL" || upperType == "NUMERIC"
    }

    /**
     * 将 Kotlin 基本数据类型字符串转换为对应的 Java 类型字符串
     *
     * @param kotlinType Kotlin类型字符串 (例如: "Int", "String", "Boolean", "Int?")
     * @return 对应的 Java 类型字符串 (例如:  "java.lang.String", "boolean", "java.lang.Integer")
     */
    fun getJavaType(kotlinType: String): String {
        val typeName = kotlinType.trim().removeSuffix("?")

        return when (typeName) {
            "String" -> "java.lang.String"

            "Int" -> "java.lang.Integer"
            "Long" -> "java.lang.Long"
            "Short" -> "java.lang.Short"
            "Byte" -> "java.lang.Byte"

            "Double" -> "java.lang.Double"
            "Float" -> "java.lang.Float"

            "Boolean" -> "java.lang.Boolean"
            "Char" -> "java.lang.Character"

            else -> error("未识别的类型")
        }
    }
}