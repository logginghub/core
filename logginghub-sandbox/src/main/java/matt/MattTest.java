package matt;

import net.miginfocom.swing.MigLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by james on 06/07/2016.
 */
public class MattTest {
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {

                JFrame frame = new JFrame();
                frame.setSize(800, 600);
                frame.setLocationRelativeTo(null);

                JPanel a = new JPanel();
                JPanel b = new JPanel();
                JPanel c = new JPanel();
                JPanel d = new JPanel();

                a.setBackground(Color.red);
                b.setBackground(Color.green);
                c.setBackground(Color.blue);
                d.setBackground(Color.black);

                frame.getContentPane().setLayout(new MigLayout("", "[1500,grow,fill,push][300,fill]", "[20,fill][580,fill]"));

                frame.getContentPane().add(a, "cell 0 0");
                frame.getContentPane().add(b, "cell 0 1");
                frame.getContentPane().add(c, "cell 1 0");
                frame.getContentPane().add(d, "cell 1 1");

                frame.setVisible(true);

            }
        });


    }
}
