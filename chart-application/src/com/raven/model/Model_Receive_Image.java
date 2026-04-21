package com.raven.model;

import java.sql.Timestamp;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Model_Receive_Image {

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Model_Receive_Image(int fileID, String image, int width, int height) {
        this.fileID = fileID;
        this.image = image;
        this.width = width;
        this.height = height;
        //this.sentAt = sentAt;
    }

    public Model_Receive_Image(Object json) throws JSONException {
        JSONObject obj;
        if (json instanceof JSONObject) {
            obj = (JSONObject) json;
        } else if (json instanceof Map) {
            obj = new JSONObject((Map<?, ?>) json);
        } else {
            obj = new JSONObject(json.toString());
//            try{
//                obj = new JSONObject(json.toString());
//            }
//            catch (JSONException e){
//                System.out.println(e.getMessage() + "ошибка из Model_Receive_Image");
//            }
        }
        try {
            fileID = obj.getInt("fileID");
            image = obj.getString("image");
            width = obj.getInt("width");
            height = obj.getInt("height");
            //sentAt = new Timestamp(obj.getLong("sentAt"));
        } catch (JSONException e) {
            System.err.println(e + "тоже ошибка из Model_Receive_Image");
        }
    }

    private int fileID;
    private String image;
    private int width;
    private int height;
    private Timestamp sentAt;

    public Timestamp getSentAt() {
        return sentAt;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }

    public JSONObject toJsonObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("fileID", fileID);
            json.put("image", image);
            json.put("width", width);
            json.put("height", height);
            json.put("sentAt", sentAt);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }
}
