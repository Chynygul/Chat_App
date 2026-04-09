package com.raven.component;

import com.raven.model.Model_File_Sender;
import com.raven.model.Model_Receive_Image;

import java.awt.Color;
import javax.swing.Icon;

public class Chat_Right extends javax.swing.JLayeredPane {

    public Chat_Right() {
        initComponents();
        txt.setBackground(new Color(179, 233, 255));
    }

    public void setText(String text) {
        if (text.equals("")) {
            txt.hideText();
        } else {
            txt.setText(text);
        }
        txt.seen();
    }

    public void setImage(Model_File_Sender fileSender) {
        txt.setImage(true, fileSender);
    }

    public void setImage(String... image) {
       // txt.setImage(false, image);
    }

    public void setImage(Model_Receive_Image dataImage) {
        txt.setImage(true, dataImage);
    }

    public void setFile(String fileName, String fileSize, int fileID) {
        txt.setFile(fileName, fileSize, fileID);
    }

    public void setFile(String fileName, String fileSize, java.io.File localFile) {
        txt.setFile(fileName, fileSize, localFile);
    }

    public void setEmoji(Icon icon) {
        txt.hideText();
        txt.setEmoji(true, icon);
    }

    public void setTime(String time) {
        // txt.setTime("10:30 PM");    //  Testing
        txt.setTime(time);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txt = new com.raven.component.Chat_Item();

        setLayer(txt, javax.swing.JLayeredPane.DEFAULT_LAYER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txt, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(txt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.raven.component.Chat_Item txt;
    // End of variables declaration//GEN-END:variables
}
