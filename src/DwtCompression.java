import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.swing.*;

public class DwtCompression {
    JFrame frame;
    JLabel lbIm1;
    BufferedImage imgOne;
    int width = 512;
    int height = 512;
    public static int COEFFICIENT;
    public static int MAX = 10;

    int[][] redChannel;
    int[][] greenChannel;
    int[][] blueChannel;

    /** Read Image RGB
     *  Reads the image of given width and height at the given imgPath into the provided BufferedImage.
     */
    private void readImageRGB(int width, int height, String imgPath, BufferedImage img)
    {
        try
        {
            int frameLength = width*height*3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            byte[] bytes = new byte[(int) (long) frameLength];

            raf.read(bytes);

            // Initialize color channels
            redChannel = new int[width][height];
            greenChannel = new int[width][height];
            blueChannel = new int[width][height];

            int ind = 0;

            System.out.println("Compressing at level: " + COEFFICIENT);
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    redChannel[y][x] = r & 0XFF;
                    greenChannel[y][x] = g & 0XFF;
                    blueChannel[y][x] = b & 0XFF;

                    if (COEFFICIENT == 9) {
                        int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                        img.setRGB(x,y,pix);
                    }
                    ind++;
                }
            }
            if (COEFFICIENT < 9) {
                Util util = new Util();
                double[][] redDWT = util.DWT(redChannel, height, width, COEFFICIENT);
                double[][] greenDWT = util.DWT(greenChannel, height, width, COEFFICIENT);
                double[][] blueDWT = util.DWT(blueChannel, height, width, COEFFICIENT);

                redChannel = util.inverseDWT(redDWT);
                greenChannel = util.inverseDWT(greenDWT);
                blueChannel = util.inverseDWT(blueDWT);

                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        int pix = 0xff000000 | ((redChannel[i][j] & 0xff) << 16) | ((greenChannel[i][j] & 0xff) << 8) |
                                (blueChannel[i][j] & 0xff);
                        img.setRGB(j, i, pix);
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void initChannels(int width, int height, String imgPath, BufferedImage img) {
        try {
            int frameLength = width*height*3;

            File file = new File(imgPath);
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(0);

            byte[] bytes = new byte[(int) (long) frameLength];

            raf.read(bytes);

            // Initialize color channels
            redChannel = new int[width][height];
            greenChannel = new int[width][height];
            blueChannel = new int[width][height];

            int ind = 0;

            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];

                    redChannel[y][x] = r & 0XFF;
                    greenChannel[y][x] = g & 0XFF;
                    blueChannel[y][x] = b & 0XFF;

                    ind++;
                }
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void showIms(String[] args){

        // Read in the specified image
        if (COEFFICIENT > -1) {
            imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            readImageRGB(width, height, args[0], imgOne);

            // Use label to display the image
            frame = new JFrame();
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            GridBagLayout gLayout = new GridBagLayout();
            frame.getContentPane().setLayout(gLayout);

            lbIm1 = new JLabel(new ImageIcon(imgOne));

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.CENTER;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = 1;
            frame.getContentPane().add(lbIm1, c);

            frame.pack();
            frame.setVisible(true);
        } else {
            imgOne = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            initChannels(width, height, args[0], imgOne);

            Util util = new Util();
            BufferedImage[] imgFrames = new BufferedImage[MAX];

            //Get image frames
            for (int coeff = 0; coeff < MAX; coeff++) {
                System.out.println("Compressing at level: " + coeff);
                imgFrames[coeff] = util.animate(redChannel, greenChannel, blueChannel, height, width, coeff);
            }

            for (int frame_i = 0; frame_i < MAX; frame_i++) {
                // Use label to display the image
                frame = new JFrame();
                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                GridBagLayout gLayout = new GridBagLayout();

                frame.getContentPane().setLayout(gLayout);
                lbIm1 = new JLabel(new ImageIcon(imgFrames[frame_i]));

                GridBagConstraints c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.anchor = GridBagConstraints.CENTER;
                c.weightx = 0.5;
                c.gridx = 0;
                c.gridy = 1;
                frame.getContentPane().add(lbIm1, c);

                frame.pack();
                frame.setVisible(true);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public static void main(String[] args) {
        COEFFICIENT = Integer.parseInt(args[1]);
        DwtCompression ren = new DwtCompression();
        ren.showIms(args);
    }
}
