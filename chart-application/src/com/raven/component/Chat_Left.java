package com.raven.component;

import com.raven.model.Model_Receive_Image;
import java.awt.Color;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Icon;

public class Chat_Left extends javax.swing.JLayeredPane {

    public Chat_Left() {
        initComponents();
        txt.setBackground(new Color(242, 242, 242));
    }

    public void setText(String text) {
        if (text.equals("")) {
            txt.hideText();
        } else {
            txt.setText(text);
        }

    }

    public void setImage(Icon... image) {
        //  txt.setImage(false, image);
        //    Update next
    }

    public void setImage(Model_Receive_Image dataImage) {
        txt.setImage(false, dataImage);
    }

    public void setFile(String fileName, String fileSize, int fileID) {
        txt.setFile(fileName, fileSize, fileID);
    }
    public void setEmoji(Icon icon) {
        txt.hideText();
        txt.setEmoji(false, icon);
    }

    public void setTime(String time) {
        // txt.setTime("10:30 PM");    //  Testing
        // Получаем текущее время
        LocalTime currentTime = LocalTime.now();

        // Форматируем время в 12-часовой формат с AM/PM
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        String formattedTime = currentTime.format(formatter);

        // Устанавливаем отформатированное время
        txt.setTime(time);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txt = new com.raven.component.Chat_Item();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(txt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.raven.component.Chat_Item txt;
    // End of variables declaration//GEN-END:variables
}
