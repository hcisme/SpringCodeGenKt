package io.github.hcisme.springcodegenkt.bean

import io.github.hcisme.springcodegenkt.utils.YamlConfigManager

object Constant {
    private const val PATH_KOTLIN = "kotlin"
    private const val PATH_RESOURCES = "resources"

    // io.github.hcisme.testcodegen
    val PACKAGE_BASE: String = YamlConfigManager.config.packageConfig.base

    // entity.pojo
    val PACKAGE_POJO: String = YamlConfigManager.config.packageConfig.pojo

    //  entity.param
    val PACKAGE_PARAM: String = YamlConfigManager.config.packageConfig.param

    val IGNORE_TABLE_PREFIX: Boolean = YamlConfigManager.config.ignoreTablePrefix
    val SUFFIX_BEAN_PARAM: String = YamlConfigManager.config.suffixBeanParam

    //  // io.github.hcisme.testcodegen.entity.pojo
    val FULL_POJO_PACKAGE_NAME: String = "$PACKAGE_BASE.$PACKAGE_POJO"

    //  // io.github.hcisme.testcodegen.entity.param
    val FULL_PARAM_PACKAGE_NAME: String = "$PACKAGE_BASE.$PACKAGE_PARAM"
    val PATH_BASE: String = YamlConfigManager.config.path.outputDir +
            "/" +
            PATH_KOTLIN +
            "/" +
            PACKAGE_BASE.replace(oldChar = '.', newChar = '/')
    val PATH_POJO: String = PATH_BASE + "/" + PACKAGE_POJO.replace(oldChar = '.', newChar = '/')
    val PATH_PARAM: String = PATH_BASE + "/" + PACKAGE_PARAM.replace(oldChar = '.', newChar = '/')
}

fun main() {
    println(Constant.PATH_POJO)
    println(Constant.PATH_PARAM)
}
