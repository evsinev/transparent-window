package transparent;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class MainFrame extends JFrame {

    private final BufferedImage image;
    private final float imageWidth;
    private final float imageHeight;
    private Point startLocation;
    float opacity = 0.50f;
    Float previousOpacity = null;
    private final File imageFile;

    public MainFrame(String aFilename) throws HeadlessException {
        super(aFilename);
        setUndecorated(true);

        imageFile = new File(aFilename);

        try {
            image = ImageIO.read(imageFile);
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startLocation = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                startLocation = null;
            }
        });
        addMouseMotionListener(new MouseInputAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if(startLocation!=null) {
                    Point current = e.getLocationOnScreen();
                    setLocation(current.x - startLocation.x, current.y - startLocation.y);
                    save();
                }

            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        shiftWindow(1, 0, e.getModifiers());
                        break;
                    case KeyEvent.VK_LEFT:
                        shiftWindow(-1, 0, e.getModifiers());
                        break;
                    case KeyEvent.VK_UP:
                        shiftWindow(0, -1, e.getModifiers());
                        break;
                    case KeyEvent.VK_DOWN:
                        shiftWindow(0, 1, e.getModifiers());
                        break;

                    case KeyEvent.VK_H:
                        toggleOpacity();
                        break;

                }
            }
        });

        // Set the window to 55% opaque (45% translucent).
        setOpacity(opacity);

        load();
        setAlwaysOnTop(true);
    }

    private void toggleOpacity() {
        if(previousOpacity!=null) {
            setOpacity(previousOpacity);
            previousOpacity = null;
        } else {
            previousOpacity = getOpacity();
            setOpacity(0);
        }
    }

    private void load() {
        Properties prop = new Properties();
        File file = getFileConfig();
        try {
            FileReader in = new FileReader(file);
            try {
                prop.load(in);
            } finally {
                in.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        setLocation(getInt(prop, "x", 100), getInt(prop, "y", 100));
        setSize(getInt(prop, "width", 720), getInt(prop, "height", 1000));
        setOpacity(getFloat(prop, "opacity", 0.55f));
    }

    private File getFileConfig() {
        String homeDir = System.getProperty("user.home");
        return new File(homeDir, ".transparent-frame.conf");
    }

    private int getInt(Properties prop, String aName, int aValue) {
        return Integer.parseInt(prop.getProperty(aName, String.valueOf(aValue)));
    }

    private float getFloat(Properties prop, String aName, float aValue) {
        return Float.parseFloat(prop.getProperty(aName, String.valueOf(aValue)));
    }

    private void save() {
        Properties prop = new Properties();
        Point location = getLocation();
        setProp(prop, "x", location.x);
        setProp(prop, "y", location.y);
        setProp(prop, "width", getWidth());
        setProp(prop, "height", getHeight());
        setProp(prop, "opacity", opacity);

        try {
            FileWriter writer = new FileWriter(getFileConfig());
            try {
                prop.store(writer, "transparent frame");
            } finally {
                writer.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setProp(Properties aProp, String aName, int aValue) {
        aProp.setProperty(aName, String.valueOf(aValue));
    }

    private void setProp(Properties aProp, String aName, float aValue) {
        aProp.setProperty(aName, String.valueOf(aValue));
    }

    private void shiftWindow(int aX, int aY, int aModifiers) {
        if( (aModifiers & KeyEvent.SHIFT_MASK) != 0) {
            aX *= 10;
            aY *= 10;
        }

        if( (aModifiers & KeyEvent.ALT_MASK) != 0) {
            Dimension size = getSize();
            setSize(size.width + aX, size.height+aY);
        } else if( (aModifiers & KeyEvent.META_MASK) != 0) {
            opacity += (0.1 * aX) + (0.1 * aY);
            if(opacity>1.0) {
                opacity = 1.0f;
            }
            if(opacity<0) {
                opacity = 0;
            }
            System.out.println("opacity = " + opacity);
            setOpacity(opacity);
        } else {
            Point location = getLocation();
            setLocation(location.x+aX, location.y + aY);
        }

        save();
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);


        int width = getWidth();
        g.drawImage(image, 0, 0, width, calcHeight(width), this);
    }

    private int calcHeight(float aWindowWidth) {
        float factor = aWindowWidth / imageWidth;
        return Math.round(factor * imageHeight);
    }

    public static void main(String[] args) {

        final String filename = args.length == 0 ? "1.png" : args[0];

        if(!new File(filename).exists()) {
            System.out.println("File " + filename + " not found");
            System.exit(1);
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        // see http://docs.oracle.com/javase/tutorial/uiswing/misc/trans_shaped_windows.html

        // Create the GUI on the event-dispatching thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainFrame frame = new MainFrame(filename);

                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                // Display the window.
                frame.setVisible(true);
            }
        });
    }
}
