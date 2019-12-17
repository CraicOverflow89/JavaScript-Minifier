package craicoverflow89.javascriptminifier

import java.io.File
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
    val flags: String = if(args.size > 2 || args[1].startsWith("-")) args.let {
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

    // Minify Logic
    fun minifyFile(input: File, output: String) {

        // Read File
        val content = input.readText()

        // NOTE: this is where a POST request is required

        // Write File
        File("$output/${input.nameWithoutExtension}.min.js").writeText(content)
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