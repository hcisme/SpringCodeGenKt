package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.FieldInfo
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.enums.DateTimePatternEnum
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File

object BuildPojo {
    private val logger = LoggerFactory.getLogger(BuildPojo::class.java)

    fun execute(tableInfo: TableInfo) {
        val pojoDir = File(Constant.PATH_POJO).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val pojoFile = File(pojoDir, "${tableInfo.beanName}.kt")

        pojoFile.bufferedWriter(Charsets.UTF_8).use { bw ->
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

        logger.info("成功生成 POJO 类: ${tableInfo.beanName}")
    }

    private fun BufferedWriter.writePackageAndImports(tableInfo: TableInfo) {
        writeLine("package ${Constant.FULL_POJO_PACKAGE_NAME}")
        newLine()

        val imports = mutableSetOf("java.io.Serializable").apply {
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
            createClassComment(it)
            newLine()
        }

        writeLine("class ${tableInfo.beanName} : Serializable {")
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

        getDateTimeFormatAnnotation(field)?.let { annotation ->
            writeLine("\t$annotation")
        }

        writeLine("\tvar ${field.propertyName}: ${field.ktType} = null")
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

        return pattern?.let { "${Constant.DATE_FORMAT_EXPRESSION}(pattern = \"$it\")" }
    }

    private fun BufferedWriter.writeLine(text: String) {
        write(text)
        newLine()
    }
}