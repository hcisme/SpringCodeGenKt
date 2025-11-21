package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.utils.writeLine
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File

object BuildServiceImpl {
    private val logger = LoggerFactory.getLogger(BuildServiceImpl::class.java)
    private lateinit var mapperClassName: String
    private lateinit var mapperPropName: String

    fun execute(tableInfo: TableInfo) {
        mapperClassName = tableInfo.beanName + Constant.SUFFIX_MAPPER
        mapperPropName = mapperClassName.replaceFirstChar { it.lowercase() }

        val serviceImplDir = File(Constant.FULL_SERVICE_IMPL_PATH).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val className = tableInfo.beanName + Constant.SUFFIX_SERVICE_IMPL
        val serviceImplFile = File(serviceImplDir, "${className}.kt")

        serviceImplFile.bufferedWriter(Charsets.UTF_8).use { bw ->
            runCatching {
                bw.writePackageAndImports(tableInfo)

                bw.writeClassHeader(tableInfo, className)

                bw.writeClassProp(tableInfo)

                bw.writeOverrideMethods(tableInfo)

                bw.writeLine("}")
                bw.flush()
            }.onFailure { e ->
                logger.error("生成 Service 类 $className 过程中发生错误", e)
            }
        }
    }

    private fun BufferedWriter.writePackageAndImports(tableInfo: TableInfo) {
        writeLine("package ${Constant.FULL_SERVICE_IMPL_PACKAGE}")
        newLine()

        writeLine("import ${Constant.FULL_ENUMS_PACKAGE}.PageSizeEnum")
        writeLine("import ${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}")
        writeLine("import ${Constant.FULL_QUERY_PACKAGE}.${tableInfo.beanQueryName}")
        writeLine("import ${Constant.FULL_QUERY_PACKAGE}.SimplePage")
        writeLine("import ${Constant.FULL_VO_PACKAGE}.PaginationResultVO")
        writeLine("import ${Constant.FULL_MAPPERS_PACKAGE}.${tableInfo.beanName}${Constant.SUFFIX_MAPPER}")
        writeLine("import ${Constant.FULL_SERVICE_PACKAGE}.${tableInfo.beanName}${Constant.SUFFIX_SERVICE}")
        writeLine("import org.springframework.stereotype.Service")
        writeLine("import jakarta.annotation.Resource")
        newLine()
    }

    private fun BufferedWriter.writeClassHeader(tableInfo: TableInfo, className: String) {
        tableInfo.comment?.let {
            createClassComment("${it}接口实现").newLine()
        }
        val interfaceName = tableInfo.beanName + Constant.SUFFIX_SERVICE
        writeLine("@Service(\"${interfaceName.replaceFirstChar { it.lowercase() }}\")")
        writeLine("class $className : $interfaceName {")
    }

    private fun BufferedWriter.writeClassProp(tableInfo: TableInfo) {
        writeLine("\t@Resource")
        writeLine("\tprivate lateinit var ${mapperPropName}: ${mapperClassName}<${tableInfo.beanName}, ${tableInfo.beanQueryName}>")
        newLine()
    }

    private fun BufferedWriter.writeOverrideMethods(tableInfo: TableInfo) {
        writeStaticMethodOverrides(tableInfo)

        writeIndexMethodOverrides(tableInfo)
    }

    private fun BufferedWriter.writeStaticMethodOverrides(tableInfo: TableInfo) {
        val beanName = tableInfo.beanName
        val queryClassName = tableInfo.beanQueryName

        createMethodComment("根据条件查询列表").newLine()
        writeLine("\toverride fun findListByParam(param: $queryClassName): List<$beanName> {")
        writeLine("\t\treturn $mapperPropName.selectList(param)")
        writeLine("\t}")
        newLine()

        createMethodComment("根据条件查询数量").newLine()
        writeLine("\toverride fun findCountByParam(param: $queryClassName): Int {")
        writeLine("\t\treturn $mapperPropName.selectCount(param)")
        writeLine("\t}")
        newLine()

        createMethodComment("分页查询").newLine()
        writeLine("\toverride fun findListByPage(param: $queryClassName): PaginationResultVO<${beanName}> {")
        writeLine("\t\tval count = findCountByParam(param)")
        writeLine("\t\tval pageSizeEnum = if (param.pageSize == null) PageSizeEnum.SIZE15.size else param.pageSize!!")
        writeLine("\t\tval page = SimplePage(param.page, count, pageSizeEnum)")
        writeLine("\t\tparam.simplePage = page")
        writeLine("\t\tval list = findListByParam(param)")
        writeLine("\t\tval result = PaginationResultVO(count, page.pageSize, page.page, page.pageTotal, list)")
        writeLine("\t\treturn result")
        writeLine("\t}")
        newLine()

        createMethodComment("新增").newLine()
        writeLine("\toverride fun add(bean: $beanName): Int {")
        writeLine("\t\treturn $mapperPropName.insert(bean)")
        writeLine("\t}")
        newLine()

        createMethodComment("新增 (或更新)").newLine()
        writeLine("\toverride fun addOrUpdate(bean: $beanName): Int {")
        writeLine("\t\treturn $mapperPropName.insertOrUpdate(bean)")
        writeLine("\t}")
        newLine()

        createMethodComment("批量新增").newLine()
        writeLine("\toverride fun addBatch(list: List<$beanName>): Int {")
        writeLine("\t\treturn $mapperPropName.insertBatch(list)")
        writeLine("\t}")
        newLine()

        createMethodComment("批量新增 (或更新)").newLine()
        writeLine("\toverride fun addOrUpdateBatch(list: List<$beanName>): Int {")
        writeLine("\t\treturn $mapperPropName.insertOrUpdateBatch(list)")
        writeLine("\t}")
        newLine()

        createMethodComment("多条件更新").newLine()
        writeLine("\toverride fun updateByParam(bean: $beanName, param: $queryClassName): Int {")
        writeLine("\t\treturn $mapperPropName.updateByParam(bean, param)")
        writeLine("\t}")
        newLine()

        createMethodComment("多条件删除").newLine()
        writeLine("\toverride fun deleteByParam(param: $queryClassName): Int {")
        writeLine("\t\treturn $mapperPropName.deleteByParam(param)")
        writeLine("\t}")
        newLine()
    }

    private fun BufferedWriter.writeIndexMethodOverrides(tableInfo: TableInfo) {
        val beanName = tableInfo.beanName

        tableInfo.keyIndexMap.forEach { item ->
            val fieldInfoList = item.value

            val serviceMethodName = fieldInfoList.mapIndexed { index, fieldInfo ->
                fieldInfo.propertyName.replaceFirstChar(Char::uppercaseChar).let {
                    if (index == 0) it else "And${it}"
                }
            }.joinToString("")

            val mapperMethodName = serviceMethodName

            val comment = serviceMethodName.split("And").joinToString(separator = " 和 ")

            val argumentStringWithType = fieldInfoList.joinToString(", ") { fieldInfo ->
                "${fieldInfo.propertyName}: ${
                    fieldInfo.ktType.replace(
                        oldValue = "?",
                        newValue = ""
                    )
                }"
            }

            val argumentStringWithoutType = fieldInfoList.joinToString(", ") { it.propertyName }

            createMethodComment("根据${comment}查询对象").newLine()
            val getSig = "override fun get${beanName}By${serviceMethodName}($argumentStringWithType): $beanName?"
            writeLine("\t$getSig {")
            writeLine("\t\treturn $mapperPropName.selectBy${mapperMethodName}($argumentStringWithoutType)")
            writeLine("\t}")
            newLine()

            createMethodComment("根据${comment}修改").newLine()
            val updateSig =
                "override fun update${beanName}By${serviceMethodName}(bean: $beanName, $argumentStringWithType): Int"
            val updateCallArgs = "bean, $argumentStringWithoutType"
            writeLine("\t$updateSig {")
            writeLine("\t\treturn $mapperPropName.updateBy${mapperMethodName}($updateCallArgs)")
            writeLine("\t}")
            newLine()

            createMethodComment("根据${comment}删除").newLine()
            val deleteSig = "override fun delete${beanName}By${serviceMethodName}($argumentStringWithType): Int"
            writeLine("\t$deleteSig {")
            writeLine("\t\treturn $mapperPropName.deleteBy${mapperMethodName}($argumentStringWithoutType)")
            writeLine("\t}")
            newLine()
        }
    }
}