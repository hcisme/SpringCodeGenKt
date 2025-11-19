package io.github.hcisme.springcodegenkt

import io.github.hcisme.springcodegenkt.builder.*

object SpringCodeGenApplication {
    fun init() {
        BuildBase.execute()

        BuildTable.getTables().forEach { tableInfo ->
            BuildPojo.execute(tableInfo)
            BuildQuery.execute(tableInfo)
            BuildMapper.execute(tableInfo)
            BuildMapperXml.execute(tableInfo)
        }
    }
}

fun main() {
    SpringCodeGenApplication.init()
}
