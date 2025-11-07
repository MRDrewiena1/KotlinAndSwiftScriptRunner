package main.java.ui;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class UnderlineHighlighter implements Highlighter.HighlightPainter {

    private final Color color;

    public UnderlineHighlighter(Color color) {
        this.color = color;
    }

    @Override
    public void paint(Graphics g, int offsetStart, int offsetEnd, Shape bounds, JTextComponent c){
        try {

            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(1.8f));

            Rectangle2D rect0 = c.modelToView2D(offsetStart);
            Rectangle2D rect1 = c.modelToView2D(offsetEnd);

            if(rect0 == null || rect1 == null){
                return;
            }

            int startlineY=(int)(rect0.getY()+rect0.getHeight()-2);
            int endlineY=(int)(rect1.getY()+rect1.getHeight()-2);

            g2d.drawLine((int)rect0.getX(),startlineY,(int)rect1.getX(),endlineY);

            g2d.dispose();

        }catch (BadLocationException e){
            e.printStackTrace();
        }
    }
}
