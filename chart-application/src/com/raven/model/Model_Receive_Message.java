package com.raven.model;

import com.raven.app.MessageType;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Model_Receive_Message {

    private static int jsonInt(JSONObject obj, String key, int def) {
        Object v = obj.opt(key);
        if (v == null || v == JSONObject.NULL) {
            return def;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static long jsonLong(JSONObject obj, String key, long def) {
        Object v = obj.opt(key);
        if (v == null || v == JSONObject.NULL) {
            return def;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public JSONObject toJsonObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("messageType", messageType.getValue());
            json.put("fromUserID", fromUserID);
            json.put("text", text);

            if (dataImage != null) {
                json.put("dataImage", dataImage.toJsonObject());
            }

            if (fileName != null) {
                json.put("fileName", fileName);
            }
            json.put("fileSize", fileSize);

            return json;
        } catch (JSONException e) {
            return null;
        }
    }

    public Model_Receive_Message(Object json) throws JSONException {
        JSONObject obj;
        if (json instanceof JSONObject) {
            obj = (JSONObject) json;
        } else if (json instanceof Map) {
            obj = new JSONObject((Map<?, ?>) json);
        } else {
            obj = new JSONObject(json.toString());
        }
        try {
            messageType = MessageType.toMessageType(jsonInt(obj, "messageType", 1));
            fromUserID = jsonInt(obj, "fromUserID", 0);
            text = obj.optString("text", "");

            if (!obj.isNull("dataImage")) {
                Object rawImg = obj.get("dataImage");
                if (rawImg instanceof JSONObject) {
                    dataImage = new Model_Receive_Image(rawImg);
                } else if (rawImg instanceof Map) {
                    dataImage = new Model_Receive_Image(new JSONObject((Map<?, ?>) rawImg));
                }
            }

            if (!obj.isNull("fileName")) {
                fileName = obj.optString("fileName", null);
            }

            fileSize = jsonLong(obj, "fileSize", 0L);
            fileID = jsonInt(obj, "fileID", 0);
            long time = obj.getLong("sentAt");
            this.sentAt = new Date(time);

        } catch (JSONException e) {
            System.err.println("ошибка из Model_Receive_Message из клиентск " + e);
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public int getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(int fromUserID) {
        this.fromUserID = fromUserID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Model_Receive_Image getDataImage() {
        return dataImage;
    }

    public void setDataImage(Model_Receive_Image dataImage) {
        this.dataImage = dataImage;
    }
    private MessageType messageType;
    private int fromUserID;
    private String text;

    public Date  getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date  sentAt) {
        this.sentAt = sentAt;
    }

    private Date  sentAt;

    private Model_Receive_Image dataImage;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    private String fileName;

    private long fileSize;

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    private int fileID;

}
