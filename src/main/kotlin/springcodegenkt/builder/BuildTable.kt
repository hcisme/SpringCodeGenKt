package io.github.hcisme.springcodegenkt.builder

import io.github.hcisme.springcodegenkt.bean.Constant
import io.github.hcisme.springcodegenkt.bean.FieldInfo
import io.github.hcisme.springcodegenkt.bean.TableInfo
import io.github.hcisme.springcodegenkt.utils.Tools
import io.github.hcisme.springcodegenkt.utils.YamlConfigManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

object BuildTable {
    private val logger: Logger = LoggerFactory.getLogger(BuildTable::class.java)
    private const val SQL_SHOW_TABLE_STATUS = "show table status"
    private const val SQL_SHOW_TABLE_FIELDS = "show full fields from %s"
    private const val SQL_SHOW_TABLE_KEY_INDEX = "show index from %s"
    private val connect: Connection by lazy {
        val config = YamlConfigManager.config.db
        runCatching {
            Class.forName(config.driverClassName)
            DriverManager.getConnection(config.url, config.username, config.password)
        }.onFailure {
            logger.error("数据库连接失败", it)
        }.getOrThrow()
    }

    fun getTables(): List<TableInfo> {
        val tableInfoList = mutableListOf<TableInfo>()

        connect.prepareStatement(SQL_SHOW_TABLE_STATUS).use { ps ->
            ps.executeQuery().use { tableResult ->
                tableResult.iterate {
                    val tableName = getString("name")
                    val comment = getString("comment")

                    val beanName = buildString {
                        val rawName = if (Constant.IGNORE_TABLE_PREFIX) {
                            tableName.substringAfter('_')
                        } else {
                            tableName
                        }
                        append(processField(field = rawName, upperCaseFirstLetter = true))
                    }
                    val tableInfo = TableInfo(
                        tableName = tableName,
                        beanName = beanName,
                        beanParamName = beanName + Constant.SUFFIX_BEAN_PARAM,
                        comment = comment
                    )
                    readFieldInfo(tableInfo)
                    readKeyIndex(tableInfo)
                    tableInfoList.add(tableInfo)
                }
            }
        }

        return tableInfoList
    }

    private fun readFieldInfo(tableInfo: TableInfo) {
        val fieldInfoList = mutableListOf<FieldInfo>()
        runCatching {
            connect.prepareStatement(SQL_SHOW_TABLE_FIELDS.format(tableInfo.tableName)).use { ps ->
                ps.executeQuery().use { rs ->
                    rs.iterate {
                        val fieldName = getString("field")
                        val propertyName = processField(field = fieldName)
                        val sqlType = getString("type").substringBefore("(").trim()
                        val extra = getString("extra")
                        fieldInfoList.add(
                            FieldInfo(
                                fieldName = fieldName,
                                propertyName = propertyName,
                                sqlType = sqlType,
                                ktType = Tools.sqlTypeToKotlinType(sqlType = sqlType),
                                comment = getString("comment"),
                                isAutoIncrement = extra.equals(other = "auto_increment", ignoreCase = true),
                            )
                        )
                        if (!tableInfo.haveDate) {
                            tableInfo.haveDate = Tools.isDateType(sqlType)
                        }
                        if (!tableInfo.haveDateTime) {
                            tableInfo.haveDateTime = Tools.isDateTimeType(sqlType)
                        }
                        if (!tableInfo.haveBigDecimal) {
                            tableInfo.haveBigDecimal = Tools.isBigDecimalType(sqlType)
                        }
                    }
                }
            }
            tableInfo.fieldList = fieldInfoList
        }.onFailure { exception ->
            logger.error("读取表 ${tableInfo.tableName} 的字段信息失败", exception)
        }
    }

    private fun readKeyIndex(tableInfo: TableInfo) {
        runCatching {
            connect.prepareStatement(SQL_SHOW_TABLE_KEY_INDEX.format(tableInfo.tableName)).use { ps ->
                ps.executeQuery().use { rs ->
                    val tempIndexMap = mutableMapOf<String, MutableList<FieldInfo>>()

                    rs.iterate {
                        val keyName = getString("key_name")
                        val nonUnique = getInt("non_unique")
                        val columnName = getString("column_name")
                        if (nonUnique == 1) return@iterate
                        val fieldInfo = tableInfo.fieldList.find { it.fieldName == columnName }
                            ?: run {
                                logger.warn("在表 ${tableInfo.tableName} 中找不到索引列 '$columnName' 对应的字段信息")
                                return@iterate
                            }

                        tempIndexMap.getOrPut(keyName) { mutableListOf() }.add(fieldInfo)
                    }
                    tableInfo.keyIndexMap.putAll(tempIndexMap)
                }
            }
        }.onFailure { exception ->
            logger.error("读取表 ${tableInfo.tableName} 的索引信息失败", exception)
        }
    }

    private fun processField(field: String, upperCaseFirstLetter: Boolean = false): String {
        return field.split('_')
            .mapIndexed { index, part ->
                when {
                    index == 0 && !upperCaseFirstLetter -> part.lowercase()
                    else -> part.replaceFirstChar(Char::uppercaseChar)
                }
            }
            .joinToString("")
    }
}

private inline fun ResultSet.iterate(block: ResultSet.() -> Unit) {
    while (next()) {
        block()
    }
}