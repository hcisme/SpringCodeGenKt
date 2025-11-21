package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.utils.writeLine
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File

object BuildService {
    private val logger = LoggerFactory.getLogger(BuildService::class.java)

    fun execute(tableInfo: TableInfo) {
        val serviceDir = File(Constant.FULL_SERVICE_PATH).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val className = tableInfo.beanName + Constant.SUFFIX_SERVICE
        val serviceFile = File(serviceDir, "${className}.kt")

        serviceFile.bufferedWriter(Charsets.UTF_8).use { bw ->
            runCatching {
                bw.writePackageAndImports(tableInfo)

                bw.writeClassHeader(tableInfo, className)

                bw.writeStaticMethods(tableInfo)

                bw.writeIndexMethods(tableInfo)

                bw.writeLine("}")
                bw.flush()
            }.onFailure { e ->
                logger.error("生成 Service 接口类 $className 过程中发生错误", e)
            }
        }
    }

    private fun BufferedWriter.writePackageAndImports(tableInfo: TableInfo) {
        writeLine("package ${Constant.FULL_SERVICE_PACKAGE}")
        newLine()

        writeLine("import ${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}")
        writeLine("import ${Constant.FULL_QUERY_PACKAGE}.${tableInfo.beanQueryName}")
        writeLine("import ${Constant.FULL_VO_PACKAGE}.PaginationResultVO")
        newLine()
    }

    private fun BufferedWriter.writeClassHeader(tableInfo: TableInfo, className: String) {
        tableInfo.comment?.let {
            createClassComment("${it}接口").newLine()
        }
        writeLine("interface $className {")
    }

    private fun BufferedWriter.writeStaticMethods(tableInfo: TableInfo) {
        val beanName = tableInfo.beanName
        val queryName = tableInfo.beanQueryName

        createMethodComment("根据条件查询列表").newLine()
        writeLine("\tfun findListByParam(param: $queryName): List<$beanName>")
        newLine()

        createMethodComment("根据条件查询数量").newLine()
        writeLine("\tfun findCountByParam(param: $queryName): Int")
        newLine()

        createMethodComment("分页查询").newLine()
        writeLine("\tfun findListByPage(param: $queryName): PaginationResultVO<$beanName>")
        newLine()

        createMethodComment("新增").newLine()
        writeLine("\tfun add(bean: $beanName): Int")
        newLine()

        createMethodComment("新增 (或更新)").newLine()
        writeLine("\tfun addOrUpdate(bean: $beanName): Int")
        newLine()

        createMethodComment("批量新增").newLine()
        writeLine("\tfun addBatch(list: List<$beanName>): Int")
        newLine()

        createMethodComment("批量新增 (或更新)").newLine()
        writeLine("\tfun addOrUpdateBatch(list: List<$beanName>): Int")
        newLine()

        createMethodComment("多条件更新").newLine()
        writeLine("\tfun updateByParam(bean: $beanName, param: $queryName): Int")
        newLine()

        createMethodComment("多条件删除").newLine()
        writeLine("\tfun deleteByParam(param: $queryName): Int")
        newLine()
    }

    private fun BufferedWriter.writeIndexMethods(tableInfo: TableInfo) {
        val beanName = tableInfo.beanName

        tableInfo.keyIndexMap.forEach { item ->
            val fieldInfoList = item.value

            val methodName = fieldInfoList.mapIndexed { index, fieldInfo ->
                fieldInfo.propertyName.replaceFirstChar(Char::uppercaseChar).let {
                    if (index == 0) it else "And${it}"
                }
            }.joinToString("")

            val comment = methodName.split("And").joinToString(separator = " 和 ")

            val argumentString = fieldInfoList.joinToString(", ") { fieldInfo ->
                "${fieldInfo.propertyName}: ${
                    fieldInfo.ktType.replace(
                        oldValue = "?",
                        newValue = ""
                    )
                }"
            }

            // getBy
            createMethodComment("根据${comment}查询对象").newLine()
            writeLine("\tfun get${beanName}By${methodName}(${argumentString}): ${beanName}?")
            newLine()

            // updateBy
            val updateArgumentString = "bean: $beanName, $argumentString"
            createMethodComment("根据${comment}修改").newLine()
            writeLine("\tfun update${beanName}By${methodName}(${updateArgumentString}): Int")
            newLine()

            // deleteBy
            createMethodComment("根据${comment}删除").newLine()
            writeLine("\tfun delete${beanName}By${methodName}(${argumentString}): Int")
            newLine()
        }
    }
}