import com.bradyrussell.tsqlparser.CaseChangingCharStream;
import com.bradyrussell.tsqlparser.TSqlDescriptionFromCommentsListener;
import com.bradyrussell.tsqlparser.generated.TSqlLexer;
import com.bradyrussell.tsqlparser.generated.TSqlParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class TestDescriptionFromComments {
    @Test
    void TestDescriptionFromComments() {
        //Arrange
        String tsql = "\t\tCREATE TABLE description_test (\n" +
                "\t\t[id] int not null identity, --Regular comments /* are ignored */\n" +
                "\t\t[testcol] nvarchar(255), /*$You can use any type of comment. $*/\n" +
                "\t\t[othercol] nvarchar(255), -- $You don't have to close the description tag.\n" +
                "\t\t[name] nvarchar(255), --You can have comments before $and$ after the description.\n" +
                "\t\t[date_of_birth] nvarchar(255), --$Subsequent$ description blocks are $ignored$.\n" +
                "\t\t[state] nvarchar(255), --$This is easier than copy & pasting the command. :)\n" +
                "\t\t[country] nvarchar(255)";

        //Act
        @SuppressWarnings("deprecation") TSqlLexer lexer = new TSqlLexer(new CaseChangingCharStream(new ANTLRInputStream(tsql), true));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        TSqlParser parser = new TSqlParser(tokenStream);
        TSqlDescriptionFromCommentsListener listener = new TSqlDescriptionFromCommentsListener(tokenStream);
        ParseTreeWalker.DEFAULT.walk(listener, parser.tsql_file());
        ArrayList<String> columnDescriptions = listener.getColumnDescriptions();

        //Assert
        String expectedResult = "EXECUTE sp_addextendedproperty @name = N'MS_Description', @value = N'You can use any type of comment. ', @level0type = N'SCHEMA', @level0name = N'', @level1type = N'TABLE', @level1name = N'description_test', @level2type = N'COLUMN', @level2name = N'testcol';\n" +
                "EXECUTE sp_addextendedproperty @name = N'MS_Description', @value = N'You don''t have to close the description tag.', @level0type = N'SCHEMA', @level0name = N'', @level1type = N'TABLE', @level1name = N'description_test', @level2type = N'COLUMN', @level2name = N'othercol';\n" +
                "EXECUTE sp_addextendedproperty @name = N'MS_Description', @value = N'and', @level0type = N'SCHEMA', @level0name = N'', @level1type = N'TABLE', @level1name = N'description_test', @level2type = N'COLUMN', @level2name = N'name';\n" +
                "EXECUTE sp_addextendedproperty @name = N'MS_Description', @value = N'Subsequent', @level0type = N'SCHEMA', @level0name = N'', @level1type = N'TABLE', @level1name = N'description_test', @level2type = N'COLUMN', @level2name = N'date_of_birth';\n" +
                "EXECUTE sp_addextendedproperty @name = N'MS_Description', @value = N'This is easier than copy & pasting the command. :)', @level0type = N'SCHEMA', @level0name = N'', @level1type = N'TABLE', @level1name = N'description_test', @level2type = N'COLUMN', @level2name = N'state';";

        Assertions.assertEquals(expectedResult, String.join("\n", columnDescriptions));
    }
}
