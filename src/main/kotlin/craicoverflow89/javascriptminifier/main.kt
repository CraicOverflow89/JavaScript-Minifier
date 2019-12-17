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
        println(" jsmin [input] [output] [-r]")
        exitProcess(-1)
    }

    // File Paths
    val cwd = System.getProperty("user.dir")

    // Input File
    val inputFile = File("$cwd/${args[0]}")

    // Invalid Path
    if(!inputFile.exists()) {
        println("Could not find the input file!")
        exitProcess(-1)
    }

    // Output Directory
    val outputPath = if(args.size > 1 && !args[1].startsWith("-")) {
        "%s/%s".format(cwd, args[1].apply {
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
    } else if(inputFile.isDirectory) inputFile.absolutePath else inputFile.parentFile.absolutePath

    // Parse Flags
    val flags: String = if(args.size > 2 || (args.size > 1 && args[1].startsWith("-"))) args.let {
        if(args[1].startsWith("-")) args[1]
        else args[2]
    }.let {

        // Invalid Argument
        if(!it.startsWith("-") || it.length < 2) {
            println("Invalid arguments!")
            println(" jsmin [input] [output] [-r]")
            exitProcess(-1)
        }

        // Parse Chars
        it.substring(1)

    } else ""
    // NOTE: should just treat final element in args as flags
    //       if(args.size > 1 && last element startsWith("-"))

    // Network Logic
    fun minifyRequest(input: File): String {

        // Create Content
        val content = StringBuilder().apply {
            //append(URLEncoder.encode("input", Charsets.UTF_8))
            append(URLEncoder.encode("input", "UTF-8"))
            append("=")
            append(URLEncoder.encode(input.readText(), "UTF-8"))
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
        println("Error minifying ${input.name}!\n${request.responseCode} ${request.responseMessage}!")
        exitProcess(-1)
    }

    // File Logic
    fun minifyFile(input: File, output: String) {

        // Minify Content
        val response = minifyRequest(input)

        // Write File
        File("$output/${input.nameWithoutExtension}.min.js").writeText(response)
    }

    // Process Logic
    fun minifyProcess(input: File, output: String) {

        // Process Directory
        if(input.isDirectory) input.listFiles().apply {

            // Iterate Files
            filter {
                it.extension == "js"
            }.forEach {
                minifyFile(it, output)
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
        else minifyFile(input, output)
    }

    // Invoke Process
    minifyProcess(inputFile, outputPath)
}