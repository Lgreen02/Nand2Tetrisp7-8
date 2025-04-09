package translator;

import java.io.*;

public class CodeWriter {
    private BufferedWriter file;
    private String filename;
    private int labelCounter;

    public CodeWriter(String outputFilename) throws IOException {
        this.file = new BufferedWriter(new FileWriter(outputFilename));
        this.filename = "";
        this.labelCounter = 0;
    }

    public void setFileName(String name) {
        this.filename = name;
    }

    public void writeInit() throws IOException {
        file.write("@256\nD=A\n@SP\nM=D\n");
        writeCall("Sys.init", 0);
    }

    public void writeArithmetic(String command) throws IOException {
        file.write("// " + command + "\n");  // Optional debug comment

        if (command.equals("add") || command.equals("sub") || command.equals("and") || command.equals("or")) {
            file.write("@SP\nAM=M-1\nD=M\nA=A-1\n");
            String op;
            switch (command) {
                case "add": op = "D+M"; break;
                case "sub": op = "M-D"; break;
                case "and": op = "D&M"; break;
                case "or":  op = "D|M"; break;
                default: op = ""; // shouldn't happen
            }
            file.write("M=" + op + "\n");
        }
        else if (command.equals("neg") || command.equals("not")) {
            file.write("@SP\nA=M-1\n");
            String op = command.equals("neg") ? "-M" : "!M";
            file.write("M=" + op + "\n");
        }
        else if (command.equals("eq") || command.equals("gt") || command.equals("lt")) {
            String jump;
            switch (command) {
                case "eq": jump = "JEQ"; break;
                case "gt": jump = "JGT"; break;
                case "lt": jump = "JLT"; break;
                default: jump = ""; // shouldn't happen
            }
            String trueLabel = "TRUE_" + labelCounter;
            String endLabel = "END_" + labelCounter;
            labelCounter++;

            file.write("@SP\nAM=M-1\nD=M\nA=A-1\nD=M-D\n");
            file.write("@" + trueLabel + "\nD;" + jump + "\n");
            file.write("@SP\nA=M-1\nM=0\n");  // false
            file.write("@" + endLabel + "\n0;JMP\n");
            file.write("(" + trueLabel + ")\n@SP\nA=M-1\nM=-1\n");  // true
            file.write("(" + endLabel + ")\n");
        }
    }

    public void writePushPop(String cmd, String segment, int index) throws IOException {
        if (cmd.equals("C_PUSH")) {
            if (segment.equals("constant")) {
                file.write("@" + index + "\nD=A\n");
            }
            else if (segment.equals("local") || segment.equals("argument") || 
                     segment.equals("this") || segment.equals("that")) {
                String base;
                switch (segment) {
                    case "local": base = "LCL"; break;
                    case "argument": base = "ARG"; break;
                    case "this": base = "THIS"; break;
                    case "that": base = "THAT"; break;
                    default: base = ""; // shouldn't happen
                }
                file.write("@" + base + "\nD=M\n@" + index + "\nA=D+A\nD=M\n");
            }
            else if (segment.equals("temp")) {
                file.write("@" + (5 + index) + "\nD=M\n");
            }
            else if (segment.equals("pointer")) {
                String base = index == 0 ? "THIS" : "THAT";
                file.write("@" + base + "\nD=M\n");
            }
            else if (segment.equals("static")) {
                file.write("@" + filename + "." + index + "\nD=M\n");
            }
            file.write("@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        }
        else if (cmd.equals("C_POP")) {
            if (segment.equals("local") || segment.equals("argument") || 
                segment.equals("this") || segment.equals("that")) {
                String base;
                switch (segment) {
                    case "local": base = "LCL"; break;
                    case "argument": base = "ARG"; break;
                    case "this": base = "THIS"; break;
                    case "that": base = "THAT"; break;
                    default: base = ""; // shouldn't happen
                }
                file.write("@" + base + "\nD=M\n@" + index + "\nD=D+A\n@R13\nM=D\n");
                file.write("@SP\nAM=M-1\nD=M\n@R13\nA=M\nM=D\n");
            }
            else if (segment.equals("temp")) {
                file.write("@SP\nAM=M-1\nD=M\n@" + (5 + index) + "\nM=D\n");
            }
            else if (segment.equals("pointer")) {
                String base = index == 0 ? "THIS" : "THAT";
                file.write("@SP\nAM=M-1\nD=M\n@" + base + "\nM=D\n");
            }
            else if (segment.equals("static")) {
                file.write("@SP\nAM=M-1\nD=M\n@" + filename + "." + index + "\nM=D\n");
            }
        }
    }

    public void writeLabel(String label) throws IOException {
        file.write("(" + label + ")\n");
    }

    public void writeGoto(String label) throws IOException {
        file.write("@" + label + "\n0;JMP\n");
    }

    public void writeIf(String label) throws IOException {
        file.write("@SP\nAM=M-1\nD=M\n");
        file.write("@" + label + "\nD;JNE\n");
    }

    public void writeCall(String functionName, int numArgs) throws IOException {
        String returnLabel = "RETURN_" + labelCounter;
        labelCounter++;
        file.write("@" + returnLabel + "\nD=A\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        for (String seg : new String[]{"LCL", "ARG", "THIS", "THAT"}) {
            file.write("@" + seg + "\nD=M\n@SP\nA=M\nM=D\n@SP\nM=M+1\n");
        }
        file.write("@SP\nD=M\n");
        file.write("@" + (numArgs + 5) + "\nD=D-A\n@ARG\nM=D\n");
        file.write("@SP\nD=M\n@LCL\nM=D\n");
        writeGoto(functionName);
        file.write("(" + returnLabel + ")\n");
    }

    public void writeReturn() throws IOException {
        file.write("// return\n");
        file.write("@LCL\nD=M\n@R13\nM=D\n");  // FRAME = LCL
        file.write("@5\nA=D-A\nD=M\n@R14\nM=D\n");  // RET = *(FRAME-5)
        file.write("@SP\nAM=M-1\nD=M\n@ARG\nA=M\nM=D\n");  // *ARG = pop()
        file.write("@ARG\nD=M+1\n@SP\nM=D\n");  // SP = ARG + 1
        int i = 1;
        for (String seg : new String[]{"THAT", "THIS", "ARG", "LCL"}) {
            file.write("@R13\nD=M\n@" + i + "\nA=D-A\nD=M\n@" + seg + "\nM=D\n");
            i++;
        }
        file.write("@R14\nA=M\n0;JMP\n");
    }

    public void writeFunction(String functionName, int numLocals) throws IOException {
        file.write("(" + functionName + ")\n");
        for (int i = 0; i < numLocals; i++) {
            writePushPop("C_PUSH", "constant", 0);
        }
    }

    public void close() throws IOException {
        file.close();
    }
}
