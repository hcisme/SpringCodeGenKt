package io.github.hcisme.springcodegenkt

import io.github.hcisme.springcodegenkt.builder.BuildPojo
import io.github.hcisme.springcodegenkt.builder.BuildTable

object SpringCodeGenApplication {
    fun init() {
        BuildTable.getTables().forEach { tableInfo ->
            BuildPojo.execute(tableInfo)
        }
    }
}

fun main() {
    SpringCodeGenApplication.init()
}
