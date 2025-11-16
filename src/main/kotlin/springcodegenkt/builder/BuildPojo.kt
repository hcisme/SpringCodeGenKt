package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.TableInfo
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

object BuildPojo {
    private val logger = LoggerFactory.getLogger(BuildPojo::class.java)

    fun execute(tableInfo: TableInfo) {
        val pojoDir = File(Constant.PATH_POJO)
        if (!pojoDir.exists()) {
            pojoDir.mkdirs()
        }
        val pojoFile = File(pojoDir, tableInfo.beanName + ".kt")
//        try {
//            pojoFile.createNewFile()
//        } catch (e: Exception) {
//            logger.error("创建表 ${tableInfo.tableName} 的pojo类${tableInfo.beanName}失败", e)
//        }
        FileOutputStream(pojoFile).use { out ->
            OutputStreamWriter(out, "UTF-8").use { writer ->
                BufferedWriter(writer).use { bw ->
                    bw.write("package ${Constant.FULL_POJO_PACKAGE_NAME}")
                    bw.newLine()
                    bw.newLine()

                    bw.write(
                        "import java.io.Serializable"
                    )
                    bw.newLine()
                    if (tableInfo.haveDate) {
                        bw.write("import java.time.LocalDate")
                        bw.newLine()
                    }
                    if (tableInfo.haveDateTime) {
                        bw.write("import java.time.LocalDateTime")
                        bw.newLine()
                    }
                    if (tableInfo.haveBigDecimal) {
                        bw.write("import java.math.BigDecimal")
                        bw.newLine()
                    }
                    bw.newLine()

                    tableInfo.comment?.let { bw.createClassComment(comment = it).newLine() }
                    bw.write("class ${tableInfo.beanName}: Serializable {")
                    bw.newLine()

                    tableInfo.fieldList.forEach { field ->
                        field.comment?.let { bw.createFieldComment(comment = it).newLine() }
                        bw.write("\tvar ${field.propertyName}: ${field.ktType} = null")
                        bw.newLine()
                        bw.newLine()
                    }

                    bw.write("}")
                    bw.newLine()
                    bw.flush()
                }
            }
        }
    }
}