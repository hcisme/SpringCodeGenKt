package io.github.hcisme.springcodegenkt.bean

import io.github.hcisme.springcodegenkt.utils.YamlConfigManager

object Constant {
    private const val SOURCE_DIR_KOTLIN = "kotlin"
    private const val SOURCE_DIR_RESOURCES = "resources"
    val IGNORE_TABLE_PREFIX: Boolean = YamlConfigManager.config.ignore.ignoreTablePrefix
    val SUFFIX_BEAN_QUERY: String = YamlConfigManager.config.suffixBeanParam
    val SUFFIX_BEAN_QUERY_FUZZY: String = YamlConfigManager.config.suffixBeanParamFuzzy
    val SUFFIX_BEAN_QUERY_TIME_START: String = YamlConfigManager.config.suffixBeanParamTimeStart
    val SUFFIX_BEAN_QUERY_TIME_END: String = YamlConfigManager.config.suffixBeanParamTimeEnd
    val SUFFIX_MAPPER: String = YamlConfigManager.config.suffixMapper
    val DATE_FORMAT_EXPRESSION: String = YamlConfigManager.config.dateFormatConfig.expression
    val DATE_FORMAT_CLASS: String = YamlConfigManager.config.dateFormatConfig.importClass

    /**
     * D:/code/kotlin/CodeGen/TestCodeGen/src/main
     */
    val OUTPUT_DIR = YamlConfigManager.config.path.outputDir

    /**
     * io.github.hcisme.testcodegen
     */
    val BASE_PACKAGE: String = YamlConfigManager.config.packageConfig.base

    // ============================================================包名
    /**
     * entity.pojo
     */
    val POJO_PACKAGE: String = YamlConfigManager.config.packageConfig.pojo

    /**
     * entity.query
     */
    val QUERY_PACKAGE: String = YamlConfigManager.config.packageConfig.query

    /**
     * entity.enums
     */
    val ENUMS_PACKAGE: String = YamlConfigManager.config.packageConfig.enums

    /**
     * mappers
     */
    val MAPPERS_PACKAGE: String = YamlConfigManager.config.packageConfig.mappers

    /**
     * utils
     */
    val UTILS_PACKAGE: String = YamlConfigManager.config.packageConfig.utils

    // ============================================================完整包名
    /**
     * io.github.hcisme.testcodegen + entity.pojo
     */
    val FULL_POJO_PACKAGE: String = "$BASE_PACKAGE.$POJO_PACKAGE"

    /**
     * io.github.hcisme.testcodegen + entity.query
     */
    val FULL_PARAM_PACKAGE: String = "$BASE_PACKAGE.$QUERY_PACKAGE"

    /**
     * io.github.hcisme.testcodegen + entity.enums
     */
    val FULL_ENUMS_PACKAGE: String = "$BASE_PACKAGE.$ENUMS_PACKAGE"

    /**
     * io.github.hcisme.testcodegen + utils
     */
    val FULL_UTILS_PACKAGE: String = "$BASE_PACKAGE.$UTILS_PACKAGE"

    /**
     * io.github.hcisme.testcodegen + mappers
     */
    val FULL_MAPPERS_PACKAGE: String = "$BASE_PACKAGE.$MAPPERS_PACKAGE"

    // ============================================================完整的路径
    val FULL_PACKAGE_BASE_PATH: String = "$OUTPUT_DIR/$SOURCE_DIR_KOTLIN/${BASE_PACKAGE.replace('.', '/')}"
    val FULL_XML_PACKAGE_BASE_PATH: String = "$OUTPUT_DIR/$SOURCE_DIR_RESOURCES/${BASE_PACKAGE.replace('.', '/')}"

    val FULL_POJO_PATH: String = "$FULL_PACKAGE_BASE_PATH/${POJO_PACKAGE.replace('.', '/')}"
    val FULL_QUERY_PATH: String = "$FULL_PACKAGE_BASE_PATH/${QUERY_PACKAGE.replace('.', '/')}"
    val FULL_ENUMS_PATH: String = "$FULL_PACKAGE_BASE_PATH/${ENUMS_PACKAGE.replace('.', '/')}"
    val FULL_UTILS_PATH: String = "$FULL_PACKAGE_BASE_PATH/${UTILS_PACKAGE.replace('.', '/')}"
    val FULL_MAPPERS_PATH: String = "$FULL_PACKAGE_BASE_PATH/${MAPPERS_PACKAGE.replace('.', '/')}"

    val FULL_XML_MAPPERS_PATH: String = "$FULL_XML_PACKAGE_BASE_PATH/${MAPPERS_PACKAGE.replace('.', '/')}"
}

fun main() {
    println(Constant.FULL_XML_MAPPERS_PATH)
}
