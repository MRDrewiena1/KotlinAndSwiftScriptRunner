package main.java;

import main.java.ui.KeywordHighlighter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptRunner extends JFrame {
     private final JTextPane editor = new JTextPane();
     private final JTextArea output = new JTextArea();
     private final JComboBox<String> langSelect = new JComboBox<>(new String[]{"Kotlin","Swift"});
     private final JButton runButton = new JButton("Run");
     private final JButton stopButton = new JButton("Stop");
     private final JLabel statusLabel = new JLabel("Idle");
     private final JLabel exitLabel = new JLabel("Exit Code: ");

     private Process runningProcess;

     public ScriptRunner() {
         super("KotlinAndSwiftScriptRunner");

         setLayout(new BorderLayout());
         JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                 new JScrollPane(editor),new JScrollPane(output));
         splitPane.setDividerLocation(450);
         add(splitPane, BorderLayout.CENTER);

         JPanel topControlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         topControlsPanel.add(new JLabel("Language:"));
         topControlsPanel.add(langSelect);
         topControlsPanel.add(runButton);
         topControlsPanel.add(stopButton);
         topControlsPanel.add(statusLabel);
         topControlsPanel.add(Box.createHorizontalStrut(240));
         topControlsPanel.add(exitLabel);
         add(topControlsPanel, BorderLayout.NORTH);

         output.setEditable(false);
         loadCustomFont();

         runButton.addActionListener(e -> runScript());
         stopButton.addActionListener(e -> stopScript());

         KeywordHighlighter highlighter = new KeywordHighlighter(editor,KeywordHighlighter.Language.KOTLIN);
         langSelect.addActionListener(e -> {
             String selectedLanguage = (String) langSelect.getSelectedItem();
             if (selectedLanguage.equals("Kotlin")) {
                 highlighter.setLanguage(KeywordHighlighter.Language.KOTLIN);
             }else {
                 highlighter.setLanguage(KeywordHighlighter.Language.SWIFT);
             }
         });

         setSize(900,600);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setVisible(true);
     }

     private void runScript() {
         if(runningProcess != null && runningProcess.isAlive()) {
             JOptionPane.showMessageDialog(this, "Process already running!!!");
             return;
         }
         String code = editor.getText();
         String language = (String) langSelect.getSelectedItem();
         new Thread(()->{
             try {
             Path tmpFile = Files.createTempFile("script", language.equals("Kotlin") ? ".kts" : ".swift");
             Files.writeString(tmpFile, code);

             ProcessBuilder pb;
             if(language.equals("Kotlin")) {
                 pb = new ProcessBuilder("kotlinc","-script",tmpFile.toString());
             } else {
                 pb = new ProcessBuilder("/usr/bin/env","swift",tmpFile.toString());
             }

             pb.redirectErrorStream(true);
             runningProcess = pb.start();

             statusLabel.setText("Running...");
             exitLabel.setText("Exit Code: -");
             exitLabel.setForeground(Color.BLACK);
             output.setText("");

             Thread outputThread = new Thread(()->{
                 try {
                     BufferedReader reader = new BufferedReader(
                             new InputStreamReader(runningProcess.getInputStream(), StandardCharsets.UTF_8));
                     char[] buffer = new char[256];
                     int n;
                     while ((n = reader.read(buffer)) != -1){
                         String s = new String(buffer,0,n);
                         SwingUtilities.invokeLater(()->output.append(s));
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             });
             outputThread.start();

             int exitCode = runningProcess.waitFor();
             outputThread.join();

             SwingUtilities.invokeLater(()->{
                 statusLabel.setText("Idle");
                 exitLabel.setText("Exit: "+exitCode);
                 exitLabel.setForeground(exitCode == 0 ? Color.GREEN : Color.RED);
             });

             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 throw new RuntimeException(e);
             }catch (IOException e) {
                 output.setText("Error: " + e.getMessage());
             } finally {
                 runningProcess = null;
             }
             }).start();

     }

     private void stopScript() {
         if(runningProcess != null && runningProcess.isAlive()) {
             runningProcess.destroy();
             statusLabel.setText("Stopped");
         }
     }

     private void loadCustomFont(){
         try (InputStream is = getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf")) {
             if (is == null) {
                 System.err.println("Could not load Font");
                 return;
             }
             Font customFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(13f);
             GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
             ge.registerFont(customFont);

             editor.setFont(customFont);
             output.setFont(customFont);
             statusLabel.setFont(customFont);
             exitLabel.setFont(customFont);
             langSelect.setFont(customFont);
             runButton.setFont(customFont.deriveFont(Font.BOLD,13f));
             stopButton.setFont(customFont.deriveFont(Font.BOLD,13f));

         } catch (Exception e) {
             e.printStackTrace();
         }
     }

     public static void main(String[] args) {
         SwingUtilities.invokeLater(ScriptRunner::new);
     }
}
