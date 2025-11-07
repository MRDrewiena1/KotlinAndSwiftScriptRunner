package main.java.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ErrorLinkHandler {

    private static final Pattern errorPattern =
        Pattern.compile("(.+?):(\\d+):(\\d+):\\s*(error):.*", Pattern.CASE_INSENSITIVE);

    private final JTextPane editorPane;
    private final JTextArea outputPane;

    private Object lastHighlight = null;

    public ErrorLinkHandler(JTextPane editorPane,JTextArea outputPane) {
        this.editorPane = editorPane;
        this.outputPane = outputPane;

        outputPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                handleClick(e);
            }
        });

        editorPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e){
                clearUnderline();
            }
        });
    }

    private void handleClick(MouseEvent e){
        int pos = outputPane.viewToModel2D(e.getPoint());
        if (pos < 0)
            return;

        String text = outputPane.getText();
        Matcher matcher = errorPattern.matcher(text);

        while(matcher.find()){
            int start = matcher.start();
            int end = matcher.end();

            if(pos >= start && pos <= end){
                int line = Integer.parseInt(matcher.group(2));
                int column = Integer.parseInt(matcher.group(3));
                goToEditorLine(line,column);
                return;
            }
        }
    }

    private void goToEditorLine(int line,int column){

        String text = editorPane.getText();

        int targetLine = Math.max(1,line);
        int offset = 0;
        int currentLine = 1;
        boolean found = false;

        for(int i = 0; i<text.length() ; i++){
            if(currentLine == targetLine&&!found){
                offset = i;
                found = true;
            }
            if(text.charAt(i) == '\n'){
                currentLine++;
            }
        }

        if(!found){
            offset = text.length()-1;
        }

        int targetOffset = offset + Math.max(0,column-1);
        targetOffset = Math.min(targetOffset,text.length()-1);

        int start = offset;
        int end = text.indexOf('\n',start);
        if(end == -1){
            end = text.length();
        }

        underlineError(start,end);

        editorPane.requestFocus();
        editorPane.setCaretPosition(targetOffset);
        editorPane.getCaret().setSelectionVisible(true);
    }

    private void underlineError(int start, int end){

        Highlighter highlighter = editorPane.getHighlighter();
        Color color = Color.RED;
        clearUnderline();
        Highlighter.HighlightPainter painter = new UnderlineHighlighter(color);
        try {
            lastHighlight = highlighter.addHighlight(start,end,painter);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }

    }

    private void clearUnderline(){

        if(lastHighlight != null){
            editorPane.getHighlighter().removeHighlight(lastHighlight);
            lastHighlight = null;
        }
    }
}
