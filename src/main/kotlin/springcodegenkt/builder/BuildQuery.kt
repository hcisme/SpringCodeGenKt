package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.FieldInfo
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.enums.DateTimePatternEnum
import io.github.hcisme.springcodegenkt.utils.writeLine
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File

object BuildQuery {
    private val logger = LoggerFactory.getLogger(BuildPojo::class.java)

    fun execute(tableInfo: TableInfo) {
        val paramDir = File(Constant.FULL_QUERY_PATH).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val paramFile = File(paramDir, "${tableInfo.beanQueryName}.kt")

        paramFile.bufferedWriter(Charsets.UTF_8).use { bw ->
            runCatching {
                // 写入包声明和导入
                bw.writePackageAndImports(tableInfo)
                // 写入类注释和类定义开始
                bw.writeClassHeader(tableInfo)
                // 写入字段
                bw.writeFields(tableInfo.fieldList)
                // 写入 toString 方法
                bw.writeToStringMethod(tableInfo.fieldList)
                // 写入类结束
                bw.writeLine("}")
                bw.flush()
            }.onFailure { e ->
                logger.error("生成 POJO 类 ${tableInfo.beanName} 过程中发生错误", e)
            }
        }
    }

    private fun BufferedWriter.writePackageAndImports(tableInfo: TableInfo) {
        writeLine("package ${Constant.FULL_PARAM_PACKAGE}")
        newLine()

        val imports = mutableSetOf<String>().apply {
            if (tableInfo.haveDate || tableInfo.haveDateTime) add(Constant.DATE_FORMAT_CLASS)
            if (tableInfo.haveDate) add("java.time.LocalDate")
            if (tableInfo.haveDateTime) add("java.time.LocalDateTime")
            if (tableInfo.haveBigDecimal) add("java.math.BigDecimal")
        }

        imports.sorted().forEach { import ->
            writeLine("import $import")
        }
        newLine()
    }

    private fun BufferedWriter.writeClassHeader(tableInfo: TableInfo) {
        tableInfo.comment?.let {
            createClassComment("$it 查询对象")
            newLine()
        }

        writeLine("class ${tableInfo.beanQueryName} {")
    }

    private fun BufferedWriter.writeFields(fieldList: List<FieldInfo>) {
        fieldList.forEach { field ->
            writeField(field)
            newLine()
        }
    }

    private fun BufferedWriter.writeField(field: FieldInfo) {
        field.comment?.let { comment ->
            createFieldComment(comment)
            newLine()
        }
        val annotationText = getDateTimeFormatAnnotation(field)
        annotationText?.let { writeLine("\t$it") }

        writeLine("\tvar ${field.propertyName}: ${field.ktType} = null")

        // String类型参数
        if (field.ktType.contains("String")) {
            newLine()
            writeLine("\tvar ${field.propertyName}${Constant.SUFFIX_BEAN_QUERY_FUZZY}: ${field.ktType} = null")
        }

        // 时间类型的参数
        annotationText?.let {
            newLine()
            writeLine("\tvar ${field.propertyName}${Constant.SUFFIX_BEAN_QUERY_TIME_START}: String? = null")
            newLine()
            writeLine("\tvar ${field.propertyName}${Constant.SUFFIX_BEAN_QUERY_TIME_END}: String? = null")
        }
    }

    private fun BufferedWriter.writeToStringMethod(fieldList: List<FieldInfo>) {
        createToStringFn(
            comment = "toString 方法",
            fieldList = fieldList
        )
        newLine()
    }

    private fun getDateTimeFormatAnnotation(field: FieldInfo): String? {
        val pattern = when {
            field.ktType.contains("LocalDateTime") -> DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.pattern
            field.ktType.contains("LocalDate") -> DateTimePatternEnum.YYYY_MM_DD.pattern
            field.ktType.contains("LocalTime") -> DateTimePatternEnum.HH_MM_SS.pattern
            else -> null
        }

        return pattern?.let { "@field:${Constant.DATE_FORMAT_EXPRESSION}(pattern = \"$it\")" }
    }
}