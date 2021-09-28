package com.bradyrussell.tsqlparser;

import com.bradyrussell.tsqlparser.generated.TSqlParser;
import com.bradyrussell.tsqlparser.generated.TSqlParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.ArrayList;
import java.util.List;

public class TSqlDescriptionFromCommentsListener extends TSqlParserBaseListener {
    private static final String descriptionCommentBeginSymbol = "$";
    private static final String descriptionCommentEndSymbol = "$";

    private final CommonTokenStream tokens;
    private final TokenStreamRewriter rewriter;
    private final ArrayList<String> columnDescriptions = new ArrayList<>();

    private String currentTable = null;
    private String currentSchema = null;

    public TSqlDescriptionFromCommentsListener(CommonTokenStream tokens) {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
    }

    @Override
    public void enterCreate_table(TSqlParser.Create_tableContext ctx) {
        if(ctx.table_name() == null) return;
        if(ctx.table_name().schema != null) {
            currentSchema = removeBrackets(ctx.table_name().schema.getText());
        } else {
            currentSchema = null;
        }

        if(ctx.table_name().table != null) {
            currentTable = removeBrackets(ctx.table_name().table.getText());
        } else {
            currentTable = null;
        }
        //System.out.println("Current Schema: "+ currentSchema);
        //System.out.println("Current Table: "+ currentTable);
    }

    @Override
    public void enterColumn_definition(TSqlParser.Column_definitionContext ctx) {
        String columnName = ctx.id_().get(0).getText().replace("[","").replace("]", "").toLowerCase();
        //System.out.println(columnName);

        int offset = 0;
        if(tokens.get(ctx.stop.getTokenIndex()+1).getType() == TSqlParser.COMMA) {
            offset = 1;
        }

        List<Token> comments = tokens.getHiddenTokensToRight(ctx.stop.getTokenIndex()+offset);

        if(comments == null) return;

        if(comments.size() > 1) {
            throw new UnsupportedOperationException("Multiple comments not allowed. Line: "+comments.get(0).getLine());
        }

        if(comments.size() == 1) {
            String commentText = comments.get(0).getText();
            //System.out.println("Found comment: "+ commentText);

            if(commentText.contains(descriptionCommentBeginSymbol)) {
                //System.out.println("Comment contains description!");
                int index = commentText.indexOf(descriptionCommentBeginSymbol);
                String descriptionFromComment = commentText.substring(index+descriptionCommentBeginSymbol.length());
                if(descriptionFromComment.contains(descriptionCommentEndSymbol)) {
                    int index2 = descriptionFromComment.indexOf(descriptionCommentEndSymbol);
                    descriptionFromComment = descriptionFromComment.substring(0, index2);
                }
                System.out.println("Description for column "+columnName+": "+descriptionFromComment);

                columnDescriptions.add(makeDescription(currentSchema, currentTable, columnName, descriptionFromComment.replace("'","''")));
            } else {
                //System.out.println("Comment is regular comment!");
            }
        }
    }

    public ArrayList<String> getColumnDescriptions() {
        return columnDescriptions;
    }

    private String removeBrackets(String in) {
        return in.replace("[","").replace("]", "");
    }

    public String makeDescription(String schema, String table, String column, String description) {
        return "EXECUTE sp_addextendedproperty @name = N'MS_Description', @value = N'"+description+"', @level0type = N'SCHEMA', @level0name = N'"+(schema == null ? "" : schema)+"', @level1type = N'TABLE', @level1name = N'"+(table == null ? "" : table)+"', @level2type = N'COLUMN', @level2name = N'"+column+"';";
    }
}
