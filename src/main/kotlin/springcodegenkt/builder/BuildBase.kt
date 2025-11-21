package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.utils.writeLine
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException

object BuildBase {
    private val logger = LoggerFactory.getLogger(BuildBase::class.java)

    fun execute() {
        build(
            filename = "DateTimePatternEnum",
            outputPath = Constant.FULL_ENUMS_PATH,
            packageName = Constant.FULL_ENUMS_PACKAGE
        )
        build(
            filename = "PageSizeEnum",
            outputPath = Constant.FULL_ENUMS_PATH,
            packageName = Constant.FULL_ENUMS_PACKAGE
        )
        build(
            filename = "SimplePage",
            outputPath = Constant.FULL_QUERY_PATH,
            packageName = Constant.FULL_QUERY_PACKAGE,
            importPackageList = listOf(
                "import ${Constant.FULL_ENUMS_PACKAGE}.PageSizeEnum"
            )
        )
        build(
            filename = "BaseQuery",
            outputPath = Constant.FULL_QUERY_PATH,
            packageName = Constant.FULL_QUERY_PACKAGE
        )
        build(
            filename = "PaginationResultVO",
            outputPath = Constant.FULL_VO_PATH,
            packageName = Constant.FULL_VO_PACKAGE
        )
        build(
            filename = "ResponseVO",
            outputPath = Constant.FULL_VO_PATH,
            packageName = Constant.FULL_VO_PACKAGE
        )
        build(
            filename = "ABaseController",
            outputPath = Constant.FULL_CONTROLLER_PATH,
            packageName = Constant.FULL_CONTROLLER_PACKAGE,
            importPackageList = listOf(
                "import ${Constant.FULL_ENUMS_PACKAGE}.ResponseCodeEnum",
                "import ${Constant.FULL_VO_PACKAGE}.ResponseVO",
                "import ${Constant.BASE_PACKAGE}.exception.BusinessException"
            )
        )
        build(
            filename = "AGlobalExceptionHandlerController",
            outputPath = Constant.FULL_CONTROLLER_PATH,
            packageName = Constant.FULL_CONTROLLER_PACKAGE,
            importPackageList = listOf(
                "import ${Constant.FULL_ENUMS_PACKAGE}.ResponseCodeEnum",
                "import ${Constant.FULL_VO_PACKAGE}.ResponseVO",
                "import ${Constant.BASE_PACKAGE}.exception.BusinessException",
                "import jakarta.servlet.http.HttpServletRequest",
                "import org.slf4j.Logger",
                "import org.slf4j.LoggerFactory",
                "import org.springframework.dao.DuplicateKeyException",
                "import org.springframework.validation.BindException",
                "import org.springframework.web.bind.annotation.ExceptionHandler",
                "import org.springframework.web.bind.annotation.RestControllerAdvice",
                "import org.springframework.web.method.annotation.HandlerMethodValidationException",
                "import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException",
                "import org.springframework.web.multipart.support.MissingServletRequestPartException",
                "import org.springframework.web.servlet.NoHandlerFoundException"
            )
        )
        build(
            filename = "ResponseCodeEnum",
            outputPath = Constant.FULL_ENUMS_PATH,
            packageName = Constant.FULL_ENUMS_PACKAGE
        )
        build(
            filename = "BusinessException",
            outputPath = "${Constant.FULL_PACKAGE_BASE_PATH}/exception",
            packageName = "${Constant.BASE_PACKAGE}.exception",
            importPackageList = listOf(
                "import ${Constant.FULL_ENUMS_PACKAGE}.ResponseCodeEnum"
            )
        )
        build(
            filename = "BaseMapper",
            outputPath = Constant.FULL_MAPPERS_PATH,
            packageName = Constant.FULL_MAPPERS_PACKAGE
        )
    }

    private fun build(
        filename: String,
        outputPath: String,
        packageName: String,
        importPackageList: List<String>? = null
    ) {
        val folder = File(outputPath).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val file = File(folder, "$filename.kt")
        runCatching {
            file.bufferedWriter(Charsets.UTF_8).use { bw ->
                bw.writeLine("package $packageName")
                bw.newLine()

                importPackageList?.let {
                    it.forEach { importPackageName -> bw.writeLine(importPackageName) }
                    bw.newLine()
                }

                readTemplateFileLineByLine(filePath = "template/${filename}.txt") { line ->
                    bw.writeLine(line)
                }
            }
        }.onFailure { e ->
            logger.error("生成基础类失败", e)
        }
    }

    private fun readTemplateFileLineByLine(filePath: String, lineCallback: (String) -> Unit) {
        val inputStream = javaClass.classLoader.getResourceAsStream(filePath)
            ?: throw FileNotFoundException("模板文件未找到: $filePath")

        inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                lineCallback(line!!)
            }
        }
    }
}
