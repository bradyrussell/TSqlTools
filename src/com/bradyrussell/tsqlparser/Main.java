package com.bradyrussell.tsqlparser;
import com.bradyrussell.tsqlparser.generated.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String input = "CREATE TABLE [dbo].[WorkOut]( -- $ this will be ignored $\n" +
                "    [WorkOutID] [bigint] IDENTITY(1,1) NOT NULL, --$This is a description for WorkOutID! 'quotes'\n" +
                "    [TimeSheetDate] [datetime] NOT NULL, --$This is the desc for timesheetdate\n" +
                "    [DateOut] [datetime] NOT NULL, -- no description here\n" +
                "    [EmployeeID] [int] NOT NULL, -- you can do this $this is the description$ too!\n" +
                "    [IsMainWorkPlace] [bit] NOT NULL,\n" +
                "    [DepartmentUID] [uniqueidentifier] NOT NULL,\n" +
                "    [WorkPlaceUID] [uniqueidentifier] NULL,\n" +
                "    TEAMUID [uniqueidentifier] NULL, -- it only takes the $first$ one $it finds$\n" +
                "    [WorkShiftCD] [nvarchar](10) NULL,\n" +
                "    [WorkHours] [real] NULL,\n" +
                "    [AbsenceCode] [varchar](25) NULL,\n" +
                "    [PaymentType] [char](2) NULL,\n" +
                "    [CategoryID] [int] NULL,\n" +
                "    [oof]  [byte] NULL,\n" +
                " CONSTRAINT [PK_WorkOut] PRIMARY KEY CLUSTERED -- nothing $here$ either!\n" +
                "(\n" +
                "    [WorkOutID] ASC\n" +
                ")WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]\n" +
                ") ON [PRIMARY]";

        System.out.println(input);

        @SuppressWarnings("deprecation") TSqlLexer lexer = new TSqlLexer(new CaseChangingCharStream(new ANTLRInputStream(input), true));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        TSqlParser parser = new TSqlParser(tokenStream);

        TSqlDescriptionFromCommentsListener listener = new TSqlDescriptionFromCommentsListener(tokenStream);

        ParseTreeWalker.DEFAULT.walk(listener, parser.tsql_file());

        ArrayList<String> columnDescriptions = listener.getColumnDescriptions();
        columnDescriptions.forEach(System.out::println);
    }
}
