package translator;
import java.io.*;
import java.util.*;

public class Parser {
    private List<String> lines;
    private int currentIndex;
    private String currentCommand;

    public Parser(String inputFile) throws IOException {
        this.lines = new ArrayList<>();
        this.currentIndex = 0;
        this.currentCommand = "";

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        while ((line = reader.readLine()) != null) {
            // Remove comments and trim whitespace
            String cleaned = line.split("//")[0].trim();
            if (!cleaned.isEmpty()) {
                lines.add(cleaned);
            }
        }
        reader.close();
    }

    public boolean hasMoreCommands() {
        return currentIndex < lines.size();
    }

    public void advance() {
        if (hasMoreCommands()) {
            currentCommand = lines.get(currentIndex);
            currentIndex++;
        }
    }

    public String commandType() {
        String cmd = currentCommand.split("\\s+")[0];
        switch (cmd) {
            case "push": return "C_PUSH";
            case "pop": return "C_POP";
            case "add":
            case "sub":
            case "neg":
            case "eq":
            case "gt":
            case "lt":
            case "and":
            case "or":
            case "not":
                return "C_ARITHMETIC";
            case "label": return "C_LABEL";
            case "goto": return "C_GOTO";
            case "if-goto": return "C_IF";
            case "function": return "C_FUNCTION";
            case "call": return "C_CALL";
            case "return": return "C_RETURN";
            default:
                throw new IllegalArgumentException("Unknown command type: " + cmd);
        }
    }

    public String arg1() {
        String type = commandType();
        if (type.equals("C_ARITHMETIC")) {
            return currentCommand;
        }
        return currentCommand.split("\\s+")[1];
    }

    public int arg2() {
        return Integer.parseInt(currentCommand.split("\\s+")[2]);
    }
}
