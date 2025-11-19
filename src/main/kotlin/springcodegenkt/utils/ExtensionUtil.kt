package io.github.hcisme.springcodegenkt.utils

import java.io.BufferedWriter
import java.sql.ResultSet

fun BufferedWriter.writeLine(text: String) {
    write(text)
    newLine()
}

inline fun ResultSet.iterate(block: ResultSet.() -> Unit) {
    while (next()) {
        block()
    }
}
