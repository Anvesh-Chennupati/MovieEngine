import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class hw3 {
    private JPanel MainPanel;


    private void titleAlign(JFrame frame) {

        Font font = frame.getFont();

        String currentTitle = frame.getTitle().trim();
        FontMetrics fm = frame.getFontMetrics(font);
        int frameWidth = frame.getWidth();
        int titleWidth = fm.stringWidth(currentTitle);
        int spaceWidth = fm.stringWidth(" ");
        int centerPos = (frameWidth / 2) - (titleWidth / 2);
        int spaceCount = centerPos / spaceWidth;
        String pad = "";
        pad = String.format("%" + (spaceCount - 14) + "s", pad);
        frame.setTitle(pad + currentTitle);

    }

    public  void startQueryEngine() {
        // initialize UI framework

//        JFrame frame = new JFrame("Movie Query Engine");
//        frame.setContentPane(new hw3().MainPanel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.pack();
//        frame.setVisible(true);

        JFrame frame = new JFrame();
        frame.setPreferredSize(new Dimension(900, 900));
        frame.setTitle("Movie Query Engine");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                titleAlign(frame);
            }

        });

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    public static void main(String args[]){
//        System.out.println("Hello");
        hw3 startPoint = new hw3();
        startPoint.startQueryEngine();
    }
}
