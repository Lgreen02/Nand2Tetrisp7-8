package translator;
import java.io.*;
import java.util.*;

public class VMTranslator {
    public static void main(String[] args) {
        

        String inputPath = "C:\\Users\\carso\\Documents\\MachineLearningHw\\Group_Project\\VMTranslator\\StaticTest";
        List<String> inputFiles = new ArrayList<>();
        String outputFile;

        // Determine input files and output file
        if (inputPath.endsWith(".vm")) {
            inputFiles.add(inputPath);
            outputFile = inputPath.replace(".vm", ".asm");
        } else {
            File dir = new File(inputPath);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".vm"));
            if (files != null) {
                for (File file : files) {
                    inputFiles.add(file.getPath());
                }
            }
            outputFile = new File(inputPath, 
                new File(inputPath).getName() + ".asm").getPath();
        }

        try {
            // Initialize code writer
            CodeWriter writer = new CodeWriter(outputFile);

            // Check if we need bootstrap code
            boolean needsBootstrap = inputFiles.size() > 1 || 
                inputFiles.stream().anyMatch(f -> f.contains("Sys.vm"));
            if (needsBootstrap) {
                writer.writeInit();
            }

            // Translate each VM file
            for (String file : inputFiles) {
                Parser parser = new Parser(file);
                writer.setFileName(new File(file).getName().replace(".vm", ""));

                while (parser.hasMoreCommands()) {
                    parser.advance();
                    String commandType = parser.commandType();

                    switch (commandType) {
                        case "C_ARITHMETIC":
                            writer.writeArithmetic(parser.arg1());
                            break;
                        case "C_PUSH":
                        case "C_POP":
                            writer.writePushPop(commandType, parser.arg1(), parser.arg2());
                            break;
                        case "C_LABEL":
                            writer.writeLabel(parser.arg1());
                            break;
                        case "C_GOTO":
                            writer.writeGoto(parser.arg1());
                            break;
                        case "C_IF":
                            writer.writeIf(parser.arg1());
                            break;
                        case "C_FUNCTION":
                            writer.writeFunction(parser.arg1(), parser.arg2());
                            break;
                        case "C_CALL":
                            writer.writeCall(parser.arg1(), parser.arg2());
                            break;
                        case "C_RETURN":
                            writer.writeReturn();
                            break;
                    }
                }
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error during translation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}