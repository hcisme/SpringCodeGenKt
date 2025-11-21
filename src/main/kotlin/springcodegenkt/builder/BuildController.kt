package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.utils.writeLine
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File

object BuildController {
    private val logger = LoggerFactory.getLogger(BuildController::class.java)
    private lateinit var servicePropName: String
    private lateinit var serviceClassName: String

    fun execute(tableInfo: TableInfo) {
        serviceClassName = "${tableInfo.beanName}${Constant.SUFFIX_SERVICE}"
        servicePropName = serviceClassName.replaceFirstChar { it.lowercase() }

        val controllerDir = File(Constant.FULL_CONTROLLER_PATH).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val className = tableInfo.beanName + Constant.SUFFIX_CONTROLLER
        val serviceImplFile = File(controllerDir, "${className}.kt")

        serviceImplFile.bufferedWriter(Charsets.UTF_8).use { bw ->
            runCatching {
                bw.writePackageAndImports(tableInfo)

                bw.writeClassHeader(tableInfo, className)

                bw.writeClassProp()

                bw.writeExampleMethods(tableInfo)

                bw.writeLine("}")
                bw.flush()
            }.onFailure { e ->
                logger.error("生成 Controller 类 $className 过程中发生错误", e)
            }
        }
    }

    private fun BufferedWriter.writePackageAndImports(tableInfo: TableInfo) {
        writeLine("package ${Constant.FULL_CONTROLLER_PACKAGE}")
        newLine()

        writeLine("import ${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}")
        writeLine("import ${Constant.FULL_QUERY_PACKAGE}.${tableInfo.beanQueryName}")
        writeLine("import ${Constant.FULL_SERVICE_PACKAGE}.${tableInfo.beanName}${Constant.SUFFIX_SERVICE}")
        writeLine("import jakarta.annotation.Resource")
        writeLine("import org.springframework.web.bind.annotation.GetMapping")
        writeLine("import org.springframework.web.bind.annotation.RequestMapping")
        writeLine("import org.springframework.web.bind.annotation.RestController")
        newLine()
    }

    private fun BufferedWriter.writeClassHeader(tableInfo: TableInfo, className: String) {
        tableInfo.comment?.let {
            createClassComment("$it Controller").newLine()
        }
        writeLine("@RestController")
        writeLine("@RequestMapping(\"/${tableInfo.beanName.replaceFirstChar { it.lowercase() }}\")")
        writeLine("class $className : ABaseController() {")
    }

    private fun BufferedWriter.writeClassProp() {
        writeLine("\t@Resource")
        writeLine("\tprivate lateinit var ${servicePropName}: ${serviceClassName}")
        newLine()
    }

    private fun BufferedWriter.writeExampleMethods(tableInfo: TableInfo) {
        val beanName = tableInfo.beanName
        val queryClassName = tableInfo.beanQueryName

        createMethodComment("根据条件查询列表").newLine()
        writeLine("\t@GetMapping(\"/list\")")
        writeLine("\tfun getAllList(query: $queryClassName): List<$beanName> {")
        writeLine("\t\treturn $servicePropName.findListByParam(query)")
        writeLine("\t}")
        newLine()
    }
}
