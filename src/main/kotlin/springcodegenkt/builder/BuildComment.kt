package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.FieldInfo
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

fun BufferedWriter.createToStringFn(
    comment: String = "toString 方法",
    fieldList: List<FieldInfo>,
    indentLevel: Int = 1
): BufferedWriter {
    val indent = "\t".repeat(indentLevel)
    val innerIndent = "\t".repeat(indentLevel + 1)

    if (comment.isNotBlank()) {
        write("$indent/**")
        newLine()
        write("$indent * $comment")
        newLine()
        write("$indent */")
        newLine()
    }

    write("${indent}override fun toString(): String {")
    newLine()
    write($$"$$innerIndent return \"${this::class.simpleName}(")

    val propertyStrings = fieldList.mapIndexed { index, field ->
        val prefix = if (index > 0) ", " else ""
        $$"$$prefix$${field.propertyName}=${$${field.propertyName}}"
    }

    write(propertyStrings.joinToString(""))
    write(")\"")
    newLine()
    write("$indent}\n")

    return this
}