package io.github.hcisme.springcodegenkt.builder

import java.io.BufferedWriter

fun BufferedWriter.createClassComment(comment: String): BufferedWriter {
    write(
        """
        |/**
        | * $comment
        | */
        """.trimMargin()
    )
    return this
}

fun BufferedWriter.createFieldComment(comment: String, indentLevel: Int = 1): BufferedWriter {
    val indent = "\t".repeat(indentLevel)
    write(
        """
            |$indent/**
            |$indent * $comment
            |$indent */
            """.trimMargin()
    )
    return this
}

fun BufferedWriter.createMethodComment(comment: String, indentLevel: Int = 1): BufferedWriter {
    val indent = "\t".repeat(indentLevel)
    write(
        """
            |$indent/**
            |$indent * $comment
            |$indent */
            """.trimMargin()
    )
    return this
}