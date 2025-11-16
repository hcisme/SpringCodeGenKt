package io.github.hcisme.springcodegenkt.utils

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 配置管理类
 */
object YamlConfigManager {
    private val logger: Logger = LoggerFactory.getLogger(YamlConfigManager::class.java)
    lateinit var config: AppConfig
        private set

    init {
        initializeFromResources()
    }

    /**
     * 从资源文件初始化
     */
    private fun initializeFromResources(resourcePath: String = "application.yml") {
        try {
            val inputStream = javaClass.classLoader.getResourceAsStream(resourcePath)
                ?: throw IllegalArgumentException("资源文件不存在: $resourcePath")

            val yamlContent = inputStream.bufferedReader().use { it.readText() }
            config = Yaml.default.decodeFromString<AppConfig>(yamlContent)
        } catch (e: Exception) {
            logger.error("应用配置文件初始化失败", e)
        }
    }
}

@Serializable
data class Application(
    val name: String
)

@Serializable
data class DatabaseConfig(
    val url: String,
    val username: String,
    val password: String,
    @SerialName("driver-class-name")
    val driverClassName: String
)

@Serializable
data class PathConfig(
    @SerialName("output-dir")
    val outputDir: String
)

@Serializable
data class PackageConfig(
    val base: String,
    val pojo: String,
    val param: String
)

@Serializable
data class AppConfig(
    val application: Application,
    val db: DatabaseConfig,
    @SerialName("ignore-table-prefix")
    val ignoreTablePrefix: Boolean,
    @SerialName("suffix-bean-param")
    val suffixBeanParam: String,
    val path: PathConfig,
    @SerialName("package-config")
    val packageConfig: PackageConfig
)
