package io.github.hcisme.springcodegenkt.enums

enum class DateTimePatternEnum(val pattern: String) {
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),
    YYYY_MM_DD("yyyy-MM-dd"),
    YYYY_MM("yyyy-MM"),
    HH_MM_SS("HH:mm:ss");
}
