package main.java.ui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeywordHighlighter {
    public enum Language {KOTLIN,SWIFT}

    private static final  int DELAY_MS = 300;
    private static final Color KEYWORD_COLOR = Color.ORANGE;

    private final JTextPane textPane;
    private final java.util.Timer timer = new Timer();
    private TimerTask pendingTask;
    private Language language;
    private Pattern currentPattern;

    private static final String[] KOTLIN_KEYWORDS = {
            "fun", "val", "var", "if", "else", "for", "while", "when", "class", "return"
    };

    private static final String[] SWIFT_KEYWORDS = {
            "func", "let", "var", "if", "else", "for", "while", "switch", "case", "class", "struct", "return"
    };

    public KeywordHighlighter(JTextPane textPane, Language language) {
        this.textPane = textPane;
        this.language = language;
        this.currentPattern = buildPatternFor(language);

        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleHighlight();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleHighlight();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleHighlight();
            }

        });
    }

    public void setLanguage(Language language) {
        this.language = language;
        this.currentPattern = buildPatternFor(language);
        highlight();
    }

    private Pattern buildPatternFor(Language language) {
        String[] words = (language == Language.KOTLIN) ? KOTLIN_KEYWORDS : SWIFT_KEYWORDS;
        return Pattern.compile("\\b(" + String.join("|", words) + ")\\b", Pattern.CASE_INSENSITIVE);
    }

    private void scheduleHighlight(){
        if(pendingTask != null){
            pendingTask.cancel();
        }
        pendingTask = new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(KeywordHighlighter.this::highlight);
            }
        };
        timer.schedule(pendingTask,DELAY_MS);
    }
    private void highlight(){
        StyledDocument document = textPane.getStyledDocument();
        Style defStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
        Style keywordStyle = document.addStyle("keyword",null);
        StyleConstants.setForeground(keywordStyle,KEYWORD_COLOR);
        StyleConstants.setBold(keywordStyle, true);

        String text = textPane.getText();
        document.setCharacterAttributes(0,text.length(),defStyle,true);

        Matcher matcher = currentPattern.matcher(text);
        while(matcher.find()){
            document.setCharacterAttributes(matcher.start(),
                    matcher.end() - matcher.start(),
                    keywordStyle,
                    true);
        }
    }



}
