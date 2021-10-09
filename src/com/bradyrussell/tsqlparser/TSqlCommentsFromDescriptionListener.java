package com.bradyrussell.tsqlparser;

import com.bradyrussell.tsqlparser.generated.TSqlParser;
import com.bradyrussell.tsqlparser.generated.TSqlParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.ArrayList;
import java.util.HashMap;

public class TSqlCommentsFromDescriptionListener extends TSqlParserBaseListener {
    private static final String descriptionCommentBeginSymbol = "$";
    private static final String descriptionCommentEndSymbol = "$";

    private final CommonTokenStream tokens;
    private final TokenStreamRewriter rewriter;

    private final HashMap<String, String> descriptions;
    private String currentTable = "";
    private String currentSchema = "";

    public TSqlCommentsFromDescriptionListener(CommonTokenStream tokens, HashMap<String, String> descriptions) {
        this.tokens = tokens;
        this.descriptions = descriptions;
        this.rewriter = new TokenStreamRewriter(tokens);
    }

    @Override
    public void enterCreate_table(TSqlParser.Create_tableContext ctx) {
        if(ctx.table_name() == null) return;
        if(ctx.table_name().schema != null) {
            currentSchema = removeBrackets(ctx.table_name().schema.getText());
        } else {
            currentSchema = "";
        }

        if(ctx.table_name().table != null) {
            currentTable = removeBrackets(ctx.table_name().table.getText());
        } else {
            currentTable = "";
        }
    }

    @Override
    public void enterColumn_definition(TSqlParser.Column_definitionContext ctx) {
        String columnName = removeBrackets(ctx.id_().get(0).getText().toLowerCase());

        int offset = 0;
        if(tokens.get(ctx.stop.getTokenIndex()+1).getType() == TSqlParser.COMMA) {
            offset = 1;
        }

        String descriptionKey = String.join(".", currentSchema, currentTable, columnName);
        if(!descriptions.containsKey(descriptionKey)) return;
        var description = descriptions.get(descriptionKey);

        rewriter.insertAfter(ctx.stop.getTokenIndex()+offset, "\t\t\t/*"+descriptionCommentBeginSymbol+description+descriptionCommentEndSymbol+"*/");
    }

    public String getResultTSql() {
        return rewriter.getText();
    }

    private String removeBrackets(String in) {
        return in.replace("[","").replace("]", "");
    }
}
