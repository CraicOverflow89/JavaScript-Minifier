package craicoverflow89.javascriptminifier

import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    // Missing Arguments
    if(args.isEmpty()) {
        println("Invalid arguments!")
        println(" jsmin [input] [output] [-rs]")
        exitProcess(-1)
    }

    // File Paths
    val cwd = System.getProperty("user.dir")

    // Parse Arguments
    val arguments = args.partition {
        !it.startsWith("-")
    }

    // Input File
    val inputFile = File("$cwd/${arguments.first[0]}")

    // Invalid Path
    if(!inputFile.exists()) {
        println("Could not find the input file!")
        exitProcess(-1)
    }

    // Output Directory
    val outputPath = when {
        arguments.first.size > 1 -> "%s/%s".format(cwd, arguments.first[1].apply {
            File(this).apply {

                // Existing Directory
                if(exists()) {

                    // Invalid Directory
                    if(!isDirectory) {
                        println("Could not use the output file as a directory!")
                        exitProcess(-1)
                    }
                }

                // Create Directory
                else mkdir()
            }
        })
        inputFile.isDirectory -> inputFile.absolutePath
        else -> inputFile.parentFile.absolutePath
    }

    // Parse Flags
    val flags = arguments.second.filter {
        it.length == 2
    }.joinToString {
        it.substring(1).toLowerCase()
    }

    // Network Logic
    fun minifyRequest(input: String): String {

        // Create Content
        val content = StringBuilder().apply {
            //append(URLEncoder.encode("input", Charsets.UTF_8))
            append(URLEncoder.encode("input", "UTF-8"))
            append("=")
            append(URLEncoder.encode(input, "UTF-8"))
        }.toString()

        // Create Request
        val request = (URL("https://javascript-minifier.com/raw").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("charset", "utf-8")
            setRequestProperty("Content-Length", content.length.toString())
            OutputStreamWriter(outputStream).apply {
                write(content)
                flush()
            }
        }

        // Parse Response
        if(request.responseCode == 200) {

            // Return Result
            return InputStreamReader(request.inputStream).readText()
        }

        // Handle Error
        println("Error minifying code!\n${request.responseCode} ${request.responseMessage}!")
        exitProcess(-1)
    }

    // File Logic
    fun minifyFile(input: File, output: String) {

        // Minify Content
        val response = minifyRequest(input.readText())

        // Write File
        File("$output/${input.nameWithoutExtension}.min.js").writeText(response)
    }

    // Process Logic
    val buffer = if(flags.contains("s")) StringBuffer() else null
    fun minifyProcess(input: File, output: String) {

        // Process Directory
        if(input.isDirectory) input.listFiles().apply {

            // Iterate Files
            filter {
                it.extension == "js"
            }.forEach {
                if(flags.contains("s")) buffer!!.append(minifyRequest(input.readText()))
                else minifyFile(it, output)
            }

            // Process Subdirectories
            if(flags.contains("r")) filter {
                it.isDirectory
            }.forEach {

                // Subdirectory Path
                "$output/${it.name}".apply {

                    // Create Directory
                    File(this).apply {
                        if(!this.exists()) this.mkdir()
                    }

                    // Invoke Process
                    minifyProcess(it, this)
                }
            }
        }

        // Process File
        else {
            if(flags.contains("s")) buffer!!.append(minifyRequest(input.readText()))
            else minifyFile(input, output)
        }
    }

    // Invoke Process
    minifyProcess(inputFile, outputPath)

    // Write Single
    if(flags.contains("s")) {
        File("$outputPath/single.min.js").writeText(buffer.toString())
    }
}