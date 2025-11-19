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
            filename = "BaseMapper",
            outputPath = Constant.FULL_MAPPERS_PATH,
            packageName = Constant.FULL_MAPPERS_PACKAGE
        )
    }

    private fun build(filename: String, outputPath: String, packageName: String) {
        val folder = File(outputPath).apply {
            takeIf { !it.exists() }?.mkdirs()
        }

        val file = File(folder, "$filename.kt")
        runCatching {
            file.bufferedWriter(Charsets.UTF_8).use { bw ->
                bw.writeLine("package $packageName")
                bw.newLine()

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
