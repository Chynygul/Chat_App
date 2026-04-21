package com.raven.component;

import com.raven.app.MessageType;
import com.raven.emoji.Emogi;
import com.raven.model.Model_File_Sender;
import com.raven.model.Model_Receive_Image;
import com.raven.model.Model_Receive_Message;
import com.raven.model.Model_Send_Message;
import com.raven.swing.ScrollBar;
import java.awt.Adjustable;
import java.awt.Color;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;

public class Chat_Body extends javax.swing.JPanel {

    private java.util.Map<Integer, Object> messageMap = new java.util.HashMap<>();
    private String lastDate = "";

    public Chat_Body() {
        initComponents();
        init();
    }

    private void init() {
        body.setLayout(new MigLayout("fillx", "", "5[bottom]5"));
        sp.setVerticalScrollBar(new ScrollBar());
        sp.getVerticalScrollBar().setBackground(Color.WHITE);
    }

    public void addItemLeft(Model_Receive_Message data) {
        addDateIfNeeded(data.getSentAt());
        if (data.getMessageType() == MessageType.TEXT) {
            Chat_Left item = new Chat_Left();
            item.setText(data.getText());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            body.add(item, "wrap, w 100::80%");
        } else if (data.getMessageType() == MessageType.EMOJI) {
            Chat_Left item = new Chat_Left();
            item.setEmoji(Emogi.getInstance().getImoji(Integer.valueOf(data.getText())).getIcon());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            body.add(item, "wrap, w 100::80%");
        } else if (data.getMessageType() == MessageType.IMAGE) {
            Chat_Left item = new Chat_Left();
            item.setText("");
            item.setImage(data.getDataImage());

            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            body.add(item, "wrap, w 100::80%");
        } else if (data.getMessageType() == MessageType.FILE) {
            Chat_Left item = new Chat_Left();
            item.setText("");
            item.setFile(data.getFileName(), formatFileSize(data.getFileSize()), data.getFileID());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            this.body.add(item, "wrap, w 100::80%");
            this.scrollToBottom();
        }
        repaint();
        revalidate();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format("%.1f KB", kb);
        double mb = kb / 1024.0;
        if (mb < 1024) return String.format("%.1f MB", mb);
        double gb = mb / 1024.0;
        return String.format("%.1f GB", gb);
    }

    private String getDateLabel(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Calendar msgCal = java.util.Calendar.getInstance();
        msgCal.setTime(date);

        boolean isToday =
                cal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                        cal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR);

        cal.add(java.util.Calendar.DAY_OF_YEAR, -1);

        boolean isYesterday =
                cal.get(java.util.Calendar.YEAR) == msgCal.get(java.util.Calendar.YEAR) &&
                        cal.get(java.util.Calendar.DAY_OF_YEAR) == msgCal.get(java.util.Calendar.DAY_OF_YEAR);

        if (isToday) return "Сегодня";
        if (isYesterday) return "Вчера";

        return new java.text.SimpleDateFormat("dd MMM yyyy").format(date);
    }

    private void addDateIfNeeded(Date date) {
        if (date == null) return;

        String currentDate = getDateLabel(date);

        if (!currentDate.equals(lastDate)) {
            lastDate = currentDate;

            Chat_Date item = new Chat_Date();
            item.setDate(currentDate);

            body.add(item, "wrap, al center");
        }
    }

//    public void addItemLeft(String text, String user, String[] image) {
//        Chat_Left_With_Profile item = new Chat_Left_With_Profile();
//        item.setText(text);
//        item.setImage(image);
//        item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
//        item.setUserProfile(user);
//        body.add(item, "wrap, w 100::80%");
//        //  ::80% set max with 80%
//        body.repaint();
//        body.revalidate();
//    }
//
//    public void addItemFile(String text, String user, String fileName, String fileSize, int fileID) {
//        Chat_Left_With_Profile item = new Chat_Left_With_Profile();
//        item.setText(text);
//        item.setFile(fileName, fileSize, fileID);
//        item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
//        item.setUserProfile(user);
//        body.add(item, "wrap, w 100::80%");
//        //  ::80% set max with 80%
//        body.repaint();
//        body.revalidate();
//    }

    public void addItemRight(Model_Send_Message data) {
        addDateIfNeeded(data.getSentAt());
        if (data.getMessageType() == MessageType.TEXT) {
            Chat_Right item = new Chat_Right();
            item.setText(data.getText());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            body.add(item, "wrap, al right, w 100::80%");
        } else if (data.getMessageType() == MessageType.EMOJI) {
            Chat_Right item = new Chat_Right();
            item.setEmoji(Emogi.getInstance().getImoji(Integer.valueOf(data.getText())).getIcon());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            body.add(item, "wrap, al right, w 100::80%");
        } else if (data.getMessageType() == MessageType.IMAGE) {
            Chat_Right item = new Chat_Right();
            item.setText("");
            item.setImage(data.getFile());

            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            body.add(item, "wrap, al right, w 100::80%");
        } else if (data.getMessageType() == MessageType.FILE) {
            Chat_Right item = new Chat_Right();
            item.setText("");

            java.io.File localFile = null;
            if (data.getFile() != null) {
                localFile = data.getFile().getFile();
            }

            item.setFile(data.getFileName(), formatFileSize(data.getFileSize()), localFile);
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            this.body.add(item, "wrap, al right, w 100::80%");
            this.scrollToBottom();
        }

        repaint();
        revalidate();
        scrollToBottom();
    }

    public void loadHistoryMessage(Model_Receive_Message data, int currentUserId) {
        if (data.getFromUserID() == currentUserId) {
            addItemHistoryRight(data);
        } else {
            addItemLeft(data);
        }
    }

    public void addItemHistoryRight(Model_Receive_Message data) {
        addDateIfNeeded(data.getSentAt() != null ? data.getSentAt() : new Date());
        if (data.getMessageType() == MessageType.TEXT) {
            Chat_Right item = new Chat_Right();
            item.setText(data.getText());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            body.add(item, "wrap, al right, w 100::80%");
        } else if (data.getMessageType() == MessageType.EMOJI) {
            Chat_Right item = new Chat_Right();
            item.setEmoji(Emogi.getInstance().getImoji(Integer.valueOf(data.getText())).getIcon());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            body.add(item, "wrap, al right, w 100::80%");
        } else if (data.getMessageType() == MessageType.IMAGE) {
            Chat_Right item = new Chat_Right();
            item.setText("");

            // для истории справа у нас нет Model_File_Sender, а есть только fileID/dataImage
            // поэтому пока используем dataImage, как и слева
            item.setImage(data.getDataImage());
            if (data.getFileID() > 0) {
                messageMap.put(data.getFileID(), item); // 🔥 сохраняем
            }

            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            body.add(item, "wrap, al right, w 100::80%");
        } else if (data.getMessageType() == MessageType.FILE) {
            Chat_Right item = new Chat_Right();
            item.setText("");
            item.setFile(data.getFileName(), formatFileSize(data.getFileSize()), data.getFileID());
            //item.setTime(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            //item.setTime(formatTime(data.getSentAt()));
            item.setTime(data.getSentAt() != null
                    ? formatTime(data.getSentAt())
                    : LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
            body.add(item, "wrap, al right, w 100::80%");
        }

        repaint();
        revalidate();
        scrollToBottom();
    }

    private String formatTime(Date date) {
        return new java.text.SimpleDateFormat("hh:mm a").format(date);
    }

//    public void updateFileMessage(int fileID, java.io.File file) {
//        Object obj = messageMap.get(fileID);
//
//        if (obj instanceof Chat_Left) {
//            Chat_Left item = (Chat_Left) obj;
//            item.setImage(file); // 🔥 ПОЛНОЕ изображение
//        } else if (obj instanceof Chat_Right) {
//            Chat_Right item = (Chat_Right) obj;
//            item.setImage(file);
//        }
//
//        repaint();
//        revalidate();
//    }

//    public void addItemFileRight(String text, String fileName, String fileSize, int fileID) {
//        Chat_Right item = new Chat_Right();
//        item.setText(text);
//        item.setFile(fileName, fileSize, fileID);
//        body.add(item, "wrap, al right, w 100::80%");
//        //  ::80% set max with 80%
//        body.repaint();
//        body.revalidate();
//    }
//
//    public void addDate(String date) {
//        Chat_Date item = new Chat_Date();
//        item.setDate(date);
//        body.add(item, "wrap, al center");
//        body.repaint();
//        body.revalidate();
//    }

    public void clearChat() {
        body.removeAll();
        lastDate = "";
        repaint();
        revalidate();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sp = new javax.swing.JScrollPane();
        body = new javax.swing.JPanel();

        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        body.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout bodyLayout = new javax.swing.GroupLayout(body);
        body.setLayout(bodyLayout);
        bodyLayout.setHorizontalGroup(
            bodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 826, Short.MAX_VALUE)
        );
        bodyLayout.setVerticalGroup(
            bodyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 555, Short.MAX_VALUE)
        );

        sp.setViewportView(body);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sp)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sp)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void scrollToBottom() {
        JScrollBar verticalBar = sp.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                verticalBar.removeAdjustmentListener(this);
            }
        };
        verticalBar.addAdjustmentListener(downScroller);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel body;
    private javax.swing.JScrollPane sp;
    // End of variables declaration//GEN-END:variables
}
