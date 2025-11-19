package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.FieldInfo
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.utils.writeLine
import org.slf4j.LoggerFactory
import java.io.BufferedWriter
import java.io.File

object BuildMapperXml {
    private val logger = LoggerFactory.getLogger(BuildMapperXml::class.java)
    private lateinit var tableAlias: String

    fun execute(tableInfo: TableInfo) {
        val xmlMappersDir = File(Constant.FULL_XML_MAPPERS_PATH).apply {
            takeIf { !it.exists() }?.mkdirs()
        }
        tableAlias = tableInfo.beanName.first().lowercaseChar().toString()

        val xmlName = tableInfo.beanName + Constant.SUFFIX_MAPPER
        val mapperXmlFile = File(xmlMappersDir, "${xmlName}.xml")

        mapperXmlFile.bufferedWriter(Charsets.UTF_8).use { bw ->
            runCatching {
                // 写入 XML 头和 <mapper> 标签
                bw.writeXmlHeader(mapperName = xmlName)

                // 写入 <resultMap>
                bw.writeResultMap(tableInfo)

                // 写入 <sql id="base_column_list">
                bw.writeBaseColumnList(tableInfo)

                // 写入 <sql id="base_condition_filed">
                bw.writeBaseConditionFiled(tableInfo)

                // 写入 <sql id="base_condition">
                bw.writeBaseCondition()

                // 写入 <sql id="query_condition">
                bw.writeQueryCondition(tableInfo)

                //写入 <select id="selectList">
                bw.writeSelectList(tableInfo)

                //写入 <select id="selectCount">
                bw.writeSelectCount(tableInfo)

                //写入 <insert id="insert">
                bw.writeInsert(tableInfo, isInsertOrUpdate = false)

                // 写入 <insert id="insertOrUpdate">
                bw.writeInsertOrUpdate(tableInfo)

                // 写入 <insert id="insertBatch">
                bw.writeInsertBatch(tableInfo)

                // 写入 <insert id="insertOrUpdateBatch">
                bw.writeInsertOrUpdateBatch(tableInfo)

                // 写入 <update id="updateByParam">
                bw.writeUpdateByParam(tableInfo)

                // 写入 <delete id="deleteByParam">
                bw.writeDeleteByParam(tableInfo)

                // 写入根据唯一索引生成的 Update/Delete/Select
                bw.writeIndexMethods(tableInfo)

                // 结束标签
                bw.writeLine("</mapper>")
                bw.flush()
            }.onFailure { e ->
                logger.error("生成 MapperXML 类 $xmlName 过程中发生错误", e)
            }
        }
    }

    /**
     * 写入 XML 头
     */
    private fun BufferedWriter.writeXmlHeader(mapperName: String) {
        writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        writeLine("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"")
        // 你的模板用的是 http，保持一致
        writeLine("\t\t\"https://mybatis.org/dtd/mybatis-3-mapper.dtd\">")
        writeLine("<mapper namespace=\"${Constant.FULL_MAPPERS_PACKAGE}.${mapperName}\">")
        newLine()
    }

    /**
     * 写入 ResultMap
     */
    private fun BufferedWriter.writeResultMap(tableInfo: TableInfo) {
        writeLine("\t<!--实体映射-->")
        writeLine("\t<resultMap id=\"base_result_map\" type=\"${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}\">")
        tableInfo.fieldList.forEach { field ->
            field.comment?.let { writeLine("\t\t<!--${it}-->") }
            writeLine("\t\t<result column=\"${field.fieldName}\" property=\"${field.propertyName}\" />")
        }
        writeLine("\t</resultMap>")
        newLine()
    }

    /**
     *  写入 base_column_list
     */
    private fun BufferedWriter.writeBaseColumnList(tableInfo: TableInfo) {
        writeLine("\t<!-- 通用查询结果列-->")
        writeLine("\t<sql id=\"base_column_list\">")
        // 每5个字段换一行
        val columnChunks = tableInfo.fieldList.map { "${tableAlias}.${it.fieldName}" }.chunked(5)
        columnChunks.forEachIndexed { index, chunk ->
            write("\t\t${chunk.joinToString(", ")}")
            if (index < columnChunks.size - 1) {
                writeLine(",")
            } else {
                newLine()
            }
        }
        writeLine("\t</sql>")
        newLine()
    }

    /**
     * 写入 base_condition_filed (精确查询)
     */
    private fun BufferedWriter.writeBaseConditionFiled(tableInfo: TableInfo) {
        writeLine("\t<sql id=\"base_condition_filed\">")
        tableInfo.fieldList.forEach { field ->
            val queryProp = "query.${field.propertyName}"
            writeLine("\t\t<if test=\"$queryProp != null and $queryProp != ''\">")
            if (field.isDateOrTime()) {
                writeLine("\t\t\t<![CDATA[ and ${tableAlias}.${field.fieldName} = str_to_date(#{$queryProp}, '%Y-%m-%d') ]]>")
            } else {
                writeLine("\t\t\tand ${tableAlias}.${field.fieldName} = #{$queryProp}")
            }
            writeLine("\t\t</if>")
        }
        writeLine("\t</sql>")
        newLine()
    }

    /**
     * 写入 base_condition (where 包装)
     */
    private fun BufferedWriter.writeBaseCondition() {
        writeLine("\t<!-- 通用条件列-->")
        writeLine("\t<sql id=\"base_condition\">")
        writeLine("\t\t<where>")
        writeLine("\t\t\t<include refid=\"base_condition_filed\" />")
        writeLine("\t\t</where>")
        writeLine("\t</sql>")
        newLine()
    }

    /**
     * 写入 query_condition (模糊查询 + 范围查询)
     */
    private fun BufferedWriter.writeQueryCondition(tableInfo: TableInfo) {
        writeLine("\t<!-- 通用查询条件列-->")
        writeLine("\t<sql id=\"query_condition\">")
        writeLine("\t\t<where>")
        writeLine("\t\t\t<include refid=\"base_condition_filed\" />")
        tableInfo.fieldList.forEach { field ->
            val propName = field.propertyName
            // 模糊查询
            if (field.ktType.contains("String")) {
                val fuzzyProp = "query.${propName + Constant.SUFFIX_BEAN_QUERY_FUZZY}"
                writeLine("\t\t\t<if test=\"$fuzzyProp != null and $fuzzyProp != ''\">")
                writeLine("\t\t\t\tand ${tableAlias}.${field.fieldName} like concat('%', #{$fuzzyProp}, '%')")
                writeLine("\t\t\t</if>")
            }
            // 日期/时间范围查询
            if (field.isDateOrTime()) {
                val startProp = "query.${propName + Constant.SUFFIX_BEAN_QUERY_TIME_START}"
                val endProp = "query.${propName + Constant.SUFFIX_BEAN_QUERY_TIME_END}"
                writeLine("\t\t\t<if test=\"$startProp != null and $startProp != ''\">")
                writeLine("\t\t\t\t<![CDATA[ and ${tableAlias}.${field.fieldName} >= str_to_date(#{$startProp}, '%Y-%m-%d') ]]>")
                writeLine("\t\t\t</if>")
                writeLine("\t\t\t<if test=\"$endProp != null and $endProp != ''\">")
                writeLine("\t\t\t\t<![CDATA[ and ${tableAlias}.${field.fieldName} < date_sub(str_to_date(#{$endProp},'%Y-%m-%d'),interval -1 day) ]]>")
                writeLine("\t\t\t</if>")
            }
        }
        writeLine("\t\t</where>")
        writeLine("\t</sql>")
        newLine()
    }

    /**
     * 写入 selectList (分页)
     */
    private fun BufferedWriter.writeSelectList(tableInfo: TableInfo) {
        writeLine("\t<!-- 查询集合-->")
        writeLine("\t<select id=\"selectList\" resultMap=\"base_result_map\" >")
        writeLine("\t\tSELECT")
        writeLine("\t\t<include refid=\"base_column_list\" />")
        writeLine("\t\tFROM ${tableInfo.tableName} $tableAlias")
        writeLine("\t\t<include refid=\"query_condition\" />")
        writeLine("\t\t<if test=\"query.orderBy!=null and query.orderBy!=''\">")
        writeLine($$"\t\t\torder by ${query.orderBy}")
        writeLine("\t\t</if>")
        writeLine("\t\t<if test=\"query.simplePage!=null\">")
        writeLine("\t\t\tlimit #{query.simplePage.start},#{query.simplePage.end}")
        writeLine("\t\t</if>")
        writeLine("\t</select>")
        newLine()
    }

    /**
     * 写入 selectCount
     */
    private fun BufferedWriter.writeSelectCount(tableInfo: TableInfo) {
        writeLine("\t<!-- 查询数量-->")
        writeLine("\t<select id=\"selectCount\" resultType=\"java.lang.Integer\" >")
        writeLine("\t\tSELECT count(1) FROM ${tableInfo.tableName} $tableAlias")
        writeLine("\t\t<include refid=\"query_condition\" />")
        writeLine("\t</select>")
        newLine()
    }

    /**
     * 写入 insert (动态插入)
     * (isInsertOrUpdate 用于 'insertOrUpdate' 复用此逻辑)
     */
    private fun BufferedWriter.writeInsert(tableInfo: TableInfo, isInsertOrUpdate: Boolean) {
        val beanType = "${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}"
        if (!isInsertOrUpdate) {
            writeLine("\t<!-- 插入 （匹配有值的字段）-->")
            writeLine("\t<insert id=\"insert\" parameterType=\"$beanType\">")
        }

        writeLine("\t\tINSERT INTO ${tableInfo.tableName}")
        writeLine("\t\t<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >")
        tableInfo.fieldList.forEach { field ->
            // 普通 insert 跳过自增ID，insertOrUpdate 包含ID
            if (!isInsertOrUpdate && field.isAutoIncrement) {
                // return@forEach 在 forEach 中是 continue
            } else {
                writeLine("\t\t\t<if test=\"bean.${field.propertyName} != null\">")
                writeLine("\t\t\t\t${field.fieldName},")
                writeLine("\t\t\t</if>")
            }
        }
        writeLine("\t\t</trim>")
        writeLine("\t\t<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >")
        tableInfo.fieldList.forEach { field ->
            if (!isInsertOrUpdate && field.isAutoIncrement) {
                // continue
            } else {
                writeLine("\t\t\t<if test=\"bean.${field.propertyName} != null\">")
                writeLine("\t\t\t\t#{bean.${field.propertyName}},")
                writeLine("\t\t\t</if>")
            }
        }
        writeLine("\t\t</trim>")

        if (!isInsertOrUpdate) {
            writeLine("\t</insert>")
            newLine()
        }
    }

    /**
     * 写入 insertOrUpdate (ON DUPLICATE KEY UPDATE)
     */
    private fun BufferedWriter.writeInsertOrUpdate(tableInfo: TableInfo) {
        val beanType = "${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}"
        writeLine("\t<!-- 插入或者更新 （匹配有值的字段）-->")
        writeLine("\t<insert id=\"insertOrUpdate\" parameterType=\"$beanType\">")

        // 复用 insert 的SQL生成逻辑
        writeInsert(tableInfo, isInsertOrUpdate = true)

        // 附加 ON DUPLICATE KEY...
        writeLine("\t\ton DUPLICATE key update")
        writeLine("\t\t<trim prefix=\"\" suffix=\"\" suffixOverrides=\",\">")
        tableInfo.fieldList.forEach { field ->
            // 模板中更新了所有字段
            writeLine("\t\t\t<if test=\"bean.${field.propertyName} != null\">")
            writeLine("\t\t\t\t${field.fieldName} = VALUES(${field.fieldName}),")
            writeLine("\t\t\t</if>")
        }
        writeLine("\t\t</trim>")
        writeLine("\t</insert>")
        newLine()
    }

    /**
     * 写入 insertBatch (批量插入)
     */
    private fun BufferedWriter.writeInsertBatch(tableInfo: TableInfo) {
        val beanType = "${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}"
        writeLine("\t<!-- 添加 （批量插入）-->")
        writeLine("\t<insert id=\"insertBatch\" parameterType=\"$beanType\">")
        writeLine("\t\tINSERT INTO ${tableInfo.tableName}(")
        // 批量插入时，模板指定了所有字段
        val allColumns = tableInfo.fieldList.map { it.fieldName }
        writeLine("\t\t${allColumns.joinToString(",${System.lineSeparator()}\t\t")}")
        writeLine("\t\t) values")
        writeLine("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">")
        writeLine("\t\t\t(")
        val allValues = tableInfo.fieldList.map { "#{item.${it.propertyName}}" }
        writeLine("\t\t\t${allValues.joinToString(",${System.lineSeparator()}\t\t\t")}")
        writeLine("\t\t\t)")
        writeLine("\t\t</foreach>")
        writeLine("\t</insert>")
        newLine()
    }

    /**
     * 写入 insertOrUpdateBatch (批量插入或更新)
     */
    private fun BufferedWriter.writeInsertOrUpdateBatch(tableInfo: TableInfo) {
        val beanType = "${Constant.FULL_POJO_PACKAGE}.${tableInfo.beanName}"
        writeLine("\t<!-- 批量新增修改 （批量插入）-->")
        writeLine("\t<insert id=\"insertOrUpdateBatch\" parameterType=\"$beanType\">")
        writeLine("\t\tINSERT INTO ${tableInfo.tableName}(")
        val allColumns = tableInfo.fieldList.map { it.fieldName }
        writeLine("\t\t${allColumns.joinToString(",${System.lineSeparator()}\t\t")}")
        writeLine("\t\t) values")
        writeLine("\t\t<foreach collection=\"list\" item=\"item\" separator=\",\">")
        writeLine("\t\t\t(")
        val allValues = tableInfo.fieldList.map { "#{item.${it.propertyName}}" }
        writeLine("\t\t\t${allValues.joinToString(",${System.lineSeparator()}\t\t\t")}")
        writeLine("\t\t\t)")
        writeLine("\t\t</foreach>")
        writeLine("\t\ton DUPLICATE key update")
        // 模板中是更新所有字段
        val updateAssignments = tableInfo.fieldList.map { "${it.fieldName} = VALUES(${it.fieldName})" }
        writeLine("\t\t${updateAssignments.joinToString(",${System.lineSeparator()}\t\t")}")
        writeLine("\t</insert>")
        newLine()
    }

    /**
     * 写入 updateByParam (多条件更新)
     */
    private fun BufferedWriter.writeUpdateByParam(tableInfo: TableInfo) {
        val queryType = "${Constant.FULL_PARAM_PACKAGE}.${tableInfo.beanQueryName}"
        writeLine("\t<!--多条件修改-->")
        writeLine("\t<update id=\"updateByParam\" parameterType=\"$queryType\">")
        writeLine("\t\tUPDATE ${tableInfo.tableName} $tableAlias")
        writeLine("\t\t<set>")
        tableInfo.fieldList.forEach { field ->
            writeLine("\t\t\t<if test=\"bean.${field.propertyName} != null\">")
            writeLine("\t\t\t\t${field.fieldName} = #{bean.${field.propertyName}},")
            writeLine("\t\t\t</if>")
        }
        writeLine("\t\t</set>")
        writeLine("\t\t<include refid=\"query_condition\" />")
        writeLine("\t</update>")
        newLine()
    }

    /**
     * 写入 deleteByParam (多条件删除)
     */
    private fun BufferedWriter.writeDeleteByParam(tableInfo: TableInfo) {
        writeLine("\t<!--多条件删除-->")
        writeLine("\t<delete id=\"deleteByParam\">")
        writeLine("\t\tdelete $tableAlias from ${tableInfo.tableName} $tableAlias")
        writeLine("\t\t<include refid=\"query_condition\" />")
        writeLine("\t</delete>")
        newLine()
    }

    /**
     * 根据索引生成的 Update/Delete/Select 方法
     */
    private fun BufferedWriter.writeIndexMethods(tableInfo: TableInfo) {
        tableInfo.keyIndexMap.forEach { entry ->
            val fieldInfoList = entry.value

            val methodName = fieldInfoList.mapIndexed { index, fieldInfo ->
                fieldInfo.propertyName.replaceFirstChar(Char::uppercaseChar).let {
                    if (index == 0) it else "And${it}"
                }
            }.joinToString("")

            val comment = fieldInfoList.joinToString(separator = " 和 ") { it.propertyName }

            val whereClause = fieldInfoList.joinToString(" and ") {
                "${tableAlias}.${it.fieldName} = #{${it.propertyName}}"
            }

            val whereClauseNoAlias = fieldInfoList.joinToString(" and ") {
                "${it.fieldName} = #{${it.propertyName}}"
            }

            // --- updateBy ---
            writeLine("\t<!-- 根据${comment}修改-->")
            writeLine("\t<update id=\"updateBy${methodName}\">")
            writeLine("\t\tUPDATE ${tableInfo.tableName}")
            writeLine("\t\t<set>")
            val indexPropertyNames = fieldInfoList.map { it.propertyName }.toSet()
            tableInfo.fieldList.forEach { field ->
                // 如果字段不是索引字段，则允许更新
                if (field.propertyName !in indexPropertyNames) {
                    writeLine("\t\t\t<if test=\"bean.${field.propertyName} != null\">")
                    writeLine("\t\t\t\t${field.fieldName} = #{bean.${field.propertyName}},")
                    writeLine("\t\t\t</if>")
                }
            }
            writeLine("\t\t</set>")
            writeLine("\t\twhere $whereClauseNoAlias")
            writeLine("\t</update>")
            newLine()

            // --- deleteBy ---
            writeLine("\t<!-- 根据${comment}删除-->")
            writeLine("\t<delete id=\"deleteBy${methodName}\">")
            writeLine("\t\tdelete from ${tableInfo.tableName} where $whereClauseNoAlias")
            writeLine("\t</delete>")
            newLine()

            // --- selectBy ---
            writeLine("\t<!-- 根据${comment}查询-->")
            writeLine("\t<select id=\"selectBy${methodName}\" resultMap=\"base_result_map\" >")
            writeLine("\t\tselect")
            writeLine("\t\t<include refid=\"base_column_list\" />")
            writeLine("\t\tfrom ${tableInfo.tableName} $tableAlias where $whereClause")
            writeLine("\t</select>")
            newLine()
        }
    }

    /**
     * 扩展函数：判断字段是否为日期或时间类型
     */
    private fun FieldInfo.isDateOrTime(): Boolean {
        val sqlTypeLower = this.sqlType.lowercase()
        return sqlTypeLower.contains("date") || sqlTypeLower.contains("time")
    }
}