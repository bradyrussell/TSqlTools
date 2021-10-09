package com.bradyrussell.tsqlparser;

import com.bradyrussell.tsqlparser.generated.TSqlParser;
import com.bradyrussell.tsqlparser.generated.TSqlParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.ArrayList;
import java.util.HashMap;

public class TSqlExtractDescriptionsListener extends TSqlParserBaseListener {
    private final CommonTokenStream tokens;
    private final TokenStreamRewriter rewriter;

    public TSqlExtractDescriptionsListener(CommonTokenStream tokens) {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
    }

    private HashMap<String, String> descriptions = new HashMap<>();

    private String currentColumn = "";
    private String currentTable = "";
    private String currentSchema = "";
    private String currentDescription = "";

    private void resetCurrentValues() {
        currentColumn = "";
        currentDescription = "";
        currentSchema = "";
        currentTable = "";
    }

    private void saveDescription() {
        descriptions.put(String.join(".", currentSchema, currentTable, currentColumn), currentDescription);
        resetCurrentValues();
    }

    @Override
    public void enterExecute_statement(TSqlParser.Execute_statementContext ctx) {
        for (TSqlParser.Execute_statement_arg_namedContext s : ctx.execute_body().execute_statement_arg().execute_statement_arg_named()) {
            String value = readTSqlNString(s.execute_parameter().getText());
            switch (s.LOCAL_ID().getText()) {
                case "@name" -> {
                    if(!value.equalsIgnoreCase("MS_Description")) {
                        System.out.println("Not an MS Description, skipping!");
                        resetCurrentValues();
                        return;
                    }
                }
                case "@value" -> {
                    currentDescription = value;
                }
                case "@level0name" -> {
                    currentSchema = value;
                }
                case "@level1name" -> {
                    currentTable = value;
                }
                case "@level2name" -> {
                    currentColumn = value;
                }
            }
        }
        saveDescription();
    }

    public String readTSqlNString(String nstring) {
        return nstring.startsWith("N'") ? nstring.substring(2, nstring.length()-1).replace("''","'") : nstring;
    }

    public HashMap<String, String> getDescriptions() {
        return descriptions;
    }
}
