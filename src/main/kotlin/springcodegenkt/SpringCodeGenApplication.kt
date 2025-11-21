package io.github.hcisme.springcodegenkt

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.builder.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object SpringCodeGenApplication {
    private val logger: Logger = LoggerFactory.getLogger(SpringCodeGenApplication::class.java)

    fun init() {
        logger.info("Initializing Spring Code Gen")
        BuildBase.execute()

        BuildTable.getTables().forEach { tableInfo ->
            BuildPojo.execute(tableInfo)
            BuildQuery.execute(tableInfo)
            BuildMapper.execute(tableInfo)
            BuildMapperXml.execute(tableInfo)
            BuildService.execute(tableInfo)
            BuildServiceImpl.execute(tableInfo)
            BuildController.execute(tableInfo)
        }

        logger.info("Spring Code Gen Success At ${Constant.OUTPUT_DIR}")
    }
}

fun main() {
    SpringCodeGenApplication.init()
}
