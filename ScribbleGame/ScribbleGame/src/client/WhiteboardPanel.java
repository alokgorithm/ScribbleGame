package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class WhiteboardPanel extends JPanel implements MouseListener, MouseMotionListener {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int lastX, lastY;
    private GameClient client;
    
    public WhiteboardPanel(GameClient client) {
        this.client = client;
        setPreferredSize(new Dimension(500, 500));
        canvas = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        g2d = canvas.createGraphics();
        // Fill canvas with white background.
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 500, 500);
        // Set drawing color to black.
        g2d.setColor(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
    }
    
    // Capture mouse pressed event.
    @Override
    public void mousePressed(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
    }
    
    // Capture mouse dragged event for drawing.
    @Override
    public void mouseDragged(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        g2d.drawLine(lastX, lastY, x, y);
        repaint();
        // Send drawing coordinates to the server in the format: x,y,x2,y2.
        client.sendMessage("DRAW:" + lastX + "," + lastY + "," + x + "," + y);
        lastX = x;
        lastY = y;
    }
    
    // Unused mouse event methods.
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    
    // Draw on the whiteboard based on data received from the server.
    public void drawFromData(String data) {
        // Expected format: "x,y,x2,y2".
        String[] parts = data.split(",");
        int x1 = Integer.parseInt(parts[0]);
        int y1 = Integer.parseInt(parts[1]);
        int x2 = Integer.parseInt(parts[2]);
        int y2 = Integer.parseInt(parts[3]);
        g2d.drawLine(x1, y1, x2, y2);
        repaint();
    }
    
    // Clear the whiteboard (for example, when a round ends).
    public void clearBoard() {
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g2d.setColor(Color.BLACK);
        repaint();
    }
}