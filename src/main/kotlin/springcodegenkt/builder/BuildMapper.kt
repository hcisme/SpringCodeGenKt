package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.FieldInfo
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.utils.writeLine
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File

object BuildMapper {
    private val logger = LoggerFactory.getLogger(BuildMapper::class.java)

    fun execute(tableInfo: TableInfo) {
        val mappersDir = File(Constant.FULL_MAPPERS_PATH).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val className = tableInfo.beanName + Constant.SUFFIX_MAPPER
        val mapperFile = File(mappersDir, "${className}.kt")

        mapperFile.bufferedWriter(Charsets.UTF_8).use { bw ->
            runCatching {
                // 写入包声明和导入
                bw.writePackageAndImports()
                // 写入类注释和类定义开始
                bw.writeClassHeader(tableInfo = tableInfo, className = className)
                bw.writeMethods(keyIndexMap = tableInfo.keyIndexMap)
                // 写入类结束
                bw.writeLine("}")
                bw.flush()
            }.onFailure { e ->
                logger.error("生成 Mapper 类 $className 过程中发生错误", e)
            }
        }
    }

    private fun BufferedWriter.writePackageAndImports() {
        writeLine("package ${Constant.FULL_MAPPERS_PACKAGE}")
        newLine()

        writeLine("import org.apache.ibatis.annotations.Param")
        newLine()
    }

    private fun BufferedWriter.writeClassHeader(tableInfo: TableInfo, className: String) {
        tableInfo.comment?.let {
            createClassComment(it)
            newLine()
        }

        writeLine("interface ${className}<T, P> : BaseMapper<T, P> {")
    }

    private fun BufferedWriter.writeMethods(keyIndexMap: MutableMap<String, MutableList<FieldInfo>>) {
        keyIndexMap.forEach { item ->
            val fieldInfoList = item.value

            val methodName = fieldInfoList.mapIndexed { index, fieldInfo ->
                fieldInfo.propertyName.replaceFirstChar(Char::uppercaseChar).let {
                    if (index == 0) it else "And${it}"
                }
            }.joinToString("")

            val comment = methodName.split("And").joinToString(separator = " 和 ")
            val argumentString = fieldInfoList.joinToString(", ") { fieldInfo ->
                "@Param(\"${fieldInfo.propertyName}\") ${fieldInfo.propertyName}: ${
                    fieldInfo.ktType.replace(oldValue = "?", newValue = "")
                }"
            }

            createMethodComment("根据${comment}获取对象").newLine()
            writeLine("\tfun selectBy${methodName}(${argumentString}): T?")
            newLine()

            createMethodComment("根据${comment}更新").newLine()
            val updateArgumentString = "@Param(\"bean\") t: T, $argumentString"
            writeLine("\tfun updateBy${methodName}(${updateArgumentString}): Int")
            newLine()

            createMethodComment("根据${comment}删除").newLine()
            writeLine("\tfun deleteBy${methodName}(${argumentString}): Int")
            newLine()
        }
    }
}