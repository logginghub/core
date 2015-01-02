package com.logginghub.utils.swing;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

import javax.swing.JFrame;

import com.logginghub.utils.ResourceUtils;

public class MainFrame extends JFrame implements WindowListener {

    private static final long serialVersionUID = 1L;

    public MainFrame(String title) {
        super.setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(this);
    }

    static public Image loadImage(String name) {
        URL url = ResourceUtils.openURL(name);
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image image = tk.getImage(url);
        return image;
    }

    public void setIcon(String path) {
        setIconImage(loadImage(path));
    }

    public void setSize(float proportion) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension((int) (screenSize.width * 0.9f), (int) (screenSize.height * 0.9f)));
        setLocationRelativeTo(null);
    }

    public void windowOpened(WindowEvent e) {}

    public void windowIconified(WindowEvent e) {}

    public void windowDeiconified(WindowEvent e) {}

    public void windowDeactivated(WindowEvent e) {}

    public void windowClosing(WindowEvent e) {}

    public void windowClosed(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {}

    
}
