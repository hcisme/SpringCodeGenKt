## 🚀 SpringBoot Mybatis 项目代码模板生成工具

### 📄 简介

本项目是一个高效的代码生成工具，专为使用 **Spring Boot** 和 **MyBatis** 技术的项目设计。它能够根据预设的模板和配置，自动生成完整的项目基础架构代码，包括 **Controller**、**Service**、**Mappers** 接口以及对应的 **Mappers XML** 文件，极大地提高了项目初始化的效率。

### ✨ 主要功能

  * **项目骨架生成：** 一键生成完整的 Spring Boot Mybatis 项目结构。
  * **分层代码生成：** 自动生成 `Controller`、`Service` 和 `Mapper` 层的 Kotlin 代码。
  * **MyBatis XML 生成：** 自动生成与 Mapper 接口对应的 SQL 映射 XML 文件。
  * **Kotlin 技术栈：** 所有生成的代码均采用 **Kotlin** 语言编写。

### 🛠️ 技术栈

  * **核心语言：** Kotlin
  * **核心框架：** Spring Boot > 3
  * **数据库框架：** Mybatis

### 📦 安装与启动

#### 1\. 环境准备

确保你的开发环境中已安装以下工具：

  * Java Development Kit (JDK) 17 或更高版本
  * Git

#### 2\. 克隆项目

使用 Git 将项目仓库克隆到本地：

```bash
git clone https://github.com/hcisme/SpringCodeGenKt
cd SpringCodeGenKt
```

#### 3\. 构建项目
```bash
./gradlew.bat clean shadowJar
```

#### 4\. 安装依赖

### ⚙️ 使用方式

1.  **修改配置文件：**
    打开项目中的配置文件（`application.yml`），根据文件中的**注释**，修改数据库连接信息、要生成的表名、包路径等核心配置参数。

2.  **执行代码生成：**
    配置完成后，运行以下命令执行 JAR 包，启动代码生成过程：

    ```bash
    java -jar <jar包名>.jar
    ```

    > **注意：** 请将 `<jar包名>` 替换为你在构建步骤中生成的实际文件名。

3.  **查看结果：**
    程序运行完成后，生成的代码文件（Controller, Service, Mappers, XML）将位于指定的输出目录下。

### 🤝 贡献

欢迎通过 Pull Request 或提交 Issue 的方式为本项目做出贡献。

### 📜 许可证

本项目采用 **[许可证名称，例如 MIT]** 许可证。详情请参阅项目根目录下的 [LICENSE](./LICENSE) 文件。
