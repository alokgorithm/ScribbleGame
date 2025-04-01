package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatPanel extends JPanel {
    private JTextArea chatArea;
    private JTextField inputField;
    private GameClient client;

    public ChatPanel(GameClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        chatArea = new JTextArea(10, 30);
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);
        inputField = new JTextField();
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText();
                if (!text.isEmpty()) {
                    // Send the guess message to the server.
                    client.sendMessage("GUESS:" + text);
                    inputField.setText("");
                }
            }
        });
    }

    // Append a new message to the chat area.
    public void appendMessage(String msg) {
        chatArea.append(msg + "\n");
    }
}
